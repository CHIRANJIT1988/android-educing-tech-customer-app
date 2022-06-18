package educing.tech.customer.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import educing.tech.customer.R;
import educing.tech.customer.adapter.ChatMessageRecyclerAdapter;
import educing.tech.customer.configuration.Configuration;
import educing.tech.customer.db.mysql.SendImageToServer;
import educing.tech.customer.db.mysql.SyncChatMessage;
import educing.tech.customer.helper.GenerateUniqueId;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.model.User;
import educing.tech.customer.session.SessionManager;
import educing.tech.customer.sqlite.SQLiteDatabaseHelper;

import static educing.tech.customer.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static educing.tech.customer.CommonUtilities.EXTRA_MESSAGE;


public class ChatWindowActivity extends AppCompatActivity implements OnTaskCompleted, View.OnClickListener
{

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;

    private boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int pageCount = 1;

    private EditText chatText;
    private RecyclerView recyclerView;
    private ChatMessageRecyclerAdapter adapter;
    private LinearLayoutManager mLayoutManager;
    private List<ChatMessage> chatMessageList = new ArrayList<>();

    private SessionManager session; // Session Manager Class
    private ChatMessage message;

    private SQLiteDatabaseHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        setSupportActionBar(toolbar);

        message = (ChatMessage) getIntent().getSerializableExtra("USER");
        setTitle(Helper.toCamelCase(message.getUserName()));

        Configuration.active_chat_user = message.user_id;


        helper = new SQLiteDatabaseHelper(this);


        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        session = new SessionManager(ChatWindowActivity.this); // Session Manager


        chatMessageList = helper.getAllChatMessage(message.getUserId(), 0, 20);
        helper.setAsRead(message.user_id);


