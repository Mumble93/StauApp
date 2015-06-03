package com.sta.dhbw.stauapp;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.sta.dhbw.jambeaconrestclient.ITrafficJamCallback;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.jambeaconrestclient.TrafficJam;
import com.sta.dhbw.stauapp.settings.SettingsActivity;
import com.sta.dhbw.stauapp.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class JamListActivity extends ListActivity implements ITrafficJamCallback
{
    private static final String TAG = JamListActivity.class.getSimpleName();

    private JamBeaconRestClient restClient = MainActivity.restClient;

    private ListView listView;
    private ArrayAdapter<TrafficJam> arrayAdapter;
    public static List<TrafficJam> transportList;

    private ProgressDialog dialog;

    private static class RowViewHolder{
        TextView jamIdText;
        TextView jamTimeText;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Aktualisieren...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

        listView = getListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, "Item " + position + " clicked");
                TrafficJam jam = (TrafficJam) listView.getItemAtPosition(position);
                LatLng latLng = new LatLng(jam.getLocation().getLatitude(), jam.getLocation().getLongitude());
                Intent intent = new Intent(view.getContext(), JamMapActivity.class);
                intent.putExtra("location", latLng);
                startActivity(intent);
            }
        });

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        dialog.show();
        restClient.getTrafficJamList(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        dialog.show();
        restClient.getTrafficJamList(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_jam_list_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_refresh_list:
                dialog.show();
                restClient.getTrafficJamList(this);
                return true;
            case R.id.start_jam_map:
                Intent jamMapIntent = new Intent(this, JamMapActivity.class);
                startActivity(jamMapIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onGetTrafficJamComplete(TrafficJam trafficJam)
    {

    }

    @Override
    public void onTrafficJamPostComplete(TrafficJam jam)
    {

    }

    @Override
    public void onGetJamListComplete(List<TrafficJam> trafficJamList)
    {
        if (arrayAdapter == null)
        {
            arrayAdapter = new JamListAdapter(this, R.layout.row_layout, trafficJamList);
            setListAdapter(arrayAdapter);
        } else
        {
            arrayAdapter.clear();
            arrayAdapter.addAll(trafficJamList);
            arrayAdapter.notifyDataSetChanged();
        }

        transportList = trafficJamList;

        if (dialog.isShowing())
        {
            dialog.dismiss();
        }
    }

    @Override
    public void onTrafficJamUpdateComplete(TrafficJam updatedJam)
    {

    }

    public static List<TrafficJam> getTrafficJams()
    {
        return transportList;
    }

    protected static class JamListAdapter extends ArrayAdapter<TrafficJam>
    {
        private Context context;
        private List<TrafficJam> trafficJamList;

        public JamListAdapter(Context context, int layoutId, List<TrafficJam> trafficJamList)
        {
            super(context, layoutId, trafficJamList);
            this.context = context;
            this.trafficJamList = trafficJamList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            RowViewHolder rowViewHolder;

            if(convertView == null)
            {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.row_layout, parent, false);

                rowViewHolder = new RowViewHolder();
                rowViewHolder.jamIdText = (TextView) convertView.findViewById(R.id.jam_id);
                rowViewHolder.jamTimeText = (TextView) convertView.findViewById(R.id.jam_time);

                convertView.setTag(rowViewHolder);
            } else
            {
                rowViewHolder = (RowViewHolder) convertView.getTag();
            }

            rowViewHolder.jamIdText.setText(trafficJamList.get(position).getId().toString());
            rowViewHolder.jamTimeText.setText(Utils.timstampToString(trafficJamList.get(position).getTimestamp()));
            return convertView;
        }
    }
}
