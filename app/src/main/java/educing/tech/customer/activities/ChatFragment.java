package educing.tech.customer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gcm.GCMRegistrar;

import org.json.JSONObject;

import java.util.List;

import educing.tech.customer.R;
import educing.tech.customer.adapter.ChatUserRecyclerAdapter;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.sqlite.SQLiteDatabaseHelper;

import static educing.tech.customer.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static educing.tech.customer.CommonUtilities.EXTRA_MESSAGE;


public class ChatFragment extends Fragment
{

    private ChatUserRecyclerAdapter adapter;
    private List<ChatMessage> userList;
    private RecyclerView recyclerView;
    private SQLiteDatabaseHelper helper;


    public ChatFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.fragment_chat_window, container, false);
        setHasOptionsMenu(true);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        getActivity().registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
        this.helper = new SQLiteDatabaseHelper(getActivity());

        displayUsers();

        return rootView;
    }


    private void displayUsers()
    {

        userList = helper.getAllChatUsers();

        ChatMessage supportObj = helper.getLastMessage("0");
        supportObj.setUserId("0");
        supportObj.setUserName("Support Team");

        userList.add(0, supportObj);


        adapter = new ChatUserRecyclerAdapter(getActivity(), userList);
        recyclerView.setAdapter(adapter);

        adapter.SetOnItemClickListener(new ChatUserRecyclerAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position)
            {
                Intent intent = new Intent(getActivity(), ChatWindowActivity.class);
                intent.putExtra("USER", userList.get(position));
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onContextItemSelected(MenuItem item)
    {

        int position = item.getOrder();

        switch (item.getItemId())
        {

            case 100:

                if(!userList.get(position).user_id.equals("0"))
                {
                    helper.clearChatUsers(userList.get(position).user_id);
                    userList.remove(position);
                    adapter.notifyDataSetChanged();
                }

                break;
        }

        return super.onContextItemSelected(item);
    }


    @Override
    public void onResume()
    {

        try
        {

            ChatMessage supportObj = helper.getLastMessage("0");

            userList.get(0).setMessage(supportObj.message);
            userList.get(0).setTimestamp(supportObj.timestamp);
            userList.get(0).setUnreadMessageCount(supportObj.unread_message);


            for (ChatMessage user: helper.getAllChatUsers())
            {

                int index = ChatMessage.findUser(userList, user.user_id);

                if (index != -1)
                {
                    userList.get(index).setMessage(user.message);
                    userList.get(index).setTimestamp(user.timestamp);
                    userList.get(index).setUnreadMessageCount(user.unread_message);
                }

                else
                {
                    userList.add(1, user);
                }
            }

            adapter.notifyDataSetChanged();
        }

        catch (Exception e)
        {

        }

        super.onResume();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_chat_window, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.action_clear:

                helper.clearChatUsers();
                userList.clear();
                userList.add(0, new ChatMessage("0", "Support Team"));

                adapter.notifyDataSetChanged();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy()
    {


        try
        {
            getActivity().unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(getActivity());
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

                String sender_id = jsonObj.getString("sender_id");
                String sender_name = jsonObj.getString("sender_name");
                String chat_message = jsonObj.getString("message");
                String timestamp = jsonObj.getString("timestamp");

                int index = ChatMessage.findUser(userList, sender_id);

                if(index != -1 )
                {

                    userList.get(index).setMessage(chat_message);
                    userList.get(index).setTimestamp(timestamp);
                    userList.get(index).setUnreadMessageCount(userList.get(index).getUnreadMessageCount() + 1);

                    userList.add(1, userList.get(index));
                    userList.remove(index + 1);
                }

                else
                {
                    userList.add(1, new ChatMessage(sender_id, sender_name, chat_message, timestamp, 1));
                }

                adapter.notifyDataSetChanged();


                // Waking up mobile if it is sleeping
                // WakeLocker.acquire(context);

                // Releasing wake lock
                // WakeLocker.release();
            }

            catch(Exception e)
            {

            }
        }
    };
}