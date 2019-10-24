package com.troff.evemarketbrowser;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
    ResultCodes for bundle.send()
    1  success
    0  web request successful, but parse failure or no data found
   -1  failure in web request or other hard failure

 */

public class MyVolleyService extends IntentService {
    ResultReceiver mReceiver;
    ProgressDialog barProgressDialog;

    DatabaseHandler database = new DatabaseHandler(this);
    private int queuedRequests = 0;
    private int volleyResponseCount = 0;
    private int numberOfTypesRequested = 0;
    private boolean fillingToolMap = false;
    private long startTime, stopTime;

    // ResultCodes for bundle.send()
    public static int RESULTCODE_SUCCESS = 1;  // Web request and parse succeeded, container holds data
    public static int RESULTCODE_NODATA = 0;  // Web request succeeded, parse failed or no data
    public static int RESULTCODE_FAILURE = -1; // Web request failed or other hard failure

    // RequestTypes received through the IntentService
    public static int REQUEST_UNKNOWN = -1;
    public static int REQUEST_SQL_UPDATE = 1;
    public static int REQUEST_GROUP_TYPEIDS = 2;


    // String URL to various API endpoints
    private static String URL_EVECENTRAL_QUICKLOOK = "http://api.eve-central.com/api/quicklook?typeid=#tid#&usesystem=#sysid#";
    private static String URL_CREST_MARKETORDERS = "https://crest-tq.eveonline.com/market/#reg#/orders/?type=https://crest-tq.eveonline.com/inventory/types/#tid#/";
    private static String URL_CREST_HISTORY = "https://crest-tq.eveonline.com/market/#reg#/history/?type=https://crest-tq.eveonline.com/inventory/types/#tid#/";
    private static String URL_CREST_CATEGORIES = "https://crest-tq.eveonline.com/inventory/categories/";   // add "#/" for specific category
    private static String URL_CREST_GROUPS = "https://crest-tq.eveonline.com/inventory/groups/";  // add "#/" for specific group
    private static String URL_CREST_TYPES = "https://crest-tq.eveonline.com/types/"; // add "#/" for specific type


    // XTOR
    public MyVolleyService() {
        super("MyVolleyService");
    }


    private String extractNameFromMarketJSON(String jsonData) {
        String retName = "";

        if (jsonData != null && jsonData.length() != 0) {
            try {
                JSONObject jsonRootObject = new JSONObject(jsonData);
                JSONArray jsonArray = jsonRootObject.optJSONArray("items");
                if (jsonArray.length() >= 1) {
                    // All the items should have the same name, grab the first order in the array
                    JSONObject jo = jsonArray.getJSONObject(0);
                    if (jo != null) {
                        // Find the type object
                        JSONObject joType = jo.optJSONObject("type");
                        if (joType != null) {
                            // Extract the name
                            retName = joType.optString("name", "!no name!");
                        }
                    }
                }
            } catch (JSONException e) {
                Log.d("troff", "extractNameFromMarketJSON() - Could not extract name!");
            }
        }
        return retName;
    }


    private void startSQLUpdate(final long region, final long station) {
        MyServiceDataSingleton data = MyServiceDataSingleton.getInstance(this); // TODO is context from MarketBrowserActivity different from here?
        List<Long> listTypeID = data.getTypeIDList();

        this.numberOfTypesRequested = listTypeID.size();

        startTime = System.currentTimeMillis();
        fillingToolMap = true;

        //barProgressDialog = data.getProgressDialog();
        barProgressDialog = null;

        for (final Long typeID : listTypeID) {
            String urlMarket = URL_CREST_MARKETORDERS.replaceAll("#reg#", Long.toString(region)).replaceAll("#tid#", Long.toString(typeID));
            String urlHistory = URL_CREST_HISTORY.replaceAll("#reg#", Long.toString(region)).replaceAll("#tid#", Long.toString(typeID));
            StringRequest requestMarket = new StringRequest(Request.Method.GET, urlMarket,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //TimeSpan ts = mapTime.get(typeID);
                            --queuedRequests;
                            if (fillingToolMap) {
                                String name = extractNameFromMarketJSON(response);
                                database.addMarketOrder(typeID, response, region, station, name);
                                //ts.stopMarket = System.currentTimeMillis();
                            } else {
                                Log.d("troff", "******** Got market response when not filling map");
                                Toast.makeText(getApplicationContext(),
                                        "Got market response when not filling",
                                        Toast.LENGTH_SHORT).show();

                            }
                            //if (ts.all()) { Log.d("troff", "typeid: " + typeID + "  m: " + ts.timeMarket() + "  h: " + ts.timeHistory()); }
                            checkToolMapCompletion();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //TimeSpan ts = mapTime.get(typeID);
                            --queuedRequests;
                            if (fillingToolMap) {
                                //ts.stopMarket = System.currentTimeMillis();
                                //Log.d("troff", "got error for market request " + typeID + " with " + queuedRequests + " left");
                            } else {
                                Log.d("troff", "******** Got market failure when not filling map");
                                Toast.makeText(getApplicationContext(),
                                        "Got market failure when not filling",
                                        Toast.LENGTH_SHORT).show();

                            }
                            //if (ts.all()) { Log.d("troff", "typeid: " + typeID + "  m: " + ts.timeMarket() + "  h: " + ts.timeHistory()); }
                            checkToolMapCompletion();
                        }
                    });

