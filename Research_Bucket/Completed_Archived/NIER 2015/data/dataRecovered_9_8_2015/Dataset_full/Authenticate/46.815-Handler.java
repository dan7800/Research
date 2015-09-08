/* $Id: Handler.java,v 1.1 2006/11/28 02:03:03 stack-sf Exp $
 *
 * Created October 28th, 2006
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.net.s3;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A protocol handler for an s3 scheme. To add this handler to the
 * java.net.URL set, you must define the system property
 * <code>-Djava.protocol.handler.pkgs=org.archive.net</code>.  So this handler
 * gets triggered, your S3 URLs must have a scheme of 's3' as in:
 * <code>s3://s3.amazonaws.com/org.archive-scratch/diff.txt</code> (TODO: An
 * s3s handler for secure access).  When run, this code will look for values in
 * system properties, <code>aws.access.key.id</code> and
 * <code>aws.access.key.secret</code>, to use authenticating w/ s3.  This
 * handler has dependency on a couple of the classes from s3-library-examples.
 * 
 * @see http://developer.amazonwebservices.com/connect/entry.jspa?externalID=132&categoryID=47
 * 
 * @author stack
 */
public class Handler extends URLStreamHandler {
    private static final String AWS_ACCESS_KEY_ID = "aws.access.key.id";
    private static final String AWS_ACCESS_KEY_SECRET = "aws.access.key.secret";
    // TODO: Use JVM Locale.
    private static final SimpleDateFormat format =
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ", Locale.US);
    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    protected URLConnection openConnection(URL u)
    throws IOException {
        // Do I need to strip any leading '/'?
        String resource = u.getFile();
        if (resource.charAt(0) == '/') {
            resource = resource.substring(1);
        }
        URL h = new URL("http", u.getHost(), u.getPort(), u.getFile());
        HttpURLConnection connection = (HttpURLConnection)h.openConnection();
        addAuthHeader(connection, "GET", resource);

        return connection;
    }
    
    
    /**
     * Add the appropriate Authorization header to the HttpURLConnection.
     * Below is based on code from s3-example-library from the AWSAuthConnection
     * class.
     * @param connection The HttpURLConnection to which the header will be added.
     * @param method The HTTP method to use (GET, PUT, DELETE)
     * @param resource The resource name (bucketName + "/" + key).
     */
    private void addAuthHeader(HttpURLConnection connection, String method,
            String resource) {
        if (connection.getRequestProperty("Date") == null) {
            connection.setRequestProperty("Date",
                format.format(new Date()) + "GMT");
        }
        if (connection.getRequestProperty("Content-Type") == null) {
            connection.setRequestProperty("Content-Type", "");
        }

        String id = System.getProperty(AWS_ACCESS_KEY_ID);
        if (id == null || id.length() <= 0) {
            throw new NullPointerException(AWS_ACCESS_KEY_ID +
                " system property is empty");
        }
        String secret = System.getProperty(AWS_ACCESS_KEY_SECRET);
        if (secret == null || secret.length() <= 0) {
            throw new NullPointerException(AWS_ACCESS_KEY_SECRET +
                " system property is empty");
        }
        String canonicalString = com.amazon.s3.Utils.makeCanonicalString(method,
            resource, connection.getRequestProperties());
        String encodedCanonical = com.amazon.s3.Utils.encode(secret,
            canonicalString, false);
        connection.setRequestProperty("Authorization",
            "AWS " + id + ":" + encodedCanonical);
    }

    /**
     * Main dumps rsync file to STDOUT.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args)
    throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java " +
                "-D" + AWS_ACCESS_KEY_ID + "=AWS_ACCESS_KEY_ID " +
                "-D" + AWS_ACCESS_KEY_SECRET + "=AWS_ACCESS_KEY_SECRET " +
                "org.archive.net.s3.Handler s3://AWS_HOST/bucket/key");
            System.exit(1);
        }
        URL u = new URL(args[0]);
        URLConnection connect = u.openConnection();
        // Write download to stdout.
        final int bufferlength = 4096;
        byte [] buffer = new byte [bufferlength];
        InputStream is = connect.getInputStream();
        try {
            for (int count = is.read(buffer, 0, bufferlength);
                    (count = is.read(buffer, 0, bufferlength)) != -1;) {
                System.out.write(buffer, 0, count);
            }
            System.out.flush();
        } finally {
            is.close();
        }
    }
}
