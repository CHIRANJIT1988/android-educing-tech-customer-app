package educing.tech.customer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import educing.tech.customer.R;
import educing.tech.customer.helper.Blur;
import educing.tech.customer.model.Advertisement;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static educing.tech.customer.configuration.Configuration.ADVERTISEMENT_URL;


public class AdvertisementRecyclerAdapter extends RecyclerView.Adapter<AdvertisementRecyclerAdapter.VersionViewHolder>
{

    private List<Advertisement> advertisementList;

    private Context context;
    private OnItemClickListener clickListener;


    public AdvertisementRecyclerAdapter(Context context, List<Advertisement> advertisementList)
    {
        this.context = context;
        this.advertisementList = advertisementList;
    }


    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerlist_deals, viewGroup, false);
        return new VersionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final VersionViewHolder versionViewHolder, int i)
    {

        final Advertisement advertisementObj = advertisementList.get(i);

        try
        {

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(advertisementObj.timestamp);

            long diff = (System.currentTimeMillis() - date.getTime()) / (60 * 60 * 1000);

            if(diff > 0)
            {
                versionViewHolder.timestamp.setText(String.valueOf("EXPIRED"));
                versionViewHolder.timestamp.setTextColor(Color.RED);
            }

            else
            {

                if(Math.abs(diff) < 24)
                {

                    long value = Math.abs(diff);
                    DecimalFormat df = new DecimalFormat("##");
                    versionViewHolder.timestamp.setText(String.valueOf("Only " + df.format(value) + " hour(s) left"));
                }

                else
                {
                    long value = diff / 24;
                    versionViewHolder.timestamp.setText(String.valueOf(Math.abs(value) + " day(s) left"));
                }

                versionViewHolder.timestamp.setTextColor(Color.GRAY);
            }

            //long output = Long.valueOf(advertisementObj.timestamp)/1000L;
            //CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(Long.parseLong(String.valueOf(output * 1000)), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        }

        catch (Exception e)
        {
            Log.v("Exception", "" + e.getMessage());
        }

        versionViewHolder.store_name.setText(advertisementObj.store_name.toUpperCase());
        versionViewHolder.description.setText(advertisementObj.message);
        versionViewHolder.rating.setRating((float) advertisementObj.store_rating);

        if(!advertisementObj.file_name.equals(""))
        {

            versionViewHolder.advertisement_image.setVisibility(View.VISIBLE);

            Transformation blurTransformation = new Transformation() {

                @Override
                public Bitmap transform(Bitmap source)
                {
                    Bitmap blurred = Blur.fastblur(context, source, 10);
                    source.recycle();
                    return blurred;
                }

                @Override
                public String key()
                {
                    return "blur()";
                }
            };


            Picasso.with(context)
                .load(ADVERTISEMENT_URL + advertisementObj.file_name) // thumbnail url goes here
                .transform(blurTransformation)
                .into(versionViewHolder.advertisement_image, new Callback() {

                    @Override
                    public void onSuccess()
                    {

                        Picasso.with(context)
                                .load(ADVERTISEMENT_URL + advertisementObj.file_name) // image url goes here
                                .into(versionViewHolder.advertisement_image);
                    }

                    @Override
                    public void onError()
                    {

                    }
                });
        }

        else
        {
            versionViewHolder.advertisement_image.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount()
    {
        return advertisementList == null ? 0 : advertisementList.size();
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView description, store_name, timestamp;
        ImageView advertisement_image;
        RatingBar rating;

        public VersionViewHolder(View itemView)
        {

            super(itemView);

            description = (TextView) itemView.findViewById(R.id.description);
            store_name = (TextView) itemView.findViewById(R.id.store_name);
            rating = (RatingBar) itemView.findViewById(R.id.rating);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            advertisement_image = (ImageView) itemView.findViewById(R.id.advertisement_image);

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