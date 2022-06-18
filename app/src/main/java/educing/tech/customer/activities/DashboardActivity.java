package educing.tech.customer.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import educing.tech.customer.R;
import educing.tech.customer.alert.CustomAlertDialog;
import educing.tech.customer.configuration.Configuration;
import educing.tech.customer.db.mysql.DeleteAccount;
import educing.tech.customer.db.mysql.ReceiveAdvertisement;
import educing.tech.customer.db.mysql.SaveDealLocation;
import educing.tech.customer.gps.GPSTracker;
import educing.tech.customer.helper.Blur;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.helper.OnAdvertisementLoad;
import educing.tech.customer.helper.OnLocationFound;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Advertisement;
import educing.tech.customer.model.Cart;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.model.Product;
import educing.tech.customer.model.Store;
import educing.tech.customer.model.User;
import educing.tech.customer.network.InternetConnectionDetector;
import educing.tech.customer.services.AlarmService;
import educing.tech.customer.session.SessionManager;
import educing.tech.customer.sqlite.SQLiteDatabaseHelper;

import static educing.tech.customer.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static educing.tech.customer.CommonUtilities.EXTRA_MESSAGE;
import static educing.tech.customer.configuration.Configuration.ADVERTISEMENT_URL;
import static educing.tech.customer.configuration.Configuration.PACKAGE_NAME;
import static educing.tech.customer.configuration.Configuration.SELLER_PACKAGE_NAME;
import static educing.tech.customer.model.Advertisement.advertisementList;

import static educing.tech.customer.sqlite.SQLiteDatabaseHelper.TABLE_ADVERTISEMENT;
import static educing.tech.customer.sqlite.SQLiteDatabaseHelper.TABLE_CHAT_MESSAGES;


