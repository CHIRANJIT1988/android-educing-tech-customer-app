package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Product;

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


public class ReceiveProduct
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;
	
	private int category_id, store_id;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;
	


	public ReceiveProduct(Context _context , OnTaskCompleted listener)
	{

		this.listener = listener;
		this.context = _context;

		this.URL = API_URL + "receive-store-products.php";
	}


	public void retrieveProduct(int category_id, int store_id)
	{

		this.category_id = category_id;
		this.store_id = store_id;
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

					Log.v("Product List: ", response);

					JSONArray arr = new JSONArray(response);

					if(arr.length() > 0)
					{

						for (int i = 0; i < arr.length(); i++)
						{

							JSONObject jsonObj = (JSONObject) arr.get(i);

							int category_id = jsonObj.getInt("product_category_id");
							int sub_category_id = jsonObj.getInt("product_sub_category_id");
							int product_id = jsonObj.getInt("product_id");
							int store_id = jsonObj.getInt("store_id");
							String name = jsonObj.getString("product_name");
							String description = jsonObj.getString("product_description");
							String image = jsonObj.getString("product_image");
							int weight = jsonObj.getInt("weight");
							String unit = jsonObj.getString("unit");
							Double price = jsonObj.getDouble("price");
							Double discount_price = jsonObj.getDouble("discount_price");


							Product product = new Product(category_id, sub_category_id, product_id, store_id, name, description, weight, unit, price, discount_price, image);
							Product.productList.add(product);
						}

						listener.onTaskCompleted(true, 200, "available");
						return;
					}

					listener.onTaskCompleted(true, 199, "Sorry!! No product found");
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

				listener.onTaskCompleted(false, 500, "Internet Connection Failure. Try Again");

			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String, String> params = new HashMap<>();

				params.put("category_id", String.valueOf(category_id));
				params.put("store_id", String.valueOf(store_id));

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}