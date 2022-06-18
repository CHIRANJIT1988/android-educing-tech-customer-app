package educing.tech.customer.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

import educing.tech.customer.R;
import educing.tech.customer.alert.CustomAlertDialog;
import educing.tech.customer.configuration.Configuration;
import educing.tech.customer.db.mysql.ReceiveProductSubCategories;
import educing.tech.customer.db.mysql.ReceiveProductCategories;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Product;
import educing.tech.customer.network.InternetConnectionDetector;
import educing.tech.customer.session.SessionManager;


public class SplashScreenActivity extends Activity implements OnTaskCompleted
{

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;

    private Context context = null;
    SharedPreferences prefs = null;
    private SessionManager session; // Session Manager Class

    private boolean isCategories, isSubCategories;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        this.context = SplashScreenActivity.this;

        session = new SessionManager(context); // Session Manager
        prefs = getSharedPreferences(Configuration.SHARED_PREF, MODE_PRIVATE);


        if (prefs.getBoolean("firstrun", true))
        {
            prefs.edit().putBoolean("firstrun", false).apply();
            startActivity(new Intent(SplashScreenActivity.this, WelcomeScreenActivity.class));
            finish();
        }

        else if(!session.isLoggedIn())
        {
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            finish();
        }

        else if(Product.productCategoryList.size() == 0 || Product.productSubCategoryList.size() == 0)
        {

            if (!new InternetConnectionDetector(getApplicationContext()).isConnected())
            {

                new CustomAlertDialog(context, this).showOKDialog("Network Error", "Internet Connection Failure", "network");
                return;
            }


            new ReceiveProductCategories(context, this).execute();
            new ReceiveProductSubCategories(context, this).retrieveProduct(1);
        }

        else
        {

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {

                    // This method will be executed once the timer is over
                    // Start your app main activity
                    startActivity(new Intent(SplashScreenActivity.this, DashboardActivity.class));
                    // close this activity
                    finish();
                }

            }, SPLASH_TIME_OUT);
        }
    }


    /** Called when the activity is about to become visible. */
    @Override
    protected void onStart()
    {

        super.onStart();
        Log.d("Inside : ", "onStart() event");
    }


    /** Called when the activity has become visible. */
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d("Inside : ", "onResume() event");
    }


    /** Called when another activity is taking focus. */
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d("Inside : ", "onPause() event");
    }


    /** Called when the activity is no longer visible. */
    @Override
    protected void onStop()
    {
        super.onStop();
        Log.d("Inside : ", "onStop() event");
    }


    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d("Inside : ", "onDestroy() event");
    }


    @Override
    public void onBackPressed()
    {

    }


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {

        try
        {

            if (flag && code == 200)
            {

                if (message.equals("categories"))
                {
                    isCategories = true;
                }

                if (message.equals("sub_categories"))
                {
                    isSubCategories = true;
                }

                if(isCategories && isSubCategories)
                {
                    startActivity(new Intent(SplashScreenActivity.this, DashboardActivity.class));
                    finish();
                }
            }

            else if (flag && code == 201)
            {
                finish();
            }

            else
            {
                new CustomAlertDialog(context, this).showOKDialog("Server Error", "Server not responding. Try later", "network");
            }
        }

        catch (Exception e)
        {

        }
    }


    /*private boolean checkMyServiceRunningOrNot()
    {

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {

            //if ("manasa.gmc.zantrik.service.AlarmService".equals(service.service.getClassName()))
            if (AlarmService.class.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }

        return false;
    }*/
}