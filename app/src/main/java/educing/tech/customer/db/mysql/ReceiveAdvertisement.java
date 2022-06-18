package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.helper.OnAdvertisementLoad;
import educing.tech.customer.model.Advertisement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import static educing.tech.customer.configuration.Configuration.API_URL;


public class ReceiveAdvertisement
{

	private OnAdvertisementLoad listener;
	
	private String URL = "";

	private Context context;

	private static final int MAX_ATTEMPTS = 10;
	private int ATTEMPTS_COUNT;
	
	
	public ReceiveAdvertisement(Context _context, OnAdvertisementLoad listener)
	{

		this.listener = listener;
		this.context = _context;
		this.URL = API_URL + "advertisement.php";
	}


	public void execute(final double latitude, final double longitude)
	{

		Log.v("URL", "" + URL);

		StringRequest postRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {


			@Override
				public void onResponse(String response)
				{

					Log.v("Response", response);

					try
					{

						JSONArray arr = new JSONArray(response);

						if (arr.length() > 0)
						{

							Advertisement.advertisementList.clear();

							for (int i = 0; i < arr.length(); i++)
							{

								JSONObject advertisementObj = (JSONObject) arr.get(i);

								int category_id = advertisementObj.getInt("category_id");
								int store_id = advertisementObj.getInt("store_id");
								String store_name = advertisementObj.getString("store_name");
								String file_name = advertisementObj.getString("file_name");

								Advertisement advertisement = new Advertisement(store_id, store_name, category_id, file_name);
								Advertisement.advertisementList.add(advertisement);
							}

							listener.onAdvertisementLoad(true, 200, "advertisement");
						}

						else
						{
							listener.onAdvertisementLoad(false, 199, "Sorry!! No Advertisement Found");
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

				try
				{

					if(ATTEMPTS_COUNT != MAX_ATTEMPTS)
					{

						execute(latitude, longitude);

						ATTEMPTS_COUNT ++;

						Log.v("#Attempt No: ", "" + ATTEMPTS_COUNT);
						return;
					}

					listener.onAdvertisementLoad(false, 500, "Internet Connection Failure. Try Again");
					Log.v("Error : ", "" + error);
				}

				catch (Exception e)
				{

				}
			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String, String> params = new HashMap<>();

				params.put("latitude", String.valueOf(latitude));
				params.put("longitude", String.valueOf(longitude));

				Log.v("location", String.valueOf(latitude));

				return params;
			}
		};


		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}