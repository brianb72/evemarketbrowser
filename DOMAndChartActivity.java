package com.troff.evemarketbrowser;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DOMAndChartActivity extends AppCompatActivity {
    private CombinedChart mChart;
    private long myTypeID = -1;
    private MarketContainer marketItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_domand_chart);

        // Get the bundle and extract the typeid
        Bundle b = getIntent().getExtras();
        if (b == null) {
            Toast.makeText(getApplicationContext(),"DomAndChart - Passed bundle not found!", Toast.LENGTH_SHORT).show();
            this.myTypeID = -1;
            return;
        } else {
            this.myTypeID = b.getLong("typeid", -1);
        }

        // Load data from our singleton
        this.marketItem = MyServiceDataSingleton.getInstance(getApplicationContext()).getMarketItem(this.myTypeID);
        if (this.marketItem == null) {
            Toast.makeText(getApplicationContext(), "DomAndChart - MarketItem not found in dataset!", Toast.LENGTH_LONG).show();
            Log.d("troff", "DomAndChart - MarketItem not found in dataset!");
        }

        // Fetch the Orders and History from the database
        DatabaseHandler db = new DatabaseHandler(this);
        db.addOrdersAndHistoryToContainer(this.marketItem, 60003760L);  // hard coded to jita
        this.marketItem.calculateAllMarketStats(21);


        // Setup the dom fragment

        getFragmentManager().beginTransaction().add(R.id.llDomFragment,
                FragmentDomSplitWithHeader.newInstance(this.myTypeID), Integer.toString(R.id.llDomFragment)).commit();
        getFragmentManager().executePendingTransactions();


        Log.d("troff", "DOM loaded");
    }

    protected void onStart() {
        super.onStart();

        FragmentDomSplitWithHeader frag = (FragmentDomSplitWithHeader) getFragmentManager().findFragmentByTag(Integer.toString(R.id.llDomFragment));
        frag.loadDomOntoFragment();

        setupChart();
    }


    public void loadDataOntoChart(int start, int stop) {
        if (start < 0 || stop < 0 || start > stop || stop >= marketItem.listHistory.size()) {
            return;
        }

        // Setup the XVALS which are the dates
        ArrayList<String> xVals = new ArrayList<String>();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        for (int i = start; i <= stop; ++i) {
            xVals.add(df.format(marketItem.listHistory.get(i).date));
        }

        // Setup the Y values
        ArrayList<BarEntry> yVolume = new ArrayList<BarEntry>();
        ArrayList<CandleEntry> yPrice = new ArrayList<CandleEntry>();
        for (int i = start; i <= stop; ++i) {
            yVolume.add(
                    new BarEntry(
                             marketItem.listHistory.get(i).getVolume(),
                            i - start)
            );
            yPrice.add(
                    new CandleEntry(
                            i - start,
                            marketItem.listHistory.get(i).getHighPrice().floatValue(),
                            marketItem.listHistory.get(i).getLowPrice().floatValue(),
                            marketItem.listHistory.get(i).getAvgPrice().floatValue(),
                            marketItem.listHistory.get(i).getAvgPrice().floatValue()
                    )
            );

        } // for

        // Add the Y values to data sets, also configure colors and styles of each set
        BarDataSet barDataSet = new BarDataSet(yVolume, "Volume");
        barDataSet.setColor(Color.LTGRAY);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(10f);
        barDataSet.setDrawValues(false);
        barDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);


        CandleDataSet candleDataSet = new CandleDataSet(yPrice, "Price");
        candleDataSet.setColor(Color.rgb(80, 80, 80));
        candleDataSet.setShadowWidth(2.0f);
        //candleDataSet.setBarSpace(0.3f);
        candleDataSet.setValueTextSize(10f);
        candleDataSet.setDrawValues(false);
        candleDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        // Create the data from the data sets
        BarData barData = new BarData();
        barData.addDataSet(barDataSet);
        CandleData candleData = new CandleData();
        candleData.addDataSet(candleDataSet);

        // Setup the combined chart and add the data
        CombinedData combinedData = new CombinedData(xVals);
        combinedData.setData(barData);
        combinedData.setData(candleData);

        // Finally, add the combined data to the chart to populate it with data
        mChart.setData(combinedData);
        mChart.invalidate();

    }

    public void setupChart() {
        mChart = (CombinedChart) findViewById(R.id.chart01);
        mChart.setDescription(marketItem.itemName);

        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawGridBackground(true);
        mChart.setDrawBarShadow(false);

        mChart.setDrawOrder(new CombinedChart.DrawOrder[] { CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.CANDLE} );

        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getAxis(YAxis.AxisDependency.RIGHT).setSpaceTop(75f);
        mChart.getAxis(YAxis.AxisDependency.LEFT).setSpaceBottom(25f);

        int stop = marketItem.listHistory.size() - 1;
        int start = stop - 50;
        loadDataOntoChart(start, stop);
    }

}
