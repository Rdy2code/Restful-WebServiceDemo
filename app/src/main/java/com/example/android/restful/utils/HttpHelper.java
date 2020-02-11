package com.example.android.restful.utils;

import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper class for working with a remote server and downloading a json respone from a URL
 */

public class HttpHelper {

    /**
     * @param requestPackage
     * @param user
     * @param password
     * @return
     * @throws IOException
     */
    public static String downloadUrl(RequestPackage requestPackage, String user, String password)
            throws IOException {

        //Endpoint is set in the MainActivity when the request is sent to the IntentService
        String address = requestPackage.getEndpoint();
        String encodedParams = requestPackage.getEncodedParams();

        //TODO: Depending on the method used, assemble the endpoint of the request.
        if (requestPackage.getMethod().equals("GET") &&  encodedParams.length() > 0) {
            //Rewrite the URL if necessary, that is if params are present
            address = String.format("%s?%s", address, encodedParams);
        }

        InputStream inputStream = null;

        //Create byte array and string for user authentication
        byte[] loginBytes = (user + ":" + password).getBytes();
        StringBuilder loginBuilder = new StringBuilder()
                .append("Basic ")
                .append(Base64.encodeToString(loginBytes, Base64.DEFAULT));

        try {

            //Wrap string in URL object
            URL url = new URL(address);
            //Open a connection to this url
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //Pass the login credentials to the web service
            conn.addRequestProperty("Authorization", loginBuilder.toString());

            //Set properties on the connection
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod(requestPackage.getMethod());
            conn.setDoInput(true);  //Means we are receiving data from the web service into the application

            //If we want to conduct a POST type search, run this block
            if (requestPackage.getMethod().equals("POST") &&
                    encodedParams.length() > 0) {
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(requestPackage.getEncodedParams());
                writer.flush();
                writer.close();
            }

            //Reach out to the server and establish a connection
            conn.connect();

            //Read the response code for the connection
            int responseCode = conn.getResponseCode();

            //If we do not get a good connection, throw an exception
            if (responseCode != 200) {
                throw new IOException("Got response code " + responseCode);
            }

            //If everything is okay, get an input stream from the connection and return a String
            inputStream = conn.getInputStream();
            return readStream(inputStream);

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * Reads an InputStream and converts it to a String.
     *
     * @param stream
     * @return
     * @throws IOException
     */
    private static String readStream(InputStream stream) throws IOException {

        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        BufferedOutputStream out = null;
        try {
            int length = 0;
            out = new BufferedOutputStream(byteArray);
            while ((length = stream.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            return byteArray.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}