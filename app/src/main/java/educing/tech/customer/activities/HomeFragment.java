package educing.tech.customer.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import educing.tech.customer.R;
import educing.tech.customer.adapter.HomeRecyclerAdapter;
import educing.tech.customer.model.Cart;
import educing.tech.customer.model.Product;


public class HomeFragment extends Fragment
{

    private RecyclerView recyclerView;
    private HomeRecyclerAdapter simpleRecyclerAdapter;
    private Context context;
    private List<Product> categoryList;


    public HomeFragment()
    {

    }

    @SuppressLint("ValidFragment")
    public HomeFragment(List<Product> categoryList)
    {
        this.categoryList = categoryList;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        context = getActivity();

        findViewById(rootView);
        displayProductCategoryList();

        return rootView;
    }


    @Override
    public void onDetach()
    {
        super.onDetach();
    }


    private void findViewById(View rootView)
    {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
    }


    private void displayProductCategoryList()
    {

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(mLayoutManager);


        if (simpleRecyclerAdapter == null)
        {
            simpleRecyclerAdapter = new HomeRecyclerAdapter(context, categoryList);
            recyclerView.setAdapter(simpleRecyclerAdapter);
        }


        simpleRecyclerAdapter.SetOnItemClickListener(new HomeRecyclerAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position)
            {

                Product product = categoryList.get(position);

                Cart.selected_category = product.getCategoryId();
                Intent intent = new Intent(context, StoresActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onResume()
    {
        super.onResume();
    }
}