package org.apache.openejb.arquillian.remote.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author rmannibucau
 */
public class RemoteUtil {
    private RemoteUtil() {
        // no-op
    }

    public static String readContent(String url) throws MalformedURLException, IOException, UnsupportedEncodingException {
        InputStream is = new URL(url).openStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        return new String(os.toByteArray(), "UTF-8");
    }
}
