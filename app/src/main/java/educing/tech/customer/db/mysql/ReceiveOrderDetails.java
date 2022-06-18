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


public class ReceiveOrderDetails
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;
	
	private String order_no;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;
	


	public ReceiveOrderDetails(Context _context , OnTaskCompleted listener)
	{

		this.listener = listener;
		this.context = _context;

		this.URL = API_URL + "fetch-order-details.php";
	}
	


	public void fetchOrderDetails(String order_no)
	{

		this.order_no = order_no;
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

						Order.myOrderDetailsList.clear();

						for (int i = 0; i < arr.length(); i++)
						{

							JSONObject jsonObj = (JSONObject) arr.get(i);

							int product_id = jsonObj.getInt("product_id");
							String product_name = jsonObj.getString("product_name");
							String product_image = jsonObj.getString("product_image");
							int weight = jsonObj.getInt("weight");
							String unit = jsonObj.getString("unit");
							double price = jsonObj.getDouble("price");
							double discount_price = jsonObj.getDouble("discount_price");
							int quantity = jsonObj.getInt("quantity");

							Order order = new Order(product_id, product_name, product_image, weight, unit, price, discount_price, quantity);
							Order.myOrderDetailsList.add(order);
						}

						listener.onTaskCompleted(true, 200, "success");
					}

					else
					{
						listener.onTaskCompleted(false, 500, "fail");
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

				listener.onTaskCompleted(false, 500, "fail"); // Invalid User

			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String, String> params = new HashMap<>();

				params.put("order_no", order_no);

				Log.v("params ", "" + order_no);
				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}