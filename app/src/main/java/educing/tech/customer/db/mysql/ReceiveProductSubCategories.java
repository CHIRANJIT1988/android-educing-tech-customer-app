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


public class ReceiveProductSubCategories
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;
	
	private int category_id;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;
	


	public ReceiveProductSubCategories(Context _context , OnTaskCompleted listener)
	{

		this.listener = listener;
		this.context = _context;

		this.URL = API_URL + "product-sub-category.php";
	}
	


	public void retrieveProduct(int category_id)
	{

		this.category_id = category_id;
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

					Log.v("Response", response);

					JSONArray arr = new JSONArray(response);

					if(arr.length() > 0)
					{

						Product.productSubCategoryList.clear();


						for (int i = 0; i < arr.length(); i++)
						{

							JSONObject jsonObj = (JSONObject) arr.get(i);

							int category_id = jsonObj.getInt("category_id");
							int sub_category_id = jsonObj.getInt("sub_category_id");
							String name = jsonObj.getString("name");

							Product product = new Product(category_id, sub_category_id, name);
							Product.productSubCategoryList.add(product);
						}

						listener.onTaskCompleted(true, 200, "sub_categories");
					}

					else
					{
						listener.onTaskCompleted(true, 199, "Sorry !! No Sub Categories Found");
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

				Log.v("Response", "" + error);

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

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}