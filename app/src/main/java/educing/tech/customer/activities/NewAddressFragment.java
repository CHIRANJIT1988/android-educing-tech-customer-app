package educing.tech.customer.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import educing.tech.customer.R;
import educing.tech.customer.alert.CustomAlertDialog;
import educing.tech.customer.db.mysql.SaveShippingAddress;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.ShippingAddress;
import educing.tech.customer.model.User;
import educing.tech.customer.session.SessionManager;


public class NewAddressFragment extends Fragment implements View.OnClickListener, OnTaskCompleted
{

	Context context;
	int color;

	private TextView editName;
	private TextView editPhoneNumber;
	private TextView editAddress;
	private TextView editLandmark;
	private TextView editCity;
	private TextView editState;
	private TextView editPincode;


	private ProgressDialog pDialog;
	private Button btnSave;
	private RelativeLayout relativeLayout;
	SessionManager session; // Session Manager Class


	public NewAddressFragment()
	{

	}


	@SuppressLint("ValidFragment")
	public NewAddressFragment(Context context, int color)
	{
		this.color = color;
		this.context = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{

		View rootView = inflater.inflate(R.layout.fragment_new_address, container, false);

		findViewById(rootView);
		btnSave.setOnClickListener(this);

		session = new SessionManager(context); // Session Manager
		pDialog = new ProgressDialog(context);

		return rootView;
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


	private void findViewById(View rootView)
	{
		editName = (TextView) rootView.findViewById(R.id.editName);
		editPhoneNumber = (TextView) rootView.findViewById(R.id.editPhoneNumber);
		editAddress = (TextView) rootView.findViewById(R.id.editAddress);
		editCity = (TextView) rootView.findViewById(R.id.editCity);
		editState = (TextView) rootView.findViewById(R.id.editState);
		editLandmark = (TextView) rootView.findViewById(R.id.editLandmark);
		editPincode = (TextView) rootView.findViewById(R.id.editPincode);

		btnSave = (Button) rootView.findViewById(R.id.btnSaveAddress);
		relativeLayout = (RelativeLayout) rootView.findViewById(R.id.layout_main);
	}


	public void onClick(View v)
	{

		if(validateForm())
		{

			/*if (!new InternetConnectionDetector(context).isConnected())
			{
				makeSnackbar("Internet Connection Failure");
				return;
			}*/

			if (!session.checkLogin())
			{
				startActivity(new Intent(context, MainActivity.class));
				return;
			}

			initProgressDialog();
			new SaveShippingAddress(context, this).saveAddress(initShippingAddress());
		}
	}



	private boolean validateForm()
	{

		if(editName.getText().toString().trim().length() < 3)
		{
			makeSnackbar("Name should be at least 3 character");
			return false;
		}

		if(editPhoneNumber.getText().toString().trim().length() != 10)
		{
			makeSnackbar("Invalid Mobile No");
			return false;
		}

		if(editAddress.getText().toString().trim().length() < 10)
		{
			makeSnackbar("Address should be at least 10 character");
			return false;
		}

		if(editCity.getText().toString().trim().length() == 0)
		{
			makeSnackbar("Enter City");
			return false;
		}

		if(editState.getText().toString().trim().length() == 0)
		{
			makeSnackbar("Enter State");
			return false;
		}

		if(editPincode.getText().toString().trim().length() != 6)
		{
			makeSnackbar("Invalid Pincode");
			return false;
		}

		return true;
	}


	private void makeSnackbar(String msg)
	{

        /*Snackbar.make(relativeLayout, msg, Snackbar.LENGTH_SHORT).setActionTextColor(Color.RED).show();*/

		Snackbar snackbar = Snackbar.make(relativeLayout, msg, Snackbar.LENGTH_SHORT);
		View snackBarView = snackbar.getView();
		snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.myPrimaryColor));
		snackbar.show();
	}


	private void initProgressDialog()
	{

		pDialog.setMessage("Adding New Address ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
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


	private ShippingAddress initShippingAddress()
	{

		ShippingAddress address = new ShippingAddress();

		address.setUserId(getUserDetails().getUserID());
		address.setName(editName.getText().toString());
		address.setPhoneNo(editPhoneNumber.getText().toString());
		address.setAddress(editAddress.getText().toString());
		address.setCity(editCity.getText().toString());
		address.setState(editState.getText().toString());
		address.setLandmark(editLandmark.getText().toString());
		address.setPincode(editPincode.getText().toString());

		return address;
	}


	@Override
	public void onTaskCompleted(boolean flag, int code, String message)
	{

		try
		{

			if(pDialog.isShowing())
			{
				pDialog.dismiss();
			}

			if (flag && code == 200)
			{


				ShippingAddress address = initShippingAddress();
				address.setAddressId(Integer.parseInt(message));
				ShippingAddress.shippingAddressList.add(0, address);

				redirectNextFragment(address);
			}

			else
			{
				new CustomAlertDialog(context, NewAddressFragment.this).showOKDialog("Fail", message);
			}
		}

		catch (Exception e)
		{

		}
	}


	private void redirectNextFragment(ShippingAddress address)
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
}