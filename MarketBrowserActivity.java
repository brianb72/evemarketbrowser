package com.troff.evemarketbrowser;

import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.os.Handler;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.StringRequest;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketBrowserActivity extends AppCompatActivity implements MyVolleyReceiver.Receiver {
/*
    class TimeSpan {
        long start = 0;
        long stopMarket = 0;
        long stopHistory = 0;
        public boolean all() {
            return (start != 0 && stopMarket != 0 && stopHistory != 0);
        }
        public long timeMarket() { return stopMarket - start; }
        public long timeHistory() { return stopHistory - start; }
    }

    Map<Long, TimeSpan> mapTime = new HashMap<Long, TimeSpan>();
*/

    ProgressDialog barProgressDialog;
    Handler updateBarHandler;

    public MyVolleyReceiver mReceiver;

    String URL_CREST_MARKETORDERS = "https://crest-tq.eveonline.com/market/10000002/orders/?type=https://crest-tq.eveonline.com/inventory/types/";
    String URL_CREST_HISTORY = "https://crest-tq.eveonline.com/market/10000002/history/?type=https://crest-tq.eveonline.com/inventory/types/";
    private static String URL_CREST_CATEGORIES = "https://crest-tq.eveonline.com/inventory/categories/";   // add "#/" for specific category
    private static String URL_CREST_GROUPS = "https://crest-tq.eveonline.com/inventory/groups/";  // add "#/" for specific group

    Map<Long, MarketContainer> mapItems = new HashMap<Long, MarketContainer>();
    boolean  fillingToolMap = false;     // Are we processing requests for the toolmap or not?
    int queuedRequests = 0, volleyResponseCount = 0;
    long startTime, stopTime;





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sql_update:
                startSQLUpdate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void startGroupFetchTypeIDs(long groupID) {
        Intent intent = new Intent(MarketBrowserActivity.this, MyVolleyService.class);
        intent.putExtra("receiverTag", mReceiver);
        intent.putExtra("request", MyVolleyService.REQUEST_GROUP_TYPEIDS);
        intent.putExtra("group", groupID);   // hard coded jita 4-4

        Log.d("troff", "*** starting service");
        startService(intent);

    }

    public void startSQLUpdate() {
        Long [] id = new Long[] { 3074L, 37291L, 41139L, 3082L, 13878L, 3090L, 7703L, 7705L, 3098L, 7707L, 7709L, 3106L, 14375L, 13864L, 13865L, 3114L, 13867L, 13868L, 14381L, 13870L, 14383L, 13872L, 561L, 562L, 563L, 564L, 565L, 566L, 567L, 568L, 569L, 570L, 571L, 572L, 573L, 574L, 575L, 13888L, 7745L, 3138L, 7747L, 12356L, 7749L, 13894L, 13880L, 14409L, 3146L, 14411L, 14413L, 7247L, 7249L, 3154L, 7251L, 7253L, 3162L, 3170L, 7783L, 7785L, 3178L, 7787L, 7789L, 20591L, 3186L, 41076L, 14393L, 41078L, 7287L, 7289L, 7291L, 7293L, 14272L, 13882L, 14274L, 20589L, 7827L, 7829L, 7831L, 7833L, 7327L, 7329L, 7331L, 7333L, 41126L, 41127L, 41128L, 41129L, 41130L, 15815L, 14395L, 41138L, 22899L, 41140L, 41141L, 41142L, 15817L, 14276L, 15818L, 22901L, 20448L, 7367L, 7369L, 7371L, 7373L, 14286L, 13885L, 22907L, 14397L, 7407L, 7409L, 7411L, 7413L, 14377L, 14278L, 13886L, 13866L, 14379L, 16132L, 16133L, 16134L, 15421L, 41079L, 20587L, 7447L, 7449L, 7451L, 7453L, 15835L, 13873L, 15814L, 14399L, 14391L, 3122L, 14387L, 15838L, 13876L, 7487L, 7489L, 7491L, 7493L, 15423L, 13879L, 12344L, 13881L, 3130L, 13883L, 15816L, 14405L, 14401L, 13884L, 7535L, 7537L, 7539L, 13890L, 7541L, 41077L, 22903L, 22905L, 7743L, 22909L, 22911L, 15834L, 22913L, 22915L, 14385L, 13889L, 13887L, 12354L, 13891L, 13874L, 13892L, 7579L, 7581L, 7583L, 7585L, 14403L, 14407L, 12346L, 37300L, 37301L, 10678L, 37303L, 10680L, 15836L, 10688L, 10690L, 7619L, 10692L, 7621L, 10694L, 7623L, 14280L, 7625L, 14282L, 14284L, 15821L, 13893L, 15823L, 15824L, 15825L, 15826L, 15827L, 15828L, 15829L, 15830L, 15831L, 15832L, 15833L, 3546L, 14415L, 15820L, 15837L, 3550L, 15840L, 15841L, 20450L, 15843L, 15844L, 34278L, 37302L, 34280L, 34282L, 7663L, 7665L, 7667L, 7669L, 14389L };

        List<Long> typeIDs = Arrays.asList(id);


        launchBarDialog(typeIDs.size());

        MyServiceDataSingleton data = MyServiceDataSingleton.getInstance(this);
        data.setTypeIDList(typeIDs);
        //data.setProgressDialog(barProgressDialog);

        // Start the intent
        Intent intent = new Intent(MarketBrowserActivity.this, MyVolleyService.class);
        intent.putExtra("receiverTag", mReceiver);
        intent.putExtra("request", MyVolleyService.REQUEST_SQL_UPDATE);
        intent.putExtra("station", 60003760L);   // hard coded jita 4-4
        intent.putExtra("region", 10000002L);    // hard coded the forge

        Log.d("troff", "*** starting service");
        startService(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_browser);

        // Setup our receiver
        mReceiver = new MyVolleyReceiver(new Handler());
        mReceiver.setReceiver(this);
        Log.d("troff", "on create d called");


        // Load our data from the SQL database
        DatabaseHandler db = new DatabaseHandler(this);
        mapItems = db.getAllDataAsMap();

        // Update our data singleton
        MyServiceDataSingleton.getInstance(this).setMap(mapItems);

        updateControl();

        // startGroupFetchTypeIDs(74L);
    }




    public void onReceiveResult(int resultCode, Bundle resultData) {

        if (resultData == null) {
            Toast.makeText(getApplicationContext(), "MarketBrowser - Received null bundle from service!", Toast.LENGTH_LONG).show();
            Log.d("troff", "MarketBrowser - Received null bundle from service!");
            return;
        }

        if (resultCode == MyVolleyReceiver.PROGRESS_UPDATE) {
            int progress = resultData.getInt("progress");
            barProgressDialog.setProgress(progress);
            Log.d("troff", "[[[[[ Progress " + progress);
            return;
        }


        int requestType = resultData.getInt("request", -1);

        if (requestType == MyVolleyService.REQUEST_SQL_UPDATE) {

            if (resultCode != MyVolleyService.RESULTCODE_SUCCESS) {
                Toast.makeText(getApplicationContext(), "MarketBrowser - SQL Update Failed", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kill the dialog
            barProgressDialog.hide();
            barProgressDialog = null;

            // Update our map
            DatabaseHandler db = new DatabaseHandler(this);
            mapItems = db.getAllDataAsMap();

            // Update our data singleton
            MyServiceDataSingleton.getInstance(this).setMap(mapItems);

            // Update the control
            updateControl();
        } else if (requestType == MyVolleyService.REQUEST_GROUP_TYPEIDS) {

            if (resultCode != MyVolleyService.RESULTCODE_SUCCESS) {
                Toast.makeText(getApplicationContext(), "MarketBrowser - Get Group TypeID's failed", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Long> group = MyServiceDataSingleton.getInstance(getApplicationContext()).getGroupList();
            String buf = "";
            for (Long typeID : group) {
                buf += typeID.toString() + "L, ";
            }
            Log.d("troff", "Group list: " + buf);
            Toast.makeText(getApplicationContext(), "Group Update Finished", Toast.LENGTH_SHORT).show();
        }

    }




    public void launchBarDialog(int maxItems) {
        barProgressDialog = new ProgressDialog(MarketBrowserActivity.this);
        barProgressDialog.setTitle("CREST Download");
        barProgressDialog.setMessage("Requesting Market and History Data...");
        barProgressDialog.setIndeterminate(false);
        barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setProgress(0);
        barProgressDialog.setMax(maxItems);
        barProgressDialog.show();
    }




    void updateControl() {
        NumberFormatter nf = new NumberFormatter();

        List<String[]> myDataList = new ArrayList<String[]>();
        List<Long> myIDList = new ArrayList<Long>();

        // Setup the strings for the list control
        for (MarketContainer cont : this.mapItems.values()) {
            String itemName = cont.itemName;
            String avgDailyOrders = nf.shrinkWithSuffix(cont.historyAverageDailyOrders);
            String avgDailyVolume = nf.shrinkWithSuffix(cont.historyAverageDailyVolume);
            myDataList.add(new String[] { itemName, avgDailyOrders, avgDailyVolume });
            myIDList.add(cont.typeID);
        }

        // Attach the strings to the list adapter
        ListView listView = (ListView) this.findViewById(R.id.listMarket);
        final MarketOverviewListAdapter listAdapter = new MarketOverviewListAdapter(this, myDataList, myIDList);
        if (listAdapter != null) { listView.setAdapter(listAdapter); }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MarketOverviewListAdapter mv = (MarketOverviewListAdapter)parent.getAdapter();
                Long typeid = mv.getTypeAtPosition(position);

                MarketContainer cont = mapItems.get(typeid);
                if (cont == null) {
                    Log.d("troff", "updateControl() - typeid " + typeid + " not found in map!");
                    Toast.makeText(getApplicationContext(),
                            "Internal Error - typeid not found in map",
                            Toast.LENGTH_LONG).show();
                }

                Intent intent = new Intent(MarketBrowserActivity.this, DOMAndChartActivity.class);
                intent.putExtra("typeid", typeid);
                startActivity(intent);
            }
        });

    }



}
