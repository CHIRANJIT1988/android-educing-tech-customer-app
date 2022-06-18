package educing.tech.customer.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;


public class AlarmService extends Service
{

	private static final String TAG = "Alarm Service";
	
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
	
	
	@Override
    public void onCreate() 
    {
    	
		Log.d(TAG, "onCreate");
		setSyncAlarm(10);
    }
	
	
	@Override
    public void onDestroy() 
    {

    	Log.d(TAG, "onDestroy");
    }

	
    @Override
    public void onStart(Intent intent, int startid) {

		Log.d(TAG, "onStart");
    }
    
    
    private void setSyncAlarm(int interval)
	{
    	
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
    
    
		Intent myIntent = new Intent(this, AlarmReceiver.class);
		myIntent.putExtra("alarm", 1);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, myIntent,  PendingIntent.FLAG_CANCEL_CURRENT);
 
	
		AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
	
		// Repeating the alarm every 1 min interval
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * interval, pendingIntent);
	}
}