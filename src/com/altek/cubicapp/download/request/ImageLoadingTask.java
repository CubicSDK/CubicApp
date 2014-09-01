package com.altek.cubicapp.download.request;

import java.net.URL;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.CheckBox;

import com.altek.cubicapp.R;
import com.altek.cubicapp.download.data.PData;


/**
 * An asynchronous task that loads an image from the given URL.
 */
public class ImageLoadingTask extends AsyncTask<Void, Integer, Void> {

  private final ImageView imageView;
  private final ImageView downloadView;
  private final String url;
  private final CachedImageFetcher cachedImageFetcher;
  private Bitmap bitmap;
  private boolean cached = false;
  private ProgressDialog progressDialog;
  private boolean cancelUiUpdate = false;
  private int download;

  /**
   * Creates a new image loading task.
   * 
   * @param imageView
   *          the view on which to set the image once it is loaded
   * @param url
   *          the URL of the image
   * @param cachedImageFetcher
   *          the image fetcher and cache to use
   * @param progressDialog
   *          optional loading message. Shows a loading message if this is not
   *          null
   */
  public ImageLoadingTask(ImageView imageView,ImageView downloadView, String url,
      CachedImageFetcher cachedImageFetcher, ProgressDialog progressDialog) {
    this.imageView = imageView;
    this.downloadView = downloadView;
    this.url = url;
    this.cachedImageFetcher = cachedImageFetcher;
    this.progressDialog = progressDialog;
  }

  /**
   * Creates a new image loading task.
   * 
   * @param imageView
   *          the view on which to set the image once it is loaded
   * @param url
   *          the URL of the image
   * @param cachedImageFetcher
   *          the image fetcher and cache to use
   */
  public ImageLoadingTask(ImageView imageView, ImageView downloadView, String url,
      CachedImageFetcher cachedImageFetcher) {
    this(imageView, downloadView , url, cachedImageFetcher, null);
  }

  /**
   * When set to true the {@link ImageLoadingTask} will not set the image bitmap
   * in the image view.
   * <p>
   * This only applies to fetches from the net. When the image is in cache, it
   * is set immediately anyway.
   */
  public void setCancelUiUpdate(boolean cancelUiUpdate) {
    this.cancelUiUpdate = cancelUiUpdate;
  }

  @Override
  protected void onPreExecute() {
    if (cachedImageFetcher.isCached(url)) {
    	PData p = cachedImageFetcher.cachedFetchImage(url,true);
      bitmap = p.bmp;
      imageView.setImageBitmap(bitmap);

      if( p.download == 0 )
    	  downloadView.setVisibility(View.INVISIBLE);
      if( p.download == 1 )
    	  downloadView.setVisibility(View.VISIBLE);
      
      cached = true;
    } else {
      if (progressDialog != null) {
        // TODO: This sometimes throws a window leaked error, when
        // activities are quickly switched around.
        progressDialog.show();
      }
      imageView.setImageResource(R.drawable.loading);
    }
  }

  
  @Override
  protected Void doInBackground(Void... params) {
    if (!cached) {
   	PData p = cachedImageFetcher.cachedFetchImage(url,false);
      bitmap = p.bmp;
      download = p.download;
    }
    return null;
  }

  @Override
  protected void onPostExecute(Void result) {
    if (!cached && !cancelUiUpdate) {
    	
        if( download == 0 )
      	  downloadView.setVisibility(View.INVISIBLE);
        if( download == 1 )
      	  downloadView.setVisibility(View.VISIBLE);
      if (bitmap==null)
      {
    	 // imageView.setImageDrawable(getContext().getResource().getDrawable(android.R.drawable.stat_notify_error));
      }
      else imageView.setImageBitmap(bitmap);
      
    }
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.hide();
    }
  }
  @Override
  protected void onCancelled() {
	  super.onCancelled();
  }
  
}
