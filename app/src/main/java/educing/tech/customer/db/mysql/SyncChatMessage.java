package educing.tech.customer.db.mysql;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import educing.tech.customer.app.MyApplication;
import educing.tech.customer.configuration.Configuration;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.helper.Security;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.session.SessionManager;
import educing.tech.customer.sqlite.SQLiteDatabaseHelper;

import static educing.tech.customer.configuration.Configuration.API_URL;
import static educing.tech.customer.sqlite.SQLiteDatabaseHelper.TABLE_CHAT_MESSAGES;
import static educing.tech.customer.configuration.Configuration.SECRET_KEY;

public class SyncChatMessage implements OnTaskCompleted
{

	private String URL = "";

	private Context context;
	private OnTaskCompleted listener;
	private SharedPreferences preferences;
	private SessionManager session;
	private SQLiteDatabaseHelper helper;

	private static final int MAX_ATTEMPTS = 10;
	private int ATTEMPTS_COUNT;


	public SyncChatMessage(Context context, OnTaskCompleted listener)
	{

		this.context = context;
		this.listener = listener;

		this.preferences = context.getSharedPreferences(Configuration.SHARED_PREF, Context.MODE_PRIVATE);
		this.session = new SessionManager(context);
		this.helper = new SQLiteDatabaseHelper(context);

		this.URL = API_URL + "sync-user-chat-message.php";
	}


	public void getAllChatImage()
	{

		ArrayList<ChatMessage> messageList = new SQLiteDatabaseHelper(context).getAllChatImages();

		for (ChatMessage message: messageList)
		{
			new SendImageToServer(context, this, message.getMessageId(), message.getFilePath(), message.getImage()).upload();
		}
	}


	public void execute()
	{

		StringRequest postRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

			@Override
			public void onResponse(String response)
			{

				try
				{

					Log.v("response", response);

					JSONArray jsonArray = new JSONArray(response);

					for(int i=0; i<jsonArray.length(); i++)
					{

						JSONObject jsonObj = (JSONObject) jsonArray.get(i);

						int id = jsonObj.getInt("id");
						int sync_status = jsonObj.getInt("sync_status");
						String message_number = jsonObj.getString("message_id");

						listener.onTaskCompleted(false, sync_status, message_number);
						new SQLiteDatabaseHelper(context).updateSyncStatus(TABLE_CHAT_MESSAGES, id, sync_status);
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
				}
			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String ,String> params=new HashMap<>();

				try
				{
					String data = String.valueOf(helper.chatMessageJSONData());
					params.put("responseJSON", Security.encrypt(data, preferences.getString("key", null)));
					params.put("user", Security.encrypt(String.valueOf(session.getUserId()), SECRET_KEY));
				}

				catch (Exception e)
				{
					Log.v("error ", "" + e.getMessage());
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


	@Override
	public void onTaskCompleted(boolean flag, int code, String message)
	{

	}
}