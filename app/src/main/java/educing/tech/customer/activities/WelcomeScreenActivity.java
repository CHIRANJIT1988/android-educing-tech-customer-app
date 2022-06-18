package educing.tech.customer.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import educing.tech.customer.R;
import educing.tech.customer.configuration.Configuration;


public class WelcomeScreenActivity extends AppCompatActivity {

    private ViewPager viewPager;
    SharedPreferences prefs = null;

    private static final String[] IMAGE_NAME = { "welcome_first", "welcome_second", "welcome_third" };

    private int currentPage;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        // Code for image swipe slider
        ImageFragmentPagerAdapter imageFragmentPagerAdapter = new ImageFragmentPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(imageFragmentPagerAdapter);

        prefs = getSharedPreferences(Configuration.SHARED_PREF, MODE_PRIVATE);
        autoSlideImages();
    }


    public static class ImageFragmentPagerAdapter extends FragmentPagerAdapter
    {

        public ImageFragmentPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }


        @Override
        public int getCount()
        {
            return IMAGE_NAME.length;
        }


        @Override
        public Fragment getItem(int position)
        {
            return SwipeFragment.newInstance(position);
        }
    }


    public static class SwipeFragment extends Fragment
    {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {

            View swipeView = inflater.inflate(R.layout.swipe_fragment, container, false);
            ImageView imageView = (ImageView) swipeView.findViewById(R.id.imageView);
            Bundle bundle = getArguments();

            int position = bundle.getInt("position");

            String imageFileName = IMAGE_NAME[position];
            int imgResId = getResources().getIdentifier(imageFileName, "drawable", "educing.tech.customer");
            imageView.setImageResource(imgResId);

            return swipeView;
        }


        static SwipeFragment newInstance(int position)
        {

            SwipeFragment swipeFragment = new SwipeFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            swipeFragment.setArguments(bundle);

            return swipeFragment;
        }
    }


    private void autoSlideImages()
    {

        final Handler handler = new Handler();

        final Runnable Update = new Runnable()
        {

            public void run()
            {

                if (currentPage == IMAGE_NAME.length)
                {
                    startActivity(new Intent(WelcomeScreenActivity.this, SplashScreenActivity.class));
                    finish();
                }

                viewPager.setCurrentItem(currentPage++, true);
            }
        };


        Timer swipeTimer = new Timer();


        swipeTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                handler.post(Update);
            }
        }, 2000, 3000);
    }


    @Override
    public void onBackPressed()
    {

    }
}
