package educing.tech.customer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import educing.tech.customer.db.mysql.SyncChatMessage;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.network.InternetConnectionDetector;
import educing.tech.customer.session.SessionManager;
import educing.tech.customer.sqlite.SQLiteDatabaseHelper;

import static educing.tech.customer.sqlite.SQLiteDatabaseHelper.TABLE_CHAT_IMAGES;
import static educing.tech.customer.sqlite.SQLiteDatabaseHelper.TABLE_CHAT_MESSAGES;


public class AlarmReceiver extends BroadcastReceiver implements OnTaskCompleted
{
	
	Context context;


	@Override
	public void onReceive(Context context, Intent intent) 
	{
		
		this.context = context;
		SessionManager session = new SessionManager(context); // Session class instance

		if(!session.isLoggedIn())
		{
			return;
		}


		int alarm = intent.getExtras().getInt("alarm");

		if(alarm == 1)
		{

			if(new InternetConnectionDetector(context).isConnected())
			{

				//makeToast("Sync Alarm Received");
				syncData();
			}
		}
	}


	private void makeToast(String msg)
	{
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}


	private void syncData()
	{

		SQLiteDatabaseHelper helper = new SQLiteDatabaseHelper(context);

		if(helper.dbSyncCount(TABLE_CHAT_MESSAGES) != 0)
		{
			new SyncChatMessage(context, this).execute();
		}

		if(helper.dbSyncCount(TABLE_CHAT_IMAGES) != 0)
		{
			new SyncChatMessage(context, this).getAllChatImage();
		}
	}


	@Override
	public void onTaskCompleted(boolean flag, int code, String message)
	{

	}
}