package educing.tech.customer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class BootCompletedReceiver extends BroadcastReceiver
{

	Context context;


	@Override
	public void onReceive(Context context, Intent intent) 
	{

		this.context = context;

		// start alarm service
		Intent service = new Intent(context, AlarmService.class);
		context.startService(service);

		makeToast("Boot Completed Receiver!");
	}


	private void makeToast(String msg)
	{
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}


	/*private void setSyncAlarm(int interval)
	{

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());


		Intent myIntent = new Intent(context, AlarmReceiver.class);
		myIntent.putExtra("alarm", 1);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, myIntent,  PendingIntent.FLAG_CANCEL_CURRENT);


		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// Repeating the alarm every 1 min interval
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * interval, pendingIntent);
	}*/
}
