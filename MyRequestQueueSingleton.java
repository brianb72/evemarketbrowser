package com.troff.evemarketbrowser;


import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;


public class MyRequestQueueSingleton {
    private static MyRequestQueueSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private MyRequestQueueSingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }


    public static synchronized MyRequestQueueSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyRequestQueueSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            //mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());

            Cache cache = new DiskBasedCache(mCtx.getCacheDir(), 30 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            mRequestQueue = new RequestQueue(cache, network, 4);        // how many connections max, up to 11?

            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


    // TODO availMem probably not what we want, can we look at heap to see how we are doing? read more
    public static void getRamInfo(Context context, String msg) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(mi);
        String memory = "Memory: " + NumberFormatter.shrinkWithSuffix(mi.availMem) + " / " + NumberFormatter.shrinkWithSuffix(mi.totalMem) + " |  " + msg;
        Log.d("troff", memory);

    }

}