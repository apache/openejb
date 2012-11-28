/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client;

import org.apache.openejb.client.event.ConnectionOpened;
import org.apache.openejb.client.event.ConnectionPoolCreated;
import org.apache.openejb.client.event.ConnectionPoolTimeout;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

public class SocketConnectionFactory implements ConnectionFactory {

    private KeepAliveStyle keepAliveStyle = KeepAliveStyle.PING;

    public static final String PROPERTY_SOCKET_TIMEOUT = "openejb.client.connection.socket.timeout";
    public static final String PROPERTY_POOL_TIMEOUT = "openejb.client.connection.pool.timeout";
    private static final String PROPERTY_POOL_TIMEOUT2 = "openejb.client.connectionpool.timeout";
    public static final String PROPERTY_POOL_SIZE = "openejb.client.connection.pool.size";
    private static final String PROPERTY_POOL_SIZE2 = "openejb.client.connectionpool.size";
    public static final String PROPERTY_KEEPALIVE = "openejb.client.keepalive";
    public static final String ENABLED_CIPHER_SUITES = "openejb.client.enabledCipherSuites";

    private static final Map<URI, Pool> connections = new ConcurrentHashMap<URI, Pool>();
    private int size = 5;
    private long timeoutPool = 1000;
    private int timeoutSocket = 500;
    private String[] enabledCipherSuites;

    public SocketConnectionFactory() {

        this.size = getSize();
        this.timeoutPool = getTimeoutPool();
        this.timeoutSocket = getTimeoutSocket();
        this.enabledCipherSuites = getEnabledCipherSuites();
        try {
            String property = System.getProperty(PROPERTY_KEEPALIVE);
            if (property != null) {
                property = property.toUpperCase();
                this.keepAliveStyle = KeepAliveStyle.valueOf(property);
            }
        } catch (Throwable e) {
            //Ignore
        }
    }

    private String[] getEnabledCipherSuites() {
        String property = System.getProperty(ENABLED_CIPHER_SUITES);
        if (property != null) {
            return property.split(",");
        } else {
            return new String[]{"SSL_DH_anon_WITH_RC4_128_MD5"};
        }
    }

    private long getTimeoutPool() {
        final Properties p = System.getProperties();
        long timeout = getLong(p, SocketConnectionFactory.PROPERTY_POOL_TIMEOUT, this.timeoutPool);
        timeout = getLong(p, SocketConnectionFactory.PROPERTY_POOL_TIMEOUT2, timeout);
        return timeout;
    }

    private int getTimeoutSocket() {
        final Properties p = System.getProperties();
        return getInt(p, SocketConnectionFactory.PROPERTY_SOCKET_TIMEOUT, this.timeoutSocket);
    }

    private int getSize() {
        final Properties p = System.getProperties();
        int size = getInt(p, SocketConnectionFactory.PROPERTY_POOL_SIZE, this.size);
        size = getInt(p, SocketConnectionFactory.PROPERTY_POOL_SIZE2, size);
        return size;
    }

    public static int getInt(final Properties p, final String property, final int defaultValue) {
        final String value = p.getProperty(property);
        try {
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            //Ignore
        }
        return defaultValue;
    }

