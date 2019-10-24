package com.troff.evemarketbrowser;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MyDOMArrayAdapter extends ArrayAdapter<MarketOrder> {
    private final Context context;
    private final List<MarketOrder> orders;
    private final OrderDirection buyOrSell;

    enum OrderDirection { BUY, SELL };

    public MyDOMArrayAdapter(Context context, List<MarketOrder> orders, OrderDirection buyOrSell) {
        super(context, R.layout.listview_domrow, orders);
        this.context = context;
        this.orders = orders;
        this.buyOrSell = buyOrSell;
    }

    // TODO viewholder pattern
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listview_domrow, parent, false);
        TextView textLeft = (TextView) rowView.findViewById(R.id.textLeft);
        TextView textRight = (TextView) rowView.findViewById(R.id.textRight);
        if (position >= orders.size()) {
            textLeft.setText("error");
            textRight.setText("error");
            Log.d("troff", "MyDomArrayAdapter - getView() position out of bounds)");
        }
        MarketOrder targetOrder = orders.get(position);
        textLeft.setText(NumberFormatter.formatISKValue(targetOrder.getPrice()));
        textRight.setText(Long.toString(targetOrder.getVol_remain()));


        if (buyOrSell == OrderDirection.BUY) {
            textLeft.setBackgroundColor(Color.BLUE);
            textRight.setBackgroundColor(Color.BLUE);
        } else if (buyOrSell == OrderDirection.SELL) {
            textLeft.setBackgroundColor(Color.RED);
            textRight.setBackgroundColor(Color.RED);
        }

        return rowView;
    }
}
