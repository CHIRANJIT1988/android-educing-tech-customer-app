package educing.tech.customer.activities;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;

import org.json.JSONObject;

import java.util.List;

import educing.tech.customer.R;
import educing.tech.customer.adapter.AdvertisementRecyclerAdapter;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Advertisement;
import educing.tech.customer.model.Cart;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.model.Store;
import educing.tech.customer.sqlite.SQLiteDatabaseHelper;

import static educing.tech.customer.sqlite.SQLiteDatabaseHelper.TABLE_ADVERTISEMENT;
import static educing.tech.customer.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static educing.tech.customer.CommonUtilities.EXTRA_MESSAGE;


public class AdvertisementActivity extends AppCompatActivity implements OnTaskCompleted
{

    private boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private int pageCount = 1;

    private RecyclerView recyclerView;
    private AdvertisementRecyclerAdapter adapter;
    private LinearLayoutManager mLayoutManager;

    private List<Advertisement> advertisementList;

    private boolean first_load = true;
    private SQLiteDatabaseHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Notifications");

        recyclerView = (RecyclerView) findViewById(R.id.quickreturn_list);

        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        this.helper = new SQLiteDatabaseHelper(this);

        fetchAdvertisement();
        helper.setAsRead();

        this.registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));


        // Instantiate NotificationManager Class
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Cancel notification
        mNotificationManager.cancel(1);
    }


    private void swipeDelete()
    {

        SwipeableRecyclerViewTouchListener swipeTouchListener =

                new SwipeableRecyclerViewTouchListener(recyclerView,

                        new SwipeableRecyclerViewTouchListener.SwipeListener() {

                            @Override
                            public boolean canSwipe(int position)
                            {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {

                                if(!first_load)
                                {

                                    for (int position : reverseSortedPositions)
                                    {
                                        helper.clearAdvertisement(advertisementList.get(position).getAdvertisementId());
                                        advertisementList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                    }

                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {

                                if(!first_load)
                                {

                                    for (int position : reverseSortedPositions)
                                    {
                                        helper.clearAdvertisement(advertisementList.get(position).getAdvertisementId());
                                        advertisementList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                    }

                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });

        recyclerView.addOnItemTouchListener(swipeTouchListener);
    }


    private void loadMoreOnScroll()
    {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                loading = true;

                int totalRecord = helper.dbRowCount(TABLE_ADVERTISEMENT);
                int totalPage;

                if (totalRecord % 10 == 0) {
                    totalPage = (totalRecord / 10);
                } else {
                    totalPage = (totalRecord / 10) + 1;
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

                                List<Advertisement> tempList = helper.getAllAdvertisement(pageCount * 10, 10);


                                for (Advertisement advertisement: tempList) {
                                    advertisementList.add(advertisementList.size(), advertisement);
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

    private void fetchAdvertisement()
    {

        loading = true;
        pageCount = 1;

        advertisementList = helper.getAllAdvertisement(0, 10);

        adapter = new AdvertisementRecyclerAdapter(getApplicationContext(), advertisementList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.SetOnItemClickListener(new AdvertisementRecyclerAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {


                int store_id = advertisementList.get(position).store_id;
                String store_name = advertisementList.get(position).store_name;
                Cart.selected_category = advertisementList.get(position).category_id;

                if(store_id != 0 || Cart.selected_category != 0)
                {

                    Intent intent = new Intent(AdvertisementActivity.this, ProductActivity.class);
                    intent.putExtra("STORE", new Store(store_id, store_name));
                    intent.putExtra("USER", new ChatMessage(String.valueOf(store_id), store_name));
                    startActivity(intent);
                }
            }
        });

        swipeDelete();
        loadMoreOnScroll();

        first_load = false;
    }


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }


    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mHandleMessageReceiver);
    }

    /**
     * Called when the activity is about to become visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Inside : ", "onCreate() event");
    }

    /**
     * Called when another activity is taking focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Inside : ", "onPause() event");
    }

    /**
     * Called when the activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Inside : ", "onStop() event");
    }



    @Override
    public void onResume()
    {

        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_delete, menu);
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

            case R.id.action_delete:

                advertisementList.clear();
                adapter.notifyDataSetChanged();
                helper.clearAllAdvertisement();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {

            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);

            Log.v("gcm_message: ", "" + newMessage);

            if(newMessage == null)
            {
                return;
            }

            try
            {

                JSONObject jsonObj = new JSONObject(newMessage);


                if(jsonObj.getString("message_type").equalsIgnoreCase("advertisement"))
                {

                    int store_id = jsonObj.getInt("store_id");
                    String store_name = jsonObj.getString("store_name");
                    double store_rating = jsonObj.getDouble("rating");
                    int category_id = jsonObj.getInt("category_id");
                    String message = jsonObj.getString("message");
                    String file_name = jsonObj.getString("file_name");
                    String timestamp = jsonObj.getString("timestamp");


                    Advertisement advertisement = new  Advertisement(store_id, store_name, store_rating, category_id, message, file_name, timestamp);
                    advertisementList.add(0, advertisement);
                    adapter.notifyDataSetChanged();
                }

                // Waking up mobile if it is sleeping
                // WakeLocker.acquire(context);

                // Releasing wake lock
                // WakeLocker.release();
            }

            catch (Exception e)
            {

            }
        }
    };
}