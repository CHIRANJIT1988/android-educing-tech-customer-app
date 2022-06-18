package educing.tech.customer.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import educing.tech.customer.R;
import educing.tech.customer.gps.GData;
import educing.tech.customer.helper.Helper;


public class SearchStoreActivity extends FragmentActivity implements View.OnClickListener
{

    private GoogleMap mMap;

    private TextView markerText;

    private LatLng center;
    private LinearLayout markerLayout;
    private List<Address> addresses;

    private TextView address;
    private ProgressBar progressBar;
    private Button btnDone;

    private double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_store);


        markerText = (TextView) findViewById(R.id.locationMarkertext);
        address = (TextView) findViewById(R.id.address);
        markerLayout = (LinearLayout) findViewById(R.id.locationMarker);
        progressBar = (ProgressBar) findViewById(R.id.pbLoading);
        btnDone = (Button) findViewById(R.id.btnDone);

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable())
        {
            finish();
        }

        else
        {
            String location = getIntent().getStringExtra("PLACE");
            new SearchLocationAsync(location).execute();
        }
    }


    private void setUpMapIfNeeded(LatLng p1)
    {

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null)
        {

            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setMyLocationEnabled(true);

            // Check if we were successful in obtaining the map.
            if (mMap != null)
            {
                setUpMap(p1.latitude, p1.longitude);
            }
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }


    private void setUpMap(double latitude, double longitude)
    {

        LatLng position = new LatLng(latitude, longitude);

        // Instantiating MarkerOptions class
        MarkerOptions options = new MarkerOptions();

        // Setting position for the MarkerOptions
        options.position(position);


        // Check if we were successful in obtaining the map.
        if (mMap != null)
        {

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position).zoom(15f).tilt(50).build();

            mMap.setMyLocationEnabled(true);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            // Clears all the existing markers
            mMap.clear();


            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition arg0)
                {

                    center = mMap.getCameraPosition().target;

                    markerText.setText(String.valueOf(" My Location "));
                    mMap.clear();
                    markerLayout.setVisibility(View.VISIBLE);

                    try
                    {

                        new GetLocationAsync(center.latitude, center.longitude).execute();

                    }

                    catch (Exception e)
                    {

                    }
                }
            });

            markerLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v)
                {

                    try
                    {

                        LatLng latLng1 = new LatLng(center.latitude, center.longitude);

                        Marker m = mMap.addMarker(new MarkerOptions()
                                .position(latLng1)
                                .title(" My Location ")
                                .snippet("")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.add_marker)));
                        m.setDraggable(true);

                        markerLayout.setVisibility(View.GONE);
                    }

                    catch (Exception e)
                    {

                    }

                }
            });

            mMap.setTrafficEnabled(false);
        }

        else
        {
            Toast.makeText(getApplicationContext(), "Unable to create Maps", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onClick(View view)
    {

        switch (view.getId())
        {

            case R.id.btnDone:

                Intent intent = new Intent();
                intent.putExtra("LATITUDE", this.latitude);
                intent.putExtra("LONGITUDE", this.longitude);
                setResult(1, intent);

                finish();
        }
    }


    private boolean isGooglePlayServicesAvailable()
    {

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == status)
        {
            return true;
        }

        else
        {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }


    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

            Log.v("location ", "lat: " + this.latitude + ", long: " + this.longitude);

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    private class SearchLocationAsync extends AsyncTask<String, Void, String>
    {

        private String location;
        private LatLng p1;

        public SearchLocationAsync(String location)
        {
            this.location = location;
        }


        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
            address.setText(String.valueOf("Searching Location ..."));
        }

        @Override
        protected String doInBackground(String... params)
        {

            // Getting reference to the SupportMapFragment
            // Create a new global location parameters object
            LocationRequest mLocationRequest = LocationRequest.create();

            //Set the update interval
            mLocationRequest.setInterval(GData.UPDATE_INTERVAL_IN_MILLISECONDS);

            // Use high accuracy
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Set the interval ceiling to one minute
            mLocationRequest.setFastestInterval(GData.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

            // Note that location updates are off until the user turns them on
            // boolean mUpdatesRequested = false;

            p1 = getLocationFromAddress(getApplicationContext(), location);
            return null;
        }


        @Override
        protected void onPostExecute(String result)
        {

            try
            {
                setUpMapIfNeeded(p1);
                btnDone.setVisibility(View.VISIBLE);
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    private class GetLocationAsync extends AsyncTask<String, Void, String>
    {

        double x, y;
        StringBuilder str;

        public GetLocationAsync(double latitude, double longitude)
        {
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
            address.setText(String.valueOf("Fetching Address ..."));
        }

        @Override
        protected String doInBackground(String... params)
        {

            try
            {

                Geocoder geocoder = new Geocoder(SearchStoreActivity.this, Locale.ENGLISH);
                addresses = geocoder.getFromLocation(x, y, 1);
                str = new StringBuilder();

                if (Geocoder.isPresent())
                {

                    Address returnAddress = addresses.get(0);

                    String localityString = returnAddress.getLocality();
                    String city = returnAddress.getCountryName();
                    String region_code = returnAddress.getCountryCode();
                    String zipcode = returnAddress.getPostalCode();

                    str.append(localityString + " ");
                    str.append(city + " " + region_code + " ");
                    str.append(zipcode + " ");

                }
            }

            catch (IOException e)
            {
                Log.e("tag", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {

            try
            {
                address.setText(String.valueOf(Helper.toCamelCase(addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1) + " ")));
                progressBar.setVisibility(View.GONE);
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        @Override
        protected void onProgressUpdate(Void... values)
        {

        }
    }
}