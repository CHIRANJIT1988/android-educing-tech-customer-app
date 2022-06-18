package educing.tech.customer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import educing.tech.customer.R;
import educing.tech.customer.configuration.Configuration;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.User;
import educing.tech.customer.network.InternetConnectionDetector;


public class LoginFragment extends Fragment implements OnClickListener, OnTaskCompleted
{

    private BroadcastReceiver mIntentReceiver;

    private Button btnLogin;
    private EditText editPhone, editPassword;
    private ImageView ivLogo;
    private TextView tvStatus;

    private ProgressBar pBar;

    private ScrollView scrollableContents;

    private RelativeLayout relativeLayout;

    private Context context = null;
    SharedPreferences prefs = null;

    private User user;


    public static CallbackManager callbackManager;
    LoginButton FB_login;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity());

        context = this.getActivity();
        prefs = getActivity().getSharedPreferences(Configuration.SHARED_PREF, Context.MODE_PRIVATE);

    }


	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
 
        final View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        findViewById(rootView);

        btnLogin.setOnClickListener(this);

        /*if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            ivLogo.setVisibility(View.GONE);
        }

        else
        {
            ivLogo.setVisibility(View.VISIBLE);
        }*/


        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String number = tm.getLine1Number();

        if(number != null)
        {
            editPhone.setText(number);
        }


        hideKeyboard(rootView);

        callbackManager = CallbackManager.Factory.create();


        float fbIconScale = .8F;
        Drawable drawable = getActivity().getResources().getDrawable(R.drawable.facebook_box);//com.facebook.R.drawable.com_facebook_button_icon);
        drawable.setBounds(0, 0, (int)(drawable.getIntrinsicWidth()*fbIconScale), (int)(drawable.getIntrinsicHeight()*fbIconScale));
        FB_login.setCompoundDrawables(drawable, null, null, null);
        FB_login.setCompoundDrawablePadding(getActivity().getResources().getDimensionPixelSize(R.dimen.fb_margin_override_textpadding));
        FB_login.setPadding(FB_login.getResources().getDimensionPixelSize(R.dimen.fb_margin_override_lr), getActivity().getResources().
                getDimensionPixelSize(R.dimen.fb_margin_override_top), 0,
                getActivity().getResources().getDimensionPixelSize(R.dimen.fb_margin_override_bottom));


        FB_login.setReadPermissions("public_profile email");


        if(AccessToken.getCurrentAccessToken() != null)
        {
            //RequestData();
            //share.setVisibility(View.VISIBLE);
            //details.setVisibility(View.VISIBLE);
            //startActivity(new Intent(getActivity(), HomeActivity.class));
        }


        FB_login.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view)
            {

                if(AccessToken.getCurrentAccessToken() != null)
                {
                    //share.setVisibility(View.INVISIBLE);
                    //details.setVisibility(View.INVISIBLE);
                    //profile.setProfileId(null);

                    Toast.makeText(getActivity(), "LOGIN", Toast.LENGTH_LONG).show();
                }
            }
        });


        FB_login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult)
            {

                if(AccessToken.getCurrentAccessToken() != null)
                {

                    //RequestData();
                    //share.setVisibility(View.VISIBLE);
                    //details.setVisibility(View.VISIBLE);

                    //startActivity(new Intent(getActivity(), HomeActivity.class));
                }
            }

            @Override
            public void onCancel()
            {

            }

            @Override
            public void onError(FacebookException exception)
            {

            }
        });

        return rootView;
    }


    /** Called when the activity is about to become visible. */
    @Override
    public void onStart()
    {

        super.onStart();
        Log.d("Inside : ", "onStart() event");
    }


    /** Called when another activity is taking focus. */
    @Override
    public void onPause()
    {
        super.onPause();
        Log.d("Inside : ", "onPause() event");

        getActivity().unregisterReceiver(this.mIntentReceiver);
    }


    /** Called when the activity is no longer visible. */
    @Override
    public void onStop()
    {
        super.onStop();
        Log.d("Inside : ", "onStop() event");
    }


    //** Called just before the activity is destroyed. */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d("Inside : ", "onDestroy() event");
    }


    private void findViewById(View rootView)
    {


        FB_login = (LoginButton) rootView.findViewById(R.id.login_button);

        btnLogin = (Button) rootView.findViewById(R.id.btnLogin);
        editPhone = (EditText) rootView.findViewById(R.id.editPhoneNumber);
        editPassword = (EditText) rootView.findViewById(R.id.editPassword);

        pBar = (ProgressBar) rootView.findViewById(R.id.pbLoading);

        tvStatus = (TextView) rootView.findViewById(R.id.status);

        ivLogo = (ImageView) rootView.findViewById(R.id.imgLogo);
        scrollableContents = (ScrollView) rootView.findViewById(R.id.scrollableContents);

        relativeLayout = (RelativeLayout) rootView.findViewById(R.id.layout_main);
    }


    public void onClick(View v)
    {

        switch (v.getId())
        {

            case R.id.btnLogin:

                if(validateForm())
                {

                    if (!new InternetConnectionDetector(getActivity()).isConnected())
                    {
                        makeSnackbar("Internet Connection Fail");
                        break;
                    }

                    pBar.setVisibility(View.VISIBLE);
                    tvStatus.setText("Logging ...");

                    scrollContentDown();

                    this.user = initUserObject();
                }

                break;
        }
    }


    private boolean validateForm()
    {

        if(editPhone.getText().toString().trim().length() != 10)
        {

            makeSnackbar("Invalid Phone Number");
            return false;
        }

        /*if(editPassword.getText().toString().trim().length() == 0)
        {

            makeSnackbar("Enter Password");
            return false;
        }*/

        return  true;
    }


    private void makeSnackbar(String msg)
    {

        Snackbar snackbar = Snackbar.make(relativeLayout, msg, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.myPrimaryColor));
        snackbar.show();
    }


    private User initUserObject()
    {

        User user = new User();

        user.setPhoneNo(editPhone.getText().toString());
        user.setPassword("123456");

        return user;
    }


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {

        try
        {

            if (flag)
            {

                tvStatus.setText("Waiting for OTP ...");

                /*if(prefs.getBoolean("hasGCM", true))
                {
                    session.createLoginSession(user.getPhoneNo(), user.getPassword());
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show();
                }*/

                // Launch GCM Activity
                Intent i = new Intent(getActivity(), GCMActivity.class);
                // Registering user on our server
                // Sending registraiton details to GCMActivity
                i.putExtra("name", user.getPassword());
                i.putExtra("phone_no", user.getPhoneNo());
                i.putExtra("email", "");
                startActivity(i);
                getActivity().finish();
            }

            else
            {
                pBar.setVisibility(View.GONE);
                tvStatus.setText("");
                makeSnackbar(message);
                scrollContentUp();
            }
        }

        catch (Exception e)
        {

        }
    }


    private void scrollContentDown()
    {

        scrollableContents.post(new Runnable() {

            @Override
            public void run()
            {
                scrollableContents.fullScroll(View.FOCUS_DOWN);
            }
        });
    }


    private void scrollContentUp()
    {

        scrollableContents.post(new Runnable() {

            @Override
            public void run() {
                scrollableContents.fullScroll(View.FOCUS_UP);
            }
        });
    }


   /* @Override
    public void onConfigurationChanged(Configuration newConfig)
    {

        super.onConfigurationChanged(newConfig);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            ivLogo.setVisibility(View.GONE);
        }

        else
        {
            ivLogo.setVisibility(View.VISIBLE);
        }
    }*/


    private void hideKeyboard(final View rootView)
    {

        editPhone.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (editPhone.getText().toString().trim().length() == 10) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                }
            }
        });
    }


    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }*/



    @Override
    public void onResume()
    {

        super.onResume();

        IntentFilter intentFilter = new IntentFilter("SmsMessage.intent.MAIN");

        mIntentReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent)
            {

                try
                {

                    String msg = intent.getStringExtra("get_msg");

                    if(msg.contains("login"))
                    {

                        String otp = msg.substring(Math.max(0, msg.length() - 6));

                        //if(otp.equals(educing.tech.customer.configuration.Configuration.confirmation_code))
                        {

                            //educing.tech.customer.configuration.Configuration.confirmation_code = "";

                            //session.createLoginSession(user.getPhoneNo(), user.getUserName());
                            //Toast.makeText(context, "Registration Successful", Toast.LENGTH_LONG).show();
                            //getActivity().finish();

                            // Launch GCM Activity
                            // Intent i = new Intent(getActivity(), GCMActivity.class);

                            // Registering user on our server
                            // Sending registration details to GCMActivity
                            //i.putExtra("name", "");
                            //i.putExtra("phone_no", user.getPhoneNo());
                            //i.putExtra("email", "");
                            //startActivity(i);

                            //getActivity().finish();
                        }


                        //tvStatus.setText("Registration OTP Verifying...");
                        //new OTPVerification(context, RegisterFragment.this).otpVerify(user.getPhoneNo(), otp);
                    }

                }

                catch (Exception e)
                {
                    Toast.makeText(context, "Login Error", Toast.LENGTH_SHORT).show();
                }
            }
        };

        getActivity().registerReceiver(mIntentReceiver, intentFilter);
    }
}