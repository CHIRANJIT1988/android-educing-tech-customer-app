package educing.tech.customer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import educing.tech.customer.R;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.ShippingAddress;


public class ShippingAddressRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    private Context context = null;
    private OnItemClickListener clickListener;
    private OnTaskCompleted listener;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;


    public ShippingAddressRecyclerAdapter(Context context, OnTaskCompleted listener)
    {
        this.context = context;
        this.listener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {


        if(i == TYPE_HEADER)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.footer_item_new_address, viewGroup, false);
            return new VHHeader(view);
        }

        else if(i == TYPE_ITEM)
        {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerlist_shipping_address, viewGroup, false);
            return new VersionViewHolder(view);
        }

        throw new RuntimeException("there is no type that matches the type " + i + " + make sure your using types correctly");
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i)
    {

        if( i == 0)
        {

            /*if (holder instanceof VHHeader)
            {
                VHHeader header = (VHHeader) holder;
            }*/

            return;
        }

        if(holder instanceof VersionViewHolder)
        {

            VersionViewHolder versionViewHolder = (VersionViewHolder) holder;

            ShippingAddress shipping_address = ShippingAddress.shippingAddressList.get(i-1);


            StringBuilder sAddress = new StringBuilder().append(shipping_address.getAddress().toUpperCase()).append(", ")
                    .append(shipping_address.getLandmark().toUpperCase()).append(", ").append(shipping_address.getCity().toUpperCase())
                    .append(", ").append(shipping_address.getState().toUpperCase()).append(", ").append(shipping_address.getPincode());


            versionViewHolder.name.setText(shipping_address.getName().toUpperCase());
            versionViewHolder.address.setText(sAddress);
            versionViewHolder.phone_no.setText(String.valueOf("M# " + shipping_address.getPhoneNo()));

            versionViewHolder.btn_continue.setTag(i);
        }
    }


    @Override
    public int getItemCount()
    {
        return ShippingAddress.shippingAddressList == null ? 0 : ShippingAddress.shippingAddressList.size() + 1;
    }


    class VHHeader extends RecyclerView.ViewHolder
    {

        Button btnAddNew;

        public VHHeader(View itemView)
        {

            super(itemView);

            btnAddNew = (Button) itemView.findViewById(R.id.btnAddNew);
            btnAddNew.setOnClickListener(onButtonClickListener);
        }


        private View.OnClickListener onButtonClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {

                if(v.getId() == R.id.btnAddNew)
                {
                    listener.onTaskCompleted(true, 300, "add");
                }
            }
        };
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        TextView name, address, phone_no;
        Button btn_continue;


        public VersionViewHolder(View itemView)
        {

            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            address = (TextView) itemView.findViewById(R.id.address);
            phone_no = (TextView) itemView.findViewById(R.id.phone_no);
            btn_continue = (Button) itemView.findViewById(R.id.btnContinue);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v)
        {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }


    @Override
    public int getItemViewType(int position)
    {

        if(position == 0)
        {
            return TYPE_HEADER;
        }

        return TYPE_ITEM;
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