            StringRequest requestHistory = new StringRequest(Request.Method.GET, urlHistory,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            --queuedRequests;
                            //TimeSpan ts = mapTime.get(typeID);
                            if (fillingToolMap) {
                                database.addHistory(typeID, response);
                                //ts.stopHistory = System.currentTimeMillis();
                            } else {
                                Log.d("troff", "******** Got history response when not filling map");
                                Toast.makeText(getApplicationContext(),
                                        "Got history response when not filling",
                                        Toast.LENGTH_SHORT).show();
                            }
                            //if (ts.all()) { Log.d("troff", "typeid: " + typeID + "  m: " + ts.timeMarket() + "  h: " + ts.timeHistory()); }
                            checkToolMapCompletion();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            --queuedRequests;
                            //TimeSpan ts = mapTime.get(typeID);

                            if (fillingToolMap) {
                                //ts.stopHistory = System.currentTimeMillis();
                                //Log.d("troff", "got error for history request " + typeID + " with " + queuedRequests + " left");
                            } else {
                                Log.d("troff", "******** Got history failure when not filling map");
                                Toast.makeText(getApplicationContext(),
                                        "Got history failure when not filling",
                                        Toast.LENGTH_SHORT).show();

                            }
                            //if (ts.all()) { Log.d("troff", "typeid: " + typeID + "  m: " + ts.timeMarket() + "  h: " + ts.timeHistory()); }
                            checkToolMapCompletion();
                        }
                    });

            // time span check
            //TimeSpan ts = new TimeSpan();
            //ts.start = System.currentTimeMillis();
            //mapTime.put(typeID, ts);

            MyRequestQueueSingleton.getInstance(this).addToRequestQueue(requestMarket);
            MyRequestQueueSingleton.getInstance(this).addToRequestQueue(requestHistory);
            queuedRequests += 2;
        }

    }

    void checkToolMapCompletion() {
        ++volleyResponseCount;

        if (volleyResponseCount % 20 == 0) {
            Bundle data = new Bundle();
            data.putInt("progress", volleyResponseCount / 2);
            mReceiver.send(MyVolleyReceiver.PROGRESS_UPDATE, data);
            /*
            if (barProgressDialog != null) {
                Log.d("troff", "]]]]]]]]]]]]] Progress Bar: " + volleyResponseCount / 2);
                barProgressDialog.setProgress(0);
                barProgressDialog.setProgress(volleyResponseCount / 2);
            }*/
            //MyRequestQueueSingleton.getRamInfo(getApplicationContext(), "Requests " + queuedRequests);
        }


        if (!fillingToolMap) {
            Log.d("troff", "******** checkToolMapCompletion() called when not filling toolMap");
            hideProgressDialogBox();
            return;
        }

        if (queuedRequests < 0) {
            Log.d("troff", "******** queuedRequests is negative!");
        }

        if (volleyResponseCount >= (numberOfTypesRequested * 2)) {
            stopTime = System.currentTimeMillis();
            hideProgressDialogBox();
            fillingToolMap = false;
            Log.d("troff", "******** SQL Update finished in " + (stopTime - startTime));
            // Send the result back to the caller
            Bundle b = new Bundle();
            b.putInt("request", REQUEST_SQL_UPDATE);
            mReceiver.send(RESULTCODE_SUCCESS, b);

        }

    }


    private void hideProgressDialogBox() {
        if (barProgressDialog != null) {
            barProgressDialog.hide();
            barProgressDialog = null;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
        Log.d("troff", "INSIDE VOLLEY SERVICE");
        mReceiver = intent.getParcelableExtra("receiverTag");
        int requestType = intent.getIntExtra("request", -1);

        if (requestType == REQUEST_SQL_UPDATE) {
            long regionID = intent.getLongExtra("region", -1);
            long stationID = intent.getLongExtra("station", -1);
            startSQLUpdate(regionID, stationID);
        } else if (requestType == REQUEST_GROUP_TYPEIDS) {
            long groupID = intent.getLongExtra("group", -1);
            getGroup(groupID);

        } else {
            Log.d("troff", "*** Unknown request type " + requestType);
            hideProgressDialogBox();
            Bundle b = new Bundle();
            b.putInt("request", REQUEST_UNKNOWN);
            mReceiver.send(RESULTCODE_FAILURE, b);
        }

    }


    private void getGroup(final long groupID) {
        String url = URL_CREST_GROUPS + groupID + "/";
        final List<Long> groupTypeIDs = new ArrayList<Long>();

        final Bundle b = new Bundle();
        b.putInt("request", REQUEST_GROUP_TYPEIDS);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRootObject = new JSONObject(response);
                            JSONArray jsonArray = jsonRootObject.optJSONArray("types");
                            // Extract each object from the 'types' array and add it to our map
                            for (int i = 0; i < jsonArray.length(); ++i) {
                                JSONObject jo = jsonArray.getJSONObject(i);
                                long typeID = jo.optLong("id", -1);
                                if (typeID != -1) {
                                    groupTypeIDs.add(typeID);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        MyServiceDataSingleton.getInstance(getApplicationContext()).setGroupList(groupTypeIDs);
                        mReceiver.send(RESULTCODE_SUCCESS, b);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Log.d("troff", "MarketBrowserActivity - Group request for " + groupID + " failed.");
                        Toast.makeText(getApplicationContext(),
                                "Failed to get group " + groupID,
                                Toast.LENGTH_SHORT).show();
                        MyServiceDataSingleton.getInstance(getApplicationContext()).setGroupList(new ArrayList<Long>()); // blank list
                        mReceiver.send(RESULTCODE_FAILURE, b);

                    }
                });
        MyRequestQueueSingleton.getInstance(this).addToRequestQueue(stringRequest);
    } // getGroup


}
