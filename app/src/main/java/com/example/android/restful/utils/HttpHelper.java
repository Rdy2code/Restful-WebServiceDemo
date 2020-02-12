package com.example.android.restful.utils;

import android.util.Base64;
import android.view.textclassifier.TextLinks;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import okhttp3.Credentials;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Helper class for working with a remote server and downloading a json response from a URL
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
        if (requestPackage.getMethod().equals("GET") && encodedParams.length() > 0) {
            //Rewrite the URL if necessary, that is if params are present
            address = String.format("%s?%s", address, encodedParams);
        }

        //Create an instance of the OkHttpClient class
        OkHttpClient client = new OkHttpClient();

        //Construct the request for OkHttp
        Request.Builder requestBuilder = new Request.Builder()
                .url(address)
                .addHeader("Authorization", Credentials.basic(user, password));

        if (requestPackage.getMethod().equals("POST")) {
            //Tell OkHttp that we are simulating a Web form
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);

            Map<String, String> params = requestPackage.getParams();
            for (String key :
                    params.keySet()) {
                builder.addFormDataPart(key, params.get(key));
            }
            RequestBody requestBody = builder.build();
            requestBuilder.method("POST", requestBody);
        }

        //Get a Request object and a Response
        Request request = requestBuilder.build();
        Response response = client.newCall(request).execute();

        //If the Response is successful
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Exception: response code " + response.code());
        }
    }
}