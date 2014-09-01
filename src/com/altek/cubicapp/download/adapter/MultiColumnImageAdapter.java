package com.altek.cubicapp.download.adapter;

import java.util.List;

import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import com.altek.cubicapp.R ;
import com.altek.cubicapp.download.PicViewConfig;
import com.altek.cubicapp.download.ThumbnailItem;
import com.altek.cubicapp.download.ThumbnailSlotView;
import com.altek.cubicapp.download.data.Photo;
import com.altek.cubicapp.download.request.CachedImageFetcher;
import com.altek.cubicapp.download.request.ImageLoadingTask;

/**
 * This adapter renders thumbnails and their description in as many columns as
 * possible.
 * 
 * @param <T>
 *          the type of the data items being displayed and returned by the
 *          callback
 */
public abstract class MultiColumnImageAdapter<T> extends BaseAdapter {
  public static interface ThumbnailClickListener<T> {
    public void thumbnailClicked(T object , boolean isChecked );
  }

  private static final String TAG = MultiColumnImageAdapter.class
      .getSimpleName();

  private final List<ThumbnailItem<T>> dataItems;
  private final LayoutInflater inflater;
  private final ThumbnailClickListener<T> listener;
  private final CachedImageFetcher cachedImageFetcher;
  private final int slotsPerRow;
  private final int slotWidth;

  /**
   * Instantiates a new MultiColumnImageAdapter.
   * 
   * @param dataItems
   *          the list of data items to display
   * @param inflater
   *          the inflater to be used to inflate the thumbnail slots
   * @param listener
   *          a listener that is notified when an album was clicked on
   * @param cachedImageFetcher
   *          used to fetch the thumbnail images
   * @param displayMetrics
   *          used to determine who many thumbnails can be display on a single
   *          row
   */
  public MultiColumnImageAdapter(List<ThumbnailItem<T>> dataItems,
      LayoutInflater inflater, ThumbnailClickListener<T> listener,
      CachedImageFetcher cachedImageFetcher, DisplayMetrics displayMetrics) {
    this.dataItems = dataItems;
    this.inflater = inflater;
    this.listener = listener;
    this.cachedImageFetcher = cachedImageFetcher;

    // Determine how many thumbnails can be put onto one row.
    float thumbnailWithPx = PicViewConfig.ALBUM_THUMBNAIL_SIZE
        * displayMetrics.density;
    slotsPerRow = (int) Math
        .floor(displayMetrics.widthPixels / thumbnailWithPx);
    Log.d(TAG, "Photos per row: " + slotsPerRow);
    slotWidth = displayMetrics.widthPixels / slotsPerRow;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LinearLayout row = (LinearLayout) convertView;
    ThumbnailSlotView[] slotViews = new ThumbnailSlotView[slotsPerRow];

    // If we are supposed to recycle the view, clear it first. We are
    // caching all the image views anyway.
    if (row != null && row.getChildCount() == slotsPerRow) {

      // We recycle all of them.
      for (int i = 0; i < slotsPerRow; ++i) {
        slotViews[i] = (ThumbnailSlotView) row.getChildAt(i);
      }
    } else {

      // Nothing to recycle, so we create a new row.
      row = new LinearLayout(inflater.getContext());
      row.setBackgroundColor(Color.BLACK);
      row.setOrientation(LinearLayout.HORIZONTAL);
      row.setGravity(Gravity.CENTER_HORIZONTAL);
      row.setPadding(0, 15, 0, 0);

      // We need to create new slots.
      for (int i = 0; i < slotsPerRow; ++i) {
        slotViews[i] = createNewSlotView(parent);
        row.addView(slotViews[i]);
      }
    }

    // Add the columns/slots to the row.
    for (int i = 0; i < slotsPerRow; ++i) {
      int dataIndex = (position * slotsPerRow) + i;
      if (dataIndex >= dataItems.size()) {
        break;
      }
      final ThumbnailItem<T> thumbnailData = dataItems.get(dataIndex);

      recycleSlotView(slotViews[i], thumbnailData);
    }
    return row;
  }

  @Override
  public long getItemId(int position) {
    return (long) Math.floor(position / slotsPerRow);
  }

  @Override
  public Object getItem(int position) {
    return null;
  }

  @Override
  public int getCount() {
    return (int) Math.ceil(dataItems.size() / (double) slotsPerRow);
  }

  /**
   * Recycles a slot view, if it already exists. This means, changing the title,
   * stopping a previous image loading task and instantiating a new image
   * loading task.
   * 
   * @param slot
   *          the slot to recycle
   * @param item
   *          the item that holds the data for the slot
   */
  private void recycleSlotView(ThumbnailSlotView slot, final ThumbnailItem<T> item) 
  {

	  final CheckBox checkDownload = (CheckBox) slot.findViewById(R.id.itemCheckBox);
	  checkDownload.setVisibility(View.VISIBLE);
	  final ImageView imgView = (ImageView)slot.findViewById(R.id.imageVideoFile);
	  
	  Photo p = (Photo)item.getDataObject();
	  checkDownload.setChecked(p.getCheck());
	  imgView.setVisibility( p.isVideoFile() ? View.VISIBLE : View.INVISIBLE) ;
/*	  
	  checkDownload.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener() {
		  @Override
		  public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
		  {
			  Log.i("testtest", "data is " + item.getDataObject() );
			  listener.thumbnailClicked(item.getDataObject() , isChecked );
			  cachedImageFetcher.setCheck( item.getThumbnailUrl() , isChecked ? 1 : 0 );
		  }
	  });
*/	  
	  checkDownload.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v)
		  {
			  Log.i("testtest", "data is " + item.getDataObject() );
			  listener.thumbnailClicked(item.getDataObject() , checkDownload.isChecked() );
		  }
	  });

	  
    slot.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
    	  checkDownload.performClick();
    	  //listener.thumbnailClicked(item.getDataObject());
      }
    });

//    TextView picTitle = (TextView) slot.findViewById(R.id.picture_title);
//    picTitle.setText(item.getTitle());
    
    
	ImageView download = (ImageView) slot.findViewById(R.id.image_download);
	download.setVisibility( View.INVISIBLE );
    
    // We need to cancel the UI update of the image loading task for this
    // slot, if one is present and instead set the loading icon.
	ImageLoadingTask previousLoadingTask = slot.getImageLoadingTask();
    if (previousLoadingTask != null) {
      previousLoadingTask.setCancelUiUpdate(true);
      previousLoadingTask.cancel(true);
    }

    ImageView albumThumbnail = (ImageView) slot
        .findViewById(R.id.album_thumbnail);

    // The ImageLoadingTask will load the thumbnail asynchronously and set
    // the result as soon as the response is in. The image will be set
    // immediately, if the result is already in cache.
    try {
    	ImageLoadingTask task = new ImageLoadingTask(albumThumbnail, download, item.getThumbnailUrl() , cachedImageFetcher);
      slot.setImageLoadingTask(task);
      task.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates a new slot view.
   * 
   * @param parent
   *          the parent of the created view
   */
  private ThumbnailSlotView createNewSlotView(ViewGroup parent) {
    // Inflate a new slot.
    ThumbnailSlotView slot = (ThumbnailSlotView) inflater.inflate(
        R.layout.picture_entry, parent, false);
    LayoutParams layoutParams = slot.getLayoutParams();
    layoutParams.width = slotWidth;
    slot.setLayoutParams(layoutParams);
    slot.setGravity(Gravity.CENTER_HORIZONTAL);
    slot.setId(R.layout.picture_entry);
    return slot;
  }
}
