package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import model.Parts;

public class InventoryAdapter extends BaseAdapter {
    private final Context context;
    private final List<Parts> partsList;

    public InventoryAdapter(Context context, List<Parts> partsList) {
        this.context = context;
        this.partsList = partsList;
    }

    @Override
    public int getCount() {
        return partsList != null ? partsList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return partsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        Parts part = partsList.get(position);
        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        text1.setText(part.getName());
        text2.setText("VIN: " + part.getAssociatedVIN());
        text1.setTextColor(context.getResources().getColor(android.R.color.white));
        text2.setTextColor(context.getResources().getColor(android.R.color.secondary_text_dark));

        return convertView;
    }
}
