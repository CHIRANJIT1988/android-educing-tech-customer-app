package educing.tech.customer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class PackageInstallationReceiver extends BroadcastReceiver
{

	Context context;


	@Override
	public void onReceive(Context context, Intent intent) 
	{

		this.context = context;

		/*Intent service = new Intent(context, AlarmService.class);
		context.startService(service);*/

		makeToast("Package Installation Receiver!");
	}


	private void makeToast(String msg)
	{
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
}
