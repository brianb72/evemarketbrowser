package com.troff.evemarketbrowser;

import android.app.ProgressDialog;
import android.content.Context;

import java.util.List;
import java.util.Map;

public class MyServiceDataSingleton {
    private static MyServiceDataSingleton mInstance;
    private static Context myContext;

    private ProgressDialog progressDialog;
    private List<Long> inTypeIDList;
    private Map<Long, MarketContainer> outMap;    // The item map of all loaded MarketContainers
    private List<Long> groupList;                 // All TypeID's in a specific group

    public MyServiceDataSingleton(Context context) {
        myContext = context;
    }


    public static synchronized MyServiceDataSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyServiceDataSingleton(context);
        }
        return mInstance;
    }


    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    public ProgressDialog getProgressDialog() {
        return this.progressDialog;
    }


    public void setTypeIDList(List<Long> typeIDList) {
        this.inTypeIDList = typeIDList;
    }

    public List<Long> getTypeIDList() {
        return this.inTypeIDList;
    }

    public void setMap(Map<Long, MarketContainer> map) {
        this.outMap = map;
    }

    public Map<Long, MarketContainer> getMap() {
        return this.outMap;
    }

    public MarketContainer getMarketItem(Long typeID) {
        if (outMap == null) { return null; }
        return outMap.get(typeID);
    }


    public List<Long> getGroupList() { return this.groupList; }
    public void setGroupList(List<Long> groupList) { this.groupList = groupList; }

}

