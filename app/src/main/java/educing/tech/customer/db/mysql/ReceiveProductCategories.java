package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Product;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import static educing.tech.customer.configuration.Configuration.API_URL;


public class ReceiveProductCategories
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;
	
	
	public ReceiveProductCategories(Context _context, OnTaskCompleted listener)
	{

		this.listener = listener;
		this.context = _context;
		this.URL = API_URL + "product-category.php";
	}


	public void execute()
	{

		Log.v("URL", "" + URL);

		// Volley's json array request object
		JsonArrayRequest req = new JsonArrayRequest(URL,

				new Response.Listener<JSONArray>()
				{

					@Override
					public void onResponse(JSONArray response)
					{

						if (response.length() > 0)
						{

							Product.productCategoryList.clear();


							// looping through json and adding to movies list
							for (int i = 0; i < response.length(); i++)
							{

								try
								{

									Log.v("Response", response.toString());
									JSONObject complainObj = response.getJSONObject(i);

									int category_id = complainObj.getInt("id");
									String name = complainObj.getString("name");
									String image = complainObj.getString("image");

									Product product = new Product(category_id, name, image);
									Product.productCategoryList.add(product);
								}

								catch (Exception e)
								{

								}
							}

							listener.onTaskCompleted(true, 200, "categories");
						}

						else
						{
							listener.onTaskCompleted(false, 199, "Sorry!! No Categories Found");
						}
					}
				}, new Response.ErrorListener() {


			@Override
			public void onErrorResponse(VolleyError error)
			{

				try
				{

					if(ATTEMPTS_COUNT != MAX_ATTEMPTS)
					{

						execute();

						ATTEMPTS_COUNT ++;

						Log.v("#Attempt No: ", "" + ATTEMPTS_COUNT);
						return;
					}

					listener.onTaskCompleted(false, 500, "Internet Connection Failure. Try Again");
					Log.v("Error : ", "" + error);
				}

				catch (Exception e)
				{

				}
			}
		});


		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(req);
	}
}