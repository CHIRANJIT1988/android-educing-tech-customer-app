package educing.tech.customer;

import static educing.tech.customer.CommonUtilities.SERVER_URL;
import static educing.tech.customer.CommonUtilities.TAG;
import static educing.tech.customer.CommonUtilities.displayMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import org.json.JSONException;
import org.json.JSONObject;

import educing.tech.customer.activities.MainActivity;
import educing.tech.customer.activities.SplashScreenActivity;
import educing.tech.customer.configuration.Configuration;
import educing.tech.customer.helper.Security;
import educing.tech.customer.model.User;
import educing.tech.customer.session.SessionManager;

import static educing.tech.customer.configuration.Configuration.API_URL;
import static educing.tech.customer.activities.RegisterFragment.user;


public final class  ServerUtilities
{

	private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    private static Context context;


    public static void register(final Context context, final User user, final String regId)
    {

        ServerUtilities.context = context;

        Log.i(TAG, "registering device (regId = " + regId + ")");

        String serverUrl = SERVER_URL;

        Map<String, String> params = new HashMap<>();

        params.put("reg_id", regId);
        params.put("name", user.getUserName());
        params.put("mobile_no", user.getPhoneNo());
        params.put("email", user.getEmail());
        params.put("device_id", user.getDeviceId());
        params.put("password", user.getPassword());

        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);


        // Once GCM returns a registration id, we need to register on our server
        // As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++)
        {

            Log.d(TAG, "Attempt #" + i + " to register");

            try
            {

                displayMessage(context, context.getString(R.string.server_registering, i, MAX_ATTEMPTS));
                post(serverUrl, params);

                GCMRegistrar.setRegisteredOnServer(context, true);

                String message = context.getString(R.string.server_registered);

                CommonUtilities.displayMessage(context, message);

                return;
            }


            catch (IOException e)
            {

                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.e(TAG, "Failed to register on attempt " + i + ":" + e);


                if (i == MAX_ATTEMPTS)
                {
                    break;
                }


                try
                {

                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                }

                catch (InterruptedException e1)
                {

                    // Activity finished before we complete - exit.
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");

                    Thread.currentThread().interrupt();

                    return;
                }


                // increase backoff exponentially
                backoff *= 2;
            }
        }


        String message = context.getString(R.string.server_register_error, MAX_ATTEMPTS);
        CommonUtilities.displayMessage(context, message);
    }
    
    
    /**
     * Unregister this account/device pair within the server.
     */
    public static void unregister(final Context context, final String regId)
    {
    
    	Log.i(TAG, "un registering device (regId = " + regId + ")");
        
    	String serverUrl = API_URL + "register-user.php" + "/unregister";
        Map<String, String> params = new HashMap<>();
        params.put("reg_id", regId);
        
        try 
        {
        
        	post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
            
            String message = context.getString(R.string.server_unregistered);
            CommonUtilities.displayMessage(context, message);
        } 
        
        catch (IOException e) 
        {
        
        	// At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            
        	String message = context.getString(R.string.server_unregister_error, e.getMessage());
            CommonUtilities.displayMessage(context, message);
        }
    }



    private static void post(String endpoint, Map<String, String> params) throws IOException
    {

        URL url;

        try
        {
            url = new URL(endpoint);
        }

        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }


        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();


        // constructs the POST body using the parameters
        while (iterator.hasNext())
        {

            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());

            if (iterator.hasNext())
            {
                bodyBuilder.append('&');
            }
        }


        String body = bodyBuilder.toString();
        Log.v(TAG, "Posting '" + body + "' to " + url);

        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;


        try
        {

            Log.e("URL", "> " + url);

            conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            // handle the response
            int status = conn.getResponseCode();

            if (status != 200)
            {
                Toast.makeText(context, "Failed to Register. Try Again", Toast.LENGTH_LONG).show();
                throw new IOException("Post failed with error code " + status);
            }

            else
            {

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null)
                {
                    sb.append(line + "\n");
                }

                br.close();

                Log.v("response: ", "" + sb.toString());


                try
                {

                    JSONObject jsonObj = new JSONObject(sb.toString());

                    int error_code = jsonObj.getInt("error_code");

                    if(error_code == 200)
                    {

                        SharedPreferences preferences = context.getSharedPreferences(Configuration.SHARED_PREF, Context.MODE_PRIVATE);
                        preferences.edit().putString("key", Security.decrypt(jsonObj.getString("key"), Configuration.SECRET_KEY)).apply();

                        SessionManager session = new SessionManager(context);
                        user.setUserID(Integer.parseInt(Security.decrypt(jsonObj.getString("user_id"), Configuration.SECRET_KEY)));
                        session.createLoginSession(user);

                        Intent callIntent = new Intent(context, SplashScreenActivity.class);
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(callIntent);

                        MainActivity.activity.finish();
                    }

                    else
                    {
                        Toast.makeText(context, "Failed to Register. Try Again", Toast.LENGTH_LONG).show();
                    }
                }

                catch (JSONException e)
                {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to Register. Try Again", Toast.LENGTH_LONG).show();
                }
            }
        }

        finally
        {

            if (conn != null)
            {
                conn.disconnect();
            }
        }
    }
}