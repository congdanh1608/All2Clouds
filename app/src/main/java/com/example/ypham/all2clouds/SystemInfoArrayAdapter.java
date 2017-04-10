package com.example.ypham.all2clouds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Y Pham on 12/6/2014.
 */
public class SystemInfoArrayAdapter extends ArrayAdapter<SystemInfo> {

    private final Context context;
    private final SystemInfo[] listConfig;

    public SystemInfoArrayAdapter(Context mContext, SystemInfo[] listValue) {
        super(mContext, R.layout.item_row_sys_info, listValue);
        this.context = mContext;
        this.listConfig = listValue;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_row_sys_info, parent, false);
        TextView sysConfigTextView = (TextView) rowView.findViewById(R.id.tv_systemConfig);
        TextView configValueTextView = (TextView) rowView.findViewById(R.id.tv_configValue);
        SystemInfo systemInfo = listConfig[position];
        sysConfigTextView.setText(systemInfo.getSystemConfig());
        configValueTextView.setText(systemInfo.getConfigValue());

        return rowView;
    }
}
