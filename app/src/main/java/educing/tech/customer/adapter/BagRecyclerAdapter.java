package educing.tech.customer.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import educing.tech.customer.R;
import educing.tech.customer.activities.ImagePreviewActivity;
import educing.tech.customer.helper.Blur;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Cart;
import educing.tech.customer.model.Product;
import educing.tech.customer.model.Store;

import java.text.DecimalFormat;
import java.util.List;

import static educing.tech.customer.configuration.Configuration.PRODUCT_IMAGE_URL;
import static educing.tech.customer.model.Cart.cart;
import static educing.tech.customer.model.Cart.searchProduct;

import static educing.tech.customer.model.Order.sub_total;
import static educing.tech.customer.model.Order.delivery_charge_total;
import static educing.tech.customer.model.Order.discount_total;
import static educing.tech.customer.model.Order.grand_total;


public class BagRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    private DecimalFormat df = new DecimalFormat("0.00");

    private OnItemClickListener clickListener;

    private List<Product> myCart;
    private Store store;
    private Context context = null;
    private OnTaskCompleted listener;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;


    public BagRecyclerAdapter(Context context, OnTaskCompleted listener, List<Product> myCart, Store store)
    {

        this.myCart = myCart;
        this.store = store;

        this.context = context;
        this.listener = listener;
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

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerlist_item_bag, viewGroup, false);
            return new VersionViewHolder(view);
        }

        throw new RuntimeException("there is no type that matches the type " + i + " + make sure your using types correctly");
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {

        if( position == myCart.size())
        {

            if(holder instanceof VHHeader)
            {

                VHHeader header = (VHHeader) holder;

                sub_total = Cart.calculateSubTotal(myCart);
                discount_total = Cart.calculateTotalDiscount(myCart);

                header.tvSubTotal.setText(String.valueOf(df.format(sub_total)));
                header.tvDiscount.setText(String.valueOf(df.format(discount_total)));


                delivery_charge_total = 0;


                if(store.delivery_status == 1)
                {

                    if(store.delivery_charge != 0)
                    {

                        double amount = store.amount;
                        double charge = store.delivery_charge;

                        delivery_charge_total = Cart.calculateDeliveryCharge(myCart, amount, charge);
                    }
                }

                header.tvDeliveryCharge.setText(String.valueOf(df.format(delivery_charge_total)));

                grand_total = Cart.calculateGrandTotal(sub_total, discount_total, delivery_charge_total);
                header.tvTotal.setText(String.valueOf(df.format(grand_total)));
            }
        }

        else if(holder instanceof VersionViewHolder)
        {

            final Product product = new Product().getProduct(myCart.get(position).getProductId());

            double total = product.price * product.quantity;
            double discounted_total = product.discount_price * product.quantity;

            final VersionViewHolder versionViewHolder = (VersionViewHolder) holder;
            versionViewHolder.thumbNail.setTag(position);


            versionViewHolder.tvName.setText(Helper.toCamelCase(product.product_name));
            versionViewHolder.tvWeight.setText(String.valueOf(product.weight + " " + product.unit.toLowerCase()));
            versionViewHolder.tvPrice.setText(String.valueOf(df.format(product.price)));
            versionViewHolder.tvQuantity.setText(String.valueOf("Qty " + product.quantity));
            versionViewHolder.tvTotal.setText(String.valueOf(df.format(total)));
            versionViewHolder.tvDiscountedTotal.setText(String.valueOf(df.format(discounted_total)));


            if(!product.product_thumbnail.equals(""))
            {

                Transformation blurTransformation = new Transformation() {

                    @Override
                    public Bitmap transform(Bitmap source) {
                        Bitmap blurred = Blur.fastblur(context, source, 10);
                        source.recycle();
                        return blurred;
                    }

                    @Override
                    public String key() {
                        return "blur()";
                    }
                };

                Picasso.with(context)
                        .load(PRODUCT_IMAGE_URL + product.product_thumbnail) // thumbnail url goes here
                        .resize(60, 60)
                        .transform(blurTransformation)
                        .into(versionViewHolder.thumbNail, new Callback() {

                            @Override
                            public void onSuccess()
                            {

                                Picasso.with(context)
                                        .load(PRODUCT_IMAGE_URL + product.product_thumbnail) // image url goes here
                                        .resize(60, 60)
                                        .placeholder(versionViewHolder.thumbNail.getDrawable())
                                        .into(versionViewHolder.thumbNail);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }


            if(total != discounted_total)
            {
                versionViewHolder.tvTotal.setPaintFlags(versionViewHolder.tvTotal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            else
            {
                versionViewHolder.tvDiscountedTotal.setVisibility(View.GONE);
            }


            versionViewHolder.ib_cancel.setTag(position);
        }
    }


    @Override
    public int getItemCount()
    {
        return myCart == null ? 0 : myCart.size() + 1;
    }


    @Override
    public int getItemViewType(int position)
    {

        if(position == myCart.size())
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

        TextView tvSubTotal, tvDiscount, tvDeliveryCharge, tvTotal;
        Button btnContinue;


        public VHHeader(View itemView)
        {

            super(itemView);

            tvSubTotal = (TextView) itemView.findViewById(R.id.sub_total);
            tvDeliveryCharge = (TextView) itemView.findViewById(R.id.delivery_charge);
            tvDiscount = (TextView) itemView.findViewById(R.id.discount);
            tvTotal = (TextView) itemView.findViewById(R.id.total);
            btnContinue = (Button) itemView.findViewById(R.id.btnContinue);

            btnContinue.setOnClickListener(onButtonClickListener);
        }


        private View.OnClickListener onButtonClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {

                if(v.getId() == R.id.btnContinue) {

                    if(myCart.size() != 0)
                    {
                        listener.onTaskCompleted(true, 200, "confirmation");
                    }

                    else
                    {
                        Toast.makeText(context, "This bag is empty.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tvName;
        TextView tvWeight;
        TextView tvPrice;
        TextView tvQuantity;
        TextView tvTotal;
        TextView tvDiscountedTotal;
        ImageView thumbNail;
        ImageButton ib_cancel;


        public VersionViewHolder(View itemView)
        {

            super(itemView);

            thumbNail = (ImageView) itemView.findViewById(R.id.product_image);
            tvName = (TextView) itemView.findViewById(R.id.product_name);
            tvWeight = (TextView) itemView.findViewById(R.id.weight);
            tvPrice = (TextView) itemView.findViewById(R.id.price);
            tvDiscountedTotal = (TextView) itemView.findViewById(R.id.discounted_total);
            tvQuantity = (TextView) itemView.findViewById(R.id.quantity);
            tvTotal = (TextView) itemView.findViewById(R.id.total);
            ib_cancel = (ImageButton) itemView.findViewById(R.id.ib_cancel);

            ib_cancel.setOnClickListener(onCancelClickListener);
            thumbNail.setOnClickListener(onThumbnailClickListener);
        }


        private View.OnClickListener onCancelClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {

                int pos = (int) v.getTag();
                int index = searchProduct(myCart.get(pos).getProductId());

                myCart.remove(pos);

                if(index != -1)
                {
                    cart.remove(index);
                }

                notifyDataSetChanged();

                listener.onTaskCompleted(true, 200, "removed");
            }
        };


        @Override
        public void onClick(View v)
        {
            clickListener.onItemClick(v, getAdapterPosition());
        }


        private View.OnClickListener onThumbnailClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int pos = (int) v.getTag();

                if(!myCart.get(pos).product_thumbnail.equals(""))
                {
                    Intent intent = new Intent(context, ImagePreviewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("URL",  myCart.get(pos).product_thumbnail);
                    intent.putExtra("DESC", myCart.get(pos).description);
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