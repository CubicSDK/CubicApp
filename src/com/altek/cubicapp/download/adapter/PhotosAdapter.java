package com.altek.cubicapp.download.adapter;

import java.util.List;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;

import com.altek.cubicapp.download.ThumbnailItem;
import com.altek.cubicapp.download.data.Photo;
import com.altek.cubicapp.download.request.CachedImageFetcher;

/**
 * The controller for the photos list.
 */
public class PhotosAdapter extends MultiColumnImageAdapter<Photo> {

  public PhotosAdapter(List<ThumbnailItem<Photo>> dataItems,
      LayoutInflater inflater, ThumbnailClickListener<Photo> listener,
      CachedImageFetcher cachedImageFetcher, DisplayMetrics displayMetrics) {
    super(dataItems, inflater, listener, cachedImageFetcher, displayMetrics);
  }
}
