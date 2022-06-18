package educing.tech.customer.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.DecimalFormat;

import educing.tech.customer.R;
import educing.tech.customer.helper.Blur;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.model.Store;

import static educing.tech.customer.configuration.Configuration.PROFILE_IMAGE_URL;


public class StoreProfileActivity extends AppCompatActivity
{

    private RatingBar ratingBar;
    private TextView tvStoreName, tvAddress, tvDistance, tvDeliveryDetails, tvOnlineStatus;
    private ImageView image;

    private Store store;
    private DecimalFormat df = new DecimalFormat("0.00");

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("");


        this.store = (Store) getIntent().getSerializableExtra("STORE");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(StoreProfileActivity.this, ChatWindowActivity.class);
                intent.putExtra("USER", new ChatMessage(String.valueOf(store.id), store.name));
                startActivity(intent);
            }
        });


        findViewById();
        display();
    }


    private void findViewById()
    {

        image = (ImageView) findViewById(R.id.header);
        tvStoreName = (TextView) findViewById(R.id.store_name);
        tvAddress = (TextView) findViewById(R.id.address);
        ratingBar = (RatingBar) findViewById(R.id.rating);

        tvDeliveryDetails = (TextView) findViewById(R.id.delivery_details);
        tvDistance = (TextView) findViewById(R.id.distance);
        tvOnlineStatus = (TextView) findViewById(R.id.online);
    }


    private void display()
    {

        StringBuilder address = new StringBuilder().append(store.address).append(", ").append(store.city).append(", ")
                .append(store.state).append(", ").append(store.country).append(" - ").append(store.pincode);

        tvStoreName.setText(store.name.toUpperCase());
        tvAddress.setText(address.toString().toUpperCase());
        ratingBar.setRating(store.rating);

        tvDistance.setText(String.valueOf(store.distance + " km from current or selected location"));

        if(store.delivery_status == 2)
        {
            tvDeliveryDetails.setText(String.valueOf("BOOKING AVAILABLE VIA CHAT"));
        }

        else if(store.delivery_status == 1)
        {

            if(store.delivery_charge == 0 && store.amount == 0)
            {
                tvDeliveryDetails.setText(String.valueOf("FREE HOME DELIVERY"));
            }

            else
            {
                tvDeliveryDetails.setText(String.valueOf("FREE DELIVERY ON PURCHASE Rs. " + df.format(store.amount)));
            }
        }

        else
        {
            tvDeliveryDetails.setText(String.valueOf("HOME DELIVERY NOT AVAILABLE"));
        }


        if(store.is_online == 0)
        {
            tvOnlineStatus.setTextColor(ContextCompat.getColor(this, R.color.myTextSecondaryColor));
            tvOnlineStatus.setText(String.valueOf("OFFLINE"));
        }

        else
        {
            tvOnlineStatus.setTextColor(ContextCompat.getColor(this, R.color.green));
            tvOnlineStatus.setText(String.valueOf("ONLINE"));
        }

        setUpMap();

        Transformation blurTransformation = new Transformation() {

            @Override
            public Bitmap transform(Bitmap source)
            {
                Bitmap blurred = Blur.fastblur(StoreProfileActivity.this, source, 10);
                source.recycle();
                return blurred;
            }

            @Override
            public String key()
            {
                return "blur()";
            }
        };


        Picasso.with(StoreProfileActivity.this)
            .load(PROFILE_IMAGE_URL + "STORE_" + store.phone_no + ".jpg") // thumbnail url goes here
            .transform(blurTransformation)
            .into(image, new Callback() {

                @Override
                public void onSuccess()
                {

                    Picasso.with(StoreProfileActivity.this)
                            .load(PROFILE_IMAGE_URL + "STORE_" + store.phone_no + ".jpg") // image url goes here
                            .into(image);
                }

                @Override
                public void onError()
                {
                    Toast.makeText(getApplicationContext(), "Failed to load Image", Toast.LENGTH_LONG).show();
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


    private void setUpMap()
    {


        try
        {

            //show error dialog if GoolglePlayServices not available
            if (!isGooglePlayServicesAvailable())
            {
                finish();
            }


            LatLng position = new LatLng(store.latitude, store.longitude);

            // Instantiating MarkerOptions class
            MarkerOptions options = new MarkerOptions();

            // Setting position for the MarkerOptions
            options.position(position);


            // Do a null check to confirm that we have not already instantiated the map.
            if (mMap == null)
            {

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(position).zoom(15f).tilt(50).build();


                // Try to obtain the map from the SupportMapFragment.
                SupportMapFragment supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap));
                //(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);

                mMap = supportMapFragment.getMap();
                mMap.setMyLocationEnabled(false);

                // LatLng position = new LatLng(store.latitude, store.longitude);

                // Instantiating MarkerOptions class
                // MarkerOptions options = new MarkerOptions();

                // Setting position for the MarkerOptions
                // options.position(position);


                // Check if we were successful in obtaining the map.
                if (mMap != null)
                {

                    mMap.addMarker(options);
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                        @Override
                        public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {

                            marker.showInfoWindow(); // show marker info window on marker click
                            return true;
                        }
                    });


                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                        @Override
                        public void onMapClick(LatLng point)
                        {

                        }
                    });
                }

                else
                {
                    Toast.makeText(getApplicationContext(), "Unable to create Maps", Toast.LENGTH_SHORT).show();
                }
            }
        }

        catch (Exception e)
        {

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
}
