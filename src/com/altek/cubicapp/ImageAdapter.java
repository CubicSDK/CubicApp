package com.altek.cubicapp;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private String[] data;
    public ImageAdapter(Context c, String[] d) {
        mContext = c;
        data=d;
    }

    public int getCount() {
        return data.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams(120, 90));           
            imageView.setPadding(8, 8, 8, 8);

        } else {
            imageView = (ImageView) convertView;
        }

        File imgFile = new  File(""+data[position]);
        Log.d("File....",imgFile.getAbsolutePath() );
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        //ImageView myImage = (ImageView) findViewById(R.id.imageQV2);
        imageView.setImageBitmap(myBitmap);
        
        return imageView;

    }


}