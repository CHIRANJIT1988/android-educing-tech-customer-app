package educing.tech.customer;

import static educing.tech.customer.CommonUtilities.SENDER_ID;
import static educing.tech.customer.CommonUtilities.displayMessage;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gcm.GCMBaseIntentService;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

import educing.tech.customer.activities.AdvertisementActivity;
import educing.tech.customer.activities.RegisterFragment;
import educing.tech.customer.activities.SplashScreenActivity;
import educing.tech.customer.configuration.Configuration;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.model.Advertisement;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.session.SessionManager;
import educing.tech.customer.sqlite.SQLiteDatabaseHelper;


public class GCMIntentService extends GCMBaseIntentService
{

	private static final String TAG = "GCMIntentService";

	
    public GCMIntentService() 
    {
        super(SENDER_ID);
    }

    
        
    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) 
    {
    
    	Log.i(TAG, "Device registered: regId = " + registrationId);
        displayMessage(context, "Your device registered with GCM");

        ServerUtilities.register(context, RegisterFragment.user, registrationId);
    }

    
    
    /**
     * Method called on device un registered
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) 
    {
    
    	Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered));
        ServerUtilities.unregister(context, registrationId);
    }

    
    
    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) 
    {
    
    	Log.i(TAG, "Received message");
        String message = intent.getExtras().getString("price");
        
        displayMessage(context, message);

        // notifies user
        generateNotification(context, message);
    }

   
    
    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total)
    {
    
    	Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        
        displayMessage(context, message);

        // notifies user
        generateNotification(context, message);
    }

    
    
    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) 
    {
        Log.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    
    
    @Override
    protected boolean onRecoverableError(Context context, String errorId) 
    {
    
    	// log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error, errorId));
        
        return super.onRecoverableError(context, errorId);
    }

    
    
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private void generateNotification(Context context, String message)
    {

        if(message == null)
        {
            return;
        }


        try
        {

            SQLiteDatabaseHelper helper = new SQLiteDatabaseHelper(context);

            Log.v("message: ", String.valueOf(message));

            JSONObject jsonObj = new JSONObject(message);

            String message_type = jsonObj.getString("message_type");


            if(message_type.equals("chat_message"))
            {

                String message_id = jsonObj.getString("message_id");
                String sender_id = jsonObj.getString("sender_id");
                String sender_name = jsonObj.getString("sender_name");
                String chat_message = jsonObj.getString("message");
                String chat_image = jsonObj.getString("image");
                String timestamp = jsonObj.getString("timestamp");


                if(!helper.insertChatUser(new ChatMessage(sender_id, sender_name, timestamp)))
                {
                    helper.updateChatUser(new ChatMessage(sender_id, sender_name, timestamp));
                }

                boolean inserted = helper.insertChatMessage(new ChatMessage(message_id, sender_id, chat_message, chat_image, timestamp, 0, 0), 1);

                if(inserted && !sender_id.equals(Configuration.active_chat_user))
                {

                    SessionManager session = new SessionManager(context); // Session Manager

                    if(session.checkLogin())
                    {
                        notify_user(context, "New Message Received", chat_message);
                    }
                }
            }

            else if(message_type.equals("advertisement"))
            {

                int store_id = jsonObj.getInt("store_id");
                String store_name = jsonObj.getString("store_name");
                double store_rating = jsonObj.getDouble("rating");
                int category_id = jsonObj.getInt("category_id");
                String deal_message = jsonObj.getString("message");
                String file_name = jsonObj.getString("file_name");
                String timestamp = jsonObj.getString("timestamp");


                Advertisement advertisement = new  Advertisement(store_id, store_name, store_rating, category_id, deal_message, file_name, timestamp);
                boolean inserted = helper.insertAdvertisement(advertisement);

                if (inserted)
                {

                    SessionManager session = new SessionManager(context); // Session Manager

                    if(session.checkLogin())
                    {

                        if(!file_name.equals(""))
                        {
                            remote_notification(deal_message, store_name, file_name);
                        }

                        else
                        {
                            big_notification(context, "New Message !!", deal_message, store_name);
                        }
                    }
                }

            }
        }

        catch (Exception e)
        {

        }
    }


    private static void notify_user(Context context, String title, String message)
    {

        int icon = R.drawable.logo;
        long when = System.currentTimeMillis();


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, SplashScreenActivity.class);

        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        long[] pattern = { 500, 500, 500 };


        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(intent)
                .setContentTitle(title)
                .setWhen(when)
                .setContentText(message);


        // Play default notification sound
        notification.setDefaults(Notification.DEFAULT_SOUND);
        notification.setVibrate(pattern);
        notification.setLights(Color.BLUE, 500, 500);
        notification.setStyle(new NotificationCompat.InboxStyle());
        notificationManager.notify(0, notification.build());
    }


    private static void big_notification(Context context, String title, String message, String store_name)
    {

        int icon = R.drawable.logo;
        long when = System.currentTimeMillis();

        Bitmap large_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, AdvertisementActivity.class);

        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 1, notificationIntent, 0);


        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setLargeIcon(large_icon)
                .setAutoCancel(true)
                .setContentIntent(intent)
                .setContentTitle(title)
                .setWhen(when)
                .setContentText(message);


        //Play default notification sound
        //notification.setDefaults(Notification.DEFAULT_SOUND);
        //notification.setVibrate(pattern);
        //notification.setLights(Color.BLUE, 500, 500);
        //notification.setStyle(new NotificationCompat.InboxStyle());
        //notificationManager.notify(0, notification.build());


        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(message);
        bigText.setBigContentTitle(title);
        bigText.setSummaryText("Message from " + Helper.toCamelCase(store_name));
        notification.setStyle(bigText);

        notificationManager.notify(1, notification.build());
    }


    private void remote_notification(String message, String store_name, String file_name)
    {

        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_advertisement_notification_style);

        try
        {

            String time = new SimpleDateFormat("hh:mm a").format(System.currentTimeMillis());

            remoteViews.setImageViewResource(R.id.notification_image, R.mipmap.ic_launcher);

            remoteViews.setTextViewText(R.id.notification_title, "Message from " + Helper.toCamelCase(store_name));
            remoteViews.setTextViewText(R.id.notification_text, message);
            remoteViews.setTextViewText(R.id.notification_time, time);
        }

        catch (Exception e)
        {

        }


        //remoteViews.setTextColor(R.id.notification_title, ContextCompat.getColor(this, android.R.color.black));
        //remoteViews.setTextColor(R.id.notification_text, ContextCompat.getColor(this, android.R.color.black));


        // build notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setContentTitle("Content Title")
                //.setContentText("Content Text")
                .setContent(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_MAX);


        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, AdvertisementActivity.class);

        // The stack builder object will contain an artificial back stack for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(SplashScreenActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);


        final Notification notification = mBuilder.build();

        // set big content view for newer androids
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification.bigContentView = remoteViews;
        }


        Picasso
                .with(this)
                .load(Configuration.ADVERTISEMENT_URL + file_name)
                .into(remoteViews, R.id.notification_image, 1, notification);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }
}