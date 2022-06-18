package educing.tech.customer.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;

import educing.tech.customer.R;
import educing.tech.customer.adapter.OrdersRecyclerAdapter;
import educing.tech.customer.alert.CustomAlertDialog;
import educing.tech.customer.db.mysql.ReceiveOrders;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Order;
import educing.tech.customer.model.User;
import educing.tech.customer.network.InternetConnectionDetector;
import educing.tech.customer.session.SessionManager;


public class OrdersFragment extends Fragment implements OnTaskCompleted
{

	private Context context = null;
	private ProgressBar pbLoading;
	private SessionManager session;
	private OrdersRecyclerAdapter adapter;

	public OrdersFragment()
	{

	}


	@SuppressLint("ValidFragment")
	public OrdersFragment(Context context)
	{
		this.context = context;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View view = inflater.inflate(R.layout.fragment_orders, container, false);

		pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);

		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dummyfrag_scrollableview);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
		recyclerView.setLayoutManager(linearLayoutManager);
		recyclerView.setHasFixedSize(true);

		this.session = new SessionManager(context);


		if (!new InternetConnectionDetector(context).isConnected())
		{
			pbLoading.setVisibility(View.GONE);
			new CustomAlertDialog(context, this).showOKDialog("Network Error", "Internet Connection Failure", "network");
			return view;
		}

		else
		{
			new ReceiveOrders(context, this).fetchOrders(getUserDetails().getUserID());
		}


		adapter = new OrdersRecyclerAdapter(context, Order.myOrderList);
		recyclerView.setAdapter(adapter);


		adapter.SetOnItemClickListener(new OrdersRecyclerAdapter.OnItemClickListener() {

			@Override
			public void onItemClick(View view, int position) {
				Order order = Order.myOrderList.get(position);
				redirectOrderDetailsFragment(order.getOrderNo(), order.delivery_charge);
			}
		});


		return view;
	}


	private User getUserDetails()
	{

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


	private void redirectOrderDetailsFragment(String order_no, double delivery_charge)
	{

		Bundle args = new Bundle();
		args.putSerializable("ORDER_NO", order_no);
		args.putSerializable("DELIVERY_CHARGE", delivery_charge);

		Fragment fragment = new OrderDetailsFragment(getActivity(), ContextCompat.getColor(context, R.color.home_background));
		fragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		fragmentTransaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim);
		fragmentTransaction.replace(R.id.container_body, fragment);
		fragmentTransaction.commit();

		getActivity().setTitle("Order Summery");
	}


	@Override
	public void onTaskCompleted(boolean flag, int code, String message)
	{

		try
		{

			pbLoading.setVisibility(View.GONE);

			if(flag && code == 199)
			{
				new CustomAlertDialog(getActivity(), this).showOKDialog("Not Found", message, "finish");
				return;
			}

			if(flag && code == 200)
			{
				adapter.notifyDataSetChanged();
				return;
			}

			if(flag && code == 201)
			{
				getActivity().finish();
				return;
			}

			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		}

		catch (Exception e)
		{

		}
	}
}