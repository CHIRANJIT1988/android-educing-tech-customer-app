package educing.tech.customer.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import educing.tech.customer.helper.TouchImageView;


public class PinchZoomActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        //TouchImageView img = new TouchImageView(this);
        //img.setImageResource(R.drawable.zoom);
        //img.setMaxZoom(4f);
        //setContentView(img);

        previewCapturedImage(getIntent().getStringExtra("URL"));
    }


    private void previewCapturedImage(final String path)
    {

        try
        {

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 4;

            final Bitmap bitmap = BitmapFactory.decodeFile(path, options);


            TouchImageView img = new TouchImageView(this);
            img.setImageBitmap(bitmap);
            img.setMaxZoom(4f);
            setContentView(img);
        }

        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }
}