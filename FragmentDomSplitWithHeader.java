package com.troff.evemarketbrowser;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.List;

public class FragmentDomSplitWithHeader extends Fragment {
    protected View mView;
    private MarketContainer marketItem;
    private Long myTypeID;

    public void setTypeID(Long typeID) {
        this.myTypeID = typeID;
    }

    public FragmentDomSplitWithHeader() {

    }

    public static FragmentDomSplitWithHeader newInstance(Long typeID) {
        FragmentDomSplitWithHeader f = new FragmentDomSplitWithHeader();
        f.setTypeID(typeID);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_domsplit_withheader,
                container, false);
        return mView;
    }

    // The main view that calls this function should not call getXXXOrdersFivePercent or
    // any other expensive functions. The fragment will call them if needed.
    public void loadDomOntoFragment() {
        MyServiceDataSingleton data = MyServiceDataSingleton.getInstance(getActivity());
        this.marketItem = data.getMarketItem(this.myTypeID);
        this.updateControl();
    }


    public void updateControl() {
        if (this.marketItem == null) {
            Toast.makeText(getActivity(),"DomFragment - Null marketItem on update", Toast.LENGTH_SHORT).show();
            return;
        }






        ListView listViewSell = (ListView) mView.findViewById(R.id.listViewSell);
        final MyDOMArrayAdapter sellAdapter = new MyDOMArrayAdapter(getActivity(), marketItem.listSell, MyDOMArrayAdapter.OrderDirection.SELL);
        if (listViewSell != null) listViewSell.setAdapter(sellAdapter);

        ListView listViewBuy = (ListView) mView.findViewById(R.id.listViewBuy);
        final MyDOMArrayAdapter buyAdapter = new MyDOMArrayAdapter(getActivity(), marketItem.listBuy, MyDOMArrayAdapter.OrderDirection.BUY);
        if (listViewBuy != null) listViewBuy.setAdapter(buyAdapter);



        // Set the text filds
        TextView textName = (TextView) mView.findViewById(R.id.textItemName);
        TextView textBuyVolume = (TextView) mView.findViewById(R.id.textBuyVolume);
        TextView textBuyAvgPrice = (TextView) mView.findViewById(R.id.textBuyAvgPrice);
        TextView textSellVolume = (TextView) mView.findViewById(R.id.textSellVolume);
        TextView textSellAvgPrice = (TextView) mView.findViewById(R.id.textSellAvgPrice);
        TextView textMarketSpreadPrice = (TextView) mView.findViewById(R.id.textMarketSpreadPrice);
        TextView textAverageSpreadPrice = (TextView) mView.findViewById(R.id.textAverageSpreadPrice);



        BigDecimal avgSpreadPrice = marketItem.getSellPriceFivePercent().subtract(marketItem.getBuyPriceFivePercent());


        textName.setText(marketItem.itemName);
        textBuyVolume.setText(Long.toString(marketItem.getBuyVolumeFivePercent()));
        textBuyAvgPrice.setText(NumberFormatter.formatISKValue(marketItem.getBuyPriceFivePercent()));
        textSellVolume.setText(Long.toString(marketItem.getSellVolumeFivePercent()));
        textSellAvgPrice.setText(NumberFormatter.formatISKValue(marketItem.getSellPriceFivePercent()));
        textMarketSpreadPrice.setText(NumberFormatter.formatISKValue(marketItem.getMarketSpread()));
        textAverageSpreadPrice.setText(NumberFormatter.formatISKValue(avgSpreadPrice));

    }
}
