package com.altek.cubicapp;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
//import Camera_Capture_Setting.Capture_Setting_Define;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.media.AudioManager;
import android.net.wifi.WifiManager;
import com.altek.cubicapp.R;
import com.altek.cubiclib.*;


public class MainActivity extends BaseActivity implements DSCFinder.DSCFinderUpdateListener{

	//Constant define
	public static final int WiFiScan 			= 0;
	public static final int WiFiAlter 			= 1;
	public static final int WiFiConfirm 		= 2;
	public static final int WiFiSearch 			= 3;
	public static final int WiFiConfig 			= 4;
	public static final int WiFiConnect 		= 5;
	public static final int WiFiAbort 			= 6;

	private static class AlertType
	{
		final static int No_Error=0;
		final static int WiFiDisable_Error=1;
		final static int NoDSCFound_Error=2;
	}
	//Max SSID
	public static final int MaxSSIDNumber 		= 15;
	public static final int ConnectTimeOutSec 	= 10;
	public static final int ConnectScanTimeOutSec 	= 30;

	
	CubicApp theApp ;
	
	//UI component
	ProgressBar  Progress_Bar;
	Button ExitBtn;
	TextView WaitText;
	TextView ConnectedtoText;
	
	
	String OldBestandAtiveAPSSID;	
    //Wifi
	
	int WiFi_Status = -1;
	
	boolean bConnectSuccessful = false;
	boolean bIsFirstConnect = false;
	Timer ConnectTimeOut = new Timer(true);
	//Thread 	scanlistThread ;
    int MatchSSID_Count = 0;
	

    AlertDialog.Builder alert_message;
    int Alter_Error_Type = AlertType.No_Error ; //RemoteControl_Error_Define.No_Error;
    boolean bAlter_Show = false;
    
	
	int TimeOutCount = 0;
	int ScanTimes = 0;

	DSCFinder finder;

