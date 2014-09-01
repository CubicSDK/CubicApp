package com.altek.cubicapp.download.data;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * A data base that stores image data.
 * 
 */
public class ImageDatabase extends AbstractPicViewDatabase {
  private static final String DATABASE_NAME = "photos_cache.db";
  private static final String TABLE_NAME = "photos";

  private static final String COLUMN_URL = "url";
  private static final String COLUMN_MODIFIED = "modified";
  private static final String COLUMN_BITMAP = "bitmap";
  private static final String COLUMN_DOWNLOAD = "download";
  
  private static final String[] ALL_COLUMNS = { COLUMN_URL, COLUMN_MODIFIED,
      COLUMN_BITMAP ,  COLUMN_DOWNLOAD };

  private static ImageDatabase imageDb;

  private SQLiteDatabase db;

  protected ImageDatabase(SQLiteDatabase db) {
    this.db = db;
  }

  /**
   * Returns the singleton instance of the {@link ImageDatabase}.
   */
  public static ImageDatabase get() {
    if (imageDb == null) {
    	Log.d("jmtets","create db");
      imageDb = new ImageDatabase(getUsableDataBase(DATABASE_NAME,
          "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_URL
              + " TEXT PRIMARY KEY," + COLUMN_MODIFIED + " TEXT,"
              + COLUMN_BITMAP + " BLOB,"
              + COLUMN_DOWNLOAD + " INTEGER);"));
    }
    return imageDb;
  }
  public static void delete()
  {
	  delete(DATABASE_NAME) ;
	  imageDb=null;
  }
  /**
   * Queries for a photo with the given URL.
   */
  public PhotoCursor query(String url) {
    return new PhotoCursor(db.query(true, TABLE_NAME, ALL_COLUMNS, COLUMN_URL + " = '" + url + "'", null, null, null, null, null), COLUMN_BITMAP , COLUMN_DOWNLOAD );
  }

  /**
   * Puts an image into the database.
   * 
   * @param url
   *          The URL of the image.
   * @param modified
   *          The version key of the image.
   * @param image
   *          The image to store.
   * @return The row.
   */
  public long put(String url, String modified, Bitmap image, int download ) {
    ContentValues values = new ContentValues();
    values.put(COLUMN_URL, url.toString());
    values.put(COLUMN_MODIFIED, modified);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
    values.put(COLUMN_BITMAP, outputStream.toByteArray());
    values.put(COLUMN_DOWNLOAD, download );

    return db.replace(TABLE_NAME, COLUMN_BITMAP, values);
  }

  
  
  public long update(String url, int download ) {
	    ContentValues values = new ContentValues();
	    values.put(COLUMN_DOWNLOAD, download );
	    return db.update(TABLE_NAME, values , COLUMN_URL + " = '" + url + "'" , null);
  }
  
  /**
   * Whether an image with the given URL exists.
   */
  public boolean exists(String url) {
    PhotoCursor c = query(url.toString());
    if (c == null) {
      return false;
    }

    boolean exists = c.moveToFirst();
    c.close();
    return exists;
  }

  /**
   * Returns whether this database is ready to be used.
   */
  public boolean isReady() {
    return db != null;
  }

}
