package educing.tech.customer.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import educing.tech.customer.R;
import educing.tech.customer.app.MyApplication;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.model.Product;

import java.util.List;

import static educing.tech.customer.configuration.Configuration.CATEGORY_IMAGE_URL;


public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.VersionViewHolder>
{

    private ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();

    private Context context = null;
    private OnItemClickListener clickListener;

    private List<Product> category;


    public HomeRecyclerAdapter(Context context, List<Product> category)
    {
        this.context = context;
        this.category = category;
    }


    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerlist_item_home, viewGroup, false);
        return new VersionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, int i)
    {

        if (imageLoader == null)
        {
            imageLoader = MyApplication.getInstance().getImageLoader();
        }


        versionViewHolder.title.setText(Helper.toCamelCase(category.get(i).getCategoryName()));

        //Animation animation = AnimationUtils.loadAnimation(context, R.anim.network_image_view);
        //versionViewHolder.thumbnail.startAnimation(animation);
        //ObjectAnimator.ofFloat(versionViewHolder.thumbnail, View.ALPHA, 0.2f, 1.0f).setDuration(2000).start();
        versionViewHolder.thumbnail.setImageUrl(CATEGORY_IMAGE_URL + category.get(i).getCategoryThumbnail(), imageLoader);


        //TypedArray images = context.getResources().obtainTypedArray(R.array.home_activities_image);
        //versionViewHolder.thumbnail.setImageResource(images.getResourceId(i, -1));

        //String imageFileName = category.get(i).getCategoryThumbnail();
        //int imgResId = context.getResources().getIdentifier(imageFileName, "drawable", "educing.tech.customer");
        //versionViewHolder.thumbnail.setImageResource(imgResId);
    }


    @Override
    public int getItemCount()
    {
        return category == null ? 0 : category.size();
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        CardView cardItemLayout;
        TextView title;
        NetworkImageView thumbnail;

        public VersionViewHolder(View itemView)
        {

            super(itemView);

            cardItemLayout = (CardView) itemView.findViewById(R.id.cardlist_item);
            title = (TextView) itemView.findViewById(R.id.listitem_name);
            thumbnail = (NetworkImageView) itemView.findViewById(R.id.thumbnail);

            itemView.setOnClickListener(this);
        }


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