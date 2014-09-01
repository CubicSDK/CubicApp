package com.altek.cubicapp.download.data;

import java.io.File;
import java.io.FileFilter;

import com.altek.cubicapp.download.PicViewConfig;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;


/**
 * Abstract super-class of all PicView databases.
 */
public class AbstractPicViewDatabase {
  private static final String TAG = AbstractPicViewDatabase.class
      .getSimpleName();
  
  private static final int DBVersion = 5;

  /**
   * Returns a usable database with the given name. If a database with this name
   * already exists, it is returned. Otherwise created with the given SQL create
   * query.
   */
  protected static SQLiteDatabase getUsableDataBase(String dbName,
      String sqlCreateQuery) {
    File dbFile = getPathToDb(dbName);

    File fileDirectory = new File(dbFile.getParent());
    if (!fileDirectory.exists()) {
      // Make sure the path for the file exists, before creating the
      // database.
      fileDirectory.mkdirs();
    }
    Log.d(TAG, "DB Path: " + dbFile.getAbsolutePath());
    boolean initDb = !dbFile.exists();
    try {
      SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(dbFile, null);

      
      if (initDb) {
    	  Log.d(TAG, "create table" );
        result.execSQL(sqlCreateQuery);
        result.setVersion(DBVersion);
      }
      else{
    	  
    	  int Version = result.getVersion();
    	  
    	  if( Version < DBVersion ){
        	  Log.d(TAG, "drop table and create new one " + Version );

    		  //remove and update
    		  result.close();
    		  deleteDatabase(dbFile);
    		  result = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
    		  result.execSQL(sqlCreateQuery);
    	      result.setVersion(DBVersion);
    	  }
      }

      return result;
    } catch (SQLiteException ex) {
      Log.w(TAG, "Could not open or image database.");
      return null;
    }
  }

  /**
   * Returns a file for the data base with the given name.
   */
  protected static File getPathToDb(String dbName) {
    String sdCardPath = Environment.getExternalStorageDirectory()
        .getAbsolutePath();
    return new File(sdCardPath + File.separator + "data" + File.separator
        + PicViewConfig.APP_NAME_PATH + File.separator + dbName);
  }
  protected static void delete(String dbName)
  {
	    File dbFile = getPathToDb(dbName);

	    File fileDirectory = new File(dbFile.getParent());
	    if (!fileDirectory.exists()) return;
	    if ( !dbFile.exists() ) return ;
	    
  		deleteDatabase(dbFile);
	    
  }
  public static boolean deleteDatabase(File file) {
      if (file == null) {
          throw new IllegalArgumentException("file must not be null");
      }

      boolean deleted = false;
      deleted |= file.delete();
      deleted |= new File(file.getPath() + "-journal").delete();
      deleted |= new File(file.getPath() + "-shm").delete();
      deleted |= new File(file.getPath() + "-wal").delete();

      File dir = file.getParentFile();
      if (dir != null) {
          final String prefix = file.getName() + "-mj";
          final FileFilter filter = new FileFilter() {
              @Override
              public boolean accept(File candidate) {
                  return candidate.getName().startsWith(prefix);
              }
          };
          for (File masterJournal : dir.listFiles(filter)) {
              deleted |= masterJournal.delete();
          }
      }
      return deleted;
  }
 
}
