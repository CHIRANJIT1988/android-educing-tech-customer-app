package educing.tech.customer.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import educing.tech.customer.R;
import educing.tech.customer.adapter.BagRecyclerAdapter;
import educing.tech.customer.alert.CustomAlertDialog;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Cart;
import educing.tech.customer.model.Order;
import educing.tech.customer.model.Product;
import educing.tech.customer.model.Store;
import educing.tech.customer.model.User;
import educing.tech.customer.session.SessionManager;


public class ShoppingBagActivity extends AppCompatActivity implements OnTaskCompleted
{

    private RecyclerView recyclerView;
    private BagRecyclerAdapter adapter;

    private Context context;
    private SessionManager session; // Session Manager Class
    public static Activity shopping_bag_activity;

    private List<Product> cart;
    public static Store store;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_bag);

        this.context = ShoppingBagActivity.this;
        shopping_bag_activity = ShoppingBagActivity.this;


        final Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("My Bag");

        session = new SessionManager(context); // Session Manager

        viewBag();
    }


    private void viewBag()
    {

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        Intent intent = getIntent();
        int category_id = intent.getIntExtra("CATEGORY_ID", 0);
        store = (Store) intent.getSerializableExtra("STORE");

        cart = Cart.buildCart(category_id, store.id);


        adapter = new BagRecyclerAdapter(context, this, cart, store);
        recyclerView.setAdapter(adapter);
    }



    @Override
    public void onResume()
    {
        super.onResume();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {

            case android.R.id.home:

                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {

        try
        {

            if (flag)
            {

                if(message.equals("removed"))
                {
                    Toast.makeText(context, "Item Removed from Bag", Toast.LENGTH_LONG).show();
                    return;
                }

                if (message.equals("confirmation"))
                {

                    if (session.checkLogin())
                    {

                        Order.orderList = new Order().buildOrderList(cart, getUserDetails());

                        Intent intent = new Intent(context, ShippingAddressActivity.class);
                        startActivity(intent);
                        return;
                    }

                    startActivity(new Intent(ShoppingBagActivity.this, MainActivity.class));
                }
            }

            else
            {
                new CustomAlertDialog(context, this).showOKDialog("", String.valueOf(message), "network");
            }
        }

        catch (Exception e)
        {

        }
    }


    private User getUserDetails()
    {

        SessionManager session = new SessionManager(context);

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
}