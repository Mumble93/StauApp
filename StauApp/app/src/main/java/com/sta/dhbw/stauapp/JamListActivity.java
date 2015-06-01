package com.sta.dhbw.stauapp;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sta.dhbw.jambeaconrestclient.ITrafficJamCallback;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.jambeaconrestclient.TrafficJam;
import com.sta.dhbw.stauapp.settings.SettingsActivity;

import java.util.List;

public class JamListActivity extends ListActivity implements ITrafficJamCallback
{
    private JamBeaconRestClient restClient = MainActivity.restClient;
    private static List<TrafficJam> trafficJams;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jam_list);

        restClient.getTrafficJamList(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    private class JamListAdapter extends ArrayAdapter<TrafficJam>
    {
        private final Context context;
        private final List<TrafficJam> trafficJamList;

        public JamListAdapter(Context context, int layoutId, List<TrafficJam> trafficJamList)
        {
            super(context, layoutId, trafficJamList);
            this.context = context;
            this.trafficJamList = trafficJamList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.activity_jam_list, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
            textView.setText(trafficJamList.get(position).getId().toString());
            return rowView;
        }
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
                restClient.getTrafficJamList(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onGetTrafficJamComplete(TrafficJam trafficJam)
    {

    }

    @Override
    public void onGetJamListComplete(List<TrafficJam> trafficJamList)
    {
        trafficJams = trafficJamList;

        final JamListAdapter adapter = new JamListAdapter(this, R.layout.jam_list_layout, trafficJamList);
        getListView().setAdapter(adapter);

    }

    @Override
    public void onTrafficJamUpdateComplete(TrafficJam updatedJam)
    {

    }
}
