package com.troff.evemarketbrowser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MarketOverviewListAdapter extends ArrayAdapter<String[]> {
    private final Context context;
    // 0 = itemName   1 = spread    2 = avg total
    private final List<String[]> stringValues;
    private final List<Long> typeIDs;

    public MarketOverviewListAdapter(Context context, List<String[]> stringValues, List<Long> typeIDs) {
        super(context, R.layout.listview_overview_row, stringValues);
        this.context = context;
        this.stringValues = stringValues;
        this.typeIDs = typeIDs;
    }

    public Long getTypeAtPosition(int position) {
        return typeIDs.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listview_overview_row, parent, false);
        TextView textName = (TextView) rowView.findViewById(R.id.textName);
        TextView textValue1 = (TextView) rowView.findViewById(R.id.textSpread);
        TextView textValue2 = (TextView) rowView.findViewById(R.id.textVolume);

        textName.setText(stringValues.get(position)[0]);

        textValue1.setText(stringValues.get(position)[1]);
        textValue2.setText(stringValues.get(position)[2]);

        return rowView;
    }


}
