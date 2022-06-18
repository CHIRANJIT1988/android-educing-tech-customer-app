package educing.tech.customer.db.mysql;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.configuration.Configuration;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.helper.Security;
import educing.tech.customer.model.Order;
import educing.tech.customer.model.User;
import educing.tech.customer.session.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import static educing.tech.customer.configuration.Configuration.API_URL;
import static educing.tech.customer.configuration.Configuration.SECRET_KEY;
import static educing.tech.customer.model.Order.delivery_charge_total;


public class SendOrderInfo
{

	private OnTaskCompleted listener;
	
	private String URL = "";

	private Context context;
	private SharedPreferences preferences;
	private SessionManager session;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;
	private List<Order> orderList;
	private int shipping_address;


	public SendOrderInfo(Context context , OnTaskCompleted listener, List<Order> orderList, int shipping_address)
	{

		this.listener = listener;
		this.context = context;

		this.preferences = context.getSharedPreferences(Configuration.SHARED_PREF, Context.MODE_PRIVATE);
		this.session = new SessionManager(context);

		this.orderList = orderList;
		this.shipping_address = shipping_address;
		this.URL = API_URL + "sync-order-details.php";
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

					int status_code = jsonObj.getInt("status_code");
					String message = jsonObj.getString("message");

					Log.v("Response: ", response);


					if(status_code == 200)
					{

						String order_no = jsonObj.getString("order_no");
						listener.onTaskCompleted(true, 200, order_no); // Successful
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

						listener.onTaskCompleted(false, 500, message); // Unsuccessful
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

				if(ATTEMPTS_COUNT != MAX_ATTEMPTS)
				{

					execute();

					ATTEMPTS_COUNT ++;

					Log.v("#Attempt No: ", "" + ATTEMPTS_COUNT);
					return;
				}

				listener.onTaskCompleted(false, 500, "Internet connection fail. Try again");
			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String ,String> params=new HashMap<>();

				try
				{

					JSONArray jsonArray = new JSONArray();

					for(int i=0; i<orderList.size(); i++)
					{

						Order order = orderList.get(i);

						JSONObject jsonObject=new JSONObject();

						jsonObject.put("order_no", order.order_no);
						jsonObject.put("user_id", String.valueOf(getUserDetails().getUserID()));
						jsonObject.put("store_id", String.valueOf(order.store_id));
						jsonObject.put("category_id", String.valueOf(order.category_id));
						jsonObject.put("product_id", String.valueOf(order.product_id));
						jsonObject.put("product_name", order.product_name);
						jsonObject.put("weight", String.valueOf(order.weight));
						jsonObject.put("unit", order.unit);
						jsonObject.put("quantity", String.valueOf(order.quantity));
						jsonObject.put("price", String.valueOf(order.price));
						jsonObject.put("discount_price", String.valueOf(order.discount_price));
						jsonObject.put("delivery_charge", String.valueOf(delivery_charge_total));
						jsonObject.put("shipping_id", String.valueOf(shipping_address));

						jsonArray.put(jsonObject);
					}

					params.put("responseJSON", Security.encrypt(jsonArray.toString(), preferences.getString("key", null)));
					params.put("user", Security.encrypt(String.valueOf(session.getUserId()), SECRET_KEY));
				}

				catch (Exception e)
				{

				}

				finally
				{
					Log.v("params ", "" + params);
				}

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}


	private User getUserDetails()
	{

		User userObj = new User();

		if (session.checkLogin())
		{
			HashMap<String, String> user = session.getUserDetails();

			userObj.setUserID(Integer.valueOf(user.get(SessionManager.KEY_USER_ID)));
			userObj.setPhoneNo(user.get(SessionManager.KEY_PHONE));
			userObj.setUserName(user.get(SessionManager.KEY_USER_NAME));
		}

		return userObj;
	}
}