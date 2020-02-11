package com.example.android.restful;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.restful.model.DataItem;
import com.example.android.restful.utils.ImageCacheManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<DataItem> mItems;
    private Context mContext;

    //store and persist each image for the lifetime of the activity
    //private Map<String, Bitmap> mBitmaps = new HashMap<>();

    public MyAdapter (Context context, List<DataItem> items) {
        this.mItems = items;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
        final DataItem item = mItems.get(position);
        try {
            holder.tvView.setText(item.getItemName());

            Bitmap bitmap = ImageCacheManager.getBitmap(mContext, item);
            if (bitmap == null) {
                ImageDownloaderTask task = new ImageDownloaderTask();
                task.setViewHolder(holder);
                task.execute(item);
            } else {
               holder.imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  {

        public TextView tvView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            tvView = itemView.findViewById(R.id.textView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    /**
     * Each time an image is needed, execute the AsyncTask, then save it to the app
     */
    private class ImageDownloaderTask extends AsyncTask <DataItem, Void, Bitmap> {

        public static final String PHOTOS_BASE_URL =
                "http://560057.youcanlearnit.net/services/images/";
        private DataItem mDataItem;
        private MyAdapter.ViewHolder mHolder;   //Reference to the current row

        public void setViewHolder (MyAdapter.ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(DataItem... dataItems) {
            mDataItem = dataItems[0];
            String imageUrl = PHOTOS_BASE_URL + mDataItem.getImage();
            InputStream inputStream = null;

            try {
                //Download and return the Bitmap
                inputStream = (InputStream) new URL(imageUrl).getContent();
                return BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mHolder.imageView.setImageBitmap(bitmap);   //Attach the image to the view
            ImageCacheManager.putBitmap(mContext, mDataItem, bitmap);
        }
    }
}
