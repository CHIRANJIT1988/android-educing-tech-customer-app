package educing.tech.customer.activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import educing.tech.customer.R;
import educing.tech.customer.helper.Blur;
import educing.tech.customer.network.InternetConnectionDetector;

import static educing.tech.customer.configuration.Configuration.MEDIA_DIRECTORY_NAME;
import static educing.tech.customer.configuration.Configuration.PRODUCT_IMAGE_URL;


public class ImagePreviewActivity extends Activity implements View.OnClickListener
{

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;

    private ImageView image;
    private TextView description, download_percent;
    private Button download;
    private RelativeLayout layout_download_progress;
    private ProgressBar circularProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        image = (ImageView) findViewById(R.id.imgFullScreen);
        description = (TextView) findViewById(R.id.description);
        download = (Button) findViewById(R.id.download);
        layout_download_progress = (RelativeLayout) findViewById(R.id.layout_download_progress);
        circularProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        download_percent = (TextView) findViewById(R.id.download_percent);

        download.setOnClickListener(this);


        description.setText(getIntent().getStringExtra("DESC"));


        Transformation blurTransformation = new Transformation() {

            @Override
            public Bitmap transform(Bitmap source)
            {
                Bitmap blurred = Blur.fastblur(ImagePreviewActivity.this, source, 10);
                source.recycle();
                return blurred;
            }

            @Override
            public String key()
            {
                return "blur()";
            }
        };


        Picasso.with(ImagePreviewActivity.this)
                .load(PRODUCT_IMAGE_URL + getIntent().getStringExtra("URL")) // thumbnail url goes here
                .transform(blurTransformation)
                .into(image, new Callback() {

                    @Override
                    public void onSuccess()
                    {

                        Picasso.with(ImagePreviewActivity.this)
                                .load(PRODUCT_IMAGE_URL + getIntent().getStringExtra("URL")) // image url goes here
                                .into(image);
                    }

                    @Override
                    public void onError()
                    {
                        Toast.makeText(getApplicationContext(), "Failed to load Image", Toast.LENGTH_LONG).show();
                    }
                });
    }


    @Override
    public void onClick(View view)
    {

        if(new InternetConnectionDetector(this).isConnected())
        {

            if(permissionCheckerStorage())
            {

                myAsyncTask myWebFetch = new myAsyncTask();
                myWebFetch.execute(PRODUCT_IMAGE_URL + getIntent().getStringExtra("URL"), getIntent().getStringExtra("URL"));
            }
        }

        else
        {
            Toast.makeText(getApplicationContext(), "Internet Connection Failure", Toast.LENGTH_LONG).show();
        }
    }


    class myAsyncTask extends AsyncTask<String, String, String>
    {

        @Override
        protected void onPostExecute(String result)
        {
            layout_download_progress.setVisibility(View.GONE);
            super.onPostExecute(result);
        }


        @Override
        protected void onPreExecute()
        {
            layout_download_progress.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... progress)
        {
            circularProgressBar.setProgress(Integer.parseInt(progress[0]));
            download_percent.setText(String.valueOf(progress[0] + "%"));
        }


        protected String doInBackground(String... args)
        {

            try
            {

                //set the download URL, a url that points to a file on the internet
                //this is the file to be downloaded
                URL url = new URL(args[0]);

                //create the new connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                //set up some things on the connection
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);

                //and connect!
                urlConnection.connect();

                //set the path where we want to save the file
                //in this case, going to save it on the root directory of the
                //sd card.
                //File SDCardRoot = Environment.getExternalStorageDirectory();

                // External sdcard location
                File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), MEDIA_DIRECTORY_NAME);
                // File noMedia = new File ( Environment.getExternalStoragePublicDirectory(DIRECTORY_NAME), ".nomedia" );

                // Create the storage directory if it does not exist
                if (!mediaStorageDir.exists())
                {

                    if (!mediaStorageDir.mkdirs())
                    {
                        //Log.d(DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                        return null;
                    }

                        /*if (!noMedia.exists())
                        {
                            FileOutputStream noMediaOutStream = new FileOutputStream ( noMedia );
                            noMediaOutStream.write ( 0 );
                            noMediaOutStream.close();
                        }*/
                }

                //create a new file, specifying the path, and the filename
                //which we want to save the file as.
                File file = new File(mediaStorageDir, args[1]);

                //this will be used to write the downloaded data into the file we created
                FileOutputStream fileOutput = new FileOutputStream(file);

                //this will be used in reading the data from the internet
                InputStream inputStream = urlConnection.getInputStream();

                //this is the total size of the file
                int totalSize = urlConnection.getContentLength();

                //variable to store total downloaded bytes
                int downloadedSize = 0;

                //create a buffer...
                byte[] buffer = new byte[1024];
                int bufferLength = 0; //used to store a temporary size of the buffer

                //now, read through the input buffer and write the contents to the file
                while ( (bufferLength = inputStream.read(buffer)) > 0 )
                {

                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);

                    //add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;

                    //this is where you would do something to report the prgress, like this maybe
                    //updateProgress(downloadedSize, totalSize);

                    // After this onProgressUpdate will be called
                    publishProgress("" + ((downloadedSize * 100) / totalSize));
                }

                //close the output stream when done
                fileOutput.close();

                //catch some possible errors...
            }

            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }


    private void requestPermissionStorage(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            makeToast("Storage permission allows us to read or write data onto memory. Please allow in App Settings for read or write data.");
        }

        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }


    private boolean permissionCheckerStorage() {

        if (!checkPermissionStorage())
        {
            requestPermissionStorage();
            return false;
        }

        return true;
    }


    private boolean checkPermissionStorage()
    {

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }

        else
        {
            return false;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {

        switch (requestCode)
        {

            case STORAGE_PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeToast("Permission Granted");
                } else {
                    makeToast("Permission Denied");
                }

                break;
        }
    }


    private void makeToast(String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}