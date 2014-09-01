package com.altek.cubicapp.download.data;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Keeps a cursor that is used in the image database.
 */
public class PhotoCursor {
  private final Cursor cursor;
  private final String columnBitmap;
  private final String download;

  public PhotoCursor(Cursor cursor, String columnBitmap, String download ) {
    this.cursor = cursor;
    this.columnBitmap = columnBitmap;
    this.download = download;
  }

  public boolean moveToFirst() {
    return cursor != null && cursor.moveToFirst();
  }

  public void close() {
    if (cursor != null) {
      cursor.close();
    }
  }

/*  
  public Bitmap getBitmapAndClose() {
    byte[] data = cursor.getBlob(cursor.getColumnIndex(columnBitmap));
    close();
    return BitmapFactory.decodeByteArray(data, 0, data.length);
  }
*/  
  
  public PData getBitmapDownloadAndClose() {
	  PData p = new PData();
	    byte[] data = cursor.getBlob(cursor.getColumnIndex(columnBitmap));
	    p.bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
	    p.download = cursor.getInt(cursor.getColumnIndex(download));
	    close();
	    return p;
	  }
}

