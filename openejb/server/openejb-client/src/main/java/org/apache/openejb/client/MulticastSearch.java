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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class MulticastSearch {

    private static final int BUFF_SIZE = 8192;

    private final MulticastSocket multicast;

    public MulticastSearch() throws IOException {
        this("239.255.3.2", 6142);
    }

    public MulticastSearch(final String host, final int port) throws IOException {
        final InetAddress inetAddress = InetAddress.getByName(host);

        multicast = new MulticastSocket(port);
        multicast.joinGroup(inetAddress);
        multicast.setSoTimeout(500);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            this.close();
        } finally {
            super.finalize();
        }
    }

    public URI search(final int timeout, final TimeUnit milliseconds) throws IOException {
        return search(new DefaultFilter(), timeout, milliseconds);
    }

    public URI search() throws IOException {
        return search(new DefaultFilter(), 0, TimeUnit.MILLISECONDS);
    }

    public URI search(final Filter filter) throws IOException {
        return search(filter, 0, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public URI search(final Filter filter, long timeout, final TimeUnit unit) throws IOException {
        timeout = TimeUnit.MILLISECONDS.convert(timeout, unit);
        long waited = 0;

        final byte[] buf = new byte[BUFF_SIZE];
        final DatagramPacket packet = new DatagramPacket(buf, 0, buf.length);

        while (timeout == 0 || waited < timeout) {
            final long start = System.currentTimeMillis();
            try {
                multicast.receive(packet);
                if (packet.getLength() > 0) {
                    final String str = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    try {
                        final URI service = new URI(str);
                        if (service != null && filter.accept(service)) {

                            final String callerHost = ((InetSocketAddress) packet.getSocketAddress()).getAddress().getHostAddress();
                            final String serviceHost = service.getHost();

                            if (MulticastPulseClient.isLocalAddress(serviceHost, false)) {
                                if (!MulticastPulseClient.isLocalAddress(callerHost, false)) {
                                    //A local service is only available to a local client
                                    continue;
                                }
                            }

                            return service;
                        }
                    } catch (URISyntaxException e) {
                        // not a service URI
                    }
                }
            } catch (SocketTimeoutException e) {
                //Ignore
            } catch (SocketException e) {
                System.out.println(e.getClass().getName() + ": " + e.getMessage());
            } finally {
                final long stop = System.currentTimeMillis();
                waited += stop - start;
            }
        }

        return null;
    }

    public void close() {
        try {
            multicast.close();
        } catch (Throwable e) {
            //Ignore
        }
    }

    public interface Filter {

        boolean accept(URI service);
    }

    public static class DefaultFilter implements Filter {

        @Override
        public boolean accept(final URI service) {
            return true;
        }
    }
}
