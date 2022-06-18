package educing.tech.customer.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import educing.tech.customer.R;


public class ShippingAddressActivity extends AppCompatActivity implements View.OnClickListener
{

    private int back_pressed = 0;
    public static ImageButton ib_shipping_address, ib_payment_details, ib_order_placed;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_address);


        //final Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        //setSupportActionBar(toolbar);

        //assert getSupportActionBar() != null;
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ib_shipping_address = (ImageButton) findViewById(R.id.ib_shipping_address);
        ib_payment_details = (ImageButton) findViewById(R.id.ib_payment_details);
        ib_order_placed = (ImageButton) findViewById(R.id.ib_order_placed);


        ib_shipping_address.setOnClickListener(this);
        redirectShippingAddressFragment();
    }


    @Override
    public void onResume()
    {

        super.onResume();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {

            case android.R.id.home:
            {
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void redirectShippingAddressFragment()
    {

        Fragment fragment = new ShippingAddressFragment(this, R.color.home_background);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim);
        fragmentTransaction.replace(R.id.container_body, fragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onClick(View view)
    {

        switch (view.getId())
        {

            case R.id.ib_shipping_address:

                redirectShippingAddressFragment();
                break;
        }
    }


    @Override
    public void onBackPressed()
    {

        if(back_pressed == 0)
        {
            back_pressed++;
            Toast.makeText(getApplicationContext(), "Press Back Button again to Back", Toast.LENGTH_LONG).show();
        }

        else
        {
            finish();
        }
    }
}