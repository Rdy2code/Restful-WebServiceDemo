package com.example.android.restful.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.android.restful.model.DataItem;
import com.example.android.restful.parsers.MyXMLParser;
import com.example.android.restful.utils.HttpHelper;
import com.example.android.restful.utils.RequestPackage;
import com.google.gson.Gson;

import java.io.IOException;

/**
 * Service class that runs tasks on a background thread and off the main thread, i.e., network requests
 * To send results back to the view, use a LocalBroadcastManager, and register a BroadcastReceiver
 * with the activity. Designed to run longer running tasks. Is completely decoupled from the interface.
 * To broadcast, will need some constants: one to id the message and one to id the message payload
 *
 * Place breakpoint in code at Intent, to stop the execution there, then run in debug, come back
 * and hover over "dataItems" to view the array of strongly typed objects
 */

public class MyIntentService extends IntentService {

    //region Static Constants
    private static final String TAG = "MyIntentService";
    public static final String MY_SERVICE_MESSAGE = "myServiceMessage";
    public static final String MY_SERVICE_PAYLOAD = "myServicePayload";
    public static final String MY_SERVICE_EXCEPTION = "myServiceException";
    public static final String MY_USER_NAME = "myUserName";
    public static final String MY_PASSWORD = "myPassword";
    //Key for network request parameters
    public static final String REQUEST_PACKAGE = "requestPackage";
    //endregion

    public MyIntentService() {
        super("MyIntentService");
    }

    //Receives instructions packaged as an intent
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        //Get the intent and its data and start the long-running background task
        //Uri uri = intent.getData(); //TODO: Use this code if not filtering for params

        //Unpack the data from the intent
        RequestPackage requestPackage = intent.getParcelableExtra(REQUEST_PACKAGE);
        String userName = intent.getStringExtra(MY_USER_NAME);
        String password = intent.getStringExtra(MY_PASSWORD);
        String response;
        try {

            //TODO: Add logic for obtaining user name and password at login time
            response = HttpHelper.downloadUrl(requestPackage, userName,  password);
        } catch (IOException e) {
            e.printStackTrace();
            //Get exception information and broadcast back to application
            Intent exceptionIntent = new Intent (MY_SERVICE_MESSAGE);
            exceptionIntent.putExtra(MY_SERVICE_EXCEPTION, e.getMessage());
            LocalBroadcastManager manager =
                    LocalBroadcastManager.getInstance(getApplicationContext());
            manager.sendBroadcast(exceptionIntent);
            return;
        }

        //region Parsers

        //JSON PARSER
        //TODO: Use this block of code to parse json feed
        Gson gson = new Gson();
        DataItem[] dataItems = gson.fromJson(response, DataItem[].class);

        //XML PARSER
        //DataItem[] dataItems = MyXMLParser.parseFeed(response);

        //endregion

        //Communicate the results of the task back to the controller using an intent, identified by key
        Intent messageIntent = new Intent (MY_SERVICE_MESSAGE); //Intent label
        messageIntent.putExtra(MY_SERVICE_PAYLOAD, dataItems);   //Intent key-value content
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance(getApplicationContext());
        manager.sendBroadcast(messageIntent);
    }

    //IntentServices have a lifecycle of their own, and we can override onCreate and onDestroy
    //to do set up and clean work as necessary
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }
}
