package com.troff.evemarketbrowser;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class MarketContainer {
    public long typeID;
    public String itemName;
    public long stationID;
    public long regionID;

    public enum MarketSide { BUY, SELL }

    public List<MarketOrder> listBuy = new ArrayList<MarketOrder>();
    public List<MarketOrder> listSell = new ArrayList<MarketOrder>();
    public List<HistoryDay> listHistory = new ArrayList<HistoryDay>();

    public BigDecimal sellPriceFivePercent = BigDecimal.ZERO;
    public BigDecimal buyPriceFivePercent = BigDecimal.ZERO;
    public long sellVolumeFivePercent = 0;
    public long buyVolumeFivePercent = 0;
    public long totalSellVolume = 0;
    public long totalBuyVolume = 0;
    public long historyAverageDailyVolume = 0;
    public long historyAverageDailyOrders = 0;


    // XTOR
    public MarketContainer(long typeID, String itemName) {
        this.typeID = typeID;
        this.itemName = itemName;
    }


    public void calculateAllMarketStats(int daysOfHistory) {
        this.totalSellVolume = calculateTotalMarketVolume(this.listBuy);
        this.totalBuyVolume = calculateTotalMarketVolume(this.listSell);

        // History
        this.historyAverageDailyVolume = getAverageDailyVolume(daysOfHistory);

        // Five percent
        calculateHowMuchToProcess5Percent(MarketSide.BUY);
        calculateHowMuchToProcess5Percent(MarketSide.SELL);
    }


    // Utils
    public BigDecimal getSellPriceFivePercent() { return sellPriceFivePercent; }
    public BigDecimal getBuyPriceFivePercent() { return buyPriceFivePercent; }
    public long getSellVolumeFivePercent() { return sellVolumeFivePercent; }
    public long getBuyVolumeFivePercent() { return buyVolumeFivePercent; }

    // Adding to list
    public void addBuyOrder(MarketOrder order) { listBuy.add(order); }
    public void addSellOrder(MarketOrder order) { listSell.add(order); }
    public void addHistory(HistoryDay day) { listHistory.add(day); }

    // Sort the OrderLists so the spread can be found with listSell[0] - listBuy[0]
    public void sortStandardOrder() {
        Collections.sort(this.listBuy, MarketOrder.COMPARE_PRICE_HIGHTOLOW);
        Collections.sort(this.listSell, MarketOrder.COMPARE_PRICE_LOWTOHIGH);

    }

    // Returns 0 if no spread
    public BigDecimal getMarketSpread() {
        // NOTE: The lists have to be sorted with sortStandardOrder() for this to work.
        this.sortStandardOrder();
        if (this.listBuy.size() < 1 || this.listSell.size() < 1) {
            return new BigDecimal("0");
        }
        BigDecimal buyPrice = this.listBuy.get(0).getPrice();
        BigDecimal sellPrice = this.listSell.get(0).getPrice();
        BigDecimal spread = sellPrice.subtract(buyPrice);
        if (spread.signum() == -1) {
            return new BigDecimal("0");   // spread can't be negative, bad data or bad sort
        }
        return spread;
    }



    public boolean parseCRESTMarketOrders(long limitToStation, String jsonData) {
        if (jsonData == null || jsonData.length() == 0) {
            Log.d("troff", "parseCRESTMarketOrders null or zero length jsonData");
            return true;
        }
        try {
            JSONObject jsonRootObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonRootObject.optJSONArray("items");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jo = jsonArray.getJSONObject(i);

                // Only work with orders at our station
                JSONObject joLocation = jo.optJSONObject("location");
                long stationID = joLocation.optLong("id", -1);

                if (this.itemName == "") {
                    // Get the name if it has not been set yet
                    JSONObject joType = jo.optJSONObject("type");
                    this.itemName = joType.optString("name", "!no name!");
                }
                if (stationID == limitToStation) {
                    // Rest of variables
                    long volume = jo.optLong("volume", -1);
                    BigDecimal price = new BigDecimal(jo.optString("price", "-1"));
                    boolean sellOrBuy = jo.optBoolean("buy", false);
                    MarketOrder order = new MarketOrder(price, volume);
                    if (sellOrBuy == false) {
                        this.listSell.add(order);
                    } else {
                        this.listBuy.add(order);
                    }
                }
            }
        } catch (JSONException e) { e.printStackTrace(); return false; }
        return true;
    }

    public boolean  parseCRESTHistory(String jsonData) {
        if (jsonData == null || jsonData.length() == 0) {
            Log.d("troff", "parseCrestHistory null or zero length jsonData");
            return true;
        }
        try {
            JSONObject jsonRootObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonRootObject.optJSONArray("items");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jo = jsonArray.getJSONObject(i);
                HistoryDay day = new HistoryDay(
                        jo.optString("date", ""),
                        jo.optLong("volume", -1),
                        jo.optLong("orderCount", -1),
                        new BigDecimal(jo.optString("lowPrice", "-1.0")),
                        new BigDecimal(jo.optString("highPrice", "-1.0")),
                        new BigDecimal(jo.optString("avgPrice", "-1.0"))
                );
                this.addHistory(day);
            }
        } catch (JSONException e) { e.printStackTrace(); return false; }
        return true;
    }


    // This function is used in our CREST updater to be a lightweight way to extract averages
    // during an update of many objects.
    public boolean parseCRESTGetAveragesFromHistory(String jsonData, int forNDays) {
        long totalVolume = 0;
        long totalOrders = 0;
        if (jsonData == null || jsonData.length() == 0) {
            Log.d("troff", "parseCrestHistory null or zero length jsonData");
            return false;
        }
        try {
            JSONObject jsonRootObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonRootObject.optJSONArray("items");
            for (int i = 0; i < jsonArray.length() && i < forNDays; ++i) {
                JSONObject jo = jsonArray.getJSONObject(i);
                totalOrders += jo.optLong("orderCount", 0);
                totalVolume += jo.optLong("volume", 0);
            }
        } catch (JSONException e) { e.printStackTrace(); return false; }

        this.historyAverageDailyVolume = totalVolume / forNDays;
        this.historyAverageDailyOrders = totalOrders / forNDays;
        return true;
    }






    public long calculateTotalMarketVolume(List<MarketOrder> orderList) {
        long totalVolume = 0;
        for (MarketOrder order : orderList) {
            totalVolume += order.getVol_remain();
        }
        return totalVolume;
    }


    public long getAverageDailyVolume(int overLastNDays) {
        long totalVolume = 0;
        int daysTotal = 0;

        if (overLastNDays < 1 || this.listHistory.isEmpty()) { return totalVolume; }

        ListIterator itr = listHistory.listIterator(listHistory.size());
        while (itr.hasPrevious()) {
            HistoryDay day = (HistoryDay) itr.previous();
            totalVolume += day.getVolume();
            ++daysTotal;
            if (daysTotal == overLastNDays) { break; }
        }

        if (daysTotal < 1) { return 0; }
        return totalVolume / daysTotal;
    }

    public long getAverageDailyOrders(int overLastNDays) {
        long totalOrders = 0;
        int daysTotal = 0;

        if (overLastNDays < 1 || this.listHistory.isEmpty()) { return totalOrders; }

        ListIterator itr = listHistory.listIterator(listHistory.size());
        while (itr.hasPrevious()) {
            HistoryDay day = (HistoryDay) itr.previous();
            totalOrders += day.getOrderCount();
            ++daysTotal;
            if (daysTotal == overLastNDays) { break; }
        }

        if (daysTotal < 1) { return 0; }
        return totalOrders / daysTotal;
    }







    public void calculateHowMuchToProcess5Percent() {
        calculateHowMuchToProcess5Percent(MarketSide.BUY);
        calculateHowMuchToProcess5Percent(MarketSide.SELL);
    }

    private void calculateHowMuchToProcess5Percent(MarketSide marketSide) {
        // Our local variables determined by marketSide
        List<MarketOrder> orderList;
        long totalVolumeOnMarket;
        long fivePercentVolume;

        // Setup our local variables, and initialize any fields if needed
        if (marketSide == MarketSide.BUY) {
            orderList = this.listBuy;
            Collections.sort(orderList, MarketOrder.COMPARE_PRICE_HIGHTOLOW);
            // Check the total buy volume if needed
            if (this.totalBuyVolume == 0) {
                this.totalBuyVolume = calculateTotalMarketVolume(this.listBuy);
            }
            // Set our local value
            totalVolumeOnMarket = this.totalBuyVolume;
            fivePercentVolume = (long)(this.totalBuyVolume * 0.05f);

        } else {
            orderList = this.listSell;
            Collections.sort(orderList, MarketOrder.COMPARE_PRICE_LOWTOHIGH);
            // Check the total sell volume if needed
            if (this.totalSellVolume == 0) {
                this.totalSellVolume = calculateTotalMarketVolume(this.listSell);
            }
            // Set our local value
            totalVolumeOnMarket = this.totalSellVolume;
            // Calculate 5% buy volume and set our local value
            fivePercentVolume = (long)(this.totalSellVolume * 0.05f);
        }

        // We need to know how much volume is on the market, and how much is 5%

        BigDecimal totalPrice = BigDecimal.ZERO;
        long processedVolume = 0;

        for (MarketOrder order : orderList) {
            // Look at the volume we are about to buy
            long testVolume = order.getVol_remain();
            long tempTestPlusProcessed = testVolume + processedVolume;

            // Would this order push us over how much we need to buy?
            if (Long.compare(tempTestPlusProcessed, fivePercentVolume) == 1) {
                // Buying too much with this order, trim it
                testVolume = fivePercentVolume - processedVolume;
            }
            // Calculate the costs
            BigDecimal costForThis = order.getPrice().multiply(new BigDecimal(testVolume));
            // Update the processed values
            totalPrice = totalPrice.add(costForThis);
            processedVolume += testVolume;
        }

        // CHECK: at the end of this processedVolume must equal fivePercentVolume
        if (Long.compare(processedVolume, fivePercentVolume) != 0) {
            Log.d("troff", "xxxxxxxxxxxx PROCESSVOLUME NOT EQUAL TO FIVEPERCENT VOLUME XXXXXXXXXXXXXXXXXXXXXXXX");
        }

        // 100 units * $2.50 = $250.00
        // $250.00 / 100 = $2.50 per
        // We bought processedVolume of the market and paid totalPrice, what is our average price per unit?
        BigDecimal pricePerUnit = BigDecimal.ZERO;

        if (fivePercentVolume > 0) {   // divide by zero check
            pricePerUnit = totalPrice.divide(new BigDecimal(fivePercentVolume), RoundingMode.CEILING);
        }


        // Save our values
        if (marketSide == MarketSide.BUY) {
            this.buyPriceFivePercent = pricePerUnit;
            this.buyVolumeFivePercent = fivePercentVolume;
            this.totalBuyVolume = totalVolumeOnMarket;
        } else {
            this.sellPriceFivePercent = pricePerUnit;
            this.sellVolumeFivePercent = fivePercentVolume;
            this.totalSellVolume = totalVolumeOnMarket;
        }
    }



}
