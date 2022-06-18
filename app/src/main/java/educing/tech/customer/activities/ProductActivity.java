package educing.tech.customer.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import educing.tech.customer.R;
import educing.tech.customer.adapter.ProductRecyclerAdapter;
import educing.tech.customer.alert.CustomAlertDialog;
import educing.tech.customer.db.mysql.ReceiveProduct;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Cart;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.model.Product;
import educing.tech.customer.model.Store;
import educing.tech.customer.network.InternetConnectionDetector;
import educing.tech.customer.utils.MyRecyclerScroll;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static educing.tech.customer.model.Cart.*;


public class ProductActivity extends AppCompatActivity implements OnTaskCompleted, View.OnClickListener
{

    private Context context = null;
    private ProgressBar pbLoading;
    private static Store store;

    private static Button fabBtn;
    private static TextView tvTotal, tvDiscount;

    private static LinearLayout fab_footer;
    private static int fabMargin;

    private static DecimalFormat df = new DecimalFormat("0.00");

    private MaterialSearchView searchView;
    private RecyclerView recyclerView;

    private List<Product> productList = new ArrayList<>();
    private ProductRecyclerAdapter adapter;

    public static Activity product_activity;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.tabanim_toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(Helper.toCamelCase(Product.getCategory(Cart.selected_category)));

        product_activity = ProductActivity.this;
        this.context = this.getApplicationContext();

        store = (Store) getIntent().getSerializableExtra("STORE");

        findViewById();

        fabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        fabBtn.setOnClickListener(this);


        Product.productList.clear();

        if(new InternetConnectionDetector(this).isConnected())
        {
            new ReceiveProduct(context, this).retrieveProduct(selected_category, store.id);
        }

        else
        {
            onTaskCompleted(false, 500, "Internet Connection Failure. Try Again");
        }


        displayProduct("");
        addSearchView();
        addOnScrollListener();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_chat);
        fab.setOnClickListener(this);
    }


    private void addSearchView()
    {

        searchView = (MaterialSearchView) findViewById(R.id.search_view);

        searchView.setVoiceSearch(true);
        searchView.setCursorDrawable(R.drawable.color_cursor_white);
        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query)
            {
                displayProduct(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                displayProduct(newText);
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {

            @Override
            public void onSearchViewShown()
            {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed()
            {
                displayProduct("");
            }
        });
    }


    @Override
    public void onClick(View view)
    {

        switch (view.getId())
        {

            case R.id.fabBtn:

                if (Cart.cart.size() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Your Bag is Empty", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(ProductActivity.this, ShoppingBagActivity.class);
                intent.putExtra("CATEGORY_ID", selected_category);
                intent.putExtra("STORE", store);
                startActivity(intent);
                break;

            case R.id.fab_chat:

                Intent intent1 = new Intent(ProductActivity.this, ChatWindowActivity.class);
                intent1.putExtra("USER", getIntent().getSerializableExtra("USER"));
                startActivity(intent1);
                break;
        }
    }


    private void findViewById()
    {

        fab_footer = (LinearLayout) findViewById(R.id.myfab_main);
        recyclerView = (RecyclerView) findViewById(R.id.dummyfrag_scrollableview);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);
        tvTotal = (TextView) findViewById(R.id.total);
        tvDiscount = (TextView) findViewById(R.id.discount);
        fabBtn = (Button) findViewById(R.id.fabBtn);
    }


    private void addOnScrollListener()
    {

        recyclerView.addOnScrollListener(new MyRecyclerScroll() {

            @Override
            public void show() {
                fab_footer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }


            @Override
            public void hide() {
                fab_footer.animate().translationY(fab_footer.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2)).start();
            }
        });
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


    private static void setTotal()
    {

        if (Cart.buildCart(selected_category, store.id).size() == 0)
        {
            fab_footer.setVisibility(View.GONE);
        }

        else
        {

            double sub_total = Cart.calculateSubTotal(Cart.buildCart(selected_category, store.id));
            double discount_total = Cart.calculateTotalDiscount(Cart.buildCart(selected_category, store.id));
            double grand_total = Cart.calculateGrandTotal(sub_total, discount_total, 0);

            fab_footer.setVisibility(View.VISIBLE);
            fabBtn.setText(String.valueOf("(" + Cart.buildCart(selected_category, store.id).size() + ") ITEMS"));

            tvTotal.setText(String.valueOf(df.format(grand_total)));
            tvDiscount.setText(String.valueOf(df.format(discount_total)));
        }
    }


    private void displayProduct(String value)
    {

        if(value.equals(""))
        {
            this.productList = Product.productList;
        }

        else
        {
            this.productList = Product.getProductList(value);
        }

        adapter = new ProductRecyclerAdapter(productList, context, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.SetOnItemClickListener(new ProductRecyclerAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

            }
        });

        setTotal();
    }


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {

        try
        {

            pbLoading.setVisibility(View.GONE);


            if(flag && code == 199)
            {
                new CustomAlertDialog(ProductActivity.this, this).showConfirmationDialog(message + ". Do you want to Chat ?", "finish");
                return;
            }

            if (flag && code == 200)
            {

                displayProduct("");
                return;
            }

            if(flag && code == 201)
            {
                finish();
                return;
            }

            if(flag && code == 202)
            {

                finish();

                Intent intent = new Intent(this, ChatWindowActivity.class);
                intent.putExtra("USER", new ChatMessage(String.valueOf(store.id), store.name));
                startActivity(intent);
                return;
            }

            else if (flag && message.equals("add") || flag && message.equals("remove"))
            {
                setTotal();
                return;
            }

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        catch (Exception e)
        {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }


    @Override
    public void onBackPressed()
    {

        if (searchView.isSearchOpen())
        {
            searchView.closeSearch();
        }

        else
        {
            super.onBackPressed();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK)
        {

            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (matches != null && matches.size() > 0)
            {

                String searchWrd = matches.get(0);

                if (!TextUtils.isEmpty(searchWrd))
                {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}