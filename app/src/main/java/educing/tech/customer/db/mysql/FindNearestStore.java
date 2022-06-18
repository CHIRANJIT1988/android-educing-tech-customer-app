package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Store;

import java.util.Collections;
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

import java.util.Comparator;

import static educing.tech.customer.configuration.Configuration.API_URL;


public class FindNearestStore
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;
	
	private int category_id;
	private double latitude, longitude;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;
	


	public FindNearestStore(Context context , OnTaskCompleted listener)
	{

		this.listener = listener;
		this.context = context;

		this.URL = API_URL + "find-nearest-store.php";
	}
	


	public void find(int category_id, double latitude, double longitude)
	{

		this.category_id = category_id;
		this.latitude = latitude;
		this.longitude = longitude;
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

					Log.v("Response: ", response);

					JSONArray arr = new JSONArray(response);

					if(arr.length() > 0)
					{

						Store.storeList.clear();

						for (int i = 0; i < arr.length(); i++)
						{

							JSONObject jsonObj = (JSONObject) arr.get(i);

							int id = jsonObj.getInt("id");
							String name = jsonObj.getString("name");
							String owner = jsonObj.getString("owner");
							String phone_no = jsonObj.getString("phone_no");

							String address = jsonObj.getString("address");
							String city = jsonObj.getString("city");
							String state = jsonObj.getString("state");
							String country = jsonObj.getString("country");
							String pincode = jsonObj.getString("pincode");
							double latitude = jsonObj.getDouble("latitude");
							double longitude = jsonObj.getDouble("longitude");
							double distance = jsonObj.getDouble("distance");
							int is_online = jsonObj.getInt("is_online");

							int delivery_status = jsonObj.getInt("delivery_status");
							double amount = jsonObj.getDouble("amount");
							double delivery_charge = jsonObj.getDouble("delivery_charge");
							double rating = jsonObj.getDouble("rating");

							Store.storeList.add(new Store(id, name, owner, phone_no, address, city, state, country, pincode, latitude, longitude, distance, delivery_status, amount, delivery_charge, (float)rating, is_online));
						}

						Collections.sort(Store.storeList, new DistanceCompare());
						listener.onTaskCompleted(true, 200, "available");
					}

					else
					{
						listener.onTaskCompleted(true, 199, "Sorry!! No Nearest Store Found");
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

				listener.onTaskCompleted(false, 500, "Internet Connection Failure. Try Again"); // Invalid User

			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String, String> params = new HashMap<>();

				params.put("category_id", String.valueOf(category_id));
				params.put("latitude", String.valueOf(latitude));
				params.put("longitude", String.valueOf(longitude));

				Log.v("params: ", String.valueOf(params));

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}


	class DistanceCompare implements Comparator<Store>
	{

		@Override
		public int compare(Store s1, Store s2)
		{

			if(s1.distance > s2.distance)
			{
				return 1;
			}

			return -1;
		}
	}
}