package com.example.hospiton;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SliderAdapterExample extends SliderViewAdapter<SliderAdapterExample.SliderAdapterVH> {

    private List<Uri>ImageUri;
    private int count=1;


    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_layout, parent,false);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {
        Uri uri=null;

        if (ImageUri != null)
        {
            uri=ImageUri.get(position);
            Picasso.get().load(uri).fit().into(viewHolder.imageViewBackground);
        }
    }

    public void datachanged(List<Uri>ImageUri)
    {
        this.ImageUri=ImageUri;
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size

        if(ImageUri!=null && count!=ImageUri.size())
        {
            Log.d("Slider",String.valueOf(ImageUri.size()));
            count=ImageUri.size();
            notifyDataSetChanged();
        }

        return count;
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.slider_imageview);
            this.itemView = itemView;
        }
    }
}