package com.altek.cubicapp;

import android.app.Application;

public class CubicApp extends Application {
	



	public final static int CONNECTION_STATE_NOT_CONNECT =1;
	public final static int CONNECTION_STATE_WIFI_CONNECT_OK =2;
	public final static int CONNECTION_STATE_PTPIP_CONNECT_OK =3;
	public final static int CONNECTION_STATE_GO_CLOSE = 4;
	
	int connectionState ;
	int connectNetworkID;
	String cubicMacAddress ;
	
	public int getConnectionState() {
		return connectionState;
	}

	public void setConnectionState(int connectionState) {
		this.connectionState = connectionState;
	}	
	public int getConnectNetworkID() {
		return connectNetworkID;
	}

	public void setSonnectNetworkID(int connectNetworkID) {
		this.connectNetworkID = connectNetworkID;
	}	
	/*  from android document , onTerminate only call on emulator.  and never call on device */
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

//	public String getCubicMacAddress() {
//		return cubicMacAddress;
//	}
//
//	public void setCubicMacAddress(String cubicMacAddress) {
//		this.cubicMacAddress = cubicMacAddress;
//	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		connectionState=CONNECTION_STATE_NOT_CONNECT ;
		
		/*
		this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks(){

			@Override
			public void onActivityCreated(Activity activity,
					Bundle savedInstanceState) {
				// TODO Auto-generated method stub
				Log.d("jmtest","app find : "+ activity.getClass().getSimpleName()+"onCreate") ;
				
			}

			@Override
			public void onActivityStarted(Activity activity) {
				// TODO Auto-generated method stub
				Log.d("jmtest","app find : "+ activity.getClass().getSimpleName()+"onStart") ;
				
			}

			@Override
			public void onActivityResumed(Activity activity) {
				// TODO Auto-generated method stub
				Log.d("jmtest","app find : "+ activity.getClass().getSimpleName()+"onResume") ;
				
			}

			@Override
			public void onActivityPaused(Activity activity) {
				// TODO Auto-generated method stub
				Log.d("jmtest","app find : "+ activity.getClass().getSimpleName()+"onPause") ;
				
			}

			@Override
			public void onActivityStopped(Activity activity) {
				// TODO Auto-generated method stub
				Log.d("jmtest","app find : "+ activity.getClass().getSimpleName()+"onStop") ;
				
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity,
					Bundle outState) {
				// TODO Auto-generated method stub
				Log.d("jmtest","app find : "+ activity.getClass().getSimpleName()+"onSaveInstance") ;
				
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				// TODO Auto-generated method stub
				Log.d("jmtest","app find : "+ activity.getClass().getSimpleName()+"onDestroy") ;
				
			}
			
		});*/
	}

	public CubicApp() {
		// TODO Auto-generated constructor stub
	
	}

}
