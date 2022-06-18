package educing.tech.customer.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import educing.tech.customer.R;
import educing.tech.customer.adapter.StoresRecyclerAdapter;
import educing.tech.customer.alert.CustomAlertDialog;
import educing.tech.customer.db.mysql.FindNearestStore;
import educing.tech.customer.gps.GPSTracker;
import educing.tech.customer.helper.OnLocationFound;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Cart;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.model.Store;
import educing.tech.customer.network.InternetConnectionDetector;

import static educing.tech.customer.configuration.Configuration.PLACES_API_BASE;
import static educing.tech.customer.configuration.Configuration.TYPE_AUTOCOMPLETE;
import static educing.tech.customer.configuration.Configuration.OUT_JSON;
import static educing.tech.customer.configuration.Configuration.API_KEY;


public class StoresActivity extends AppCompatActivity implements OnTaskCompleted, OnLocationFound, View.OnClickListener, AdapterView.OnItemClickListener
{

    private static final int GPS_PERMISSION_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private StoresRecyclerAdapter simpleRecyclerAdapter;
    private AutoCompleteTextView edit_search;
    private ProgressBar progressBar;

    public static Activity store_activity;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        ImageButton ib_back = (ImageButton) findViewById(R.id.ib_back);
        progressBar = (ProgressBar) findViewById(R.id.pbLoading);
        edit_search = (AutoCompleteTextView) findViewById(R.id.edit_search);

        store_activity = StoresActivity.this;


        edit_search.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item_auto_complete));
        edit_search.setOnItemClickListener(this);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Stores");

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/


        ib_back.setOnClickListener(this);

        if(permissionCheckerGPS())
        {
            displayStoreList();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {

        String str = (String) adapterView.getItemAtPosition(position);

        if(new InternetConnectionDetector(this).isConnected())
        {

            progressBar.setVisibility(View.GONE);

            Intent intent = new Intent(StoresActivity.this, SearchStoreActivity.class);
            intent.putExtra("PLACE", str);
            startActivityForResult(intent, 1);
        }

        else
        {
            Toast.makeText(getApplicationContext(), "Internet Connection Failure", Toast.LENGTH_LONG).show();
        }
    }


    private void displayStoreList()
    {

        Store.storeList.clear();

        if(new InternetConnectionDetector(this).isConnected())
        {

            GPSTracker gps = new GPSTracker(this, StoresActivity.this);

            if(!gps.canGetLocation())
            {
                showSettingsAlert();
            }
        }

        else
        {
            onTaskCompleted(false, 500, "Internet Connection Failure. Try Again");
        }


        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        simpleRecyclerAdapter = new StoresRecyclerAdapter(this, Store.storeList);
        recyclerView.setAdapter(simpleRecyclerAdapter);


        simpleRecyclerAdapter.SetOnItemClickListener(new StoresRecyclerAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                Intent intent = new Intent(StoresActivity.this, ProductActivity.class);
                intent.putExtra("STORE", Store.storeList.get(position));
                intent.putExtra("USER", new ChatMessage(String.valueOf(Store.storeList.get(position).getId()), Store.storeList.get(position).getName()));
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {

            case android.R.id.home:

                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {

        try
        {

            progressBar.setVisibility(View.GONE);

            if (flag && code == 199)
            {
                new CustomAlertDialog(StoresActivity.this, this).showOKDialog("Not Found", message, "finish");
                return;
            }

            if (flag && code == 200) {
                simpleRecyclerAdapter.notifyDataSetChanged();
                return;
            }

            if (flag && code == 201)
            {
                return;
            }

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        catch (Exception e)
        {

        }
    }


    @Override
    public void onLocationFound(Location location)
    {

        if(new InternetConnectionDetector(this).isConnected())
        {
            new FindNearestStore(getApplicationContext(), this).find(Cart.selected_category, location.getLatitude(), location.getLongitude());
        }

        else
        {
            onTaskCompleted(false, 500, "Internet Connection Failure. Try Again");
        }
    }


    @Override
    public void onClick(View view)
    {

        switch (view.getId())
        {

            case R.id.ib_back:

                finish();
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        try
        {

            if (requestCode == 1 && resultCode == 1)
            {

                double latitude = data.getDoubleExtra("LATITUDE", 0);
                double longitude = data.getDoubleExtra("LONGITUDE", 0);

                if(new InternetConnectionDetector(this).isConnected())
                {
                    progressBar.setVisibility(View.VISIBLE);
                    new FindNearestStore(getApplicationContext(), this).find(Cart.selected_category, latitude, longitude);
                }

                Log.v("location ", "lat: " + latitude + ", long: " + longitude);
            }
        }

        catch (Exception e)
        {

        }
    }




    public static ArrayList<String> autocomplete(String input)
    {

        ArrayList<String> resultList = new ArrayList<>();

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try
        {

            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:in");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());

            System.out.println("URL: "+url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];

            while ((read = in.read(buff)) != -1)
            {
                jsonResults.append(buff, 0, read);
            }
        }

        catch (MalformedURLException e)
        {
            Log.e("Google Place Error: ", "Error processing Places API URL", e);
            return resultList;
        }

        catch (IOException e)
        {
            Log.e("Google Place Error: ", "Error connecting to Places API", e);
            return resultList;
        }

        finally
        {

            if (conn != null)
            {
                conn.disconnect();
            }
        }

        try
        {

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<>(predsJsonArray.length());

            for (int i = 0; i < predsJsonArray.length(); i++)
            {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        }

        catch (JSONException e)
        {
            Log.e("Google Place Error: ", "Cannot process JSON results", e);
        }

        return resultList;
    }


    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable
    {

        private ArrayList<String> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId)
        {
            super(context, textViewResourceId);
        }


        @Override
        public int getCount() {
            return resultList.size();
        }


        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }


        @Override
        public Filter getFilter()
        {

            Filter filter = new Filter()
            {

                @Override
                protected FilterResults performFiltering(CharSequence constraint)
                {

                    FilterResults filterResults = new FilterResults();

                    if (constraint != null)
                    {

                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }

                    return filterResults;
                }


                @Override
                protected void publishResults(CharSequence constraint, FilterResults results)
                {

                    if (results != null && results.count > 0)
                    {
                        notifyDataSetChanged();
                    }

                    else
                    {
                        notifyDataSetInvalidated();
                    }
                }
            };

            return filter;
        }
    }


    public void showSettingsAlert()
    {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        // Setting Dialog Title
        alertDialog.setTitle("GPS Settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is required to find nearest stores");
        // Setting Cancelable
        alertDialog.setCancelable(false);

        // On pressing Settings button
        alertDialog.setPositiveButton("Turn On GPS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                finish();
            }
        });


        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    private boolean checkPermissionGPS()
    {

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }

        else
        {
            return false;
        }
    }


    private boolean permissionCheckerGPS()
    {

        if (!checkPermissionGPS())
        {
            requestPermissionGPS();
            return false;
        }

        return true;
    }


    private void requestPermissionGPS(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            makeToast("GPS permission allows us to access location data. Please allow in App Settings for location.");
        }

        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {

        switch (requestCode)
        {

            case GPS_PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    displayStoreList();
                    makeToast("Permission Granted");
                }

                else
                {
                    makeToast("Permission Denied");
                }

                break;
        }
    }


    private void makeToast(String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}