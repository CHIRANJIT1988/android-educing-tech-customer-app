package educing.tech.customer.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import educing.tech.customer.R;
import educing.tech.customer.adapter.OrderDetailsRecyclerAdapter;
import educing.tech.customer.db.mysql.ReceiveOrderDetails;
import educing.tech.customer.helper.OnTaskCompleted;


public class OrderDetailsFragment extends Fragment implements OnTaskCompleted
{

	private int color;

	private Context context = null;
	private RecyclerView recyclerView;
	private ProgressBar pbLoading;



	public OrderDetailsFragment()
	{

	}


	@SuppressLint("ValidFragment")
	public OrderDetailsFragment(Context context, int color)
	{
		this.color = color;
		this.context = context;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View view = inflater.inflate(R.layout.fragment_order_details, container, false);


		final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.dummyfrag_bg);
		frameLayout.setBackgroundColor(color);


		recyclerView = (RecyclerView) view.findViewById(R.id.dummyfrag_scrollableview);
		pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
		recyclerView.setLayoutManager(linearLayoutManager);
		recyclerView.setHasFixedSize(true);

		new ReceiveOrderDetails(context, this).fetchOrderDetails(this.getArguments().getString("ORDER_NO"));
		return view;
	}


	@Override
	public void onTaskCompleted(boolean flag, int code, String message)
	{

		try
		{

			pbLoading.setVisibility(View.GONE);

			if (flag && message.equals("success"))
			{
				OrderDetailsRecyclerAdapter adapter = new OrderDetailsRecyclerAdapter(context, this.getArguments().getFloat("DELIVERY_CHARGE"));
				recyclerView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}

			if (!flag)
			{
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		}

		catch (Exception e)
		{

		}
	}
}