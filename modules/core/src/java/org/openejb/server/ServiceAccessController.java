/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  The Server will call the following methods.
 *
 *    newInstance()
 *    init( port, properties)
 *    start()
 *    stop()
 *
 * All Daemon implementations must have a no argument
 * constructor.
 *
 */
public class ServiceAccessController implements ServerService {
    private final ServerService next;
    private IPAddressPermission[] allowHosts;

    public ServiceAccessController(ServerService next) {
        this.next = next;
    }

    public ServiceAccessController(String name, ServerService next, IPAddressPermission[] ipAddressMasks) {
        this.next = next;
        this.allowHosts = ipAddressMasks;
    }

    public void service(Socket socket) throws ServiceException, IOException {
        // Check authorization
        checkHostsAuthorization(socket.getInetAddress(), socket.getLocalAddress());

        next.service(socket);
    }

    public IPAddressPermission[] getAllowHosts() {
        return allowHosts;
    }

    public void setAllowHosts(IPAddressPermission[] ipAddressMasks) {
        this.allowHosts = ipAddressMasks;
    }

    public void checkHostsAuthorization(InetAddress clientAddress, InetAddress serverAddress) throws SecurityException {
        // Check the client ip against the server ip. Hosts are
        // allowed to access themselves, so if these ips
        // match, the following for loop will be skipped.
        if (clientAddress.equals(serverAddress)) {
            return;
        }

        for (int i = 0; i < allowHosts.length; i++) {
            if (allowHosts[i].implies(clientAddress)) {
                return;
            }
        }

        throw new SecurityException("Host " + clientAddress.getHostAddress() + " is not authorized to access this service.");
    }

    private void parseAdminIPs(Properties props) throws ServiceException {
        LinkedList ipAddressMasksList = new LinkedList();

        try {
            InetAddress[] localIps = InetAddress.getAllByName("localhost");
            for (int i = 0; i < localIps.length; i++) {
                if (localIps[i] instanceof Inet4Address) {
                    ipAddressMasksList.add(new ExactIPAddressPermission(localIps[i].getAddress()));
                } else {
                    ipAddressMasksList.add(new ExactIPv6AddressPermission(localIps[i].getAddress()));
                }
            }
        } catch (UnknownHostException e) {
            throw new ServiceException("Could not get localhost inet address", e);
        }

        String ipString = props.getProperty("only_from");
        if (ipString != null) {
            StringTokenizer st = new StringTokenizer(ipString, " ");
            while (st.hasMoreTokens()) {
                String mask = st.nextToken();
                ipAddressMasksList.add(IPAddressPermissionFactory.getIPAddressMask(mask));
            }
        }

        allowHosts = (IPAddressPermission[]) ipAddressMasksList.toArray(new IPAddressPermission[ipAddressMasksList.size()]);
    }

    public void init(Properties props) throws Exception {
        parseAdminIPs(props);
        next.init(props);
    }

    public void start() throws ServiceException {
        next.start();
    }

    public void stop() throws ServiceException {
        next.stop();
    }

    public String getName() {
        return next.getName();
    }

    public String getIP() {
        return next.getIP();
    }

    public int getPort() {
        return next.getPort();
    }
    
    public interface IPAddressPermission extends Serializable {
        public boolean implies(InetAddress address);
    }

    private static class ExactIPAddressPermission implements IPAddressPermission {
        private static final Pattern MASK_VALIDATOR = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

        private static boolean canSupport(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            return matcher.matches();
        }

        private final byte[] bytes;
        
        private ExactIPAddressPermission(byte[] bytes) {
            this.bytes = bytes;
        }
        
        private ExactIPAddressPermission(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Mask " + mask + " does not match pattern " + MASK_VALIDATOR.pattern());
            }
            
            bytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                String group = matcher.group(i + 1);
                int value = Integer.parseInt(group);
                if (value < 0 || 255 < value) {
                    throw new IllegalArgumentException("byte #" + i + " is not valid.");
                }
                bytes[i] = (byte) value;
            }
        }
        
        public boolean implies(InetAddress address) {
            if (false == address instanceof Inet4Address) {
                return false;
            }
            
            byte[] byteAddress = address.getAddress();
            for (int i = 0; i < 4; i++) {
                if (byteAddress[i] != bytes[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class StartWithIPAddressPermission implements IPAddressPermission {
        private static final Pattern MASK_VALIDATOR = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.0$");

        private static boolean canSupport(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            return matcher.matches();
        }
        
        private final byte[] bytes;
        
        private StartWithIPAddressPermission(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Mask " + mask + " does not match pattern " + MASK_VALIDATOR.pattern());
            }
            
            Byte[] tmpBytes = new Byte[4];
            boolean isWildCard = false;
            int size = 0;
            for (int i = 0; i < 3; i++) {
                String group = matcher.group(i + 1);
                if (group.equals("0")) {
                    isWildCard = true;
                } else if (isWildCard) {
                    throw new IllegalArgumentException("0 at position " + size + " in mask");
                } else {
                    int value = Integer.parseInt(group);
                    if (value < 0 || 255 < value) {
                        throw new IllegalArgumentException("byte #" + i + " is not valid.");
                    }
                    tmpBytes[i] = new Byte((byte) value);
                    size++;
                }
            }
            
            bytes = new byte[size];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = tmpBytes[i].byteValue();
            }
        }
        
        public boolean implies(InetAddress address) {
            if (false == address instanceof Inet4Address) {
                return false;
            }
            
            byte[] byteAddress = address.getAddress();
            for (int i = 0; i < bytes.length; i++) {
                if (byteAddress[i] != bytes[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class FactorizedIPAddressPermission implements IPAddressPermission {
        private static final Pattern MASK_VALIDATOR = Pattern.compile("^((\\d{1,3}){1}(\\.\\d{1,3}){0,2}\\.)?\\{(\\d{1,3}){1}((,\\d{1,3})*)\\}$");

        private static boolean canSupport(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            return matcher.matches();
        }
        
        private final byte[] prefixBytes;
        private final byte[] suffixBytes;
        
        private FactorizedIPAddressPermission(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Mask " + mask + " does not match pattern " + MASK_VALIDATOR.pattern());
            }

            // group 1 is the factorized IP part.
            // e.g. group 1 in "1.2.3.{4,5,6}" is "1.2.3."
            String prefix = matcher.group(1);
            StringTokenizer tokenizer = new StringTokenizer(prefix, ".");
            prefixBytes = new byte[tokenizer.countTokens()];
            for (int i = 0; i < prefixBytes.length; i++) {
                String token = tokenizer.nextToken();
                int value = Integer.parseInt(token);
                if (value < 0 || 255 < value) {
                    throw new IllegalArgumentException("byte #" + i + " is not valid.");
                }
                prefixBytes[i] = (byte) value;
            }
            
            // group 5 is a comma separated list of optional suffixes.
            // e.g. group 5 in "1.2.3.{4,5,6}" is ",5,6"
            String suffix = matcher.group(5);
            tokenizer = new StringTokenizer(suffix, ",");
            suffixBytes = new byte[1 + tokenizer.countTokens()];
            
            // group 4 is the compulsory and first suffix.
            // e.g. group 4 in "1.2.3.{4,5,6}" is "4"
            int value = Integer.parseInt(matcher.group(4));
            int i = 0;
            if (value < 0 || 255 < value) {
                throw new IllegalArgumentException("suffix " + i + " is not valid.");
            }
            suffixBytes[i++] = (byte) value;

            for (; i < suffixBytes.length; i++) {
                String token = tokenizer.nextToken();
                value = Integer.parseInt(token);
                if (value < 0 || 255 < value) {
                    throw new IllegalArgumentException("byte #" + i + " is not valid.");
                }
                suffixBytes[i] = (byte) value;
            }
        }
        
        public boolean implies(InetAddress address) {
            if (false == address instanceof Inet4Address) {
                return false;
            }
            
            byte[] byteAddress = address.getAddress();
            for (int i = 0; i < prefixBytes.length; i++) {
                if (byteAddress[i] != prefixBytes[i]) {
                    return false;
                }
            }
            byte lastByte = byteAddress[prefixBytes.length];
            for (int i = 0; i < suffixBytes.length; i++) {
                if (lastByte == suffixBytes[i]) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class NetmaskIPAddressPermission implements IPAddressPermission {
        private static final Pattern MASK_VALIDATOR = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})/((\\d{1,2})|(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3}))$");

        private static boolean canSupport(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            return matcher.matches();
        }
        
        private final byte[] networkAddressBytes;
        private final byte[] netmaskBytes;
        
        private NetmaskIPAddressPermission(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Mask " + mask + " does not match pattern " + MASK_VALIDATOR.pattern());
            }

            networkAddressBytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                String group = matcher.group(i + 1);
                int value = Integer.parseInt(group);
                if (value < 0 || 255 < value) {
                    throw new IllegalArgumentException("byte #" + i + " is not valid.");
                }
                networkAddressBytes[i] = (byte) value;
            }

            netmaskBytes = new byte[4];
            String netmask = matcher.group(6);
            if (null != netmask) {
                int value = Integer.parseInt(netmask);
                int pos = value / 8;
                int shift = 8 - value % 8;
                for (int i = 0; i < pos; i++) {
                    netmaskBytes[i] = (byte) 0xff;
                }
                netmaskBytes[pos] = (byte) (0xff << shift);
            } else {
                for (int i = 0; i < 4; i++) {
                    String group = matcher.group(i + 7);
                    int value = Integer.parseInt(group);
                    if (value < 0 || 255 < value) {
                        throw new IllegalArgumentException("byte #" + i + " is not valid.");
                    }
                    netmaskBytes[i] = (byte) value;
                }
            }
        }
        
        public boolean implies(InetAddress address) {
            if (false == address instanceof Inet4Address) {
                return false;
            }
            
            byte[] byteAddress = address.getAddress();
            for (int i = 0; i < 4; i++) {
                if ((netmaskBytes[i] & byteAddress[i]) != networkAddressBytes[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class ExactIPv6AddressPermission implements IPAddressPermission {
        private static final Pattern MASK_VALIDATOR = Pattern.compile("^(([a-fA-F0-9]{1,4}:){7})([a-fA-F0-9]{1,4})$");

        private static boolean canSupport(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            return matcher.matches();
        }

        private final byte[] bytes;
        
        private ExactIPv6AddressPermission(byte[] bytes) {
            this.bytes = bytes;
        }        
        
        private ExactIPv6AddressPermission(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Mask " + mask + " does not match pattern " + MASK_VALIDATOR.pattern());
            }
            
            bytes = new byte[16];
            int pos = 0;
            StringTokenizer tokenizer = new StringTokenizer(mask, ":");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                int value = Integer.parseInt(token, 16);
                bytes[pos++] = (byte) ((value & 0xff00) >> 8);
                bytes[pos++] = (byte) value;
            }
        }
        
        public boolean implies(InetAddress address) {
            if (false == address instanceof Inet6Address) {
                return false;
            }
            
            byte[] byteAddress = address.getAddress();
            for (int i = 0; i < 16; i++) {
                if (byteAddress[i] != bytes[i]) {
                    return false;
                }
            }
            return true;
        }
    }
    
    private static class NetmaskIPv6AddressPermission implements IPAddressPermission {
        private static final Pattern MASK_VALIDATOR = Pattern.compile("^(([a-fA-F0-9]{1,4}:){7}[a-fA-F0-9]{1,4})/((\\d{1,3})|(([a-fA-F0-9]{1,4}:){7}[a-fA-F0-9]{1,4}))$");

        private static boolean canSupport(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            return matcher.matches();
        }
        
        private final byte[] networkAddressBytes;
        private final byte[] netmaskBytes;
        
        private NetmaskIPv6AddressPermission(String mask) {
            Matcher matcher = MASK_VALIDATOR.matcher(mask);
            if (false == matcher.matches()) {
                throw new IllegalArgumentException("Mask " + mask + " does not match pattern " + MASK_VALIDATOR.pattern());
            }

            networkAddressBytes = new byte[16];
            int pos = 0;
            StringTokenizer tokenizer = new StringTokenizer(matcher.group(1), ":");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                int value = Integer.parseInt(token, 16);
                networkAddressBytes[pos++] = (byte) ((value & 0xff00) >> 8);
                networkAddressBytes[pos++] = (byte) value;
            }

            netmaskBytes = new byte[16];
            String netmask = matcher.group(4);
            if (null != netmask) {
                int value = Integer.parseInt(netmask);
                pos = value / 8;
                int shift = 8 - value % 8;
                for (int i = 0; i < pos; i++) {
                    netmaskBytes[i] = (byte) 0xff;
                }
                netmaskBytes[pos] = (byte) (0xff << shift);
            } else {
                pos = 0;
                tokenizer = new StringTokenizer(matcher.group(5), ":");
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    int value = Integer.parseInt(token, 16);
                    netmaskBytes[pos++] = (byte) ((value & 0xff00) >> 8);
                    netmaskBytes[pos++] = (byte) value;
                }
            }
        }
        
        public boolean implies(InetAddress address) {
            if (false == address instanceof Inet6Address) {
                return false;
            }
            
            byte[] byteAddress = address.getAddress();
            for (int i = 0; i < 16; i++) {
                if ((netmaskBytes[i] & byteAddress[i]) != networkAddressBytes[i]) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public static class IPAddressPermissionFactory {
        
        public static IPAddressPermission getIPAddressMask(String mask) {
            if (StartWithIPAddressPermission.canSupport(mask)) {
                return new StartWithIPAddressPermission(mask);
            } else if (ExactIPAddressPermission.canSupport(mask)) {
                return new ExactIPAddressPermission(mask);
            } else if (FactorizedIPAddressPermission.canSupport(mask)) {
                return new FactorizedIPAddressPermission(mask);
            } else if (NetmaskIPAddressPermission.canSupport(mask)) {
                return new NetmaskIPAddressPermission(mask);
            } else if (ExactIPv6AddressPermission.canSupport(mask)) {
                return new ExactIPv6AddressPermission(mask);
            } else if (NetmaskIPv6AddressPermission.canSupport(mask)) {
                return new NetmaskIPv6AddressPermission(mask);
            }
            throw new IllegalArgumentException("Mask " + mask + " is not supported.");
        }
    }
    
    public static class IPAddressPermissionEditor extends PropertyEditorSupport {
        private IPAddressPermission addressMask;
        
        public void setAsText(String text) throws IllegalArgumentException {
            addressMask = IPAddressPermissionFactory.getIPAddressMask(text);
         }

        public Object getValue() {
            return addressMask;
        }
    }
}
