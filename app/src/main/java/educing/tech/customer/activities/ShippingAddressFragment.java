package educing.tech.customer.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;

import educing.tech.customer.R;
import educing.tech.customer.adapter.ShippingAddressRecyclerAdapter;
import educing.tech.customer.alert.CustomAlertDialog;
import educing.tech.customer.db.mysql.ReceiveShippingAddress;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.ShippingAddress;
import educing.tech.customer.model.User;
import educing.tech.customer.network.InternetConnectionDetector;
import educing.tech.customer.session.SessionManager;


public class ShippingAddressFragment extends Fragment implements OnTaskCompleted
{

    ShippingAddressRecyclerAdapter adapter;
    Context context;
    private int color;

    private ProgressBar pbLoading;
    private RecyclerView recyclerView;


    public ShippingAddressFragment()
    {

    }


    @SuppressLint("ValidFragment")
    public ShippingAddressFragment(Context context, int color)
    {
        this.color = color;
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_shipping_address, container, false);

        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);
        recyclerView = (RecyclerView) view.findViewById(R.id.dummyfrag_scrollableview);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);



        if (!new InternetConnectionDetector(context).isConnected())
        {
            pbLoading.setVisibility(View.GONE);
            new CustomAlertDialog(context, this).showOKDialog("Network Error", "Internet Connection Failure", "network");
            return view;
        }


        if(ShippingAddress.shippingAddressList.size() == 0)
        {
            new ReceiveShippingAddress(context, this).fetchAddress(getUserDetails().getUserID());
        }

        else
        {
            pbLoading.setVisibility(View.GONE);
        }


        adapter = new ShippingAddressRecyclerAdapter(context, ShippingAddressFragment.this);
        recyclerView.setAdapter(adapter);

        adapter.SetOnItemClickListener(new ShippingAddressRecyclerAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                initShippingAddress(ShippingAddress.shippingAddressList.get(position-1));
            }
        });

        return view;
    }


    /** Called when the activity is about to become visible. */
    @Override
    public void onStart()
    {

        super.onStart();
        Log.d("Inside : ", "onCreate() event");
    }



    /** Called when the activity has become visible. */
    @Override
    public void onResume()
    {

        super.onResume();
        Log.d("Inside : ", "onResume() event");
    }



    /** Called when another activity is taking focus. */
    @Override
    public void onPause()
    {

        super.onPause();
        Log.d("Inside : ", "onPause() event");
    }



    /** Called when the activity is no longer visible. */
    @Override
    public void onStop()
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


    private void initShippingAddress(ShippingAddress address)
    {

        Bundle args = new Bundle();
        args.putSerializable("SHIPPING_ADDRESS_OBJ", address);


        Fragment fragment = new OrderSummeryFragment(getActivity(), R.color.home_background);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim);
        fragmentTransaction.replace(R.id.container_body, fragment);
        fragmentTransaction.commit();

        ShippingAddressActivity.ib_shipping_address.setBackgroundResource(R.drawable.ib_order_status_completed);
    }


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {

        try
        {

            pbLoading.setVisibility(View.GONE);

            if (flag && code == 200)
            {
                adapter.notifyDataSetChanged();
                return;
            }

            if (flag && code == 300)
            {

                Fragment fragment = new NewAddressFragment(context, R.color.home_background);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim);
                fragmentTransaction.replace(R.id.container_body, fragment);
                fragmentTransaction.commit();

                return;
            }

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

        catch (Exception e)
        {

        }
    }


    private User getUserDetails()
    {

        SessionManager session = new SessionManager(context);

        User userObj = new User();

        if (session.checkLogin())
        {
            HashMap<String, String> user = session.getUserDetails();

            userObj.setUserID(Integer.valueOf(user.get(SessionManager.KEY_USER_ID)));
            userObj.setPhoneNo(user.get(SessionManager.KEY_PHONE));
            userObj.setUserName(user.get(SessionManager.KEY_USER_NAME));
        }

        return userObj;
    }
}