        ImageButton buttonSend = (ImageButton) findViewById(R.id.ibSend);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(mLayoutManager);


        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));


        if (adapter == null)
        {

            adapter = new ChatMessageRecyclerAdapter(getApplicationContext(), this, chatMessageList);
            recyclerView.setAdapter(adapter);
            recyclerView.scrollToPosition(0);


            adapter.SetOnItemClickListener(new ChatMessageRecyclerAdapter.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position)
                {

                }
            });
        }

        loadMoreOnScroll();

        chatText = (EditText) findViewById(R.id.editChat);
        chatText.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {


                    if (!chatText.getText().toString().isEmpty()) {
                        ChatMessage chat = new ChatMessage(GenerateUniqueId.generateMessageId(getUserDetails().getPhoneNo()), message.getUserId(), chatText.getText().toString(), "", String.valueOf(System.currentTimeMillis()), 1, 1);
                        return sendChatMessage(chat);
                    }
                }

                return false;
            }
        });


        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (!chatText.getText().toString().isEmpty()) {
                    ChatMessage chat = new ChatMessage(GenerateUniqueId.generateMessageId(getUserDetails().getPhoneNo()), message.getUserId(), chatText.getText().toString(), "", String.valueOf(System.currentTimeMillis()), 1, 1);
                    sendChatMessage(chat);
                }
            }
        });


        permissionCheckerStorage();

        /*listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {

            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });*/


        // Instantiate NotificationManager Class
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Cancel notification
        mNotificationManager.cancel(0);
    }


    private void loadMoreOnScroll()
    {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                loading = true;

                int totalRecord = new SQLiteDatabaseHelper(getApplicationContext()).dbRowCount(SQLiteDatabaseHelper.TABLE_CHAT_MESSAGES);
                int totalPage;

                if (totalRecord % 10 == 0) {
                    totalPage = (totalRecord / 20);
                } else {
                    totalPage = (totalRecord / 20) + 1;
                }

                if (pageCount < totalPage) {

                    if (dy > 0) //check for scroll down
                    {

                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                        if (loading) {

                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {

                                loading = false;
                                Log.v("onScrolled: ", "Last Item Wow ! " + pageCount);

                                List<ChatMessage> tempList = new SQLiteDatabaseHelper(getApplicationContext()).getAllChatMessage(message.user_id, pageCount * 20, 20);


                                for (ChatMessage message: tempList) {
                                    chatMessageList.add(chatMessageList.size(), message);
                                }

                                adapter.notifyDataSetChanged();

                                recyclerView.scrollToPosition(totalItemCount - 1);
                                pageCount++;
                            }
                        }
                    }
                }
            }
        });
    }


    @Override
    public void onClick(View view)
    {

        switch (view.getId())
        {

            case R.id.ibCamera:

                if(permissionCheckerCamera())
                {

                    if(permissionCheckerStorage())
                    {
                        Intent intent = new Intent(ChatWindowActivity.this, CameraActivity.class);
                        startActivityForResult(intent, 1);
                    }
                }

                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if(requestCode == 1 && resultCode == 1)
        {

            String name = data.getStringExtra("IMAGE_NAME");
            String path = data.getStringExtra("PATH");

            String message_id = GenerateUniqueId.generateMessageId(getUserDetails().getPhoneNo());

            ChatMessage chat = new ChatMessage(message_id, message.getUserId(), chatText.getText().toString(), name, String.valueOf(System.currentTimeMillis()), 1, 1);
            sendChatMessage(chat);

            helper.insertChatImages(message_id, path);

            new SendImageToServer(getApplicationContext(), this, message_id, path, name).upload();
            Log.v("path: ", path);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_window, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {

            case android.R.id.home:

                finish();
                return true;

            case R.id.action_clear:

                helper.clearChatMessages(message.getUserId());
                chatMessageList.clear();
                adapter.notifyDataSetChanged();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean sendChatMessage(ChatMessage chatMessage) {

        chatMessageList.add(0, chatMessage);

        recyclerView.scrollToPosition(0);
        chatText.setText("");

        adapter.notifyDataSetChanged();


        ChatMessage chatUserObj = new ChatMessage(message.getUserId(), message.getUserName(), String.valueOf(System.currentTimeMillis()));

        if(!helper.insertChatUser(chatUserObj))
        {
            helper.updateChatUser(chatUserObj);
        }


        helper.insertChatMessage(chatMessage, 0);
        new SyncChatMessage(getApplicationContext(), this).execute();

        return true;
    }


    @Override
    protected void onDestroy()
    {

        Configuration.active_chat_user = "00";

        try
        {
            unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(this);
        }

        catch (Exception e)
        {
            Log.e("UnRegister Error", "> " + e.getMessage());
        }

        super.onDestroy();
    }


    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {


                String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);


                /**
                 * Take appropriate action on this message
                 * depending upon your app requirement
                 * For now i am just displaying it on the screen
                 * */


                // Showing received message
                // lblMessage.append(newMessage + "\n");


                if(newMessage == null)
                {
                    return;
                }


                Log.v("message: ", newMessage);


                JSONObject jsonObj = new JSONObject(newMessage);

                String message_id = jsonObj.getString("message_id");
                String sender_id = jsonObj.getString("sender_id");
                String sender_name = jsonObj.getString("sender_name");
                String chat_message = jsonObj.getString("message");
                String chat_image = jsonObj.getString("image");
                String timestamp = jsonObj.getString("timestamp");


                if(sender_id.equals(message.user_id))
                {

                    helper.setAsRead(message.user_id);

                    ChatMessage chatMessage = new ChatMessage(message_id, sender_id, chat_message, chat_image, timestamp, 0, 0);
                    sendChatMessage(chatMessage);


                    adapter.notifyDataSetChanged();

                    // Waking up mobile if it is sleeping
                    // WakeLocker.acquire(context);

                    // Releasing wake lock
                    // WakeLocker.release();
                }

            }

            catch(Exception e)
            {

            }
        }
    };


    private User getUserDetails()
    {

        User userObj = new User();

        if (session.checkLogin())
        {
            HashMap<String, String> user = session.getUserDetails();

            userObj.setPhoneNo(user.get(SessionManager.KEY_PHONE));
            userObj.setUserName(user.get(SessionManager.KEY_USER_ID));
        }

        return userObj;
    }


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {

        if(code == 200)
        {
            adapter.notifyDataSetChanged();
            return;
        }

        int index = ChatMessage.findIndex(chatMessageList, message);

        if(index != -1)
        {
            chatMessageList.get(index).setSyncStatus(code);
            adapter.notifyDataSetChanged();
            return;
        }

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    private boolean checkPermissionCamera()
    {

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }

        else
        {
            return false;
        }
    }


    private boolean checkPermissionStorage()
    {

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (result == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }

        else
        {
            return false;
        }
    }


    private void requestPermissionCamera(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))
        {
            makeToast("Camera permission allows us to capture image or record video. Please allow in App Settings for capture image or record video.");
        }

        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }


    private void requestPermissionStorage(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            makeToast("Storage permission allows us to read or write data onto memory. Please allow in App Settings for read or write data.");
        }

        else
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }


    private boolean permissionCheckerCamera() {

        if (!checkPermissionCamera())
        {
            requestPermissionCamera();
            return false;
        }

        return true;
    }


    private boolean permissionCheckerStorage() {

        if (!checkPermissionStorage())
        {
            requestPermissionStorage();
            return false;
        }

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {

        switch (requestCode)
        {

            case CAMERA_PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    makeToast("Permission Granted");
                }

                else
                {
                    makeToast("Permission Denied");
                }

                break;

            case STORAGE_PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeToast("Permission Granted");
                } else {
                    makeToast("Permission Denied");
                }

                break;
        }
    }


    private void makeToast(String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}
