package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Order;

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


public class ReceiveOrders
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;
	
	private int user_id;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;
	


	public ReceiveOrders(Context context , OnTaskCompleted listener)
	{

		this.listener = listener;
		this.context = context;

		this.URL = API_URL + "fetch-orders.php";
	}
	


	public void fetchOrders(int user_id)
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

					Log.v("Response: ", response);

					JSONArray arr = new JSONArray(response);

					if(arr.length() > 0)
					{

						Order.myOrderList.clear();

						for (int i = 0; i < arr.length(); i++)
						{

							JSONObject jsonObj = (JSONObject) arr.get(i);

							String order_no = jsonObj.getString("order_no");
							String category_name = jsonObj.getString("category_name");
							String category_image = jsonObj.getString("image");
							int store_id = jsonObj.getInt("store_id");
							String store_name = jsonObj.getString("store_name");
							String order_date = jsonObj.getString("order_date");
							String order_status = jsonObj.getString("order_status");
							double rating = jsonObj.getDouble("rating");
							double delivery_charge = jsonObj.getDouble("delivery_charge");

							Order order = new Order(user_id, order_no, category_name, category_image, store_id, store_name, order_date, order_status, rating, delivery_charge);
							Order.myOrderList.add(order);
						}

						listener.onTaskCompleted(true, 200, "available");
					}

					else
					{
						listener.onTaskCompleted(true, 199, "Sorry!! No order found");
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

				listener.onTaskCompleted(false, 500, "Internet Connection Fail. Try Again"); // Invalid User

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