    public static long getLong(final Properties p, final String property, final long defaultValue) {
        final String value = p.getProperty(property);
        try {
            if (value != null) return Long.parseLong(value);
            else return defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Connection getConnection(final URI uri) throws java.io.IOException {

        final Pool pool = getPool(uri);

        SocketConnection conn = pool.get();
        if (conn == null) {
            try {
                conn = new SocketConnection(uri, pool);
                conn.open(uri);
            } catch (IOException e) {
                pool.put(null);
                throw e;
            }
        }

        try {
            conn.lock.tryLock(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted();
            pool.put(conn);
            throw new IOException("Connection busy");
        }

        final OutputStream ouputStream = conn.getOuputStream();
        if (conn.socket.isClosed()) {
            pool.put(null);
            return getConnection(uri);
        }

        try {
            ouputStream.write(this.keepAliveStyle.ordinal());
            ouputStream.flush();

            switch (this.keepAliveStyle) {
                case PING_PING: {
                    ouputStream.write(this.keepAliveStyle.ordinal());
                    ouputStream.flush();
                    break;
                }
                case PING_PONG: {
                    //noinspection ResultOfMethodCallIgnored
                    conn.getInputStream().read();
                    break;
                }
            }
        } catch (IOException e) {
            pool.put(null);
            throw e;
        }

        return conn;
    }

    private Pool getPool(final URI uri) {
        Pool pool = connections.get(uri);
        if (pool == null) {
            pool = new Pool(uri, getSize(), this.timeoutPool);
            connections.put(uri, pool);
        }
        return pool;
    }

    class SocketConnection implements Connection {

        private Socket socket = null;
        private final URI uri;

        private boolean discarded;
        private final Pool pool;
        private final Lock lock = new ReentrantLock();
        private OutputStream out;
        private InputStream in;
        private boolean gzip = false;

        public SocketConnection(final URI uri, final Pool pool) {
            this.uri = uri;
            this.pool = pool;
        }

        @Override
        protected void finalize() throws Throwable {

            try {

                cleanUp();

            } finally {
                super.finalize();
            }
        }

        private void cleanUp() {
            if (null != in) {
                try {
                    in.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }

            if (null != out) {
                try {
                    out.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }

            if (null != socket) {
                try {
                    socket.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }

        protected void open(final URI uri) throws IOException {

            /*-----------------------*/
            /* Open socket to server */
            /*-----------------------*/
            final InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());

            try {
                final String scheme = uri.getScheme();
                if (scheme.equalsIgnoreCase("ejbds") || scheme.equalsIgnoreCase("zejbds")) {
                    final SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
                    sslSocket.setEnabledCipherSuites(enabledCipherSuites);
                    this.socket = sslSocket;

                } else {
                    this.socket = new Socket();
                }

                if (scheme.startsWith("z")) {
                    gzip = true;
                }

                this.socket.setTcpNoDelay(true);
                this.socket.setSoLinger(true, 10);
                this.socket.connect(address, SocketConnectionFactory.this.timeoutSocket);

                Client.fireEvent(new ConnectionOpened(uri));

            } catch (ConnectException e) {
                throw this.failure("Cannot connect to server '" + uri.toString() + "'.  Check that the server is started and that the specified serverURL is correct.", e);

            } catch (IOException e) {
                throw this.failure("Cannot connect to server: '" + uri.toString() + "'.  Exception: " + e.getClass().getName() + " : " + e.getMessage(), e);

            } catch (SecurityException e) {
                throw this.failure("Cannot access server: '" + uri.toString() + "' due to security restrictions in the current VM: " + e.getClass().getName() + " : " + e.getMessage(), e);

            } catch (Throwable e) {
                throw this.failure("Cannot  connect to server: '" + uri.toString() + "' due to an unknown exception in the OpenEJB client: " + e.getClass().getName() + " : " + e.getMessage(), e);
            }

        }

        private IOException failure(final String err, final Throwable e) {
            this.discard();
            return new IOException(err, e);
        }

        @Override
        public void discard() {
            try {
                pool.put(null);
            } finally {
                discarded = true;
                cleanUp();
            }

            // don't bother unlocking it
            // it should never get used again
        }

        @Override
        public URI getURI() {
            return uri;
        }

        @Override
        public void close() throws IOException {
            if (discarded) return;

            pool.put(this);
            lock.unlock();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            /*----------------------------------*/
            /* Open input streams */
            /*----------------------------------*/
            try {
                if (in == null) {
                    if (!gzip) {
                        in = new BufferedInputStream(socket.getInputStream());
                    } else {
                        in = new GZIPInputStream(socket.getInputStream());
                    }
                }

                return new Input(in);

            } catch (StreamCorruptedException e) {
                throw this.failure("Cannot open input stream to server, the stream has been corrupted: " + e.getClass().getName(), e);

            } catch (IOException e) {
                throw this.failure("Cannot open input stream to server: " + e.getClass().getName(), e);

            } catch (Throwable e) {
                throw this.failure("Cannot open output stream to server: " + e.getClass().getName(), e);
            }
        }

        @Override
        public OutputStream getOuputStream() throws IOException {

            try {

                if (out == null) {
                    if (!gzip) {
                        out = new BufferedOutputStream(socket.getOutputStream());
                    } else {
                        out = new FlushableGZIPOutputStream(socket.getOutputStream());
                    }
                }

                return new Output(out);

            } catch (IOException e) {
                throw this.failure("Cannot open output stream to server: " + e.getClass().getName(), e);

            } catch (Throwable e) {
                throw this.failure("Cannot open output stream to server: " + e.getClass().getName(), e);
            }
        }
    }

    public class Input extends java.io.FilterInputStream {

        public Input(final InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
        }
    }

    public class Output extends java.io.FilterOutputStream {
        public Output(final OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
            flush();
        }
    }

    private static class Pool {
        private final Semaphore semaphore;
        private final Stack<SocketConnection> pool;
        private final long timeout;
        private final TimeUnit timeUnit;
        private final int size;
        private final URI uri;

        private Pool(final URI uri, final int size, final long timeout) {
            this.uri = uri;
            this.size = size;
            this.semaphore = new Semaphore(size);
            this.pool = new Stack<SocketConnection>();
            this.timeout = timeout;
            this.timeUnit = TimeUnit.MILLISECONDS;

            for (int i = 0; i < size; i++) {
                pool.push(null);
            }

            Client.fireEvent(new ConnectionPoolCreated(uri, size, timeout, timeUnit));
        }

        public SocketConnection get() throws IOException {
            try {
                if (semaphore.tryAcquire(timeout, timeUnit)) {
                    return pool.pop();
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            }

            ConnectionPoolTimeoutException exception = new ConnectionPoolTimeoutException("No connections available in pool (size " + size + ").  Waited for " + timeout + " milliseconds for a connection.");
            exception.fillInStackTrace();
            Client.fireEvent(new ConnectionPoolTimeout(uri, size, timeout, timeUnit, exception));
            throw exception;
        }

        public void put(final SocketConnection connection) {
            pool.push(connection);
            semaphore.release();
        }

        @Override
        public String toString() {
            return "Pool{" +
                    "size=" + size +
                    ", available=" + semaphore.availablePermits() +
                    ", uri=" + uri +
                    '}';
        }
    }
}
