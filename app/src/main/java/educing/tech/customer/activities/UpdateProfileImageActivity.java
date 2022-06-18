package educing.tech.customer.activities;

import educing.tech.customer.R;
import com.soundcloud.android.crop.Crop;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import static  educing.tech.customer.configuration.Configuration.MEDIA_DIRECTORY_NAME;


public class UpdateProfileImageActivity extends Activity
{

    private ImageView resultView;
    public static final int MEDIA_TYPE_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_update_profile_image);
        resultView = (ImageView) findViewById(R.id.result_image);

        resultView.setImageDrawable(null);
        Crop.pickImage(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_crop_image, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        if (item.getItemId() == R.id.action_select)
        {
            resultView.setImageDrawable(null);
            Crop.pickImage(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result)
    {

        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK)
        {
            beginCrop(result.getData());
        }

        else if (requestCode == Crop.REQUEST_CROP)
        {
            handleCrop(resultCode, result);
        }
    }


    private void beginCrop(Uri source)
    {

        //Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));

        String file_name = "USER_" + getIntent().getStringExtra("FILE_NAME") + ".jpg";
        String file_path = generateFilePath();

        Uri destination = Uri.fromFile(new File(file_path, file_name));
        Crop.of(source, destination).asSquare().start(this);

        Intent intent = new Intent();
        intent.putExtra("PATH", file_path + "/" + file_name);
        intent.putExtra("FILE_NAME", file_name);
        setResult(MEDIA_TYPE_IMAGE, intent);
    }


    private void handleCrop(int resultCode, Intent result)
    {

        if (resultCode == RESULT_OK)
        {
            resultView.setImageURI(Crop.getOutput(result));
            finish();
        }

        else if (resultCode == Crop.RESULT_ERROR)
        {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private String generateFilePath()
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

        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + MEDIA_DIRECTORY_NAME;
    }
}