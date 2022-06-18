package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.helper.OnTaskCompleted;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import static educing.tech.customer.configuration.Configuration.API_URL;


public class RateProduct
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;

	private String order_no;
	private float rating;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;


	public RateProduct(Context _context , OnTaskCompleted listener)
	{
		this.listener = listener;
		this.context = _context;
		this.URL = API_URL + "rate-product.php";
	}


	public void rateProduct(String order_no, float rating)
	{
		this.order_no = order_no;
		this.rating = rating;
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

					Log.v("response", response);

					if(error_code == 200)
					{
						listener.onTaskCompleted(true, error_code, message); // Successful
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
				listener.onTaskCompleted(false, 500, "Internet connection fail. Try again");
			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String ,String> params=new HashMap<>();

				params.put("order_no", order_no);
				params.put("rating", String.valueOf(rating));

				Log.v("params ", "" + params);

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}