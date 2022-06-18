package educing.tech.customer.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import educing.tech.customer.R;


public class OrdersActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayView(0);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {

            case android.R.id.home:

                Fragment f = getSupportFragmentManager().findFragmentById(R.id.container_body);

                if (f instanceof OrderDetailsFragment)
                {
                    displayView(0);
                    return true;
                }

                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void displayView(int position) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (position)
        {

            case 0:

                title = "My Orders";
                fragment = new OrdersFragment(this);
                break;

            default:

                break;
        }


        if (fragment != null)
        {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim);
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            setTitle(title);
        }
    }
}