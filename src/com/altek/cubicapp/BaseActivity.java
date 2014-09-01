package com.altek.cubicapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class BaseActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (debugActivitySwictchFlow()) dLog("onCreate") ;
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (debugActivitySwictchFlow()) dLog("onStart") ;
	}
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		if (debugActivitySwictchFlow()) dLog("onRestart") ;
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (debugActivitySwictchFlow()) dLog("onResume") ;

	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (debugActivitySwictchFlow()) dLog("onPause") ;

	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (debugActivitySwictchFlow()) dLog("onStop") ;

	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (debugActivitySwictchFlow()) dLog("onDestroy") ;

	}
	/**
	 * debug log with current class name
	 * @param msg
	 */
	public void dLog(String msg)
	{
		Log.d(getClass().getSimpleName(),msg) ;
	}
	/**
	 * error log with current class name
	 * @param msg
	 */
	public void errLog(String msg)
	{
		Log.e(getClass().getSimpleName(),msg) ;
	}
	/**
	* Overwrite this method to enable/disable the log message for onCreate,onStart,onResume,onPause,onStop,onDestroy 
	*/
	protected boolean debugActivitySwictchFlow() { return false; }
	
}
