package educing.tech.customer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import educing.tech.customer.R;
import educing.tech.customer.helper.Helper;
import educing.tech.customer.model.ChatMessage;


public class ChatUserRecyclerAdapter extends RecyclerView.Adapter<ChatUserRecyclerAdapter.VersionViewHolder>
{

    private Context context = null;
    private OnItemClickListener clickListener;

    private List<ChatMessage> chatList;
    private String[] bgColors;


    public ChatUserRecyclerAdapter(Context context, List<ChatMessage> chatList)
    {
        this.context = context;
        this.chatList = chatList;
        bgColors = context.getApplicationContext().getResources().getStringArray(R.array.user_color);

    }


    @Override
    public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerlist_item_chat_user, viewGroup, false);
        return new VersionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(VersionViewHolder versionViewHolder, int i)
    {

        ChatMessage chatObj = chatList.get(i);

        ShapeDrawable background = new ShapeDrawable();
        background.setShape(new OvalShape()); // or RoundRectShape()


        versionViewHolder.name.setText(Helper.toCamelCase(chatObj.user_name));
        versionViewHolder.chat.setText(chatObj.message);

        versionViewHolder.thumbnail.setText(chatList.get(i).user_name.substring(0, 1).toUpperCase());

        try
        {

            if(DateUtils.isToday(Long.parseLong(chatObj.timestamp)))
            {
                String datetime = new SimpleDateFormat("hh:mm a").format(Long.parseLong(chatObj.timestamp));
                versionViewHolder.timestamp.setText(datetime);
            }

            else
            {
                String datetime = new SimpleDateFormat("dd/MM/yyyy").format(Long.parseLong(chatObj.timestamp));
                versionViewHolder.timestamp.setText(datetime);
            }
        }

        catch (Exception e)
        {

        }

        if(chatObj.unread_message > 0)
        {

            versionViewHolder.unread_count.setVisibility(View.VISIBLE);
            versionViewHolder.unread_count.setText(String.valueOf(chatObj.unread_message));

            if(chatObj.unread_message > 9)
            {
                versionViewHolder.unread_count.setText("9+");
            }
        }

        else
        {
            versionViewHolder.unread_count.setVisibility(View.GONE);
        }

        ShapeDrawable unread_count_background = new ShapeDrawable();
        unread_count_background.setShape(new OvalShape()); // or RoundRectShape()
        unread_count_background.getPaint().setColor(Color.parseColor("#259b24"));
        versionViewHolder.unread_count.setBackground(unread_count_background);


        if(i != 0)
        {

            String color = bgColors[i % bgColors.length];
            background.getPaint().setColor(Color.parseColor(color));
        }

        else
        {
            background.getPaint().setColor(ContextCompat.getColor(context, R.color.myPrimaryColor));
        }

        versionViewHolder.thumbnail.setBackground(background);
    }


    @Override
    public int getItemCount()
    {
        return chatList == null ? 0 : chatList.size();
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener
    {

        TextView name, chat, timestamp, thumbnail, unread_count;

        public VersionViewHolder(View itemView)
        {

            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            chat = (TextView) itemView.findViewById(R.id.chat);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            thumbnail = (TextView) itemView.findViewById(R.id.thumbnail);
            unread_count = (TextView) itemView.findViewById(R.id.unread_count);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
        {

            //menu.setHeaderTitle("Select The Action");
            //groupId, itemId, order, title
            menu.add(0, 100, getAdapterPosition(), "Delete");
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