	AlertDialog dscDlg =null ;	
	ArrayAdapter<String> adapter=null;
	String dscWant=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		theApp = (CubicApp)getApplication();
		if (theApp.getConnectionState()!=CubicApp.CONNECTION_STATE_NOT_CONNECT) this.finish();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);                
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		bConnectSuccessful = false;
		Initial_Alter();  
		//Initial();		
	    Initial_Button_Listener();
	    
	    finder = new DSCFinder(this);
	    finder.setDscUpdateListener(this);
	    ConnectTimeOut.schedule(new timerTask(), 1000, 1000);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	
	@Override
	public void updateConnectStatus(int reason, String ssid) {
		dLog( ssid + (reason==DSCFinder.DSCFinderUpdateListener.DSC_CONNECTED ? "Connectted" : "Disconnectted") );
		switch(reason)
		{
		case DSCFinder.DSCFinderUpdateListener.DSC_CONNECTED:
			dLog("connected : "+ ssid  ) ;
			if (dscWant==null)
				{
					dLog("no dsc assigned");
					return ;
				}
			else dLog("wait for "+  dscWant) ;
			if (ssid.equals(dscWant)==false) return;
			if (dscDlg!=null) dscDlg.dismiss() ;
		 	ConnectTimeOut.cancel(); ;
			WiFi_Status = WiFiAbort; 
			theApp.setConnectionState(CubicApp.CONNECTION_STATE_WIFI_CONNECT_OK) ;
			bConnectSuccessful = true;
			Intent myIntent = new Intent();
		    
	        myIntent.setClass(MainActivity.this, RemoteControlUIActivity.class);	
            startActivity(myIntent);	
			
			break;
		case DSCFinder.DSCFinderUpdateListener.DSC_DISCONNECT:
			break;
		}
	}

	@Override
	public void updateDSCList(ArrayList<String> arg0) {
		dLog("Udate DSC List");
		showSelectMenu(arg0) ;
	}
	
	void showSelectMenu(ArrayList<String> dscList)
	{
		if (dscList.size()!=0)
		{
			dLog("update dsc list");
			if (adapter==null)
			{
			    adapter=new ArrayAdapter<String>(this,R.layout.dsclistitem,R.id.txtDSCItem);				    
			}
			adapter.clear();
			adapter.addAll(dscList);
			adapter.sort(new Comparator<String>(){
			@Override
			public int compare(String lhs,	String rhs) {
				return finder.getDSCSignalStrength(rhs)-finder.getDSCSignalStrength(lhs) ;
			}}) ;
			if (dscDlg==null )
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setAdapter(adapter, new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String dscSelect = adapter.getItem(which) ;					
						dscWant=dscSelect;
						finder.connectTo(dscSelect);
						dscDlg=null;
					}
				});
				
				builder.setPositiveButton(R.string.ssid_scan_again, new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finder.forceCheck();
						dscDlg=null ;
					}
					
				});
				builder.setTitle(R.string.ssid_scan_list);
				dscDlg = builder.create();
				dscDlg.show();
			}
		}
    	Progress_Bar.setVisibility(View.INVISIBLE);	
	    WaitText.setVisibility(View.INVISIBLE);	
	    ConnectedtoText.setVisibility(View.INVISIBLE);	
		
	}
	
	@Override 
	protected void onStart()
	{
		super.onStart();
		if (theApp.getConnectionState()!=CubicApp.CONNECTION_STATE_NOT_CONNECT) 
		{
			theApp.setConnectionState(CubicApp.CONNECTION_STATE_GO_CLOSE) ;
			this.finish();
			return ;
		}
		//  Initial();
		Initial();
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		finder.terminate() ;
		WiFi_Status = WiFiAbort;    	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return true;
					
	}
	
    void Initial_Button_Listener()
    {
		ExitBtn             = (Button) this.findViewById(R.id.ExitBtn);

		Progress_Bar			= (ProgressBar) this.findViewById(R.id.progressBar1);
		
	    ExitBtn.setOnClickListener(new Button.OnClickListener() {
					public void onClick(View v) {	
						System.exit(0);					 				                														
				}});
	    
	    registerForContextMenu(ExitBtn); 
	    WaitText             = (TextView) this.findViewById(R.id.Please_Wait);
	    ConnectedtoText             = (TextView) this.findViewById(R.id.Wait_Connect);
	    
    }

	void Initial()
    {
		
		
		WiFi_Status = WiFiScan;		
		WifiManager mainWifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);  
		if(mainWifi != null)
		{
			theApp.setSonnectNetworkID(mainWifi.getConnectionInfo().getNetworkId());
		}
		if (!mainWifi.isWifiEnabled()) {
			Alter_Error_Type = AlertType.WiFiDisable_Error;
			displayErrorAlter();
        } 
		else
		{
			finder.start() ;
		}			 
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			System.exit(0);
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}	

	void Initial_Alter()
	{
		
		alert_message=new AlertDialog.Builder(this);

		alert_message.setTitle(R.string.alter_WiFiDisable);
		
		alert_message.setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener() {
			   
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
					WifiManager mainWifi = (WifiManager) MainActivity.this.getSystemService(Context.WIFI_SERVICE);  				   
					switch (Alter_Error_Type)
					{
						case AlertType.WiFiDisable_Error:
							mainWifi.setWifiEnabled(true);
							finder.start() ;
							break;
						case AlertType.NoDSCFound_Error:
							ScanTimes=0;
							ConnectFailsTimes = 0;
							break;
							
					}
					bAlter_Show = false;
					WiFi_Status=WiFiScan;	
			   }
			  });
		alert_message.setNegativeButton( android.R.string.no, new DialogInterface.OnClickListener() {
			   
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
				   System.exit(0);	
			   // Toast.makeText(getApplicationContext(), "No Button Clicked", Toast.LENGTH_LONG).show();
			   }
			  });
	}
	void displayErrorAlter()
	{
		if(alert_message == null)
			return;
		
		if(bAlter_Show)
			return;
		
		bAlter_Show = true;
		WiFi_Status=WiFiAlter;	
		switch (Alter_Error_Type)
		{
			case AlertType.WiFiDisable_Error:
				alert_message.setTitle(R.string.alter_WiFiDisable);
				break;
			case AlertType.NoDSCFound_Error:
				alert_message.setTitle(R.string.alter_NoDSCFound);
				break;
		}
		alert_message.show();
	}
    
	void ReInitialScan()
	{
 		ScanTimes = 0;
 		WiFi_Status=WiFiScan;		
 		Progress_Bar.setVisibility(View.VISIBLE);
	    WaitText.setVisibility(View.VISIBLE);	
	    ConnectedtoText.setVisibility(View.VISIBLE);	
	}
	int ConnectFailsTimes=0;
	public class timerTask extends TimerTask
	{
	    public void run()
	    {
	    	if(WiFi_Status == WiFiAbort)
	    	{
	    		ScanTimes = 0;
	    		ConnectFailsTimes = 0;
	    		return;
	    	}
	    	
	    	if(WiFi_Status > WiFiConfirm)
	    	{
	    		TimeOutCount++;
	    		ScanTimes = 0;
	    		
	    		if(TimeOutCount > ConnectTimeOutSec)
	    		{
	    			WiFi_Status = WiFiScan;
	    			//mainWifi.startScan();
	    			finder.forceCheck() ;
	    			ConnectFailsTimes++;
	    		}
	    		
	    	}
	    	else
	    	{
	    		if(WiFi_Status == WiFiScan)
	    			ScanTimes++;	    		
	    		TimeOutCount = 0;
	    	}
	    	
	    	if(ScanTimes > ConnectScanTimeOutSec||ConnectFailsTimes == 3)
    		{
    			
    			runOnUiThread(new Runnable() {  
    				@Override
               		 public void run() {           			        			
    					ScanTimes = 0;
    					ConnectFailsTimes = 0;
               			Toast.makeText(MainActivity.this, R.string.alter_NoDSCFound, 5000).show();
                    			}
                			}
               			 );
    			
    		}
	    }
	}
	@Override
	public void errorReport(int errorCode, Object object) {
		// TODO Auto-generated method stub
		
	}
	    	
}

