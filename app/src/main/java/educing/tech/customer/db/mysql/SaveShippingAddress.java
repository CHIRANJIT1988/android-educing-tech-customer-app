package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.configuration.Configuration;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.helper.Security;
import educing.tech.customer.model.ShippingAddress;
import educing.tech.customer.session.SessionManager;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import static educing.tech.customer.configuration.Configuration.API_URL;
import static educing.tech.customer.configuration.Configuration.SECRET_KEY;


public class SaveShippingAddress
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;
	private SharedPreferences preferences;
	private SessionManager session;
	private ShippingAddress shippingAddress;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;


	public SaveShippingAddress(Context _context , OnTaskCompleted listener)
	{
		this.listener = listener;
		this.context = _context;
		this.preferences = context.getSharedPreferences(Configuration.SHARED_PREF, Context.MODE_PRIVATE);
		this.session = new SessionManager(context);

		this.URL = API_URL + "sync-shipping-address.php";
	}


	public void saveAddress(ShippingAddress shippingAddress)
	{
		this.shippingAddress = shippingAddress;
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

					JSONObject jsonObj = new JSONObject(response);

					int error_code = jsonObj.getInt("error_code");
					String message = jsonObj.getString("message");

					Log.v("Response: ", response);

					if(error_code == 200)
					{
						int address_id = jsonObj.getInt("address_id");
						listener.onTaskCompleted(true, error_code, String.valueOf(address_id)); // Successful
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

				catch (Exception e)
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

				listener.onTaskCompleted(false, 500, "Internet connection fail. Try again");
			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String ,String> params=new HashMap<>();

				try
				{

					JSONObject jsonObject = new JSONObject();

					jsonObject.put("user_id", String.valueOf(shippingAddress.getUserId()));
					jsonObject.put("name", shippingAddress.getName());
					jsonObject.put("phone_no", shippingAddress.getPhoneNo());
					jsonObject.put("landmark", shippingAddress.getLandmark());
					jsonObject.put("address", shippingAddress.getAddress());
					jsonObject.put("state", shippingAddress.getState());
					jsonObject.put("city", shippingAddress.getCity());
					jsonObject.put("pincode", shippingAddress.getPincode());

					params.put("responseJSON", Security.encrypt(jsonObject.toString(), preferences.getString("key", null)));
					params.put("user", Security.encrypt(String.valueOf(session.getUserId()), SECRET_KEY));
				}

				catch (Exception e)
				{

				}

				Log.v("params ", "" + params);

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}