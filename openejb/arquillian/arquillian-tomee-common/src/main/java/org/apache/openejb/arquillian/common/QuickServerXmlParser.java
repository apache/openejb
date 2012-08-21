package org.apache.openejb.arquillian.common;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class QuickServerXmlParser extends DefaultHandler {
    private static final SAXParserFactory FACTORY = SAXParserFactory.newInstance();
    static {
        FACTORY.setNamespaceAware(true);
        FACTORY.setValidating(false);
    }

    private static final String STOP_KEY = "STOP";
    private static final String HTTP_KEY = "HTTP";
    private static final String AJP_KEY = "AJP";
    private static final String DEFAULT_CONNECTOR_KEY = HTTP_KEY;

    public static final String DEFAULT_HTTP_PORT = "8080";
    public static final String DEFAULT_STOP_PORT = "8005";
    public static final String DEFAULT_AJP_PORT = "8009";

    private final Map<String, String> ports = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    public QuickServerXmlParser() { // ensure defaults are present
        ports.put(STOP_KEY, DEFAULT_STOP_PORT);
        ports.put(HTTP_KEY, DEFAULT_HTTP_PORT);
        ports.put(AJP_KEY, DEFAULT_AJP_PORT);
    }

    @Override
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes) throws SAXException {
        if ("Server".equalsIgnoreCase(localName)) {
            final String port = attributes.getValue("port");
            if (port != null) {
                ports.put(STOP_KEY, port);
            } else {
                ports.put(STOP_KEY, DEFAULT_STOP_PORT);
            }
        } else if ("Connector".equalsIgnoreCase(localName)) {
            String protocol = attributes.getValue("protocol");
            if (protocol == null) {
                protocol = DEFAULT_CONNECTOR_KEY;
            } else if (protocol.contains("/")) {
                protocol = protocol.substring(0, protocol.indexOf("/"));
            }
            final String port = attributes.getValue("port");
            ports.put(protocol.toUpperCase(), port);
        }
    }

    public static QuickServerXmlParser parse(final File serverXml) {
        final QuickServerXmlParser handler = new QuickServerXmlParser();
        try {
            final SAXParser parser = FACTORY.newSAXParser();
            parser.parse(serverXml, handler);
        } catch (Exception e) {
            // no-op: using defaults
        }
        return handler;
    }

    public String http() {
        return value(HTTP_KEY, DEFAULT_HTTP_PORT);
    }

    public String ajp() {
        return value(AJP_KEY, DEFAULT_AJP_PORT);
    }

    public String stop() {
        return value(STOP_KEY, DEFAULT_STOP_PORT);
    }

    private String value(final String key, final String defaultValue) {
        final String port = ports.get(key);
        if (port == null) {
            return defaultValue;
        }
        return port;
    }

    @Override
    public String toString() {
        return "QuickServerXmlParser" + ports;
    }
}
