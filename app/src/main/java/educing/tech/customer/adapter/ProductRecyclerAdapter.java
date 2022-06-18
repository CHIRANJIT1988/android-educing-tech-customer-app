package educing.tech.customer.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import educing.tech.customer.R;
import educing.tech.customer.activities.ImagePreviewActivity;
import educing.tech.customer.helper.Blur;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.Product;

import java.text.DecimalFormat;
import java.util.List;

import static educing.tech.customer.model.Cart.*;
import static educing.tech.customer.configuration.Configuration.PRODUCT_IMAGE_URL;


public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.VersionViewHolder>
{

    private DecimalFormat df = new DecimalFormat("0.00");

    private List<Product> productList;
    private OnItemClickListener clickListener;
    private Context context = null;
    private OnTaskCompleted listener = null;


    public ProductRecyclerAdapter(List<Product> productList, Context context, OnTaskCompleted listener)
    {
        this.productList = productList;
        this.context = context;
        this.listener = listener;
    }


    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerlist_item_product, viewGroup, false);
        return new VersionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final VersionViewHolder versionViewHolder, int i)
    {

        final Product product = productList.get(i);

        int index = searchProduct(product.product_id);
        Log.v("Index: ", "" + index);


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

        versionViewHolder.tvName.setText(Helper.toCamelCase(product.product_name));
        versionViewHolder.tvWeight.setText(String.valueOf(product.weight + " " + product.unit.toLowerCase()));
        versionViewHolder.tvPrice.setText(String.valueOf(df.format(product.price)));
        versionViewHolder.tvDiscountPrice.setText(String.valueOf(df.format(product.discount_price)));


        if(product.price !=  product.discount_price)
        {
            versionViewHolder.tvPrice.setPaintFlags(versionViewHolder.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        else
        {
            versionViewHolder.tvDiscountPrice.setVisibility(View.GONE);
        }

        versionViewHolder.add_quantity.setTag(i);
        versionViewHolder.subtract_quantity.setTag(i);
        versionViewHolder.thumbNail.setTag(i);


        if(index != -1)
        {
            versionViewHolder.tvQuantity.setText(String.valueOf(cart.get(index).getQuantity()));
        }

        else
        {
            versionViewHolder.tvQuantity.setText("0");
        }
    }


    @Override
    public int getItemCount()
    {
        return productList == null ? 0 : productList.size();
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView tvName;
        TextView tvWeight;
        TextView tvPrice;
        TextView tvDiscountPrice;
        ImageView thumbNail;
        ImageButton add_quantity;
        ImageButton subtract_quantity;
        TextView tvQuantity;


        public VersionViewHolder(View itemView)
        {

            super(itemView);

            thumbNail = (ImageView) itemView.findViewById(R.id.product_image);
            tvName = (TextView) itemView.findViewById(R.id.product_name);
            tvWeight = (TextView) itemView.findViewById(R.id.weight);
            tvPrice = (TextView) itemView.findViewById(R.id.price);
            tvDiscountPrice = (TextView) itemView.findViewById(R.id.discount_price);

            tvQuantity = (TextView) itemView.findViewById(R.id.quantity);
            add_quantity = (ImageButton) itemView.findViewById(R.id.add_quantity);
            subtract_quantity = (ImageButton) itemView.findViewById(R.id.subtract_quantity);

            add_quantity.setOnClickListener(onAddClickListener);
            subtract_quantity.setOnClickListener(onSubtractClickListener);
            thumbNail.setOnClickListener(onThumbnailClickListener);

            itemView.setOnClickListener(this);
        }


        private View.OnClickListener onAddClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {

                int pos = (int) v.getTag();

                Product product = productList.get(pos);
                int index = searchProduct(product.product_id);
                Log.v("Index: ", "" + index);


                if(index == -1)
                {

                    int quantity = 1;

                    product.setQuantity(quantity);
                    cart.add(product);

                    tvQuantity.setText(String.valueOf(quantity));
                }

                else
                {
                    int quantity = cart.get(index).getQuantity() + 1;
                    cart.get(index).setQuantity(quantity);
                    tvQuantity.setText(String.valueOf(quantity));
                }

                listener.onTaskCompleted(true, 300, "add");
            }
        };


        private View.OnClickListener onSubtractClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {

                int pos = (int) v.getTag();

                Product product = productList.get(pos);

                int index = searchProduct(product.product_id);

                if(index != -1)
                {

                    int quantity = cart.get(index).getQuantity() - 1;
                    cart.get(index).setQuantity(quantity);
                    tvQuantity.setText(String.valueOf(quantity));

                    if(quantity == 0)
                    {
                        cart.remove(index);
                    }
                }

                listener.onTaskCompleted(true, 300, "remove");
            }
        };


        private View.OnClickListener onThumbnailClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int pos = (int) v.getTag();

                if(!productList.get(pos).product_thumbnail.equals(""))
                {
                    Intent intent = new Intent(context, ImagePreviewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("URL",  productList.get(pos).product_thumbnail);
                    intent.putExtra("DESC", productList.get(pos).description);
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