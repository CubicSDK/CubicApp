package com.altek.cubicapp.download;


/**
 * Classes implementing this interface can be used to be shown e.g. in list
 * views that show thumbnails.
 */
public class ThumbnailItem<T> {
  private String title;
  private String thumbnailUrl;
  private T dataObject;

  /**
   * Initializes the thumbnail item with the given values
   */
  public ThumbnailItem(String title, String thumbnailUrl, T dataObject) {
    this.title = title;
    this.thumbnailUrl = thumbnailUrl;
    this.dataObject = dataObject;
  }

  /**
   * Returns the title of the thumbnail item.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the URL to the thumbnail.
   */
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  /**
   * Returns the data object associated with this thumbnail item.
   */
  public T getDataObject() {
    return dataObject;
  }
}
