package educing.tech.customer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import educing.tech.customer.R;
import educing.tech.customer.ServerUtilities;
import educing.tech.customer.helper.GenerateUniqueId;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.User;
import educing.tech.customer.db.mysql.OTPVerification;
import educing.tech.customer.network.InternetConnectionDetector;

import static educing.tech.customer.CommonUtilities.SENDER_ID;


public class RegisterFragment extends Fragment implements OnClickListener, OnTaskCompleted
{

    private BroadcastReceiver mIntentReceiver;


    private Button btnRegister, btnConfirmationCode;
    private EditText editPhone, editName;
    private TextView tvStatus;
    private ProgressBar pBar;

    private LinearLayout linear_main;

    private Context context = null;
    public static User user;
    private static boolean is_registering = false;


    // AsyncTask
    private AsyncTask<Void, Void, Void> mRegisterTask;



    public RegisterFragment()
    {

    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
    }


	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
 
        final View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        findViewById(rootView);
        context = this.getActivity();

        btnRegister.setOnClickListener(this);
        btnConfirmationCode.setOnClickListener(this);

        hideKeyboard(rootView);

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


    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy()
    {

        if (mRegisterTask != null)
        {
            mRegisterTask.cancel(true);
        }


        try
        {
            //context.unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(context);
        }

        catch (Exception e)
        {
            Log.e("UnRegister Error", "> " + e.getMessage());
        }

        super.onDestroy();
        Log.d("Inside : ", "onDestroy() event");
    }


    private void findViewById(View rootView)
    {

        btnRegister = (Button) rootView.findViewById(R.id.btnRegister);
        btnConfirmationCode = (Button) rootView.findViewById(R.id.btnConfirmationCode);

        editPhone = (EditText) rootView.findViewById(R.id.editPhoneNumber);
        editName = (EditText) rootView.findViewById(R.id.editName);
        pBar = (ProgressBar) rootView.findViewById(R.id.pbLoading);
        tvStatus = (TextView) rootView.findViewById(R.id.status);
        linear_main = (LinearLayout) rootView.findViewById(R.id.linear_main);
    }


    public void onClick(View v)
    {

        switch (v.getId())
        {

            case R.id.btnRegister:

                if(validateForm())
                {

                    if (!new InternetConnectionDetector(getActivity()).isConnected())
                    {
                        makeSnackbar("Internet Connection Fail");
                        return;
                    }

                    pBar.setVisibility(View.VISIBLE);
                    tvStatus.setText(String.valueOf("Waiting for OTP"));

                    user = initUserObject();

                    btnConfirmationCode.setVisibility(View.GONE);
                    new OTPVerification(context, this).otpVerify(user);
                    is_registering = false;
                }

                break;

            case R.id.btnConfirmationCode:

                displayConfirmationCodeDialog();
                break;
        }
    }


    /*public static boolean isValidEmail(CharSequence target)
    {

        if (target.toString().trim().length() == 0)
        {
            return true;
        }

        else
        {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }*/


    private boolean validateForm()
    {

        if(editName.getText().toString().trim().length() < 3)
        {

            makeSnackbar("Name should be minimum 3 characters");
            return false;
        }

        if(editPhone.getText().toString().trim().length() != 10)
        {

            makeSnackbar("Invalid Phone Number");
            return false;
        }

        /*if(!isValidEmail(editEmail.getText().toString()))
        {
            makeSnackbar("Invalid Email");
            return false;
        }*/

        return  true;
    }


    private void makeSnackbar(String msg)
    {

        Snackbar snackbar = Snackbar.make(linear_main, msg, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.myPrimaryColor));
        snackbar.show();
    }


    private User initUserObject()
    {

        WifiManager m_wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        User user = new User();

        user.setPhoneNo(editPhone.getText().toString());
        user.setUserName(editName.getText().toString());
        user.setEmail("");
        user.setPassword("123456");
        user.setConfirmationCode(String.valueOf(GenerateUniqueId.getRandomNo(999999, 100000)));
        user.setDeviceId(String.valueOf(m_wm.getConnectionInfo().getMacAddress()));

        return user;
    }


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {

        try
        {

            if (flag)
            {

                btnConfirmationCode.setVisibility(View.VISIBLE);
            }

            else
            {
                tvStatus.setText("");
                pBar.setVisibility(View.GONE);
                btnConfirmationCode.setVisibility(View.GONE);
                makeSnackbar("Failed to Register. Try Again");
            }
        }

        catch (Exception e)
        {

        }
    }



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

                    if(msg.contains("registration"))
                    {

                        String otp = msg.substring(Math.max(0, msg.length() - 6));

                        if(otp.equals(user.confirmation_code))
                        {

                            if(!is_registering)
                            {
                                tvStatus.setText(String.valueOf("Registering ..."));
                                gcm_registration();
                            }

                            is_registering = true;
                        }
                    }

                }

                catch (Exception e)
                {
                    Toast.makeText(context, "Registration Error", Toast.LENGTH_SHORT).show();
                }
            }
        };

        getActivity().registerReceiver(mIntentReceiver, intentFilter);
    }


    private void gcm_registration()
    {

        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(context);

        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(context);

        //context.registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));

        // Get GCM registration id
        final String regId = GCMRegistrar.getRegistrationId(context);

        // Check if regid already presents
        if (regId.equals(""))
        {
            // Registration is not present, register now with GCM
            GCMRegistrar.register(context, SENDER_ID);
        }

        else
        {

            // Try to register again, but not in the UI thread.
            // It's also necessary to cancel the thread onDestroy(),
            // hence the use of AsyncTask instead of a raw thread.
            // final Context context = this;

            mRegisterTask = new AsyncTask<Void, Void, Void>()
            {

                @Override
                protected Void doInBackground(Void... params)
                {
                    // Register on our server
                    // On server creates a new user
                    ServerUtilities.register(context, user, regId);
                    return null;
                }


                @Override
                protected void onPostExecute(Void result)
                {
                    mRegisterTask = null;
                }

            };

            mRegisterTask.execute(null, null, null);
        }
    }


    private void displayConfirmationCodeDialog()
    {

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        final EditText edittext= new EditText(context);
        alert.setMessage("Enter OTP");
        alert.setCancelable(false);

        edittext.setHeight(50);
        edittext.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        //edittext.SetRawInputType(Android.Text.InputTypes.NumberFlagDecimal | Android.Text.InputTypes.ClassNumber);
        //edittext.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});


        alert.setView(edittext);


        alert.setPositiveButton("Verify", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {


                if (edittext.getText().toString().trim().length() == 6) {

                    if (user.getConfirmationCode().equals(edittext.getText().toString()))
                    {
                        if(!is_registering)
                        {
                            tvStatus.setText(String.valueOf("Registering ..."));
                            gcm_registration();
                        }

                        is_registering = true;

                    } else {
                        makeSnackbar("Verification Fail. Try Again");
                    }

                    dialog.dismiss();
                }
            }
        });


        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                dialog.cancel();
            }
        });

        alert.show();
    }
}