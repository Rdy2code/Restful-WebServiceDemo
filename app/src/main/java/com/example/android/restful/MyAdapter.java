package com.example.android.restful;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.restful.model.DataItem;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Includes Picasso for retrieving and storing images for loading in into the recyclerview
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<DataItem> mItems;
    private Context mContext;

    public static final String PHOTOS_BASE_URL =
            "http://560057.youcanlearnit.net/services/images/";

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

            String url = PHOTOS_BASE_URL + item.getImage();

            //Download image on background thread, cache the image, and display the image
            Picasso.get().load(url).resize(50, 50).into(holder.imageView);

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
}
