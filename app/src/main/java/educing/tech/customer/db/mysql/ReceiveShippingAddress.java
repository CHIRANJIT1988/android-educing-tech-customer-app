package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.ShippingAddress;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import static educing.tech.customer.configuration.Configuration.API_URL;


public class ReceiveShippingAddress
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;
	
	private int user_id;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;
	


	public ReceiveShippingAddress(Context _context , OnTaskCompleted listener)
	{

		this.listener = listener;
		this.context = _context;

		this.URL = API_URL + "receive-shipping-address.php";
	}
	


	public void fetchAddress(int user_id)
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

					Log.v("Shipping Address: ", response);

					JSONArray arr = new JSONArray(response);

					Log.v("Length: ", "" + arr.length());

					if(arr.length() > 0)
					{

						ShippingAddress.shippingAddressList.clear();

						for (int i = 0; i < arr.length(); i++)
						{

							JSONObject jsonObj = (JSONObject) arr.get(i);

							int address_id = jsonObj.getInt("address_id");
							int user_id = jsonObj.getInt("user_id");
							String name = jsonObj.getString("name");
							String phone_no = jsonObj.getString("phone_no");
							String landmark = jsonObj.getString("landmark");
							String address = jsonObj.getString("address");
							String city = jsonObj.getString("city");
							String state = jsonObj.getString("state");
							String country = jsonObj.getString("country");
							String pincode = jsonObj.getString("pincode");


							ShippingAddress sAddress = new ShippingAddress(address_id, user_id, name, pincode, phone_no, landmark, address, city, state, country);
							ShippingAddress.shippingAddressList.add(sAddress);
						}

						listener.onTaskCompleted(true, 200, "Success");
					}

					else
					{
						listener.onTaskCompleted(true, 500, "No Address Found");
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

				Log.v("Product List: ", "" + error);

				if(ATTEMPTS_COUNT != MAX_ATTEMPTS)
				{

					execute();

					ATTEMPTS_COUNT ++;

					Log.v("#Attempt No: ", "" + ATTEMPTS_COUNT);
					return;
				}

				listener.onTaskCompleted(false, 500, "Internet Connection Failure. Try Again");

			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String, String> params = new HashMap<>();

				params.put("user_id", String.valueOf(user_id));

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}