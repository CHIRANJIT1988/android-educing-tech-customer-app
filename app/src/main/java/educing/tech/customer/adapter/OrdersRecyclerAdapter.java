package educing.tech.customer.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import educing.tech.customer.R;
import educing.tech.customer.activities.ChatWindowActivity;
import educing.tech.customer.app.MyApplication;
import educing.tech.customer.db.mysql.RateProduct;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.model.Order;
import educing.tech.customer.network.InternetConnectionDetector;

import java.util.List;

import static educing.tech.customer.configuration.Configuration.CATEGORY_IMAGE_URL;


public class OrdersRecyclerAdapter extends RecyclerView.Adapter<OrdersRecyclerAdapter.VersionViewHolder> implements OnTaskCompleted {

    private ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();

    private Context context = null;
    private OnItemClickListener clickListener;

    private List<Order> myOrderList;
    private ProgressDialog pDialog;


    public OrdersRecyclerAdapter(Context context, List<Order> myOrderList) {
        this.context = context;
        this.myOrderList = myOrderList;
    }


    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerlist_orders, viewGroup, false);
        return new VersionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, int i) {

        if (imageLoader == null) {
            imageLoader = MyApplication.getInstance().getImageLoader();
        }

        versionViewHolder.rating.setTag(i);
        versionViewHolder.cancel_order.setTag(i);

        Order order = myOrderList.get(i);

        versionViewHolder.order_date.setText(String.valueOf("Order Placed on " + Helper.dateTimeFormat(order.order_date)));
        versionViewHolder.store_name.setText(String.valueOf("Order Received by " + Helper.toCamelCase(order.store_name)));
        versionViewHolder.order_no.setText(String.valueOf("Order No : " + order.order_no));
        versionViewHolder.category_name.setText(Helper.toCamelCase(order.category_name));
        versionViewHolder.rating.setRating((float) order.rating);

        versionViewHolder.thumbnail.setImageUrl(CATEGORY_IMAGE_URL + order.category_image, imageLoader);


        if (order.order_status.equals("RECEIVED"))
        {

            versionViewHolder.cancel_order.setVisibility(View.VISIBLE);

            versionViewHolder.ib_order_packed.setBackgroundResource(R.drawable.ib_order_progress);
            versionViewHolder.iv_out_for_delivery.setBackgroundResource(R.drawable.ib_order_progress);
            versionViewHolder.iv_order_delivered.setBackgroundResource(R.drawable.ib_order_progress);

            versionViewHolder.rating.setVisibility(View.GONE);
        }

        if (order.order_status.equals("PACKED"))
        {

            versionViewHolder.cancel_order.setVisibility(View.VISIBLE);

            versionViewHolder.ib_order_packed.setBackgroundResource(R.drawable.ib_order_status_completed);
            versionViewHolder.iv_out_for_delivery.setBackgroundResource(R.drawable.ib_order_progress);
            versionViewHolder.iv_order_delivered.setBackgroundResource(R.drawable.ib_order_progress);

            versionViewHolder.rating.setVisibility(View.GONE);
        }

        if (order.order_status.equals("OUT"))
        {

            versionViewHolder.cancel_order.setVisibility(View.VISIBLE);

            versionViewHolder.ib_order_packed.setBackgroundResource(R.drawable.ib_order_status_completed);
            versionViewHolder.iv_out_for_delivery.setBackgroundResource(R.drawable.ib_order_status_completed);
            versionViewHolder.iv_order_delivered.setBackgroundResource(R.drawable.ib_order_progress);

            versionViewHolder.rating.setVisibility(View.GONE);
        }

        if (order.order_status.equals("DELIVERED"))
        {

            versionViewHolder.cancel_order.setVisibility(View.GONE);

            versionViewHolder.ib_order_packed.setBackgroundResource(R.drawable.ib_order_status_completed);
            versionViewHolder.iv_out_for_delivery.setBackgroundResource(R.drawable.ib_order_status_completed);
            versionViewHolder.iv_order_delivered.setBackgroundResource(R.drawable.ib_order_status_completed);

            versionViewHolder.rating.setVisibility(View.VISIBLE);
        }


        if (order.order_status.equals("CANCELLED"))
        {

            versionViewHolder.cancel_order.setVisibility(View.GONE);

            versionViewHolder.ib_order_packed.setBackgroundResource(R.drawable.ib_order_cancelled);
            versionViewHolder.iv_out_for_delivery.setBackgroundResource(R.drawable.ib_order_cancelled);
            versionViewHolder.iv_order_delivered.setBackgroundResource(R.drawable.ib_order_cancelled);

            versionViewHolder.rating.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return myOrderList == null ? 0 : myOrderList.size();
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView store_name, category_name, order_no, order_date;
        NetworkImageView thumbnail;
        Button cancel_order;
        ImageView ib_order_packed, iv_out_for_delivery, iv_order_delivered;
        RatingBar rating;


        public VersionViewHolder(View itemView) {

            super(itemView);

            store_name = (TextView) itemView.findViewById(R.id.store_name);
            order_no = (TextView) itemView.findViewById(R.id.order_no);
            category_name = (TextView) itemView.findViewById(R.id.category_name);
            order_date = (TextView) itemView.findViewById(R.id.order_date);
            thumbnail = (NetworkImageView) itemView.findViewById(R.id.category_image);
            cancel_order = (Button) itemView.findViewById(R.id.btn_cancel_order);
            ib_order_packed = (ImageView) itemView.findViewById(R.id.ib_order_packed);
            iv_out_for_delivery = (ImageView) itemView.findViewById(R.id.iv_out_for_delivery);
            iv_order_delivered = (ImageView) itemView.findViewById(R.id.iv_order_delivered);

            rating = (RatingBar) itemView.findViewById(R.id.ratingBar1);

            itemView.setOnClickListener(this);
            rating.setOnRatingBarChangeListener(onButtonClickListener);
            cancel_order.setOnClickListener(onCancelButtonClickListener);
        }

        private View.OnClickListener onCancelButtonClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {

                int pos = (int) v.getTag();

                Intent intent = new Intent(context, ChatWindowActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("USER", new ChatMessage(String.valueOf(myOrderList.get(pos).store_id), String.valueOf(myOrderList.get(pos).store_name)));
                context.startActivity(intent);
            }
        };


        private RatingBar.OnRatingBarChangeListener onButtonClickListener = new RatingBar.OnRatingBarChangeListener() {


           public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromTouch)
           {

               int pos = (int) ratingBar.getTag();

               if(fromTouch)
               {

                   if (!new InternetConnectionDetector(context).isConnected())
                   {
                       Toast.makeText(context, "Internet Connection Fail", Toast.LENGTH_SHORT).show();
                       return;
                   }

                   initProgressDialog();
                   new RateProduct(context, OrdersRecyclerAdapter.this).rateProduct(myOrderList.get(pos).order_no, rating);
               }
           }
        };


        @Override
        public void onClick(View v) {
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


    @Override
    public void onTaskCompleted(boolean flag, int code, String message)
    {

        try
        {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }

        catch (Exception e)
        {

        }

        finally
        {

            if (pDialog.isShowing())
            {
                pDialog.dismiss();
            }
        }
    }


    private void initProgressDialog()
    {

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Rating ...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }
}