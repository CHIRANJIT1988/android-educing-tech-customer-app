package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import static educing.tech.customer.configuration.Configuration.API_URL;


public class SaveDealLocation
{

	private String URL = "";

	private Context context;
	
	private int user_id;
	private double latitude, longitude;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;
	


	public SaveDealLocation(Context context)
	{

		this.context = context;
		this.URL = API_URL + "save-deal-location.php";
	}
	


	public void save(int user_id, double latitude, double longitude)
	{

		this.user_id = user_id;
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

					JSONObject jsonObj = new JSONObject(response);

					int error_code = jsonObj.getInt("error_code");
					String message = jsonObj.getString("message");

					Log.v("Response: ", "" + response);

					if (error_code == 200) // checking for error node in json
					{
						Toast.makeText(context, message, Toast.LENGTH_LONG).show();
					}

					else
					{

						if(ATTEMPTS_COUNT != MAX_ATTEMPTS)
						{

							execute();

							ATTEMPTS_COUNT ++;

							Log.v("#Attempt No: ", "" + ATTEMPTS_COUNT);
						}

						Toast.makeText(context, message, Toast.LENGTH_LONG).show();
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
					Toast.makeText(context, "Internet Connection Failure", Toast.LENGTH_LONG).show();
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

				params.put("user_id", String.valueOf(user_id));
				params.put("latitude", String.valueOf(latitude));
				params.put("longitude", String.valueOf(longitude));

				Log.v("param: ", String.valueOf(params));

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}