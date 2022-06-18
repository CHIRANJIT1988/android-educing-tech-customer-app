package educing.tech.customer.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import educing.tech.customer.R;


public class MainActivity extends AppCompatActivity
{

    private static final int READ_SMS_PERMISSION_REQUEST_CODE = 1;

    private int back_pressed = 0;
    public static MainActivity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //final Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        //setSupportActionBar(toolbar);

        //assert getSupportActionBar() != null;
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        activity = MainActivity.this;

        final ViewPager viewPager = (ViewPager) findViewById(R.id.tabanim_viewpager);
        // Fixes bug for disappearing fragment content
        viewPager.setOffscreenPageLimit(1);
        setupViewPager(viewPager);

        permissionCheckerReadSMS();
    }


    private void setupViewPager(ViewPager viewPager)
    {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFrag(new RegisterFragment(), "Sign up");

        viewPager.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case android.R.id.home:
            {
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    static class ViewPagerAdapter extends FragmentPagerAdapter
    {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager)
        {
            super(manager);
        }


        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }


        @Override
        public int getCount()
        {
            return mFragmentList.size();
        }


        public void addFrag(Fragment fragment, String title)
        {

            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position)
        {
            return mFragmentTitleList.get(position);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);
        LoginFragment.callbackManager.onActivityResult(requestCode, resultCode, data);

        /*switch (requestCode)
        {

            case RegisterFragment.RC_SIGN_IN:

                if (resultCode == RESULT_OK)
                {
                    RegisterFragment.signedInUser = false;
                }

                RegisterFragment.mIntentInProgress = false;

                if (!RegisterFragment.mGoogleApiClient.isConnecting())
                {
                    RegisterFragment.mGoogleApiClient.connect();
                }

                break;
        }*/
    }


    @Override
    public void onBackPressed()
    {

        if(back_pressed == 0)
        {
            back_pressed++;
            Toast.makeText(getApplicationContext(), "Press Back Button again to Exit", Toast.LENGTH_LONG).show();
        }

        else
        {
            finish();
        }
    }


    private boolean permissionCheckerReadSMS() {

        if (!checkPermissionReadSMS()) {
            requestPermissionReadSMS();
            return false;
        }

        return true;
    }


    private boolean checkPermissionReadSMS()
    {

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }

        else
        {
            return false;
        }
    }


    private void requestPermissionReadSMS(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS))
        {
            makeToast("SMS permission allows us to read or receive SMS. Please allow in App Settings for read or receive SMS.");
        }

        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, READ_SMS_PERMISSION_REQUEST_CODE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {

        switch (requestCode)
        {

            case READ_SMS_PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeToast("Permission Granted");
                } else {
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