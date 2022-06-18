package educing.tech.customer.db.mysql;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
import educing.tech.customer.sqlite.SQLiteDatabaseHelper;

import static educing.tech.customer.configuration.Configuration.API_URL;
import static educing.tech.customer.sqlite.SQLiteDatabaseHelper.TABLE_CHAT_IMAGES;
import static educing.tech.customer.sqlite.SQLiteDatabaseHelper.KEY_MESSAGE_ID;


public class SendImageToServer {

	private String URL = "";
	private String ba1 = "";

	private String file_id, path, name;

	private Context context;
	private OnTaskCompleted listener;

	private static final int MAX_ATTEMPTS = 10;
	private int ATTEMPTS_COUNT;


	public SendImageToServer(Context context, OnTaskCompleted listener, String file_id, String path, String name) {

		this.context = context;
		this.listener = listener;

		this.file_id = file_id;
		this.path = path;
		this.name = name;

		this.URL = API_URL + "upload-chat-image.php";
	}


	public void upload() {

		File imgFile = new File(path);
		{

			if (imgFile.exists()) {

				try {

					// bimatp factory
					BitmapFactory.Options options = new BitmapFactory.Options();

					// downsizing image as it throws OutOfMemory Exception for larger
					// images
					options.inSampleSize = 4;

					Bitmap bitmap = BitmapFactory.decodeFile(path, options);

					// Bitmap bitmap = BitmapFactory.decodeFile(path);
					// Bitmap resized = Bitmap.createScaledBitmap(bitmap, 280, 280, false);

					ByteArrayOutputStream bao = new ByteArrayOutputStream();

					bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bao);


					byte[] ba = bao.toByteArray();
					ba1 = Base64.encodeBytes(ba);

					new UploadToServer(file_id, name).execute();

				} catch (Exception e) {

				}
			} else {
				// If File Not Found Change status to yes
				new SQLiteDatabaseHelper(context).updateSyncStatus(TABLE_CHAT_IMAGES, KEY_MESSAGE_ID, file_id, 1);
			}
		}
	}


	public class UploadToServer
	{

		private String id, name;


		public UploadToServer(String id, String name)
		{
			this.id = id;
			this.name = name;
		}

		public void execute() {

			StringRequest postRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

				@Override
				public void onResponse(String response) {

					try
					{

						Log.v("response", "" + response);

						JSONObject jsonObj = new JSONObject(response);

						String id = jsonObj.getString("id");
						int sync_status = jsonObj.getInt("sync_status");

						new SQLiteDatabaseHelper(context).updateSyncStatus(TABLE_CHAT_IMAGES, KEY_MESSAGE_ID, id, sync_status);

						if (sync_status == 1)
						{
							listener.onTaskCompleted(true, 300, "Uploaded Successfully");
						}

						else
						{
							listener.onTaskCompleted(true, 500, "Failed to Upload");
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

					listener.onTaskCompleted(true, 500, "Failed to Upload");
					Log.v("Error: ", "" + error.getMessage());
				}
			})

			{

				@Override
				protected Map<String, String> getParams() {

					Map<String, String> params = new HashMap<>();

					params.put("base64", ba1);
					params.put("ImageName", name);
					params.put("id", id);

					return params;
				}
			};

			// Adding request to request queue
			MyApplication.getInstance().addToRequestQueue(postRequest);
		}
	}
}