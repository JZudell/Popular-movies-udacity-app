package com.example.computeraccount.newmovieratings;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Computeraccount on 9/17/15.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<MovieObject> movieObjects;

    public final int ITEM_WIDTH = 100;
    public final int ITEM_HEIGHT = 280;

    public ImageAdapter(Context c) {

        mContext = c;
        movieObjects = new ArrayList<>();
    }

    public void addAll(Collection x) {
        movieObjects.clear();
        movieObjects.addAll(x);
        notifyDataSetChanged();
    }

    public int getCount() {
        return movieObjects.size();
    }

    public Object getItem(int position) {
        return movieObjects.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;


        if (convertView == null) {

            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getPx(ITEM_HEIGHT, this.mContext)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } else {
            imageView = (ImageView) convertView;
        }

        String path = movieObjects.get(position).getPosterPath();

        Picasso.with(this.mContext).load(path).into(imageView);
        return imageView;
    }

    public int getPx(int dimensionDp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dimensionDp * density + 0.5f);
    }

}

