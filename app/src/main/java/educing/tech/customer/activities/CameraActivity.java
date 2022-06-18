package educing.tech.customer.activities;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import educing.tech.customer.R;
import educing.tech.customer.helper.GenerateUniqueId;

import static educing.tech.customer.configuration.Configuration.MEDIA_DIRECTORY_NAME;


public class CameraActivity extends Activity
{
	
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static String IMAGE_NAME = "";
	
	private Uri fileUri; // file url to store image/video


	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
        Log.d("Inside : ", "onCreate() event");

		IMAGE_NAME = GenerateUniqueId.generateRandomString();
	}
	
	
	
	/** Called when the activity is about to become visible. */   
	@Override
	protected void onStart()
	{
		
		super.onStart();
		Log.d("Inside : ", "onStart() event");
        
		initActivity();
	}
	
	
	
	/** Called when the activity has become visible. */  
	@Override
	protected void onResume() 
	{    
		super.onResume();
		Log.d("Inside : ", "onResume() event");
	}

	
	
	/** Called when another activity is taking focus. */
	@Override
	protected void onPause() 
	{  
		super.onPause();
		Log.d("Inside : ", "onPause() event");
	}

	
	
	/** Called when the activity is no longer visible. */
	@Override
	protected void onStop() 
	{      
		super.onStop();
		Log.d("Inside : ", "onStop() event");
	}
	
	
	
	/** Called just before the activity is destroyed. */
	@Override
	public void onDestroy() 
	{
	      super.onDestroy();
	      Log.d("Inside : ", "onDestroy() event");
	}
	
	
	
	private void initActivity()
	{

		// Checking camera availability
		if (!isDeviceSupportCamera())
		{
			Toast.makeText(getApplicationContext(), "Sorry! Your device doesn't support camera", Toast.LENGTH_LONG).show();
			
			// will close the app if the device does't have camera
			finish();
		}
		
		else
		{
			captureImage();
		}
	}
	
	
	
	/**
	 * Checking device has camera hardware or not
	 * */
	private boolean isDeviceSupportCamera() 
	{
		
		if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
		{
			// this device has a camera
			return true;
		} 
		
		else 
		{
			// no camera on this device
			return false;
		}
	}
	
	
	
	// Capturing Camera Image will launch camera app request image capture
	private void captureImage() 
	{
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		// start the image capture Intent
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
		
		setResult(RESULT_OK, intent);
		
	}
	
	
	
	// Here we store the file url as it will be null after returning from camera app
	 
	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{
	
		super.onSaveInstanceState(outState);

		// save file url in bundle as it will be null on screen orientation changes
		outState.putParcelable("file_uri", fileUri);
	}


	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) 
	{
	
		super.onRestoreInstanceState(savedInstanceState);

		// get the file url
		fileUri = savedInstanceState.getParcelable("file_uri");
	}
	
	
	/**
	 * Receiving activity result method will be called after closing the camera
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	
		if (resultCode == RESULT_OK) 
		{

			Intent intent = new Intent();
			intent.putExtra("PATH", String.valueOf(fileUri.getPath()));
			intent.putExtra("IMAGE_NAME", IMAGE_NAME);

			setResult(1, intent);

			finish(); //finishing activity
		} 
			
		else if (resultCode == RESULT_CANCELED) 
		{
			
			Intent intent=new Intent();
			setResult(0, intent);
        
			finish(); //finishing activity
		} 
			
		else 
		{
			
			Intent intent=new Intent();
			setResult(0, intent);
        
			finish(); //finishing activity
		}
	}
	
	
	// Creating file uri to store image/video
	public Uri getOutputMediaFileUri(int type) 
	{
		return Uri.fromFile(getOutputMediaFile(type));
	}

	
	// returning image / video
	private static File getOutputMediaFile(int type) 
	{

		// External sdcard location
		File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), MEDIA_DIRECTORY_NAME);

		
		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) 
		{
			if (!mediaStorageDir.mkdirs()) 
			{
				Log.d(MEDIA_DIRECTORY_NAME, "Oops! Failed create " + MEDIA_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		// Create a media file name
		// String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		
		File mediaFile;
		
		if (type == MEDIA_TYPE_IMAGE) 
		{
			IMAGE_NAME = "IMG_" + IMAGE_NAME + ".jpg";
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + IMAGE_NAME);
		}
		
		else 
		{
			return null;
		}

		return mediaFile;
	}
}