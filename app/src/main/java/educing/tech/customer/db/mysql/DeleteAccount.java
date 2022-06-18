package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.configuration.Configuration;
import educing.tech.customer.helper.OnTaskCompleted;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import educing.tech.customer.helper.Security;

import static educing.tech.customer.configuration.Configuration.API_URL;
import static educing.tech.customer.configuration.Configuration.SECRET_KEY;


public class DeleteAccount
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;
	
	private int user_id;
	private SharedPreferences preferences;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;

	
	public DeleteAccount(Context context , OnTaskCompleted listener)
	{
		this.listener = listener;
		this.context = context;
		this.preferences = context.getSharedPreferences(Configuration.SHARED_PREF, Context.MODE_PRIVATE);

		this.URL = API_URL + "delete-account.php";
	}
	
	
	public void deleteAccount(int user_id)
	{
		
		this.user_id = user_id;
		execute();
	}


	public void execute()
	{

		StringRequest postRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

			@Override
			public void onResponse(String response)
			{

				try
				{

					Log.v("Verification: ", response);

					JSONObject jsonObj = new JSONObject(response);

					int error_code = jsonObj.getInt("error_code");
					String message = jsonObj.getString("message");

					if (error_code == 200) // checking for error node in json
					{
						listener.onTaskCompleted(true, error_code, message); // Successful
					}

					else
					{

						if(ATTEMPTS_COUNT != MAX_ATTEMPTS)
						{

							execute();

							ATTEMPTS_COUNT ++;

							Log.v("#Attempt No: ", "" + ATTEMPTS_COUNT);
							return;
						}

						listener.onTaskCompleted(false, error_code, message); // Unsuccessful
					}
				}

				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error)
			{

				if(ATTEMPTS_COUNT != MAX_ATTEMPTS)
				{

					execute();

					ATTEMPTS_COUNT ++;

					Log.v("#Attempt No: ", "" + ATTEMPTS_COUNT);
					return;
				}

				listener.onTaskCompleted(false, 500, "Internet connection fail. Try Again");
			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String, String> params = new HashMap<>();

				try
				{
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("user_id", String.valueOf(user_id));
					jsonObject.put("api_key", String.valueOf(Security.encrypt(preferences.getString("key", null), SECRET_KEY)));

					params.put("responseJSON", jsonObject.toString());
				}

				catch (Exception e)
				{

				}

				Log.v("Data: ", "" + params);

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}