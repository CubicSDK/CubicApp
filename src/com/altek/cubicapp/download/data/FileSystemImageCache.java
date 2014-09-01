package com.altek.cubicapp.download.data;

import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.ArrayList;

import android.database.sqlite.SQLiteDiskIOException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * A cache which stores image data on the device storage. Uses a
 * {@link ImageDatabase} as the backend.
 * 
 */
public class FileSystemImageCache {
  private static final String TAG = FileSystemImageCache.class.getSimpleName();

  private ImageDatabase imageDb;

  public FileSystemImageCache() {
    imageDb = ImageDatabase.get();
  }

  /**
   * Gets the photo with the given URL from the database.
   * 
   * @param url
   *          The {@link URL} of the photo to get.
   * @return The {@link Bitmap} object or <code>null</code>, if the database
   *         does not contain a Bitmap with the given URL.
   */

  public PData get(String url) {
	  PData p = new PData();
	    if (!imageDb.isReady()) {
	      return p;
	    }
	    PhotoCursor c = imageDb.query(url.toString());

	    if (c == null) {
	      return p;
	    }

	    if (!c.moveToFirst()) {
	      c.close();
	      return p;
	    }

	    p = c.getBitmapDownloadAndClose();
	    //return c.getBitmapAndClose();
	    return p;
	  }
  
  /**
   * Stores the image with the given URL and the modified/version String.
   * 
   * @param url
   *          The URL of the photo to be put.
   * @param modified
   *          The modified/version string.
   * @param image
   *          The image data.
   */
  private synchronized boolean put(String url, String modified, Bitmap image, int download ) {
    if (!imageDb.isReady()) {
      return false;
    }
    Log.i(TAG, "Attempting to put " + url.toString());
    if (imageDb.exists(url)) {
      Log.i(TAG, "ALREADY EXISTS!");
      return false;
    }

    Log.i(TAG, "Putting photo into DB.");
    try {
      return imageDb.put(url, modified, image , download ) != -1;
    } catch (SQLiteDiskIOException ex) {
      Log.w(TAG, "Unable to put photo in DB, disk full or unavailable.");
      return false;
    }
  }

  
  public boolean update(String url, int download ) {
	  
	  Log.i(TAG, "1Attempting to put " + url.toString());	  
	  
	    if (!imageDb.isReady()) {
	      return false;
	    }
	    Log.i(TAG, "Attempting to put " + url.toString());
	    if (!imageDb.exists(url)) {
	      Log.i(TAG, "NOT EXISTS!");
	      return false;
	    }

	    Log.i(TAG, "Putting photo into DB.");
	    try {
	  	  Log.i(TAG, "2Attempting to put " + url.toString());	  
	    	
	    	
	      return imageDb.update(url, download ) != -1;
	    } catch (SQLiteDiskIOException ex) {
	      Log.w(TAG, "Unable to put photo in DB, disk full or unavailable.");
	      return false;
	    }
	  }
  
  /**
   * Same as {@link #put(URL, String, Bitmap)} but returns immediately. The
   * actual putting is done asynchronously.
   * 
   * @param url
   *          The URL of the photo to be put.
   * @param modified
   *          The modified/version string.
   * @param photo
   *          The photo data.
   */
  private List<AsyncTask> runningTasks = new ArrayList<AsyncTask>();;
  
  public void onStop() {
      for( AsyncTask task : runningTasks ) {
         task.cancel(true);
      }
      runningTasks.clear();
  }
  
  public void asyncPut(final String url, final String modified, final Bitmap photo, final int download ) {
    if (!imageDb.isReady()) {
      return;
    }

    AsyncTask<Void, Integer, Void> task = new AsyncTask<Void, Integer, Void>() {

   	  @Override
   	  protected void onPreExecute() {
   		  runningTasks.add( this );
   	  }
    	
   	  @Override
      protected Void doInBackground(Void... params) {
   		  try{
   			  put(url, modified, photo, download );
   		  }
   		  catch(Exception e){
   			Log.i(TAG, "put data into database error " + e.toString());
   		  }
        return null;
      }
      
      @Override
      protected void onPostExecute(Void result) {
    	  runningTasks.remove( this );
      }

      
      @Override  
      protected void onCancelled() {  
    	  super.onCancelled();
      }    
      
    };
    
    task.execute();
  }
  
  
//  private final MessageManager messageManager = new MessageManager();	  
  
  /**
   * Manages a thread-safe message queue using a Looper worker thread to complete blocking tasks.
   */
/*  
  public class MessageManager implements Runnable {    	
      public Handler messageHandler;
      private final BlockingQueue<PData> messageQueue = new LinkedBlockingQueue<PData>();
      private Boolean isMessagePending = Boolean.valueOf(false);

      @Override
      public void run() {
          Looper.prepare();
          messageHandler = new Handler() {
              @Override
              public void handleMessage(Message msg) {
                  Log.w(this.getClass().getSimpleName(), "Please post() your blocking runnables to Mr Manager, " +
                          "don't use sendMessage()");
              }

          };
          Looper.loop();
      }

      private void consumeAsync() {
          messageHandler.post(new Runnable() {
              @Override
              public void run() {
                  synchronized (isMessagePending) {
                      if (isMessagePending.booleanValue()) {
                          return;
                      }

                      synchronized (messageQueue) {
                          if (messageQueue.size() == 0) {
                              return;
                          }
                          //XXXXXX
                      }

                      isMessagePending = Boolean.valueOf(true);
                  }
              }
          });
      }

      public void notifyAckReceivedAsync() {
          messageHandler.post(new Runnable() {
              @Override
              public void run() {
                  synchronized (isMessagePending) {
                      isMessagePending = Boolean.valueOf(false);
                  }
                  messageQueue.remove();
              }
          });
          consumeAsync();
      }

      public void notifyNackReceivedAsync() {
          messageHandler.post(new Runnable() {
              @Override
              public void run() {
                  synchronized (isMessagePending) {
                      isMessagePending = Boolean.valueOf(false);
                  }
              }
          });
          consumeAsync();
      }

      public boolean offer(final Integer data) {
          final boolean success = messageQueue.offer(data);

          if (success) {
              consumeAsync();
          }

          return success;
      }
  }
*/
  
}
