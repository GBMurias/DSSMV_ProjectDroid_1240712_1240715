package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import model.Vehicle;

public class HistoryAdapter extends BaseAdapter {
    private final Context context;
    private final List<Vehicle> vehicleList;

    public HistoryAdapter(Context context, List<Vehicle> vehicleList) {
        this.context = context;
        this.vehicleList = vehicleList;
    }

    @Override
    public int getCount() {
        return vehicleList != null ? vehicleList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return vehicleList.get(position);
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

        Vehicle vehicle = vehicleList.get(position);
        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        text1.setText(vehicle.getModel());
        text2.setText("VIN: " + vehicle.getVin());
        text1.setTextColor(context.getResources().getColor(android.R.color.white));
        text2.setTextColor(context.getResources().getColor(android.R.color.secondary_text_dark));

        return convertView;
    }
}
