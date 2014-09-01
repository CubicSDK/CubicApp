package com.altek.cubicapp.download;

import com.altek.cubicapp.download.request.ImageLoadingTask;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


/**
 * A view for showing a thumbnail in e.g. a list view.
 */

//public class ThumbnailSlotView extends LinearLayout {
public class ThumbnailSlotView extends RelativeLayout {

  private ImageLoadingTask imageLoadingTask;

  public ThumbnailSlotView(Context context, AttributeSet attributes) {
    super(context, attributes);
  }

  /**
   * Sets the {@link ImageLoadingTask} that is currently loading content for
   * this view.
   */
  public void setImageLoadingTask(ImageLoadingTask task) {
    imageLoadingTask = task;
  }

  /**
   * Gets the {@link ImageLoadingTask} that is currently loading content for
   * this view, or <code>null</code>.
   */
  public ImageLoadingTask getImageLoadingTask() {
    return imageLoadingTask;
  }

}

