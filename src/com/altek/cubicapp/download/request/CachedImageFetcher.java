package com.altek.cubicapp.download.request;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.altek.cubicapp.download.data.FileSystemImageCache;
import com.altek.cubicapp.download.data.PData;

/**
 * This class should be use to fetch images. It makes use of the file-system and
 * in-memory cache to load images.
 */
public class CachedImageFetcher {
	
  public interface ImgControlInterface
  {
	  public Bitmap getImageSub(int objId);
  }
	
  private static final String TAG = CachedImageFetcher.class.getSimpleName();

  //private HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
  private HashMap<String, SoftReference<PData>> cache = new HashMap<String, SoftReference<PData>>();

  /** Used to synchronize access based on URLs. */
//  private HashMap<String, URL> urls = new HashMap<String, URL>();

  private FileSystemImageCache fileSystemCache;
  private ImgControlInterface imgHandler;
  /**
   * Instantiated the {@link CachedImageFetcher}.
   * 
   * @param fileSystemCache
   *          the cache to use as a fallback, if the given value could not be
   *          found in memory
   */
  public CachedImageFetcher(FileSystemImageCache fileSystemCache,ImgControlInterface imgHandler) {
    this.fileSystemCache = fileSystemCache;
    this.imgHandler=imgHandler ;
  }

  public void clearCache(){
	  cache.clear();
  }
  public void onStop()
  {
	  fileSystemCache.onStop() ;
  }
  /**
   * Performs a cached fetch. If the image is in one of the caches (file-system
   * or in-memory), this version is returned. If the image could not be found in
   * cache, it's fetched and automatically put into both caches.
   */

  public void updateDownloadFlag(String id ) {
	  fileSystemCache.update(id, 1 );
  }
  
  
//  public Bitmap cachedFetchImage(String id ) {
  public PData cachedFetchImage(String id ,boolean inUIThread) {  
    // Make sure we have a URL object that we can synchronize on.

    // Synchronize per URL.
    synchronized (id) {
      // Get it from memory, if we still have it.
      if (cache.containsKey(id) && cache.get(id).get() != null) {
        return cache.get(id).get();
      }

/*      
      // If it's not in memory, try to load it from file system.
      Bitmap bitmap = fileSystemCache.get(id);
      // If it is also not found in the file system cache, try to fetch it
      // from the network.
      if (bitmap == null) {
        bitmap = fetchImageFromWeb(id);
        if (bitmap != null) {
          fileSystemCache.asyncPut(id, "TODO", bitmap , 0 );
        }
      }
      if (bitmap != null) {
        cache.put(id, new SoftReference<Bitmap>(bitmap));
      }
      return bitmap;
*/      
      // If it's not in memory, try to load it from file system.
      PData pdata = fileSystemCache.get(id);
      // If it is also not found in the file system cache, try to fetch it
      // from the network.
      if (!inUIThread)
      {
	      if ( pdata.bmp == null) {
	        pdata.bmp = fetchImageFromWeb(id);
	        if ( pdata.bmp != null) {
	          fileSystemCache.asyncPut(id, "TODO", pdata.bmp , 0 );
	        }
	      }
      }
	  if ( pdata.bmp != null) {
        cache.put(id, new SoftReference<PData>(pdata));
      }
      return pdata;

    }
  }

  /**
   * Returns whether the image with the given URL exists in cache.
   */
  public boolean isCached(String url) {
    return cache.containsKey(url);
  }

  
  /**
   * Fetches the given image from the web.
   */
  private Bitmap fetchImageFromWeb( String id ) {
	//  return RemoteControlUIActivity.getimage( Integer.parseInt(id) );
	  return imgHandler.getImageSub(Integer.parseInt(id)) ;
  }
}
