package educing.tech.customer.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import educing.tech.customer.R;
import educing.tech.customer.activities.StoreProfileActivity;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.model.Store;

import java.text.DecimalFormat;
import java.util.List;


public class StoresRecyclerAdapter extends RecyclerView.Adapter<StoresRecyclerAdapter.VersionViewHolder>
{

    private Context context = null;
    private OnItemClickListener clickListener;

    private List<Store> storeList;
    private String[] bgColors;
    private DecimalFormat df = new DecimalFormat("0.00");


    public StoresRecyclerAdapter(Context context, List<Store> storeList)
    {
        this.context = context;
        this.storeList = storeList;
        bgColors = context.getResources().getStringArray(R.array.user_color);

    }


    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerlist_item_stores, viewGroup, false);
        return new VersionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, int i)
    {

        Store store = storeList.get(i);

        versionViewHolder.product_category.setTag(i);

        versionViewHolder.rating.setRating(store.rating);
        versionViewHolder.store_name.setText(Helper.toCamelCase(store.name));
        versionViewHolder.distance.setText(String.valueOf(store.distance + "km"));

        if(store.delivery_status == 2)
        {
            versionViewHolder.delivery_charge.setText(String.valueOf("BOOKING AVAILABLE VIA CHAT"));
        }

        else if(store.delivery_status == 1)
        {

            if(store.delivery_charge == 0 && store.amount == 0)
            {
                versionViewHolder.delivery_charge.setText(String.valueOf("FREE HOME DELIVERY"));
            }

            else
            {
                versionViewHolder.delivery_charge.setText(String.valueOf("FREE DELIVERY ON PURCHASE Rs. " + df.format(store.amount)));
            }
        }

        else
        {
            versionViewHolder.delivery_charge.setText(String.valueOf("HOME DELIVERY NOT AVAILABLE"));
        }


        if(store.is_online == 0)
        {
            versionViewHolder.online.setTextColor(ContextCompat.getColor(context, R.color.myTextSecondaryColor));
            versionViewHolder.online.setText(String.valueOf("OFFLINE"));
        }

        else
        {
            versionViewHolder.online.setTextColor(ContextCompat.getColor(context, R.color.green));
            versionViewHolder.online.setText(String.valueOf("ONLINE"));
        }


        ShapeDrawable background = new ShapeDrawable();
        background.setShape(new OvalShape()); // or RoundRectShape()

        versionViewHolder.product_category.setText(store.getName().substring(0, 1).toUpperCase());

        String color = bgColors[i % bgColors.length];
        background.getPaint().setColor(Color.parseColor(color));

        versionViewHolder.product_category.setBackground(background);
    }


    @Override
    public int getItemCount()
    {
        return storeList == null ? 0 : storeList.size();
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        TextView product_category, delivery_charge, store_name, distance, online;
        RatingBar rating;


        public VersionViewHolder(View itemView)
        {

            super(itemView);

            product_category = (TextView) itemView.findViewById(R.id.product_category);
            delivery_charge = (TextView) itemView.findViewById(R.id.delivery_charge);
            store_name = (TextView) itemView.findViewById(R.id.store_name);
            distance = (TextView) itemView.findViewById(R.id.distance);
            online = (TextView) itemView.findViewById(R.id.online);
            rating = (RatingBar) itemView.findViewById(R.id.rating);

            itemView.setOnClickListener(this);
            product_category.setOnClickListener(onButtonClickListener);
        }

        private View.OnClickListener onButtonClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {

                int pos = (int) product_category.getTag();

                if(v.getId() == R.id.product_category)
                {

                    Intent intent = new Intent(context, StoreProfileActivity.class);
                    intent.putExtra("STORE", storeList.get(pos));
                    context.startActivity(intent);
                }
            }
        };

        @Override
        public void onClick(View v)
        {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }


    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }


    public void SetOnItemClickListener(final OnItemClickListener itemClickListener)
    {
        this.clickListener = itemClickListener;
    }
}