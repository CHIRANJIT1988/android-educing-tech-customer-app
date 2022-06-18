package educing.tech.customer.db.mysql;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import educing.tech.customer.app.MyApplication;
import educing.tech.customer.helper.Base64;
import educing.tech.customer.helper.OnTaskCompleted;

import static educing.tech.customer.configuration.Configuration.API_URL;



public class SendProfilePictureToServer
{

	private String URL = "";
	private String ba1 = "";

	private String path, name;

	private Context context;
	private OnTaskCompleted listener;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;


	public SendProfilePictureToServer(Context context, OnTaskCompleted listener, String path, String name)
	{

		this.context = context;
		this.listener = listener;

		this.path = path;
		this.name = name;

		this.URL = API_URL + "upload-profile-pic.php";
	}


	public void upload()
	{
	
		File imgFile = new File(path);
		{
	
			if(imgFile.exists())
			{
	    
				try
				{

					// bimatp factory
					BitmapFactory.Options options = new BitmapFactory.Options();

					// downsizing image as it throws OutOfMemory Exception for larger
					// images
					options.inSampleSize = 4;

					Bitmap bitmap = BitmapFactory.decodeFile(path, options);

					// Bitmap bitmap = BitmapFactory.decodeFile(path);
					Bitmap resized = Bitmap.createScaledBitmap(bitmap, 280, 280, false);

					ByteArrayOutputStream bao = new ByteArrayOutputStream();

					resized.compress(Bitmap.CompressFormat.JPEG, 80, bao);
			
				
					byte[] ba = bao.toByteArray();
					ba1 = Base64.encodeBytes(ba);
			
					//new UploadToServer(name).execute();

					execute();
				}
				
				catch(Exception e)
				{
					
				}
			}
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

					JSONObject jsonObj = new JSONObject(response);

					Log.v("Response: ", response);

					int sync_status = jsonObj.getInt("sync_status");

					if(sync_status == 1)
					{
						listener.onTaskCompleted(true, 300, "Profile Pic Changed Successfully");
					}

					else
					{
						listener.onTaskCompleted(true, 500, "Failed to Change Profile Pic");
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
				}

				listener.onTaskCompleted(true, 500, "Failed to Change Profile Pic");
				Log.v("Error: ", "" + error.getMessage());
			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String, String> params = new HashMap<>();

				params.put("base64", ba1);
				params.put("ImageName", name);

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}