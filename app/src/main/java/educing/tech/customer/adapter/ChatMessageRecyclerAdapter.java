package educing.tech.customer.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;

import educing.tech.customer.R;
import educing.tech.customer.activities.PinchZoomActivity;
import educing.tech.customer.helper.Blur;
import educing.tech.customer.helper.OnTaskCompleted;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.network.InternetConnectionDetector;

import static educing.tech.customer.configuration.Configuration.MEDIA_DIRECTORY_NAME;
import static educing.tech.customer.configuration.Configuration.CHAT_IMAGE_URL;


public class ChatMessageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{

    private static final int TYPE_TEXT = 1;
    private static final int TYPE_IMAGE = 2;

    private Context context = null;
    private OnTaskCompleted listener;
    private OnItemClickListener clickListener;

    private List<ChatMessage> chatMessageList;

    private String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + MEDIA_DIRECTORY_NAME + "/";


    public ChatMessageRecyclerAdapter(Context context, OnTaskCompleted listener, List<ChatMessage> chatMessageList)
    {
        this.context = context;
        this.listener = listener;
        this.chatMessageList = chatMessageList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {

        if(i == TYPE_IMAGE)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_message_image, viewGroup, false);
            return new ImageViewHolder(view);
        }

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_chat_message_text, viewGroup, false);
        return new VersionViewHolder(view);
    }


    @Override
    public int getItemViewType(int position)
    {

        if(chatMessageList.get(position).image.equals(""))
        {
            return TYPE_TEXT;
        }

        return TYPE_IMAGE;
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i)
    {

        ChatMessage chatMessageObj = chatMessageList.get(i);

        if(holder instanceof VersionViewHolder)
        {

            VersionViewHolder versionViewHolder = (VersionViewHolder) holder;

            versionViewHolder.chatText.setText(chatMessageObj.message);
            versionViewHolder.singleMessageContainerMain.setGravity(chatMessageObj.message_type > 0 ? Gravity.END : Gravity.START);

            try
            {
                String datetime = new SimpleDateFormat("hh:mm a").format(Long.parseLong(chatMessageObj.getTimestamp()));
                versionViewHolder.time.setText(datetime);
            }

            catch (Exception e)
            {

            }


            if (chatMessageObj.message_type == 0)
            {

                versionViewHolder.status.setVisibility(View.GONE);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(0, 0, 40, 0);
                versionViewHolder.singleMessageContainerMain.setLayoutParams(params);
                versionViewHolder.singleMessageContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.round_border_layout_incoming_message));
            }

            else
            {

                versionViewHolder.status.setVisibility(View.VISIBLE);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(40, 0, 0, 0);
                versionViewHolder.singleMessageContainerMain.setLayoutParams(params);
                versionViewHolder.singleMessageContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.round_border_layout_outgoing_message));

                if (chatMessageObj.sync_status == 1)
                {
                    versionViewHolder.status.setImageResource(R.drawable.ic_check_all_grey600_18dp);
                }

                else if (chatMessageObj.sync_status == 0)
                {
                    versionViewHolder.status.setImageResource(R.drawable.ic_check_grey600_18dp);
                }
            }
        }

        else if(holder instanceof ImageViewHolder)
        {

            final ImageViewHolder imageViewHolder = (ImageViewHolder) holder;

            imageViewHolder.singleMessageContainerMain.setGravity(chatMessageObj.message_type > 0 ? Gravity.END : Gravity.START);
            imageViewHolder.image.setTag(i);
            imageViewHolder.layout_download.setTag(i);


            if(fileExist(chatMessageObj.image))
            {

                imageViewHolder.layout_download.setVisibility(View.GONE);

                try
                {

                    File f = new File(PATH + chatMessageObj.image);
                    Picasso.with(context).load(f).resize(280, 280)
                            .into(imageViewHolder.image);
                }

                catch(Exception e)
                {

                }
            }

            else
            {

                imageViewHolder.layout_download.setVisibility(View.VISIBLE);

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
                    .load(CHAT_IMAGE_URL + chatMessageObj.image) // thumbnail url goes here
                    .resize(280, 280)
                    .transform(blurTransformation)
                    .into(imageViewHolder.image, new Callback() {

                        @Override
                        public void onSuccess()
                        {

                        }

                        @Override
                        public void onError() {

                        }
                    });
            }


            try
            {
                String datetime = new SimpleDateFormat("hh:mm a").format(Long.parseLong(chatMessageObj.getTimestamp()));
                imageViewHolder.time.setText(datetime);
            }

            catch (Exception e)
            {

            }


            if (chatMessageObj.message_type == 0)
            {

                imageViewHolder.status.setVisibility(View.GONE);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(0, 0, 40, 0);
                imageViewHolder.singleMessageContainerMain.setLayoutParams(params);
                imageViewHolder.singleMessageContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.round_border_layout_incoming_message));
            }

            else
            {

                imageViewHolder.status.setVisibility(View.VISIBLE);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(40, 0, 0, 0);
                imageViewHolder.singleMessageContainerMain.setLayoutParams(params);
                imageViewHolder.singleMessageContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.round_border_layout_outgoing_message));

                if (chatMessageObj.sync_status == 1)
                {
                    imageViewHolder.status.setImageResource(R.drawable.ic_check_all_grey600_18dp);
                }

                else if (chatMessageObj.sync_status == 0)
                {
                    imageViewHolder.status.setImageResource(R.drawable.ic_check_grey600_18dp);
                }
            }
        }

        //versionViewHolder.singleMessageContainer.setBackgroundResource(chatMessageObj.left ? R.drawable.bubble_b : R.drawable.bubble_a);
    }


    @Override
    public int getItemCount()
    {
        return chatMessageList == null ? 0 : chatMessageList.size();
    }


    class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        TextView time, download_percent;
        ImageView image, status;
        LinearLayout singleMessageContainer, singleMessageContainerMain, layout_download;
        RelativeLayout layout_download_progress;
        ProgressBar circularProgressBar;

        public ImageViewHolder(View itemView)
        {

            super(itemView);

            singleMessageContainerMain = (LinearLayout) itemView.findViewById(R.id.singleMessageContainerMain);
            singleMessageContainer = (LinearLayout) itemView.findViewById(R.id.singleMessageContainer);
            time = (TextView) itemView.findViewById(R.id.time);
            status = (ImageView) itemView.findViewById(R.id.status);
            image = (ImageView) itemView.findViewById(R.id.image);
            layout_download = (LinearLayout) itemView.findViewById(R.id.layout_download);
            layout_download_progress = (RelativeLayout) itemView.findViewById(R.id.layout_download_progress);
            circularProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar2);
            download_percent = (TextView) itemView.findViewById(R.id.download_percent);


            itemView.setOnClickListener(this);
            image.setOnClickListener(onButtonClickListener);
            layout_download.setOnClickListener(onDownloadClickListener);
        }


        private View.OnClickListener onButtonClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {

                int pos = (int) image.getTag();

                if(v.getId() == R.id.image)
                {

                    if(fileExist(chatMessageList.get(pos).image))
                    {
                        Intent intent = new Intent(context, PinchZoomActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("URL", PATH + chatMessageList.get(pos).image);
                        context.startActivity(intent);
                    }
                }
            }
        };


        private View.OnClickListener onDownloadClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                int pos = (int) layout_download.getTag();

                if(new InternetConnectionDetector(context).isConnected())
                {
                    myAsyncTask myWebFetch = new myAsyncTask();
                    myWebFetch.execute(CHAT_IMAGE_URL + chatMessageList.get(pos).image, chatMessageList.get(pos).image);
                }

                else
                {
                    Toast.makeText(context, "Internet Connection Failure", Toast.LENGTH_LONG).show();
                }
            }
        };


        @Override
        public void onClick(View v)
        {
            clickListener.onItemClick(v, getAdapterPosition());
        }


        class myAsyncTask extends AsyncTask<String, String, String>
        {

            @Override
            protected void onPostExecute(String result)
            {
                layout_download_progress.setVisibility(View.GONE);
                listener.onTaskCompleted(true, 200, "refresh");
                super.onPostExecute(result);
            }


            @Override
            protected void onPreExecute()
            {

                layout_download.setVisibility(View.GONE);
                layout_download_progress.setVisibility(View.VISIBLE);

                super.onPreExecute();
            }

            @Override
            protected void onProgressUpdate(String... progress)
            {
                circularProgressBar.setProgress(Integer.parseInt(progress[0]));
                download_percent.setText(String.valueOf(progress[0] + "%"));
            }


            protected String doInBackground(String... args)
            {

                try
                {

                    //set the download URL, a url that points to a file on the internet
                    //this is the file to be downloaded
                    URL url = new URL(args[0]);

                    //create the new connection
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    //set up some things on the connection
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);

                    //and connect!
                    urlConnection.connect();

                    //set the path where we want to save the file
                    //in this case, going to save it on the root directory of the
                    //sd card.
                    //File SDCardRoot = Environment.getExternalStorageDirectory();

                    // External sdcard location
                    File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), MEDIA_DIRECTORY_NAME);
                    // File noMedia = new File ( Environment.getExternalStoragePublicDirectory(DIRECTORY_NAME), ".nomedia" );

                    // Create the storage directory if it does not exist
                    if (!mediaStorageDir.exists())
                    {

                        if (!mediaStorageDir.mkdirs())
                        {
                            //Log.d(DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                            return null;
                        }

                        /*if (!noMedia.exists())
                        {
                            FileOutputStream noMediaOutStream = new FileOutputStream ( noMedia );
                            noMediaOutStream.write ( 0 );
                            noMediaOutStream.close();
                        }*/
                    }

                    //create a new file, specifying the path, and the filename
                    //which we want to save the file as.
                    File file = new File(mediaStorageDir, args[1]);

                    //this will be used to write the downloaded data into the file we created
                    FileOutputStream fileOutput = new FileOutputStream(file);

                    //this will be used in reading the data from the internet
                    InputStream inputStream = urlConnection.getInputStream();

                    //this is the total size of the file
                    int totalSize = urlConnection.getContentLength();

                    //variable to store total downloaded bytes
                    int downloadedSize = 0;

                    //create a buffer...
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0; //used to store a temporary size of the buffer

                    //now, read through the input buffer and write the contents to the file
                    while ( (bufferLength = inputStream.read(buffer)) > 0 )
                    {

                        //add the data in the buffer to the file in the file output stream (the file on the sd card
                        fileOutput.write(buffer, 0, bufferLength);

                        //add up the size so we know how much is downloaded
                        downloadedSize += bufferLength;

                        //this is where you would do something to report the prgress, like this maybe
                        //updateProgress(downloadedSize, totalSize);

                        // After this onProgressUpdate will be called
                        publishProgress("" + ((downloadedSize * 100) / totalSize));
                    }

                    //close the output stream when done
                    fileOutput.close();

                    //catch some possible errors...
                }

                catch (MalformedURLException e)
                {
                    e.printStackTrace();
                }

                catch (IOException e)
                {
                    e.printStackTrace();
                }

                return null;
            }
        }
    }


    class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        TextView chatText;
        TextView time;
        ImageView status;
        LinearLayout singleMessageContainer, singleMessageContainerMain;

        public VersionViewHolder(View itemView)
        {

            super(itemView);

            singleMessageContainerMain = (LinearLayout) itemView.findViewById(R.id.singleMessageContainerMain);
            singleMessageContainer = (LinearLayout) itemView.findViewById(R.id.singleMessageContainer);
            chatText = (TextView) itemView.findViewById(R.id.singleMessage);
            time = (TextView) itemView.findViewById(R.id.time);
            status = (ImageView) itemView.findViewById(R.id.status);

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


    private boolean fileExist(String file_name)
    {

        File imgFile = new File(PATH + file_name);
        {
            return imgFile.exists();
        }
    }
}