package educing.tech.customer.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import educing.tech.customer.R;


public class OrderConfirmationFragment extends Fragment implements View.OnClickListener
{

    public OrderConfirmationFragment()
    {

    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_order_confirmation, container, false);

        ShippingAddressActivity.ib_order_placed.setBackgroundResource(R.drawable.ib_order_status_completed);
        TextView textView = (TextView) view.findViewById(R.id.order_number);

        textView.setText(String.valueOf("Order Number: " + this.getArguments().getString("ORDER_NO")));

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                try
                {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    // close this activity
                    getActivity().finish();
                }

                catch (Exception e)
                {

                }

            }

        }, 3000);

        return view;
    }


    @Override
    public void onDetach()
    {
        super.onDetach();
    }


    @Override
    public void onClick(View view)
    {
        getActivity().finish();
    }
}
