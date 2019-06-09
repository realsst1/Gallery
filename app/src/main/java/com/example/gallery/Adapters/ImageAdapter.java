package com.example.gallery.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.gallery.Models.PhotoResult;
import com.example.gallery.R;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    PhotoResult photoResult;
    Context context;

    public ImageAdapter(PhotoResult photoResult) {
        this.photoResult = photoResult;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_layout_single,viewGroup,false);
        context=viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder viewHolder, int i) {

        viewHolder.setImage(photoResult.getPhotos().getPhoto().get(i).getUrl_s());

    }

    @Override
    public int getItemCount() {
        return photoResult.photos.photo.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
        }
        public void setImage(String uri){
            ImageView imageView=(ImageView)view.findViewById(R.id.imageViewSingle);
            Glide.with(context).load(uri).into(imageView);
        }
    }
}
