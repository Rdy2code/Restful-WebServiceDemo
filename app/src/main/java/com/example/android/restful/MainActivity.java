package com.example.android.restful;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.restful.model.DataItem;
import com.example.android.restful.services.MyIntentService;
import com.example.android.restful.utils.NetworkHelper;
import com.example.android.restful.utils.RequestPackage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Starts an IntentService to retrieve a JSON from a network server
 * To convert the JSON to Java objects, or POJOs with getter and setter methods, parse the JSON
 * manually with code, or use any of the following third party libraries. The goal is to create a class
 * that represents the data objects returned by the data service.
 * 1.   jsonschema2pojo
 */
public class MainActivity extends AppCompatActivity {

    //region Constants
    //For authentication, change URL to access "secured" sited
    //Also refactor IntentService to send exception information back to the visual layer
    private static final String TAG = "MainActivity";
    private static final String JSON_URL =
            "http://560057.youcanlearnit.net/secured/json/itemsfeed.php";

    private static final String XML_URL =
            "http://560057.youcanlearnit.net/secured/xml/itemsfeed.php";
    //endregion

    //region Variables
    private boolean networkOk;
    TextView output;
    private Switch wifiSwitch;
    private WifiManager wifiManager;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView attemptsTextView;
    private int counter = 3;
    private List<DataItem> mItemList;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Map<String, Bitmap> mBitmaps;   //Memory storage of images.
    //endregion

    //region BroadcastReceivers
    private BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Filter for correct response or exception from IntentService
            if (intent.hasExtra(MyIntentService.MY_SERVICE_PAYLOAD)) {
                //Get the list of DataItem objects that represent the Json response from the backend
                //IntentService class
                DataItem[] dataItems =
                        (DataItem[]) intent.getParcelableArrayExtra(MyIntentService.MY_SERVICE_PAYLOAD);

                //Convert raw array to ArrayList of objects
                mItemList = Arrays.asList(dataItems);
                setUpRecyclerView();

            } else if (intent.hasExtra(MyIntentService.MY_SERVICE_EXCEPTION)) {
                String message = intent.getStringExtra(MyIntentService.MY_SERVICE_EXCEPTION);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:
                    wifiSwitch.setChecked(true);
                    wifiSwitch.setText("WiFi is ON ");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    wifiSwitch.setChecked(false);
                    wifiSwitch.setText("WiFi is OFF ");
                    break;
            }
        }
    };

    //endregion

    //region onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //region Initialize Layout Views
        wifiSwitch = findViewById(R.id.switch_wifi);
        userNameEditText = findViewById(R.id.user_name);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.btn_login);
        attemptsTextView = findViewById(R.id.tv_login_attempts);
        //endregion

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = userNameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!userName.isEmpty() && !password.isEmpty()) {
                    validate(userName, password);
                }
            }
        });

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //Listen for changes in the switch status from off to on
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    wifiManager.setWifiEnabled(true);
                    wifiSwitch.setText("WiFi is ON ");
                } else {
                    wifiManager.setWifiEnabled(false);
                    wifiSwitch.setText("WiFi is OFF ");
                }
            }
        });

        //Register the IntentFilter as a local broadcast for this activity
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter intentFilter = new IntentFilter(MyIntentService.MY_SERVICE_MESSAGE);
        //Hook up the receiver with the intentfilter
        manager.registerReceiver(localReceiver, intentFilter);
    }
    //endregion

    //region Lifecycle cleanup
    @Override
    protected void onStart() {
        super.onStart();
        //Set an intent filter to listen for global state changes in wifi connection
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        //Register this filter with the wifi Broadcast Receiver
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiStateReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(localReceiver);
    }
    //endregion

    //region Buttons
    //Start the IntentService
    public void runClickHandler(View view) {
        //Check network once again before requesting JSON
        networkOk = NetworkHelper.hasNetworkAccess(this);
        if (networkOk) {
            Intent intent = new Intent(this, MyIntentService.class);
            intent.setData(Uri.parse(XML_URL)); //TODO: Switch XML_URL to JSON_URL to use and parse json feed
            startService(intent);
        } else {
            Toast.makeText(this, "Network unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearClickHandler(View view) {
        output.setText("");
    }
    //endregion

    private void validate (String userName, String password) {
        if ((userName.equals("nadias")) && (password.equals("NadiasPassword"))) {
            networkOk = NetworkHelper.hasNetworkAccess(this);
            if (networkOk) {

                //To filter the network response according to specific search parameters,
                //create instance of RequestPackage class
                RequestPackage requestPackage = new RequestPackage();
                //Set the endpoint, base URL
                requestPackage.setEndPoint(JSON_URL);   //TODO: Or use XML_URL
                //Set the parameters to be used for the search, by category and type
                requestPackage.setParam("category", "Desserts");

                //TODO: Here is where you can set the method for the network search request to GET or POST
                requestPackage.setMethod("POST");

                //Create an explicit intent
                Intent intent = new Intent(this, MyIntentService.class);

                //Put everything we need to know about the request in the RequestPackage object
                intent.putExtra(MyIntentService.REQUEST_PACKAGE, requestPackage);

                //intent.setData(Uri.parse(XML_URL)); //TODO: Switch XML_URL to JSON_URL to use and parse json feed
                intent.putExtra(MyIntentService.MY_USER_NAME, userName);
                intent.putExtra(MyIntentService.MY_PASSWORD, password);
                startService(intent);

            } else {
                Toast.makeText(this, "Network unavailable", Toast.LENGTH_SHORT).show();
            }

        } else {
            counter --;
            attemptsTextView.setText(getString(R.string.textview_attempts_remaining, counter));
            if (counter == 0) {
                loginButton.setEnabled(false);
                Toast.makeText(this, "You have exceeded the max attempts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setUpRecyclerView () {
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (mItemList != null) {
            mAdapter = new MyAdapter(this, mItemList);
            recyclerView.setAdapter(mAdapter);
        }
    }
}
