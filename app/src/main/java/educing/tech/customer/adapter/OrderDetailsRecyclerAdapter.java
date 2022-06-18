package educing.tech.customer.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import educing.tech.customer.R;
import educing.tech.customer.activities.ImagePreviewActivity;
import educing.tech.customer.app.MyApplication;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.model.Order;

import java.text.DecimalFormat;

import static educing.tech.customer.configuration.Configuration.PRODUCT_IMAGE_URL;
import static educing.tech.customer.model.Order.delivery_charge_total;
import static educing.tech.customer.model.Order.myOrderDetailsList;


public class OrderDetailsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    private ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
    private DecimalFormat df = new DecimalFormat("0.00");
    private OnItemClickListener clickListener;
    private Context context = null;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private double delivery_charge;


    public OrderDetailsRecyclerAdapter(Context context, double delivery_charge)
    {
        this.context = context;
        this.delivery_charge = delivery_charge;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {

        if(i == TYPE_HEADER)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.footer_item, viewGroup, false);
            return new VHHeader(view);
        }

        else if(i == TYPE_ITEM)
        {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerlist_order_details, viewGroup, false);
            return new VersionViewHolder(view);
        }

        throw new RuntimeException("there is no type that matches the type " + i + " + make sure your using types correctly");
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {

        if( position == myOrderDetailsList.size())
        {

            if(holder instanceof VHHeader)
            {

                VHHeader header = (VHHeader) holder;

                double sub_total = Order.calculateSubTotal(myOrderDetailsList);
                double discount_total = Order.calculateTotalDiscount(myOrderDetailsList);
                double total = Order.calculateGrandTotal(sub_total, discount_total, delivery_charge);


                header.tvSubTotal.setText(String.valueOf(df.format(sub_total)));
                header.tvDiscount.setText(String.valueOf(df.format(discount_total)));
                header.tvDeliveryCharge.setText(String.valueOf(df.format(delivery_charge)));
                header.tvTotal.setText(String.valueOf(df.format(total)));

                header.tvDeliveryCharge.setText(String.valueOf(df.format(delivery_charge_total)));

                header.btnContinue.setVisibility(View.GONE);
            }
        }

        else if(holder instanceof VersionViewHolder)
        {

            Order order = myOrderDetailsList.get(position);

            if (imageLoader == null)
            {
                imageLoader = MyApplication.getInstance().getImageLoader();
            }

            double total = order.price * order.quantity;
            double discounted_total = order.discount_price * order.quantity;

            VersionViewHolder versionViewHolder = (VersionViewHolder) holder;

            versionViewHolder.thumbNail.setTag(position);

            versionViewHolder.thumbNail.setImageUrl(PRODUCT_IMAGE_URL + order.product_image, imageLoader);
            versionViewHolder.tvName.setText(Helper.toCamelCase(order.product_name));
            versionViewHolder.tvWeight.setText(String.valueOf(order.weight + " " + order.unit.toLowerCase()));
            versionViewHolder.tvPrice.setText(String.valueOf(df.format(order.price)));
            versionViewHolder.tvQuantity.setText(String.valueOf("Qty " + order.quantity));
            versionViewHolder.tvTotal.setText(String.valueOf(df.format(total)));
            versionViewHolder.tvDiscountedTotal.setText(String.valueOf(df.format(discounted_total)));


            if(total != discounted_total)
            {
                versionViewHolder.tvTotal.setPaintFlags(versionViewHolder.tvTotal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            else
            {
                versionViewHolder.tvDiscountedTotal.setVisibility(View.GONE);
            }


            versionViewHolder.ib_cancel.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount()
    {
        return myOrderDetailsList == null ? 0 : myOrderDetailsList.size() + 1;
    }


    @Override
    public int getItemViewType(int position)
    {

        if(position == myOrderDetailsList.size())
        {
            return TYPE_HEADER;
        }

        return TYPE_ITEM;
    }


    /*private boolean isPositionHeader(int position)
    {
        return position == 0;
    }*/


    class VHHeader extends RecyclerView.ViewHolder
    {

        TextView tvSubTotal, tvDeliveryCharge, tvDiscount, tvTotal;
        Button btnContinue;


        public VHHeader(View itemView)
        {

            super(itemView);

            tvSubTotal = (TextView) itemView.findViewById(R.id.sub_total);
            tvDeliveryCharge = (TextView) itemView.findViewById(R.id.delivery_charge);
            tvDiscount = (TextView) itemView.findViewById(R.id.discount);
            tvTotal = (TextView) itemView.findViewById(R.id.total);
            btnContinue = (Button) itemView.findViewById(R.id.btnContinue);
        }
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tvName;
        TextView tvWeight;
        TextView tvPrice;
        TextView tvQuantity;
        TextView tvTotal;
        TextView tvDiscountedTotal;
        NetworkImageView thumbNail;
        ImageButton ib_cancel;


        public VersionViewHolder(View itemView)
        {

            super(itemView);

            thumbNail = (NetworkImageView) itemView.findViewById(R.id.product_image);
            tvName = (TextView) itemView.findViewById(R.id.product_name);
            tvWeight = (TextView) itemView.findViewById(R.id.weight);
            tvPrice = (TextView) itemView.findViewById(R.id.price);
            tvDiscountedTotal = (TextView) itemView.findViewById(R.id.discounted_total);
            tvQuantity = (TextView) itemView.findViewById(R.id.quantity);
            tvTotal = (TextView) itemView.findViewById(R.id.total);
            ib_cancel = (ImageButton) itemView.findViewById(R.id.ib_cancel);

            thumbNail.setOnClickListener(onThumbnailClickListener);
        }


        @Override
        public void onClick(View v)
        {
            clickListener.onItemClick(v, getAdapterPosition());
        }


        private View.OnClickListener onThumbnailClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int pos = (int) v.getTag();

                if(!myOrderDetailsList.get(pos).product_image.equals(""))
                {
                    Intent intent = new Intent(context, ImagePreviewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("URL",  myOrderDetailsList.get(pos).product_image);
                    context.startActivity(intent);
                }
            }
        };
    }


    public interface OnItemClickListener
    {
        void onItemClick(View view, int position);
    }


    /*public void SetOnItemClickListener(final OnItemClickListener itemClickListener)
    {
        this.clickListener = itemClickListener;
    }*/
}