public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnTaskCompleted, OnLocationFound, OnAdvertisementLoad, View.OnClickListener
{


    private static final int GPS_PERMISSION_REQUEST_CODE = 1;

    private ViewPager mViewPager, viewPager;
    private int currentPage;
    private SessionManager session; // Session Manager Class
    private SharedPreferences pref;
    private ProgressDialog pDialog;
    private User userObj;
    private Menu menu;
    private TextView nav_user_name, nav_mobile_number;
    private ImageView nav_profile_pic;

    private SQLiteDatabaseHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("txtBravo");

        helper = new SQLiteDatabaseHelper(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        nav_user_name = (TextView) header.findViewById(R.id.nav_user_name);
        nav_mobile_number = (TextView) header.findViewById(R.id.nav_mobile_number);
        nav_profile_pic = (ImageView) header.findViewById(R.id.nav_profile_pic);

        viewPager = (ViewPager) findViewById(R.id.pager);

        pDialog = new ProgressDialog(DashboardActivity.this);
        session = new SessionManager(DashboardActivity.this); // Session Manager
        pref = getSharedPreferences(Configuration.SHARED_PREF, Context.MODE_PRIVATE);


        if (!session.checkLogin())
        {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        this.userObj = getUserDetails();


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        // Fixes bug for disappearing fragment content
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        this.registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));


        Intent service = new Intent(getApplicationContext(), AlarmService.class);
        startService(service);

        if(permissionCheckerGPS())
        {
            gpsAlertDialog();
        }
    }


    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mHandleMessageReceiver);
    }


    private void gpsAlertDialog()
    {

        if(new InternetConnectionDetector(this).isConnected())
        {

            GPSTracker gps = new GPSTracker(this, DashboardActivity.this);

            if(!gps.canGetLocation())
            {
                showSettingsAlert();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        try
        {
            menu.clear();
        }

        catch (Exception e)
        {

        }

        finally
        {

            getMenuInflater().inflate(R.menu.menu_home, menu);

            this.menu = menu;

            MenuItem menuItemBidders = menu.findItem(R.id.action_advertisement);
            menuItemBidders.setIcon(buildCounterDrawable(helper.unreadMessageCount(TABLE_ADVERTISEMENT), R.drawable.ic_bell_white_24dp));

            MenuItem menuItemBidders1 = menu.findItem(R.id.action_message);
            menuItemBidders1.setIcon(buildCounterDrawable(helper.unreadMessageCount(TABLE_CHAT_MESSAGES), R.drawable.ic_email_outline_white_24dp));
        }

        return true;
    }


    @Override
    public void onClick(View view)
    {

        switch (view.getId())
        {

            case R.id.fragment_swipe:

                int position = 0;

                if(currentPage != 0)
                {
                    position = currentPage - 1;
                }

                int store_id = Advertisement.advertisementList.get(position).store_id;
                String store_name = Advertisement.advertisementList.get(position).store_name;
                Cart.selected_category = Advertisement.advertisementList.get(position).category_id;

                if(store_id != 0 || Cart.selected_category != 0)
                {
                    Intent intent = new Intent(DashboardActivity.this, ProductActivity.class);
                    intent.putExtra("STORE", new Store(store_id, store_name));
                    intent.putExtra("USER", new ChatMessage(String.valueOf(store_id), store_name));
                    startActivity(intent);
                }

                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case R.id.action_advertisement:

                displayView(8);
                break;

            case R.id.action_message:

                mViewPager.setCurrentItem(1);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed()
    {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }

        else
        {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.nav_orders:

                displayView(1);
                break;

            case R.id.nav_faq:

                displayView(2);
                break;

            case R.id.nav_about_us:

                displayView(3);
                break;

            case R.id.nav_share:

                displayView(4);
                break;

            case R.id.nav_rate:

                displayView(5);
                break;

            case R.id.nav_delete_account:

                displayView(6);
                break;

            case R.id.nav_edit_profile:

                displayView(7);
                break;

            case R.id.nav_deals:

                displayView(8);
                break;

            case R.id.nav_register:

                displayView(9);
                break;

            case R.id.nav_terms_and_conditions:

                displayView(10);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    private void setupViewPager(ViewPager viewPager)
    {

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        adapter.addFrag(new HomeFragment(Product.productCategoryList), "CATEGORIES");
        adapter.addFrag(new ChatFragment(), "CHAT");

        viewPager.setAdapter(adapter);
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();


        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }


        @Override
        public Fragment getItem(int position)
        {
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


    private void displayView(int position)
    {

        switch (position) {


            case 1:

                if (!session.checkLogin())
                {
                    startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                    finish();
                    return;
                }

                startActivity(new Intent(DashboardActivity.this, OrdersActivity.class));
                break;

            case 2:

                startActivity(new Intent(DashboardActivity.this, FAQActivity.class));
                break;

            case 3:

                startActivity(new Intent(DashboardActivity.this, AboutUsActivity.class));
                break;

            case 4:

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "I have been using txtBravo for some time and found it to be a handy app to find and connect with local businesses on the go via free chat. Download now https://play.google.com/store/apps/details?id=" + PACKAGE_NAME;
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "txtBravo Android App");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share Via"));
                break;

            case 5:

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PACKAGE_NAME)));
                break;

            case 6:

                new CustomAlertDialog(DashboardActivity.this, this).showConfirmationDialog("Deactivate", "Deactivate this Account ?", "delete");
                break;

            case 7:

                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                break;

            case 8:

                startActivity(new Intent(DashboardActivity.this, AdvertisementActivity.class));
                break;

            case 9:

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + SELLER_PACKAGE_NAME)));
                break;

            case 10:

                startActivity(new Intent(DashboardActivity.this, TermsAndConditionsActivity.class));
                break;
        }
    }


    @Override
    public void onResume()
    {

        super.onResume();

        try
        {

            User user = getUserDetails();

            nav_user_name.setText(Helper.toCamelCase(user.getUserName()));
            nav_mobile_number.setText(String.valueOf(user.getPhoneNo()));

            previewCapturedImage(pref.getString("profile_pic", ""));

            onCreateOptionsMenu(menu);
        }

        catch (Exception e)
        {

        }
    }


    private User getUserDetails()
    {

        User userObj = new User();

        if (session.checkLogin())
        {
            HashMap<String, String> user = session.getUserDetails();

            userObj.setUserID(Integer.valueOf(user.get(SessionManager.KEY_USER_ID)));
            userObj.setPhoneNo(user.get(SessionManager.KEY_PHONE));
            userObj.setUserName(user.get(SessionManager.KEY_USER_NAME));
        }

        return userObj;
    }


    private void initProgressDialog()
    {

        pDialog.setMessage("Deactivating Account ...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }


    @Override
    public void onAdvertisementLoad(boolean flag, int code, String message)
    {

        if(code == 200)
        {

            // Code for image swipe slider
            ImageFragmentPagerAdapter imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager());
            viewPager.setOffscreenPageLimit(5);
            viewPager.setAdapter(imageFragmentPagerAdapter);

            autoSlideImages();
        }
    }


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {

        if(flag && code == 199)
        {
            initProgressDialog();
            new DeleteAccount(DashboardActivity.this, this).deleteAccount(userObj.getUserID());
            return;
        }

        if(flag && code == 200)
        {
            new CustomAlertDialog(DashboardActivity.this, this).showOKDialog("Confirmation", "Account deleted successfully", "deleted");
            return;
        }

        if(flag && code == 201)
        {

            SessionManager session = new SessionManager(this); // Session Manager
            session.logoutUser();
            finish();
        }
    }


    private Drawable buildCounterDrawable(int count, int backgroundImageId)
    {

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.counter_menuitem_layout, null);
        view.setBackgroundResource(backgroundImageId);


        if (count == 0)
        {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
        }

        else if(count > 9)
        {
            count = 9;
        }


        TextView textView = (TextView) view.findViewById(R.id.count);
        textView.setText(String.valueOf(count));


        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());


        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);
    }


    public static class ImageFragmentPagerAdapter extends FragmentPagerAdapter {

        public ImageFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public int getCount() {
            return advertisementList.size();
        }


        @Override
        public Fragment getItem(int position) {
            return SwipeFragment.newInstance(position);
        }
    }


    public static class SwipeFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View swipeView = inflater.inflate(R.layout.swipe_fragment, container, false);
            final ImageView imageView = (ImageView) swipeView.findViewById(R.id.imageView);
            Bundle bundle = getArguments();

            final int position = bundle.getInt("position");
            final String imageFileName = advertisementList.get(position).file_name;

            Transformation blurTransformation = new Transformation() {

                @Override
                public Bitmap transform(Bitmap source)
                {
                    Bitmap blurred = Blur.fastblur(getActivity(), source, 10);
                    source.recycle();
                    return blurred;
                }

                @Override
                public String key()
                {
                    return "blur()";
                }
            };


            Picasso.with(getActivity())
                    .load(ADVERTISEMENT_URL + imageFileName) // thumbnail url goes here
                    .transform(blurTransformation)
                    .into(imageView, new Callback() {

                        @Override
                        public void onSuccess()
                        {

                            Picasso.with(getActivity())
                                    .load(ADVERTISEMENT_URL + imageFileName) // image url goes here
                                    .into(imageView);
                        }

                        @Override
                        public void onError()
                        {

                        }
                    });


            return swipeView;
        }


        static SwipeFragment newInstance(int position) {

            SwipeFragment swipeFragment = new SwipeFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            swipeFragment.setArguments(bundle);

            return swipeFragment;
        }
    }


    private void autoSlideImages() {

        final Handler handler = new Handler();

        final Runnable Update = new Runnable() {

            public void run() {

                if (currentPage == advertisementList.size()) {
                    currentPage = 0;
                }

                viewPager.setCurrentItem(currentPage++, true);
            }
        };

        Timer swipeTimer = new Timer();

        swipeTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                handler.post(Update);
            }
        }, 5000, 3000);
    }


    @Override
    public void onLocationFound(Location location)
    {

        if(new InternetConnectionDetector(this).isConnected())
        {
            new ReceiveAdvertisement(getApplicationContext(), this).execute(location.getLatitude(), location.getLongitude());
        }

        showConfirmationDialog("Deal Location", "Do you want to receive offer notifications from this location ?", location.getLatitude(), location.getLongitude());
    }


    public void showSettingsAlert()
    {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        // Setting Dialog Title
        alertDialog.setTitle("GPS Settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is required to display offers near to you");
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
                    gpsAlertDialog();
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


    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {

            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);

            Log.v("gcm_message: ", "" + newMessage);

            if(newMessage == null)
            {
                return;
            }

            try
            {

                JSONObject jsonObj = new JSONObject(newMessage);


                if(jsonObj.getString("message_type").equalsIgnoreCase("advertisement"))
                {
                    onCreateOptionsMenu(menu);
                }

                if(jsonObj.getString("message_type").equalsIgnoreCase("chat_message"))
                {
                    onCreateOptionsMenu(menu);
                }


                // Waking up mobile if it is sleeping
                // WakeLocker.acquire(context);

                // Releasing wake lock
                // WakeLocker.release();
            }

            catch (Exception e)
            {

            }
        }
    };


    // Alert dialog for save button
    public void showConfirmationDialog(String title, String message, final double latitude, final double longitude)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setMessage(message).setTitle(title).setCancelable(false) // set dialog message

                // Yes button click action
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if(new InternetConnectionDetector(getApplicationContext()).isConnected())
                        {
                            new SaveDealLocation(getApplicationContext()).save(userObj.getUserID(), latitude, longitude);
                        }
                    }
                })


                // No button click action
                .setNegativeButton("NO THANKS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });


        AlertDialog alertDialog = alertDialogBuilder.create(); // create alert dialog

        alertDialog.show(); // show it
    }


    private void previewCapturedImage(final String path)
    {

        try
        {

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 4;

            final Bitmap bitmap = BitmapFactory.decodeFile(path, options);

            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 128, 128, false);

            nav_profile_pic.setImageDrawable(new RoundImage(resized));
        }

        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }


    public static class RoundImage extends Drawable
    {

        private final Bitmap mBitmap;
        private final Paint mPaint;
        private final RectF mRectF;

        private final int mBitmapWidth;
        private final int mBitmapHeight;


        public RoundImage(Bitmap bitmap)
        {

            mBitmap = bitmap;

            mRectF = new RectF();
            mPaint = new Paint();

            mPaint.setAntiAlias(true);
            mPaint.setDither(true);

            final BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            mPaint.setShader(shader);

            mBitmapWidth = mBitmap.getWidth();
            mBitmapHeight = mBitmap.getHeight();
        }


        @Override
        public void draw(Canvas canvas)
        {
            canvas.drawOval(mRectF, mPaint);
        }


        @Override
        protected void onBoundsChange(Rect bounds)
        {

            super.onBoundsChange(bounds);
            mRectF.set(bounds);
        }


        @Override
        public void setAlpha(int alpha)
        {

            if (mPaint.getAlpha() != alpha)
            {

                mPaint.setAlpha(alpha);
                invalidateSelf();
            }
        }


        @Override
        public void setColorFilter(ColorFilter cf)
        {
            mPaint.setColorFilter(cf);
        }


        @Override
        public int getOpacity()
        {
            return PixelFormat.TRANSLUCENT;
        }


        @Override
        public int getIntrinsicWidth()
        {
            return mBitmapWidth;
        }


        @Override
        public int getIntrinsicHeight()
        {
            return mBitmapHeight;
        }


        /*public void setAntiAlias(boolean aa)
        {
            mPaint.setAntiAlias(aa);
            invalidateSelf();
        }*/


        @Override
        public void setFilterBitmap(boolean filter)
        {
            mPaint.setFilterBitmap(filter);
            invalidateSelf();
        }


        @Override
        public void setDither(boolean dither)
        {
            mPaint.setDither(dither);
            invalidateSelf();
        }


        public Bitmap getBitmap()
        {
            return mBitmap;
        }
    }
}