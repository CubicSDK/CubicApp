package com.altek.cubicapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;

import Camera_Capture_Setting.Capture_Setting_Define;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.os.AsyncTask;


import com.altek.cubicapp.R;
import com.altek.cubicapp.download.ThumbnailItem;
import com.altek.cubicapp.download.adapter.MultiColumnImageAdapter.ThumbnailClickListener;
import com.altek.cubicapp.download.adapter.PhotosAdapter;
import com.altek.cubicapp.download.data.FileSystemImageCache;
import com.altek.cubicapp.download.data.ImageDatabase;
import com.altek.cubicapp.download.data.Photo;
import com.altek.cubicapp.download.request.CachedImageFetcher;
import com.altek.cubiclib.CubicCamera;
import com.altek.cubiclib.CubicCamera.TakeMode;
import com.altek.cubiclib.CubicCamera.DSCStatusUpdateListener;



public class RemoteControlUIActivity extends BaseActivity implements DSCStatusUpdateListener {

	CubicApp theApp ;
	/**
	 * This constant use for getPartialByIdAndSaveFile, app do not need buffer for it. 
	 * all buffer manager in low level driver  The size will impact the progress update frequency 
	 */
	private final static int DATA_SIZE_WHEN_GET_PARTITAL = 1800*1024 ;	//1800 K

	String  lockStr=new String("lock");

	private static class AlertType
	{
		final static int 	No_Error=0;
		static final int 	MemoryFull_Warning 			= 0;
		static final int 	Battery_Warning 			= 1;
		static final int 	CardErr_Warning 			= 2;
		static final int 	LensErr_Warning 			= 3;
		static final int 	TemperatureErr_Warning 		= 4;
		
		static final int 	DisConnect_Error 			= 1;
		static final int 	FWVersionIncorrect_Error 	= 2;
		static final int 	NoSDCard_Error 				= 3;
		static final int 	MemoryFull_Error 			= 4;
		static final int	PTPIP_Packet_Error			= 8;
	}
	
	private static class MessageType
	{
		static final int 	Format_Message 				= 0;
		static final int 	BackKey_Message 			= 1;
		static final int 	AutoPowerOffAlter 			= 2;		
	}


	
	int LIVEVIEW_WIDTH  = 0; 
	int LIVEVIEW_HEIGHT = 0;

	boolean bFirstCapture = true;
	/*UI Components*/
	
	ImageButton 	TakeImageBtn;
	Button 	VideoRecBtn;
	ImageView 	WideBtn;	
	ImageView 	TeleBtn;	
	Button 	AEAFBtn;
	Button 	CloseBtn;

	TextView VideoRecText;
	TextView ImageRemainCountText;
	ImageView 	Batterylevel;	
	ImageView 	VideoRecing;	
	ImageView 	SDCardIcon;
	ImageView   TouchShotIcon ;
	ImageView   macroIcon ;
	
	/*Memory Buffer*/
	byte[] MacAddressBytes= new byte[32];	
	//
	// TODO : network
	int[] ObjectHandleArray= new int[9999+1000];
	int[] TakeObjectHandleArray= new int[9999];	
	int[] StorageIDArray= new int[6];	
	/*WiFi*/
	WifiManager  mainWifi;	
	String MacAddress;  
	
	/*control variable*/
	boolean bActive = false;

	boolean bPlayShutterSound = false;
	boolean bInitialFinish = false;
	boolean bDownloading = true;
	boolean bAddTakeObjectHandle = true;
	String myJpgPath=null ;
	boolean IsStopLVDisplay = false;
	boolean IsIRPFinish = true;
	boolean IsNoSDcard = false;//for not support internal check.

	Thread  rtpDispayThrad;

	File myDir; 

/*UI */
	ProgressDialog progressDialog =null;
	ProgressDialog ProgressBar;
	MediaPlayer mp;
	
	/*LV */
	/*LV Display*/
	boolean IsDisplayLV = false;
	boolean IsLVStop = true;
	boolean IsCheckEventStop = true;
	float mScale; //ratio of dp to px
	Paint mFramePaint;
	Rect mDrawrect;
	Rect mShowrect;
	Rect mSrcLVRect ;
	Rect mDestSurfaceRect ;
	int mLiveviewX, mLiveviewY;
	SurfaceView m_Surface;
	AtomicBoolean surfaceReady=new AtomicBoolean(true) ;
	// Touch AF 
	
	int dwTouchAFRectWidth = 0;
	float fLVRatio = 0;
	BitmapFactory.Options bmpop = new BitmapFactory.Options(); //==//==speed

	
	/*Timer*/
	Timer Videotimer = new Timer(true);
	int VideoRecSec = 10000000 ;
	int 	MaxRecTime 	= 10;//sec
	int DirectTransfer = 0 ;
	int predegree = 100;

	byte BatteryLevelValue = 0x5c;
	long dtMili = System.currentTimeMillis();
	String ImageRemainCountStr;
	int    RemainCount = 0;
	int currentView ;
	
	//PopMenu
	ContextMenu   popupFlashMenu; 
	ContextMenu   popupSettingMenu; 
	ContextMenu   popupVideoModeMenu; 
	ContextMenu   popupAutoPowerOffMenu; 	
	ContextMenu   popupBeepSoundMenu;
	ContextMenu   popupTouchShotdMenu;
    ImageButton imageFlashButton;
    ImageButton imageSettingButton;
    ImageButton ImagemodeChangeButton;
    ImageButton imageVideoModeButton;
    ImageButton  imageDownloadButton;
    
    AlertDialog.Builder alert_box;
    AlertDialog.Builder alter_Error;
    AlertDialog.Builder alter_Warning;    
    int Alter_Error_Type = AlertType.No_Error;
    int Prev_Alter_Error_Type = AlertType.No_Error;
    int Alter_Message_Type = MessageType.BackKey_Message;
    int Alter_Warning_Type = AlertType.MemoryFull_Warning;
    boolean bAlter_Show = false;
    boolean bAlter_Message_Show = false;
    boolean bAlter_Warning_Show = false;    
    PrintWriter CheckSocketout;
 
    CubicCamera camera ;
    
	@Override
	protected void onResume() {
		super.onResume();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		theApp = (CubicApp)getApplication();
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);                
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		WindowManager.LayoutParams lp = getWindow().getAttributes() ;
		lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL ;
		getWindow().setAttributes(lp) ;		
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activyty_remote_control_ui_portrait);
        
        Bundle extras = this.getIntent().getExtras();
		String root = Environment.getExternalStorageDirectory().toString();
        myDir = new File(root + "/DCIM/");  
        
		ObjectHandleArray[0] = 0 ;
		mainWifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
	    WifiInfo info1 = mainWifi.getConnectionInfo();
	    MacAddress = info1.getMacAddress();
		MacAddressBytes = MacAddress.getBytes();
		
		Initial_UI();
		Initial_TimerAndClass();
		Initial_Button_Listener();
		
		
		Thread t = new Thread(new Initial_RemoteControl_Action(),"init remote control");
		t.start(); // 開始執行Runnable.run();
		
		bActive = true;
		
		progressDialog = ProgressDialog.show( 
				RemoteControlUIActivity.this,       // Activity Context  
				getResources().getString(R.string.RemoteControl),  // Dialog Title, if left blank no title is shown 
				getResources().getString(R.string.Processing),  // Message To Display 
				   false,         // Spinner Style 
				   true           // Cancelable 
				 );
		TakeObjectHandleArray[0] = 0;
		IsStopLVDisplay = false;

		DirectTransfer = 0 ;
		IsLVStop = true;
		IsCheckEventStop = true;
		
		Init_menu();  
		Videotimer.schedule(new timerTask(), 1000, 1000);  
	}
	
	public void PTPIPPacketErrorHandle() {
		Log.e("RemoteControl","PTPIPPacketErrorHandle");
		runOnUiThread(new Runnable() {  
		@Override
   		 public void run() {           			        			
			Alter_Error_Type = AlertType.PTPIP_Packet_Error;
			bAlter_Show = false;
   			displayErrorAlter();
        			}
    			}
   			 ); 	
	}
	  
	@Override
    public void onDestroy()
    {
		if (camera!=null)
			new Thread( new Runnable() {

				@Override
				public void run() {
					try {
						camera.terminate() ;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			},"remote destroy") ;
		DisconnectHandle(true);
		        
        super.onDestroy();
        ObjectHandleArray[0] = 0 ;
        DirectTransfer = 0 ;
        IsStopLVDisplay = false;
        IsIRPFinish = true;
        if(progressDialog !=null)
        	progressDialog.dismiss();
    }
	
	@Override
	protected void onStop()
	{
		super.onStop();
		if (theApp.getConnectionState() == CubicApp.CONNECTION_STATE_GO_CLOSE)
		{
			Videotimer.cancel();
			int retrycount = 10;
			if (camera!=null)
				new Thread( new Runnable() {

					@Override
					public void run() {
						try {
							camera.terminate() ;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				},"remote stop") ;
			DisconnectHandle(true);
			if(mainWifi !=null)
			{
				mainWifi.disconnect();
				mainWifi.enableNetwork(theApp.getConnectNetworkID(),false);
				mainWifi.reconnect(); 
				mainWifi.startScan();
			}
			System.exit(0) ;
		}

			ObjectHandleArray[0] = 0 ;
			IsStopLVDisplay = false;
			IsIRPFinish = true;
			if(progressDialog !=null)
				progressDialog.dismiss();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	void Initial_UI()
	{

		
		//Button Initial
		WideBtn    		= (ImageView) this.findViewById(R.id.ZoomWideBtn);
		TeleBtn    		= (ImageView) this.findViewById(R.id.ZoomTeleBtn);
		Batterylevel    		= (ImageView) this.findViewById(R.id.imageViewBatt);
		TakeImageBtn 	= (ImageButton) this.findViewById(R.id.TakeImgBtn);
		
		VideoRecBtn    	= (Button) this.findViewById(R.id.VideoRecBtn);
		CloseBtn    	= (Button) this.findViewById(R.id.MainTitle);
		m_Surface 		= (SurfaceView) findViewById(R.id.surfaceView1);

		m_Surface.getHolder().addCallback(new Callback() {

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				surfaceReady.set(true);
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				surfaceReady.set(false);
			}			
		});
    	
		
		VideoRecText	= (TextView) this.findViewById(R.id.VideoRecTime);
		
		SDCardIcon		= (ImageView) this.findViewById(R.id.imageViewSD);
		VideoRecing		= (ImageView) this.findViewById(R.id.VideoRecing);
		TouchShotIcon   = (ImageView)findViewById(R.id.imageViewTouchShot);
		macroIcon       = (ImageView)findViewById(R.id.imageViewMacro) ;
		
		VideoRecText.setTextColor(Color.WHITE);
		VideoRecing.setVisibility(View.INVISIBLE);
		VideoRecText.setVisibility(View.INVISIBLE);
		VideoRecBtn.setVisibility(View.INVISIBLE);
		CloseBtn.setVisibility(View.GONE);
		ImageRemainCountText	= (TextView) this.findViewById(R.id.ImageRemaincount);
		ImageRemainCountText.setTextColor(Color.WHITE);
		ImageRemainCountText.setVisibility(View.INVISIBLE);
		
		imageFlashButton = (ImageButton) this.findViewById(R.id.imageButton_flash);
		imageFlashButton.setVisibility(View.VISIBLE);
		imageSettingButton = (ImageButton) this.findViewById(R.id.imageButton_setting);
		imageVideoModeButton = (ImageButton) this.findViewById(R.id.imageVideoModeButoon);
		imageVideoModeButton.setVisibility(View.GONE);
		ImagemodeChangeButton = (ImageButton) this.findViewById(R.id.imageButton_changeMode);
		imageDownloadButton = (ImageButton)findViewById(R.id.btnDownload) ;
		SetButtonEnable(bInitialFinish);

	}
	void SetButtonEnable(boolean a_bEnable)
	{
		TakeImageBtn.setEnabled(a_bEnable);
		WideBtn.setEnabled(a_bEnable);
		TeleBtn.setEnabled(a_bEnable);
		ImagemodeChangeButton.setEnabled(a_bEnable);
		imageFlashButton.setEnabled(a_bEnable);
		imageSettingButton.setEnabled(a_bEnable);
		imageDownloadButton.setEnabled(IsNoSDcard?false:a_bEnable);
	}
	void reInitUI(int id)
	{
		IsStopLVDisplay=true;
			surfaceReady.set(false);	
         //initial ui component
		currentView = id ;	
		setContentView(id);
        LIVEVIEW_WIDTH=0;
        LIVEVIEW_HEIGHT=0;		
        Initial_UI();      
 		Initial_Button_Listener();
		Init_menu();  
		UpdateLVIcon();
		IsStopLVDisplay=false;
	}
	
	boolean bForceBackKey = false;
	void Initial_TimerAndClass()
	{
		
		alert_box=new AlertDialog.Builder(this);

		alert_box.setTitle(R.string.formatMessage);
		
		alert_box.setPositiveButton(R.string.Yes,new DialogInterface.OnClickListener() {
			   
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
				   switch (Alter_Message_Type)
					{
						case MessageType.Format_Message:
							 SetupMenuSetting(R.id.setting_item_format);
							 bAlter_Message_Show = false;
							 break;	
						case MessageType.BackKey_Message:
							bForceBackKey = true;
							dispatchKeyEvent(new KeyEvent (KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
						    dispatchKeyEvent(new KeyEvent (KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
						    Log.d("123", "BackKey_Error!");
							break;						
						case MessageType.AutoPowerOffAlter:
							if (camera!=null)
							{
									new Thread( new Runnable() {
										@Override
										public void run() {
											try {
												camera.responseCamera(CubicCamera.RESPONSE_AGREE_CAMERA_OFF, true) ;
											} catch (Exception e) {
											}
										}
										
									},"remote poweroff alert true").start();
							}
							 break;
						default:
				            break;
					}				   
			   }
			  });
		alert_box.setNegativeButton( R.string.No, new DialogInterface.OnClickListener() {
			   
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
				   bAlter_Message_Show = false;
				   if(Alter_Message_Type == MessageType.AutoPowerOffAlter)
				   {
						if (camera!=null)
						{
							new Thread( new Runnable() {
								@Override
								public void run() {
									try {
											camera.responseCamera(CubicCamera.RESPONSE_AGREE_CAMERA_OFF, false) ;
									} catch (Exception e) {
									}
								}
								
							},"remote poweroff alert false").start();
						}
				   }
			   }
			  });
		
		alter_Error =new AlertDialog.Builder(this);
		alter_Error.setTitle(R.string.alter_disconnect);
		
		alter_Error.setPositiveButton( R.string.Yes,new DialogInterface.OnClickListener() {
			   
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
					bAlter_Show = false;
					switch (Alter_Error_Type)
					{
						case AlertType.DisConnect_Error:
						case AlertType.FWVersionIncorrect_Error:
						case AlertType.PTPIP_Packet_Error:
							 bActive = false;
							 DisconnectHandle(true);
							break;						
						case AlertType.NoSDCard_Error:	
							break;
						case AlertType.MemoryFull_Error:								
							break;			
				        default:
				            break;
					}

			   }
			  });
		
		alter_Error.setNegativeButton( R.string.No, new DialogInterface.OnClickListener() {
			   
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
				   bAlter_Show = false;
					progressDialog = ProgressDialog.show( 
							RemoteControlUIActivity.this,       // Activity Context  
							getResources().getString(R.string.RemoteControl),  // Dialog Title, if left blank no title is shown 
							getResources().getString(R.string.APPProcessing),  // Message To Display 
							   false,         // Spinner Style 
							   true           // Cancelable 
							 );
				   
					Thread t = new Thread(new DisConnectAction(),"remote disconnect");			
					t.start(); // 開始執行Runnable.run();
			   }
			  });
		
		alter_Error.setCancelable(false);
		
		alter_Warning =new AlertDialog.Builder(this);
		alter_Warning.setTitle(R.string.alter_memory_full);
		
		alter_Warning.setPositiveButton( R.string.Yes,new DialogInterface.OnClickListener() {
			   
			   @Override
			   public void onClick(DialogInterface dialog, int which) {
				   
					switch (Alter_Warning_Type)
					{
						case AlertType.MemoryFull_Warning:
						case AlertType.Battery_Warning:
							bAlter_Warning_Show = false;
							break;
						case AlertType.LensErr_Warning:
						case AlertType.CardErr_Warning:
						case AlertType.TemperatureErr_Warning:	
							bActive = false;
							DisconnectHandle(true);
							break;
				        default:
				            break;
					}

			   }
			  });
		

		
		alter_Warning.setCancelable(false);	
		
	
		ProgressBar=new ProgressDialog(this);
		ProgressBar.setMessage(getResources().getString(R.string.Capture));
		ProgressBar.setTitle(R.string.PleaseWait);
		ProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		ProgressBar.setProgress(0);
		ProgressBar.setMax(100);
		ProgressBar.dismiss();
	}
	public class DisConnectAction implements Runnable {
	       
		public void run() { // implements Runnable run()
			DisconnectHandle(false);	
		}
	}

	void displayErrorAlter()
	{
		if(alter_Error == null)
			return;
		
		if(bAlter_Show)
			return;

		//If displaying these two error msgs before, it means connection issue happens
		//Don't need to display other msg again.
		if(AlertType.DisConnect_Error == Prev_Alter_Error_Type ||
				AlertType.PTPIP_Packet_Error == Prev_Alter_Error_Type)
			return;
		
		bAlter_Show = true;
		
		switch (Alter_Error_Type)
		{
			case AlertType.DisConnect_Error:
				alter_Error.setTitle(R.string.alter_disconnect);
				break;
			case AlertType.FWVersionIncorrect_Error:
				alter_Error.setTitle(R.string.alter_FWVersionOlder);
				break;
			case AlertType.NoSDCard_Error:	
				alter_Error.setTitle(R.string.alter_no_sdcard);
				break;
			case AlertType.MemoryFull_Error:	
				alter_Error.setTitle(R.string.alter_memory_full);
				break;				
			case AlertType.PTPIP_Packet_Error:
				alter_Error.setTitle(R.string.alter_PTPIP_Packet_Error);
				break;				
		}
		Prev_Alter_Error_Type = Alter_Error_Type;
		alter_Error.show();
	}
	void displayMessageAlter()
	{
		if(alert_box == null)
			return;
		
		if(bAlter_Message_Show)
			return;
		
		bAlter_Message_Show = true;
		
		switch (Alter_Message_Type)
		{
			case MessageType.Format_Message:
				alert_box.setTitle(R.string.formatMessage);
				break;	
			case MessageType.BackKey_Message:
				alert_box.setTitle(R.string.alter_BackKEYWarning);
			break;		
			case MessageType.AutoPowerOffAlter:
				alert_box.setTitle(R.string.alter_AutoPowerOffWarning);
			break;			
		}
		alert_box.show();
	}
	void displayWarningAlter()
	{
		if(alter_Warning == null)
			return;
		
		if(bAlter_Warning_Show)
			return;
		
		bAlter_Warning_Show = true;
		
		switch (Alter_Warning_Type)
		{
			case AlertType.MemoryFull_Warning:
				if (!camera.isCurrentlyUsingSDCard())
					alter_Warning.setTitle(R.string.alter_memory_full);
				else
					alter_Warning.setTitle(R.string.alter_store_full);
				break;
			case AlertType.Battery_Warning:
				alter_Warning.setTitle(R.string.alter_BatteryEmptyWarning);
				break;
			case AlertType.CardErr_Warning:
				alter_Warning.setTitle(R.string.alter_CardErrWarning);
				break;	
			case AlertType.LensErr_Warning:
				alter_Warning.setTitle(R.string.alter_LensErrWarning);
				break;
			case AlertType.TemperatureErr_Warning:
				alter_Warning.setTitle(R.string.alter_TemperatureErrWarning);
				break;		
		}
		alter_Warning.show();
	}	
	void UpdateLVIcon()
	{
		
       	ImageRemainCountText.setVisibility(View.VISIBLE);
       	UpdateFlashIcon( (Integer)camera.getCameraSetting(CubicCamera.CAMERA_SETTING_FLASHMODE) );
        UpdateBatteryIcon();
            	//SD Card icon
        		if (!camera.isCurrentlyUsingSDCard())
        		{
        			SDCardIcon.setImageResource(R.drawable.lcd_ic_internal);
        			if ( !camera.getCapability().isSupportInternalMemory() )
        			{
        				IsNoSDcard = true;
        				imageDownloadButton.setEnabled(false);
        			}
        		}
        		else
        		{
        			SDCardIcon.setImageResource(R.drawable.lcd_ic_sdcard);
        			if ( !camera.getCapability().isSupportInternalMemory() )
        				IsNoSDcard = false;
        		}
        		UpdateTakeModeIcon();
        		
        	
	}
	public class Initial_RemoteControl_Action implements Runnable {
	       
		public void run() { // implements Runnable run()
			try 
			{
				camera = new CubicCamera(RemoteControlUIActivity.this);
				camera.setDSCStatusUpdateListener(RemoteControlUIActivity.this);
				camera.setDownloadDir(myDir);
				camera.setDisableDownloadQV(false);
				//
				// start the liveview thread first , 
				//   the liveview thread will block until the camera.start finish some initial 
				//   in this way can let liveview display ASAP
				//
				//Thread RTP_Server = new Thread(new RTP_Server_Start());		
				//RTP_Server.start(); 
				rtpDispayThrad = new Thread( new RTP_Server_Start(),"remote rtp output");
				rtpDispayThrad.start();
				
				if (camera.start()==false)
				{
					dLog("camera initial fail");
					if(progressDialog != null) 	progressDialog.dismiss();
				    runOnUiThread(new Runnable() {  
		       		 	@Override
		       		 	public void run() { 		       		 			
		       		 		Alter_Error_Type = AlertType.DisConnect_Error;
		       		 		bAlter_Show = false;
		       		 		displayErrorAlter();
		            	}
		        	}); 
				}
				theApp.setConnectionState(CubicApp.CONNECTION_STATE_PTPIP_CONNECT_OK) ;
				bInitialFinish = true;
			}
			catch(Exception e)   //  do not handle here, because the camera.start can not execute on UI thread and we already on normal thread
			{
				errLog(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	void UpdateBatteryIcon()
	{
		int resId;
		int[][] levelArray={ 
				{80,R.drawable.lcd_ic_batt_full},
				{50,R.drawable.lcd_ic_batt_mid},
				{25,R.drawable.lcd_ic_batt_low}
		};
		
		int batteryLevel = camera.getBatteryLevel() ;
		if (batteryLevel ==0xff)
		{
			resId=R.drawable.lcd_ic_batt_ac ;
		}
		else
		{
			resId=R.drawable.lcd_ic_batt_empty ;
			for (int[] data:levelArray)
			{
				if (batteryLevel>=data[0]) 
				{
					resId=data[1];
					break;
				}
 			}
		}		
		Batterylevel.setImageResource(resId) ;
		if (resId==R.drawable.lcd_ic_batt_empty)
		{
			Alter_Warning_Type = AlertType.Battery_Warning;
			displayWarningAlter();			
		}
	}
	
//
// this thread read available data and display to screen
//
public class RTP_Server_Start implements Runnable {
        
		public void run() { // implements Runnable run()
			
			int skip_frame = 2;
			IsDisplayLV = true;
			IsLVStop = false;
			byte[] dispData;
			
			try {
				camera.startLiveview();
			
				while (!rtpDispayThrad.isInterrupted())
				{
					if(IsDisplayLV == true)
					{
						if(skip_frame > 0 /*||length <= 0*/)
						{
							skip_frame--;
							Thread.sleep(5);
							continue;
					    }
						dispData = camera.getLiveviewFrame() ;
						if (dispData!=null)
						{
							outputLiveview(dispData) ;
							camera.releaseLiveviewFrame(dispData);
						}
					}
				}
				errLog("liveview stop");
				IsLVStop = true;
				camera.stopLiveview();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

	void outputLiveview(byte[] dispData)
	{
		if(IsStopLVDisplay==true) 	return;
		Bitmap iconbmp = null;
		iconbmp = BitmapFactory.decodeByteArray(dispData, 0, dispData.length);
		
		if (null==iconbmp) return ;
		Canvas sfCanvas=null;
		SurfaceHolder holder=null ;
		try
		{
				if (surfaceReady.get()==true)
				{
					if(LIVEVIEW_WIDTH == 0 || LIVEVIEW_HEIGHT == 0)
					{	
							LIVEVIEW_WIDTH = iconbmp.getWidth();
							LIVEVIEW_HEIGHT = iconbmp.getHeight();
							LV_Display_Initial();
					}
					holder = m_Surface.getHolder() ;
					sfCanvas = holder.lockCanvas();
					if(sfCanvas != null)
					{
						sfCanvas.drawBitmap(iconbmp,mSrcLVRect,mDestSurfaceRect,null);
						if(mShowrect != null)
							sfCanvas.drawRect(mShowrect, mFramePaint); 
						
						holder.unlockCanvasAndPost(sfCanvas);
					}
				} 
			}
		catch (Exception e) 
		{
			if (sfCanvas!=null) holder.unlockCanvasAndPost(sfCanvas);
			return;
		}
	}
    
    void LV_Display_Initial()
    {
    			
		mDrawrect = m_Surface.getHolder().getSurfaceFrame();
		mScale = RemoteControlUIActivity.this.getResources().getDisplayMetrics().density;
		mFramePaint = new  Paint(Paint.ANTI_ALIAS_FLAG);
		mFramePaint.setStyle(Paint.Style.STROKE);
		mFramePaint.setColor(Color.BLACK);
		mFramePaint.setStrokeWidth(2 * mScale);
		  
		mShowrect = new Rect(mDrawrect);
		
		mLiveviewX =  0;	
		mLiveviewY =  0;	
		mSrcLVRect = new Rect(0,0,LIVEVIEW_WIDTH,LIVEVIEW_HEIGHT) ;
		
		int vTmpWidth = mShowrect.width() ;
		int vTmpHeight = ( LIVEVIEW_HEIGHT*vTmpWidth ) /LIVEVIEW_WIDTH ;
		if ( vTmpHeight > mShowrect.height())
		{
			vTmpHeight = mShowrect.height();
			vTmpWidth = (LIVEVIEW_WIDTH*vTmpHeight)/LIVEVIEW_HEIGHT ;
		}
		
		int startX = ( (vTmpWidth>=mShowrect.width()) ? 0 : (mShowrect.width() - vTmpWidth) /2 ) ;
		int startY = ( (vTmpHeight>=mShowrect.height()) ? 0 : (mShowrect.height() - vTmpHeight) /2 ) ;
		mDestSurfaceRect = new Rect(
				startX, startY ,
				startX+vTmpWidth,startY+vTmpHeight) ;
		dwTouchAFRectWidth =  (mDestSurfaceRect.right - mDestSurfaceRect.left )/20;
		fLVRatio = (float)(vTmpWidth)/(float)(LIVEVIEW_WIDTH);
		
	
		
		
		bmpop.inTempStorage = new byte[1024 * 64]; //==//==speed
		bmpop.inSampleSize = 1;					  //==//==speed	
		 
    }
    
	void Initial_Button_Listener()
    {
		 
		CloseBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				
				bActive = false;
		        IsDisplayLV = false;
				ObjectHandleArray[0] = 0 ;
				IsStopLVDisplay = false;
				IsIRPFinish = true;
		        try {
		        	
		        	camera.terminate();
		        	camera.stopLiveview() ;
		        }catch (Exception e) {
		        	
				} 
				
		        android.os.Process.killProcess(android.os.Process.myPid());
		        RemoteControlUIActivity.this.finish();

				Log.d("DrawFocusRect","CloseBtn");
				
				
		}});
		
		m_Surface.setOnTouchListener(new OnTouchListener() {

		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		    	if(!bInitialFinish)
		    		return false;
		    	bAddTakeObjectHandle = true;
		    	if(IsNoSDcard||RemainCount == 0)
		    	{		    				    		
		    		if(IsNoSDcard)
		    		{
		    			Alter_Error_Type = AlertType.NoSDCard_Error;
		    			displayErrorAlter();
		    			return false;
		    		}
		    		else
		    		{
		    			bAddTakeObjectHandle = false;
		    			Alter_Warning_Type = AlertType.MemoryFull_Warning;
	    				displayWarningAlter();
	    				CubicCamera.TakeMode takeMode = camera.getTakeMode() ;
		    			//if(FWVersion <= Not_Support_MemoryFull_TakeImage|| camera.isVideoMode(takeMode) )
	    				if ( !camera.getCapability().isSupportMemFullTakeImage()  ||  takeMode.isNowVideoMode())
		    				return false;
		    		}
		    		
		    		
		    	}
		    	int TouchX = (int)event.getX();
				int TouchY = (int)event.getY();
				int focusX=0;
				int focusY=0;
				if((TouchX > (mDestSurfaceRect.left + dwTouchAFRectWidth))&&(TouchX < (mDestSurfaceRect.right - dwTouchAFRectWidth)))
					focusX =(int) ((TouchX-mDestSurfaceRect.left)/fLVRatio);
				else
					return false;
				if((TouchY > (mDestSurfaceRect.top + dwTouchAFRectWidth))&&(TouchY < (mDestSurfaceRect.bottom - dwTouchAFRectWidth)))
					focusY = (int)((TouchY-mDestSurfaceRect.top)/fLVRatio);
				else
					return false;
				
		    	if (event.getAction() == MotionEvent.ACTION_DOWN ) {
		    		ProgressBar.setProgress(0);
		    		if ( camera.getTakeMode().isNowStillMode() )
		    		{
		    			ProgressBar.setMessage(getResources().getString(R.string.Capture));
		    			
		    		}
		    		else
		    		{
		    			return false;
		    		}
    				ProgressBar.setProgress(10);
					bPlayShutterSound = true;
					
					class TouchShotAction implements Runnable
					{
						private int focusX,focusY;
						public TouchShotAction(int posx,int posy)
						{
							focusX=posx;
							focusY=posy;
						}
						@Override
						public void run() {
							// TODO Auto-generated method stub
							try{
								camera.touchShot(focusX, focusY);
							}
							catch(Exception e)
							{
								
							}
						}
					}
					new Thread(new TouchShotAction(focusX,focusY),"remote touchshot").start();    				
		    	}
		    	else if (event.getAction() == MotionEvent.ACTION_UP )
		    	{
		    		
		    	}
		    	return false;
		    }
		   });
		
		TakeImageBtn.setOnTouchListener(new OnTouchListener() {

		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		    	
		    	if(!bInitialFinish)
		    		return false;
		    	CubicCamera.TakeMode takeMode = camera.getTakeMode() ;
		    	bAddTakeObjectHandle = true;
		    	if(IsNoSDcard||RemainCount == 0)
		    	{		    				    		
		    		if(IsNoSDcard)
		    		{
		    			Alter_Error_Type = AlertType.NoSDCard_Error;
		    			displayErrorAlter();
		    			return false;
		    		}
		    		else
		    		{
		    			bAddTakeObjectHandle = false;
		    			Alter_Warning_Type = AlertType.MemoryFull_Warning;
	    				displayWarningAlter();
	    				if ( !camera.getCapability().isSupportMemFullTakeImage() ||
	    					  takeMode.isNowVideoMode() )
		    			{		    				
		    				return false;
		    			}
		    		}
		    		
		    		
		    	}
		    	if (event.getAction() == MotionEvent.ACTION_DOWN ) {
		    		ProgressBar.setProgress(0);
		    		if( takeMode.isNowStillMode() )
		    		{
		    			ProgressBar.setMessage(getResources().getString(R.string.Capture));
		    			
		    			//ProgressBar.show();
		    		}
		    		else
		    		{
		    			TakeImageBtn.setImageResource(R.drawable.lcd_btn_shutter_stop);
		    		}
		    		dtMili = System.currentTimeMillis();
    				ProgressBar.setProgress(10);
    				new Thread(new Runnable() {

						@Override
						public void run() {
							try
							{
								camera.doS1Lock(true) ;
							}
							catch(Exception e)
							{
								
							}
						}
    					
    				},"remote s1lock").start() ;
		    	}
		    	else if (event.getAction() == MotionEvent.ACTION_UP )
		    	{
		    		

		    		if ( takeMode.isNowVideoMode() )
		    		{
		    		    if(VideoRecSec >= MaxRecTime)
		    		    {
		    			    //lockScreenRotation();
		    			    VideoRecSec =0;
		    		    }
		    		    else
		    		    {
		    			    VideoRecSec =MaxRecTime;
		    			    //unLockScreenRotation();
		    		    }
                    }

					
					ProgressBar.setProgress(30);
					bPlayShutterSound = true;
					Thread t = new Thread(new CaptureStillAction(),"remote capture still");
					t.start(); // 開始執行Runnable.run();
					
		    	}
		    	return false;
		    }
		   });
		
		WideBtn.setOnTouchListener(new OnTouchListener() {

		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		    	if(!bInitialFinish)
		    		return false;
		    	int retrycount;
		    	if (event.getAction() == MotionEvent.ACTION_DOWN ) {

		    		retrycount = 6;
		    		new Thread(new Runnable(){

						@Override
						public void run() {
							try
							{
								camera.getZoomControl().wideKey.press();
							}
							catch(Exception e) {}
						}
		    			
		    		},"remote wide press").start() ;
		    	}
		    	else if (event.getAction() == MotionEvent.ACTION_UP )
		    	{
		    		retrycount = 20;
		    		new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try
							{
								camera.getZoomControl().wideKey.release();
							}
							catch(Exception e) {}
						}
		    			
		    		},"remote wide release").start() ;
		    	}
		    	return false;
		    }
		   });
		
		TeleBtn.setOnTouchListener(new OnTouchListener() {

		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		    	if(!bInitialFinish)
		    		return false;
		    	if (event.getAction() == MotionEvent.ACTION_DOWN ) {

		    		
		    		new Thread(new Runnable(){

						@Override
						public void run() {
							try
							{
								camera.getZoomControl().teleKey.press();
							}
							catch(Exception e) {}
						}
		    			
		    		},"remote tele press").start() ;
		    	}
		    	else if (event.getAction() == MotionEvent.ACTION_UP )
		    	{
		    		new Thread(new Runnable(){

						@Override
						public void run() {
							try
							{
								camera.getZoomControl().teleKey.release();
							}
							catch(Exception e) {}
						}
		    			
		    		},"remote tele release").start() ;
		    	}
		    	return false;
		    }
		   });
		

    	imageDownloadButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							camera.switchToBrowserMode(true);
						}
						catch(Exception e) {};
					}
					
				},"remote switch to review").start() ;
	        	ChangeToDownLoadView();	
			}
    		
    	});
    	imageFlashButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!bInitialFinish)
		    		return ;
				int flashMode = (Integer)camera.getCameraSetting(CubicCamera.CAMERA_SETTING_FLASHMODE);
				int newMode ;
				switch(flashMode)
				{
					case CubicCamera.CAMERA_SETTING_FLASHMODE_AUTO:
						newMode = CubicCamera.CAMERA_SETTING_FLASHMODE_FILL ;
						break;
					case CubicCamera.CAMERA_SETTING_FLASHMODE_FILL:
						newMode = CubicCamera.CAMERA_SETTING_FLASHMODE_OFF;
						break;
					default:
						newMode = CubicCamera.CAMERA_SETTING_FLASHMODE_AUTO;
						break;
				}
				class setFlashMode implements Runnable
				{
					int mode ;
					setFlashMode(int newMode) { mode=newMode; };
					public void run() {
						try {
							camera.setCameraSetting(CubicCamera.CAMERA_SETTING_FLASHMODE, mode);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}							
					}					
				}
				new Thread(new setFlashMode(newMode),"remote set flash").start() ;
				
			}
    		
    	});
    	this.imageVideoModeButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
//				if(PTPRunningFlag.getPTPIPIsActionRunning()==true||IsIRPFinish == false)
//					return ;
				CubicCamera.TakeMode takeMode = camera.getTakeMode() ;
				int currVideoSize = takeMode.currentVideoMode() ;
				int newSize = TakeMode.VIDEOSIZE_FULLHD;
				if (currVideoSize ==  TakeMode.VIDEOSIZE_FULLHD)
				{
					newSize =TakeMode.VIDEOSIZE_VGA ;
				}
				else if (currVideoSize == TakeMode.VIDEOSIZE_VGA)
				{
					newSize = TakeMode.VIDEOSIZE_640x640;
				}
				else if (currVideoSize == TakeMode.VIDEOSIZE_640x640)
				{
					newSize = TakeMode.VIDEOSIZE_FULLHD;
				}
				class setVideoMode implements Runnable
				{
					int mode ;
					setVideoMode(int newMode) { mode=newMode; };
					public void run() {
						try {
							camera.getTakeMode().switchToVideo(mode);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}							
					}					
				}
				new Thread(new setVideoMode(newSize),"remote set videomode").start() ;
			}
    	});		
    }
	
	public class CaptureStillAction implements Runnable {
        
		public void run() { // implements Runnable run()
			try {
			if(bFirstCapture)
			{
				bFirstCapture = false;
				camera.syncTime() ;
		    	
			}
			camera.doCapture() ;
			
	    	camera.doS1Lock(false); 
			Log.d("CaptureStillAction", "PTPOverIP_InitateCapture"); 
			}
			catch(Exception e)
			{
				
			}
		}
	}
	public class PlaySoundAction implements Runnable {
		private int myParam = 1;
		void PlaySoundActionParam(int  myParam){
			this.myParam = myParam;        	    
        }
		
		public void run() { // implements Runnable run()
			if(mp != null)
				mp.stop();
   			
			if(this.myParam == Capture_Setting_Define.ShutterSound)  			
   				mp = MediaPlayer.create(RemoteControlUIActivity.this, R.raw.shutter_sound_1);				  			
   			else if(this.myParam == Capture_Setting_Define.PowerOnSound)
   				mp = MediaPlayer.create(RemoteControlUIActivity.this, R.raw.powerup_sound_1);
   			
   			if(mp != null)
   				mp.start();
		}
	}
	int ProbePacketResp = 0;
    int TimerCount =0;	
	public class timerTask extends TimerTask
	  {
	    public void run()
	    {
	    	    TimerCount++;
	    	    if(VideoRecSec >= MaxRecTime)
		    	{
	    	    	return;
		    	}
	    	VideoRecSec++;
	    	runOnUiThread(new Runnable() {  
                @Override
                public void run() { 
                	if(VideoRecSec == 1)
                	{
                		DisplayVideoLVOSD(false);
                	}
                	if(VideoRecSec >= MaxRecTime)
                	{
                		DisplayVideoLVOSD(true);
                	}
                	String VideoRecStr;
                	if(MaxRecTime > 60)
                	{
                		 VideoRecStr = String.format("%02d", (MaxRecTime-VideoRecSec)/60)+":"
                						 +String.format("%02d", (MaxRecTime-VideoRecSec)%60);
                	}
                	else
                	{
                		 VideoRecStr = String.format("%02d", MaxRecTime-VideoRecSec);
                	}
                	VideoRecText.setText(VideoRecStr.toCharArray(), 0, VideoRecStr.length());                                      
                	}
            	}
	    	); 
	    	
	    	if(VideoRecSec >= MaxRecTime)
	    	{
	    		
				Thread t = new Thread(new CaptureStillAction(),"remote capture still");
				t.start(); // 開始執行Runnable.run();
	    	}
	    }
	  };

	void DisconnectHandle(boolean bExitAPP)
	{
        IsDisplayLV = false;
        if(bExitAPP == false)
        {
        	Log.d("123...", "Start disconnect"); 
			mainWifi.disconnect();
					
        	mainWifi.setWifiEnabled(false); 
        	while(mainWifi.isWifiEnabled())
        		SystemClock.sleep(250);
        	mainWifi.setWifiEnabled(true);
        	while(!mainWifi.isWifiEnabled())
        		SystemClock.sleep(250);
        	theApp.setConnectionState(CubicApp.CONNECTION_STATE_NOT_CONNECT);
        	//progressDialog.dismiss();
        	Log.d("123...", "End"); 
        	System.exit(0);
        }
        
        finish() ;
       
	}

	void DisplayVideoLVOSD(boolean a_bshow)
    {
    	int LVOSDVisiblity = View.INVISIBLE;
    	int VideoRecVisiblity = View.VISIBLE;
    	if(a_bshow == false)
    	{
    		LVOSDVisiblity = View.INVISIBLE;
    		VideoRecVisiblity = View.VISIBLE;
    	}
    	else
    	{
    		LVOSDVisiblity = View.VISIBLE;
    		VideoRecVisiblity = View.INVISIBLE;;
    	}
    	//Video Rec OSD
		 VideoRecing.setVisibility(VideoRecVisiblity);
		 VideoRecText.setVisibility(VideoRecVisiblity);
		 //Video LV OSD
		 SDCardIcon.setVisibility(LVOSDVisiblity);
//		 PlayBackBtn.setVisibility(LVOSDVisiblity);
		 ImageRemainCountText.setVisibility(LVOSDVisiblity);
		 imageFlashButton.setVisibility(LVOSDVisiblity);
		 imageSettingButton.setVisibility(LVOSDVisiblity);
		 ImagemodeChangeButton.setVisibility(LVOSDVisiblity);
		 imageVideoModeButton.setVisibility(LVOSDVisiblity);
		 imageDownloadButton.setVisibility(LVOSDVisiblity);
		 //TeleBtn.setVisibility(LVOSDVisiblity);
		 //WideBtn.setVisibility(LVOSDVisiblity);
    }
	
	private void Init_menu() {
	 
        //Flash
		registerForContextMenu(imageFlashButton);           
		
		//Setting
		registerForContextMenu(imageSettingButton); 
		
		//Auto power off time
		registerForContextMenu(VideoRecBtn); 
		
		//Beepsound
		registerForContextMenu(CloseBtn); 
		
		//Touchshot
		registerForContextMenu(Batterylevel); 
	}
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == R.id.imageButton_setting)
        {
        	menu.setHeaderTitle(R.string.SetupMenu);   
            //menu.setHeaderIcon(R.drawable.setting);          
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.settingmenu, menu);
        	popupSettingMenu =menu;
        	MenuItem item ;

        	item = menu.findItem(R.id.setting_item_sound) ;
        	item.setChecked(!(Boolean)camera.getCameraSetting(CubicCamera.CAMERA_SETTING_SOUNDVOLUME_MUTE)) ;
        	item = menu.findItem(R.id.setting_item_TouchShot) ;
        	item.setChecked((Boolean)camera.getCameraSetting(CubicCamera.CAMERA_SETTING_TOUCHSHOT_ENABLE));

        	if(IsNoSDcard==true)
			{
				item = menu.findItem(R.id.setting_item_format) ;    		    			
				item.setVisible(false);    			
			}
        	if ( camera.getCapability().isSupportPowerOffTimeSetting() )
			{
    			String poweroff_title = getResources().getString(R.string.setting_item_autopoweroff) ;
    			String poweroff_setting ="" ;
				item = menu.findItem(R.id.setting_item_autopoweroff) ;  
				int AutopowerOffTime=(Integer)camera.getCameraSetting(CubicCamera.CAMERA_SETTING_AUTOPOWEROFF_TIME);
     			if(AutopowerOffTime == Capture_Setting_Define.AutoPowerOff1Min)
     				poweroff_setting = getResources().getString(R.string.autopowerofftime_item_1min);
     			else if(AutopowerOffTime == Capture_Setting_Define.AutoPowerOff5Min)
     				poweroff_setting = getResources().getString(R.string.autopowerofftime_item_5min);
     			else if(AutopowerOffTime == Capture_Setting_Define.AutoPowerOffOFF)
     				poweroff_setting = getResources().getString(R.string.autopowerofftime_item_off);
          		item.setTitle(poweroff_title+" :  "+ poweroff_setting);	
				item.setVisible(true);  
			}
        	if (camera.getCapability().stillMode.isSupportMacroMode() ) 
        	{
				item = menu.findItem(R.id.setting_item_MarcoMode) ;    		    			
				item.setVisible(true); 
			  	item = menu.findItem(R.id.setting_item_MarcoMode) ;
	        	item.setChecked((Boolean)camera.getCameraSetting(CubicCamera.CAMERA_SETTING_MACROMODE_ENABLE));
			}
        }
        else if(v.getId() == R.id.VideoRecBtn)
        {
        	
        	menu.setHeaderTitle(R.string.setting_item_autopoweroff);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.autopowerofftime, menu);
            popupAutoPowerOffMenu =menu;
        }
        return ;
    }
	
	void setPowerOffTime(int newValue)
	{
		class PowerOffTimeSetting implements Runnable{
			int setting ;
			PowerOffTimeSetting(int newSetting) { setting=newSetting; }
			@Override
			public void run() {
				try
				{
					camera.setCameraSetting(CubicCamera.CAMERA_SETTING_AUTOPOWEROFF_TIME, setting) ;
				}
				catch(Exception  e) {} ;
			}
			
		}
		new Thread(new PowerOffTimeSetting(newValue),"remote set power off time").start() ;
	}
	@Override  
	public boolean onContextItemSelected(MenuItem item) {   
		
		
		switch(item.getItemId()) {
        case R.id.setting_item_autopoweroff:
        	Thread t = new Thread(new ShowAutoPowerOffMenuAction(),"remote show auto power off");
			t.start(); // 開始執行Runnable.run(); 
            break; 
        case R.id.autopowerofftime_item_2:   
        	setPowerOffTime(Capture_Setting_Define.AutoPowerOff1Min);
            break;
        case R.id.autopowerofftime_item_10:  
        	setPowerOffTime( Capture_Setting_Define.AutoPowerOff5Min);
            break;
        case R.id.autopowerofftime_item_off:
        	setPowerOffTime(Capture_Setting_Define.AutoPowerOffOFF);
            break;  
        case R.id.setting_item_sound:
        	{
        		boolean setting = !item.isChecked() ;
				class setSoundVolume implements Runnable
				{
					boolean mode ;
					setSoundVolume(boolean newMode) { mode=newMode; };
					public void run() {
						try {
							camera.setCameraSetting(CubicCamera.CAMERA_SETTING_SOUNDVOLUME_MUTE, mode==true? false : true);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}							
					}					
				}
				new Thread(new setSoundVolume(setting),"remote sound mute").start() ;        		
        	}
        	break;
        case R.id.setting_item_format:
        	
	    	Alter_Message_Type = MessageType.Format_Message;			    	
	    	displayMessageAlter();
            break; 
        case R.id.setting_item_TouchShot:
	        {
        		boolean setting = !item.isChecked() ;
				class setTouchShot implements Runnable
				{
					boolean mode ;
					setTouchShot(boolean newMode) { mode=newMode; };
					public void run() {
						try {
							camera.setCameraSetting(CubicCamera.CAMERA_SETTING_TOUCHSHOT_ENABLE, mode);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}							
					}					
				}
				new Thread(new setTouchShot(setting),"remote touch shot enable").start() ;        		
	        }
        	break;
        case R.id.setting_item_MarcoMode:
        {
    		boolean setting = !item.isChecked() ;
			class setMacroMode implements Runnable
			{
				boolean mode ;
				setMacroMode(boolean newMode) { mode=newMode; };
				public void run() {
					try {
						camera.setCameraSetting(CubicCamera.CAMERA_SETTING_MACROMODE_ENABLE, mode);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}							
				}					
			}
			new Thread(new setMacroMode(setting),"remote set macro").start() ;        		
        	
        }
    	break;
        case R.id.setting_item_about:
			{
				AlertDialog.Builder about_box=new AlertDialog.Builder(this);

				about_box.setTitle(R.string.str_about);
				LayoutInflater inflater = getLayoutInflater();
				View about_dlg_view = inflater.inflate(R.layout.about_view, null) ;
				
				try {
					String ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
					TextView txt = (TextView)about_dlg_view.findViewById(R.id.txtVer);
					txt.setText(ver) ;
					
					txt = (TextView)about_dlg_view.findViewById(R.id.txtFWVer);
					int FWVersion = camera.getFWVersion() ;
					if(FWVersion >= 10000)
					{
						txt.setText(new String(Integer.toString(FWVersion/10000)+"."+Integer.toString(FWVersion%10000)));
					}
					
					else
					{
						txt.setText(new String("0."+Integer.toString(FWVersion)));
					}
					
				
					
				}
				catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					//
				}
				about_box.setView(about_dlg_view);
				about_box.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
					   @Override
					   public void onClick(DialogInterface dialog, int which) {
						   dialog.dismiss();
					   // Toast.makeText(getApplicationContext(), "Yes Button Clicked",Toast.LENGTH_LONG).show();
					   }
					  });
				about_box.show() ;
	
			}
			break;
             
        default:
            break;
		}
		return true;   
	}
	 public class ShowAutoPowerOffMenuAction implements Runnable {
	       
			public void run() { // implements Runnable run()
				try{
				Thread.sleep(250);
				}catch(Exception e) {};
				//util.DelayFun(250);
     	 runOnUiThread(new Runnable() {  
     		 @Override
     		 public void run() {           			 
     			VideoRecBtn.showContextMenu();
     			int AutopowerOffTime=(Integer)camera.getCameraSetting(CubicCamera.CAMERA_SETTING_AUTOPOWEROFF_TIME);
     			if(AutopowerOffTime == Capture_Setting_Define.AutoPowerOff1Min)
     				popupAutoPowerOffMenu.getItem(0).setChecked(true); 
     			else if(AutopowerOffTime == Capture_Setting_Define.AutoPowerOff5Min)
     				popupAutoPowerOffMenu.getItem(1).setChecked(true);
     			else if(AutopowerOffTime == Capture_Setting_Define.AutoPowerOffOFF)
     				popupAutoPowerOffMenu.getItem(2).setChecked(true);
          			}
      			}
     			 ); 
      		    	
			}
	} 
	void UpdateFlashIcon(int flashmode)
	{
		int resId;
		switch(flashmode)
		{
			case CubicCamera.CAMERA_SETTING_FLASHMODE_AUTO:
				resId=R.drawable.lcd_btn_flash_auto;
				break;
			case CubicCamera.CAMERA_SETTING_FLASHMODE_OFF:
				resId=R.drawable.lcd_btn_flash_off;
				break;
			default:
				resId=R.drawable.lcd_btn_flash_fill;
				break;
		}
		imageFlashButton.setImageResource(resId);

	}
	public void onChangeMode(View button) {
		
   	if (IsIRPFinish==false ) return ;
   	
			 
			new Thread(new Runnable()
			{

				@Override
				public void run() {
					try{
						if (camera.getTakeMode().isNowStillMode())
						 {
							camera.getTakeMode().switchToVideo() ;
						 }else
						 {
							 camera.getTakeMode().switchToStill() ;
						 }
					}
					catch(Exception e) {};
				}				
			},"remote on change mode").start() ;
		 
	}

	void UpdateTakeModeIcon()
	{		
		int resId =0;
		switch(camera.getTakeMode().currentVideoMode())
		{
			case CubicCamera.TakeMode.VIDEOSIZE_FULLHD:
				resId=R.drawable.lcd_btn_video_fhd;
				break;
			case CubicCamera.TakeMode.VIDEOSIZE_VGA:
				resId=R.drawable.lcd_btn_video_qshare1;
				break;
			case CubicCamera.TakeMode.VIDEOSIZE_640x640:
				resId=R.drawable.lcd_btn_video_qshare2;
				break;
					
		}
		if (camera.getTakeMode().isNowVideoMode())
		{
			imageVideoModeButton.setImageResource(resId);
			ImagemodeChangeButton.setImageResource(R.drawable.lcd_btn_mode_video);
			TakeImageBtn.setImageResource(R.drawable.lcd_btn_shutter_record);
			 imageFlashButton.setVisibility(View.GONE);
			 imageVideoModeButton.setVisibility(View.VISIBLE);
			 TouchShotIcon.setVisibility(View.INVISIBLE) ;
			 macroIcon.setVisibility(View.INVISIBLE) ;
		}
		else
		{
			 ImagemodeChangeButton.setImageResource(R.drawable.lcd_btn_mode_still);			
			 TakeImageBtn.setImageResource(R.drawable.lcd_btn_shutter_still);
			 imageFlashButton.setVisibility(View.VISIBLE);
			 imageVideoModeButton.setVisibility(View.GONE);
			 TouchShotIcon.setVisibility((Boolean)camera.getCameraSetting(CubicCamera.CAMERA_SETTING_TOUCHSHOT_ENABLE) ==true ? View.VISIBLE : View.INVISIBLE) ;
			 macroIcon.setVisibility((Boolean)camera.getCameraSetting(CubicCamera.CAMERA_SETTING_MACROMODE_ENABLE) ? View.VISIBLE : View.INVISIBLE) ;			
		}
	}

	public void onShowPopSettingMenu(View button) {
	        
		    
		 imageSettingButton.showContextMenu();			 
		 while(ResetAutoPowerOffAPI(true) == false) {
			 try {
				 Thread.sleep(100);
			 }
			 catch(Exception e) {} ;
		 }
	}
	 
	void SetupMenuSetting(int a_dwSetupAction) 
	{
		
		if(a_dwSetupAction == R.id.setting_item_format)
		{
	    	ImageDatabase.delete() ;
	    	new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						camera.formatStorage() ;
					} catch (Exception e) {
					}					
				}
	    	},"remote setup menu setting").start();
		}
		
	}
    boolean ResetAutoPowerOffAPI(boolean a_bIsAPPRunning)
    {
		new Thread( new Runnable() {

			@Override
			public void run() {
			
				try {
					camera.resetAutoPowerOff() ;
				} catch (Exception e) {
				}
			}			
		},"remote reset autopower off").start() ;
        return true;
    }	
	 

	 void SetRemainCountString() 
	 {
		 CubicCamera.TakeMode takeMode = camera.getTakeMode();
		 RemainCount = camera.getRemainCount() ;
		if(takeMode.isNowStillMode())
		{	
			ImageRemainCountStr = Integer.toString(RemainCount);
		}		
		else
		{
			int videoMode = takeMode.currentVideoMode() ;
			//Initial Video Max Recorder Time
			if(videoMode == CubicCamera.TakeMode.VIDEOSIZE_VGA)
			{
				MaxRecTime = (RemainCount > Capture_Setting_Define.YoutubeMaxRexTime) ? Capture_Setting_Define.YoutubeMaxRexTime : RemainCount ;
				
			}
			else if(videoMode == CubicCamera.TakeMode.VIDEOSIZE_640x640)
			{
				MaxRecTime = (RemainCount > Capture_Setting_Define.InstagramRexTime) ? Capture_Setting_Define.InstagramRexTime : RemainCount ;
			}
			else
			{
				MaxRecTime = RemainCount;
			}
			ImageRemainCountStr = String.format("%02d", RemainCount/3600)+":"
										+String.format("%02d", (RemainCount%3600)/60)+":"
										+String.format("%02d",(RemainCount%3600)%60);
				
		
		 }
       	ImageRemainCountText.setText(ImageRemainCountStr.toCharArray(), 0, ImageRemainCountStr.length());
	 }
	 
	 /*
	  * 
	  * Below code for download mode
	  * 
	  */

	  private ListView mainList;
	  private LayoutInflater inflater;
	  private List<Photo> photos;
	  private CachedImageFetcher cachedImageFetcher;
	  private static final String DownloadTAG = "download_activity";
	  final ConditionVariable startDone = new ConditionVariable();
		//for download log
		private static final String TAG = "DownLoadView"; 
		private DownloadFileFromURL downloadtask;
		
	  	//We have to use thread
		 public class RemoteControl_GetThumbnailAction implements Runnable {
		    boolean isValidObject(int id)
		    {
	        	long objHandle =id&0x0fffffff ;
	        	if ( (objHandle & 0xffff)==0 ) return false ;
	        	short mediaType = (short) (Long.rotateRight(objHandle, 26) &0x3);
	        	if ( (mediaType!=0) && (mediaType!=1) ) return false ;
		    	return true;
		    }
		    boolean isVideoFile(int id)
		    {
	        	long objHandle =id&0x0fffffff ;
	        	short mediaType = (short) (Long.rotateRight(objHandle, 26) &0x3);
		    	return mediaType==1;
		    }
		    Photo createPhoto(int id)
		    {
	        	Photo currentPhoto = new Photo();
	        	currentPhoto.setCheck(false);
	        	currentPhoto.setVideoFile(isVideoFile(id)) ;
	        	String idstr = Integer.toString(id);
	        	currentPhoto.setThumbnailUrl( idstr );
	        	currentPhoto.setImageUrl( idstr );
		    	return currentPhoto ;
		    }
			public void run() { // implements Runnable run()
	    		
	    		CheckCardIdForCache();				// call here to prevent extra thread and ptpin ready check
	    		
		        photos = new ArrayList<Photo>();
		        camera.enumPictureObject(new CubicCamera.PictureHandle() {					
					int i=0;
					@Override
					public boolean handler(int objId) {
						Photo currentPhoto =createPhoto(objId) ;
			        	photos.add(currentPhoto);
			        	if ((i++%100)==0) ResetAutoPowerOffAPI(true);
						return true;
					}
				});
		        startDone.open();

			}
		}

		 
		 public Bitmap getimage(int id)
		 {
			 Bitmap bmp=null;
			 try {
				bmp = camera.getThumbImage(id);
			} catch (Exception e) {
				e.printStackTrace();				
			}
			 return bmp;
		 }
		    public static final int progress_bar_type = 0; 
		    public static final int dlg_noimage =1;
		    
		    // Progress Dialog
		    private ProgressDialog pDialog;
		    private AlertDialog.Builder altDialog;
		    
			 //download file directory
			 File myDownloadDir; 
		    /**
		     * Showing Dialog
		     * */
		    @Override
		    protected Dialog onCreateDialog(int id) {
		        switch (id) {
		        case progress_bar_type: // we set this to 0
		            pDialog = new ProgressDialog(this);
		            pDialog.setMessage( getString( R.string.str_download_msg) );
		            pDialog.setIndeterminate(false);
		            pDialog.setMax(100);
		            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		            pDialog.setCancelable(true);
		            pDialog.setCanceledOnTouchOutside(false);
		            //pDialog.setCancelable(false);
		            
		            pDialog.setButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener()
		            {
		                public void onClick(DialogInterface dialog, int which)
		                {
		                    // Use either finish() or return() to either close the activity or just the dialog
		                 downloadtask.cancel( true );
		                 downloadtask=null ;
		                }
		            });
		            pDialog.show();
		            return pDialog;
		        case dlg_noimage:
		        	altDialog = new AlertDialog.Builder(this) ;
		        	altDialog.setMessage(R.string.str_no_picture)
		        			.setCancelable(false)
		        			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		                        public void onClick(DialogInterface dialog, int id) {
		                        	dialog.dismiss() ;
		                        	backToLiveviewMode();
		                            // FIRE ZE MISSILES!
		                        }
		                    });
		        	return altDialog.show() ;
		        default:
		            return null;
		        }
		    }
		    public  static String getMimeType(String url)
		    {
		        String type = null;
		        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		        if (extension != null) {
		            MimeTypeMap mime = MimeTypeMap.getSingleton();
		            type = mime.getMimeTypeFromExtension(extension.toLowerCase(Locale.US));
		        }
		        return type;
		    }		 
		    /**
		     * Background Async Task to download file
		     * */
		    class DownloadFileFromURL extends AsyncTask<String, String, String> {
		    	
		        @Override
				protected void onCancelled() {
					super.onCancelled();
		            dismissDialog(progress_bar_type);
		            
		            cachedImageFetcher.clearCache();
		            loadPhotos();				
				}

				/**
		         * Before starting background thread
		         * Show Progress Bar Dialog
		         * */
		        @Override
		        protected void onPreExecute() {
		            super.onPreExecute();
		          
		            showDialog(progress_bar_type);
		        }
		 
		        /**
		         * Downloading file in background thread
		         * */
		        @Override
		        protected String doInBackground(String... f_url) {
		            int count = 0;
	                int total = 0;
		            try {
		            	
		    	    	Iterator<Photo> iterator = photos.iterator();
		    	    	while (iterator.hasNext()) {
		    	    		Photo element = (Photo)iterator.next();
		    	    		if( element.getCheck() == true ){
		    	    			total++;
			               }
		    	    	}

		    	    	iterator = photos.iterator();
		    	    	while (iterator.hasNext() && !isCancelled()) {
		    	    		Photo element = (Photo)iterator.next();
		    	    		if( element.getCheck() == true ){
		    	    			count++;

		    	    			CubicCamera.ObjectInfo objInfo = camera.getObjectInfoById(Integer.parseInt(element.getFullImageUrl()));
		    	    			element.setName(objInfo.getFileName());
		    	    			Log.i(TAG, "get file name: " + element.getName()  );
		    	    			
		    	    			int offset = 0 ;
	                              
	                              
		    	    			//replace SavePartialFile
		    				 	long fileSize = 0;
		    				 	Log.i(TAG, "+++ SavePartialFile +++");
		    				 	    		
		    			    	File file = new File (myDownloadDir, element.getName() );
		    			    	boolean isFileExist = file.exists () ; 
		    			    	Log.i(TAG, "filename = " + element.getName());
		    			        if (file.exists ()) file.delete (); 
		    			        
		    			        FileOutputStream out;
		    					try {
		    						out = new FileOutputStream(file);
		    						fileSize = objInfo.getCompressedSize() ;
		    						
		    					 	long totalsize = fileSize;
		    					 	boolean showEstimate = (totalsize >=10*1024*1024) ;
		    					 	
		    					 	publishProgress(""+(int)((0*100)/totalsize) ,getResources().getString(R.string.str_download_msg) + count + "/" + total );
		    					 	long processsize = 0;
		    					 	int a_dwMaxSize = 600*1024;
		    					 	long xfrTimeInMs ,estimateXfrRemainTime ;
		    					 	String downloadStr = getResources().getString(R.string.str_download_file);
		    					 	String estimateStr = getResources().getString(R.string.str_download_estimate);
		    					 	String estimateDispStr ;
		    					 	long xfrTimeInMsStart = android.os.SystemClock.elapsedRealtime();
		    					 	while(fileSize > 0  && (!isCancelled()) )
		    				    		{
		    				    			Log.i(TAG, "Start PTPOverIP_GetPartialObjecAndSaveFile...");
		    				    			int readSize=camera.getPartialByIdAndSaveFile(Integer.parseInt(element.getFullImageUrl()), offset, a_dwMaxSize, out) ;
		    				    			xfrTimeInMs = android.os.SystemClock.elapsedRealtime() - xfrTimeInMsStart ;
		    				    			Log.d(TAG, " Offset : " + offset +" readsize :"+ readSize) ;
		    				    			offset = offset +a_dwMaxSize;
		    				    			fileSize = fileSize - a_dwMaxSize;
		    				    			
		    				    			estimateDispStr = null;
		    				    			if ( fileSize>0 && showEstimate)
		    				    			{
			    				    			estimateXfrRemainTime =  fileSize*xfrTimeInMs/offset ; 
			    				    			estimateXfrRemainTime = estimateXfrRemainTime/1000 ;
			    				    			int  value;
			    				    			value = (int) ((estimateXfrRemainTime / (60*60)) % 24);;
			    				    			if (value !=0 )
			    				    			{
			    				    				estimateDispStr =  value + ":" ;
			    				    			}
			    				    			value = (int) ((estimateXfrRemainTime / (60)) % 60); ;
			    				    			if (estimateDispStr ==null ) {
			    				    				if (value!=0) estimateDispStr = value + ":" ;
			    				    			}
			    				    			else 
			    				    			{
			    				    				estimateDispStr = estimateDispStr + value + ":" ;
			    				    			}
			    				    			value = (int) (estimateXfrRemainTime) % 60 ;
			    				    			if (estimateDispStr ==null ) {
			    				    				estimateDispStr = Integer.toString(value)  ;
			    				    			}
			    				    			else 
			    				    			{
			    				    				estimateDispStr = estimateDispStr + value  ;
			    				    			}
		    				    			}	
		    				    			//Log.d(TAG, "-->FileSize:"+Integer.toString(tPTP_Over_IP_Cmd.OperationRespPacket.Param[0]));
		    				    			processsize += a_dwMaxSize;
		    				    			publishProgress(""+(int)((processsize*100)/totalsize) , 
		    				    					downloadStr + count + "/" + total  +
		    				    							(estimateDispStr==null ? "" : "\n"+ estimateStr+ " " + estimateDispStr) );
		    				    		}	
		    					 	out.flush();
		    						out.close();
		    						if (isCancelled())
		    						{
		    							file.delete() ;
		    						}
		    						else {
		    						if (!isFileExist)
		    						{
			    					 	ContentValues values = new ContentValues(2);
			    						String filepath = file.getPath();
			    						
			    				
			    					    File jpgFile=new File(filepath) ;
			    					    Log.d(TAG,"get file"+filepath) ;
			    					    
			    						String MimeType = getMimeType(filepath) ;
			    						
			    					    values.put(Images.Media.TITLE, jpgFile.getName());
			    					    values.put(Images.Media.DISPLAY_NAME, jpgFile.getName());
			    					    values.put(Images.Media.DATE_TAKEN, jpgFile.lastModified());
			    					    values.put(Images.Media.MIME_TYPE, MimeType);
			    					    values.put(Images.Media.DATA, filepath );
			    					    values.put(Images.Media.SIZE, jpgFile.length());
			    					    if (MimeType.startsWith("video"))
			    					    {
			    					    	
			    					    	getContentResolver().insert(Video.Media.EXTERNAL_CONTENT_URI, values);
			    					    }
			    					    else
			    					    {
			    					    	getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
			    					    }        
		    						}
		    						}
		    				} catch (FileNotFoundException e) {
		    						e.printStackTrace();
		    					} catch (IOException e) {
		    						e.printStackTrace();
		    					}
		    					if (!isCancelled())
		    					{
		    					element.setCheck(false);
			                    cachedImageFetcher.updateDownloadFlag( element.getFullImageUrl() );
		    					}
			               }
		    	    	}
		 
		            } 
		            catch (Exception e) {		            	
		            	e.printStackTrace() ;
		            }
		            return null;
		        }
		 
		        /**
		         * Updating progress bar
		         * */
		        protected void onProgressUpdate(String... progress) {
		            // setting progress percentage
		            pDialog.setProgress(Integer.parseInt(progress[0]));
		            pDialog.setMessage(progress[1]);
		       }
		 
		        /**
		         * After completing background task
		         * Dismiss the progress dialog
		         * **/
		        @Override
		        protected void onPostExecute(String file_url) {
		            // dismiss the dialog after the file was downloaded
		            dismissDialog(progress_bar_type);
		            
		            cachedImageFetcher.clearCache();
		            loadPhotos();
		        }
		 
		    }		 
	
	long prvCardId ;	    
	private static final String PreviousCardId = "PreviousCardId" ;
		    
	private void SaveParameter()
    {
		SharedPreferences perf = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = perf.edit();
		editor.putLong(PreviousCardId, prvCardId);
		editor.commit();
	}
	
	private void RestoreParameter()
	{		
		SharedPreferences perf = getPreferences(MODE_PRIVATE) ;
		prvCardId = perf.getLong(PreviousCardId, 0);
	}

	
	
	boolean cardIdBeenChecked =false;
	
	//
	// please note , this routine can not call by UI thread
	//
	void CheckCardIdForCache()
	{
		if (!cardIdBeenChecked)
		{
			cardIdBeenChecked=true;
			RestoreParameter();
			long currentCardId = camera.getCardId();
			if (currentCardId!=prvCardId)
			{
				ImageDatabase.delete();
				prvCardId=currentCardId ;
				SaveParameter();
			}			
		}
	}
		    
		   // Timer downloadTimer ;
	 public void ChangeToDownLoadView() {
		 
			
			imageDownloadButton.setEnabled(false);
			currentView = R.layout.photo_list ;
			setContentView(currentView);
        	IsStopLVDisplay=true;

			Thread getThumbnailThread = new Thread(new RemoteControl_GetThumbnailAction(),"remote get thumbnail");
			getThumbnailThread.start(); // 開始執行Runnable.run();
		 
		    mainList = (ListView) findViewById(R.id.photolist);
		    inflater = LayoutInflater.from(this);
				startDone.block() ;
				startDone.close() ;

		    cachedImageFetcher = new CachedImageFetcher(new FileSystemImageCache(), new CachedImageFetcher.ImgControlInterface(){

				@Override
				public Bitmap getImageSub(int objId) {
					return getimage(objId);
					
				}
		    
		    });
		    initCurrentConfiguration();
		    loadPhotos();
		    
		    
	        final Button selectBtn = (Button)this.findViewById(R.id.selectBtn);
	        selectBtn.setOnClickListener(new OnClickListener() {

	            @Override
	            public void onClick(View v) {
	    	        //file path
	    	        String root = Environment.getExternalStorageDirectory().toString();
	    	    	myDownloadDir = new File(root + "/DCIM"); 
	                // starting new Async Task
	                downloadtask = new DownloadFileFromURL();
	                downloadtask.execute( "test" );
	            }
	            
	        });
	        
	        final Button cancelBtn = (Button)this.findViewById(R.id.btnCancelDownload);
	        cancelBtn.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	if (null!=cachedImageFetcher) cachedImageFetcher.onStop();
	            	backToLiveviewMode();
	            }
	        });
	        if (photos.size()==0)
	        {
	        	showDialog(dlg_noimage);
	        }
		    
	 }
	 	private void backToLiveviewMode()
	 	{
        	photos.clear();
        	new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						camera.switchToBrowserMode(false);
					}catch (Exception e) {}
				}
        		
        	},"remote switch to review").start();
        	reInitUI(R.layout.activyty_remote_control_ui_portrait);
			imageDownloadButton.setEnabled(IsNoSDcard?false:true);	 		
	 	}
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if  (  (currentView==R.layout.photo_list) &&
					(keyCode == KeyEvent.KEYCODE_BACK) )
			{
				backToLiveviewMode();
    			return true;
			}
			else if(keyCode == KeyEvent.KEYCODE_BACK&&bForceBackKey == false)
			{ 			    
			    	bAlter_Message_Show = false;
			    	Alter_Message_Type = MessageType.BackKey_Message;			    	
			    	displayMessageAlter();
			    	return false;			    
			}			
			return super.onKeyDown(keyCode, event);
		}
	 
	  private void initCurrentConfiguration() {
		    CachedImageFetcher savedImageFecther = (CachedImageFetcher) getLastNonConfigurationInstance();
		    if (savedImageFecther != null) {
		      cachedImageFetcher = savedImageFecther;
		    }
	  }

	  @Override
	  public Object onRetainNonConfigurationInstance() {
	    return cachedImageFetcher;
	  }

	  private void loadPhotos() {
		    if (photos == null) {
		      Log.d(DownloadTAG, "No photos!");
		      return;
		    }

		    ThumbnailClickListener<Photo> clickListener = new ThumbnailClickListener<Photo>() {
		      @Override
		      public void thumbnailClicked(Photo photo , boolean isChecked ) {
		    	  //we get a click event here
		    	  if(isChecked==true)
		    		  photo.setCheck(true);
		    	  else
		    		  photo.setCheck(false);
	        	 ResetAutoPowerOffAPI(true);
		      }
		    };
		    
		    mainList.setAdapter(new PhotosAdapter(
		    						wrap(photos),
		    						inflater,
		    						clickListener, 
		    						cachedImageFetcher, 
		    						this.getResources().getDisplayMetrics()
		    						)
		    					);
		    BaseAdapter adapter = (BaseAdapter) mainList.getAdapter();
		    adapter.notifyDataSetChanged();
		    adapter.notifyDataSetInvalidated();
		    mainList.invalidateViews();
	}
	  
	  // Wraps a list of {@link Photo}s into a list of {@link ThumbnailItem}s, so
	  // they can be displayed in the list.

	  private static List<ThumbnailItem<Photo>> wrap(List<Photo> photos) {
	    List<ThumbnailItem<Photo>> result = new ArrayList<ThumbnailItem<Photo>>();
	    for (Photo photo : photos) {
	      result.add(new ThumbnailItem<Photo>(photo.getName(), photo.getThumbnailUrl(), photo));
	    }
	    return result;
	  }

	@Override
	public void batteryStatusUpdate(int arg0) {
		UpdateBatteryIcon();
	}

	@Override
	public void cameraAction(int action) {
		switch(action)
		{
		case DSCStatusUpdateListener.DSC_ACTION_S2_Event:
			MotionEvent motionEvent = MotionEvent.obtain(
					SystemClock.uptimeMillis(), 
					SystemClock.uptimeMillis() + 100, 
			    MotionEvent.ACTION_DOWN, 
			    0.0f, 
			    0.0f, 
			    0
			);

			TakeImageBtn.dispatchTouchEvent(motionEvent);
			
			motionEvent = MotionEvent.obtain(
					SystemClock.uptimeMillis(), 
					SystemClock.uptimeMillis() + 100, 
			    MotionEvent.ACTION_UP, 
			    0.0f, 
			    0.0f, 
			    0
			);

			TakeImageBtn.dispatchTouchEvent(motionEvent);
			break;
		}
	}

	@Override
	public void errorNotify(int CameraErrType) {
		// TODO Auto-generated method stub
	 	bAlter_Warning_Show = false;
	 	if(CameraErrType == CubicCamera.ERROR_CODE.STORAGE_UNUSABLE || 
	 		CameraErrType == CubicCamera.ERROR_CODE.STORAGE_NOT_FORMAT)
	 		Alter_Warning_Type = AlertType.CardErr_Warning;	
	 	else if(CameraErrType == CubicCamera.ERROR_CODE.LENS_ERROR )
	 		Alter_Warning_Type = AlertType.LensErr_Warning;
	 	else if(CameraErrType == CubicCamera.ERROR_CODE.HIGH_TEMPERATURE )
	 		Alter_Warning_Type = AlertType.TemperatureErr_Warning;
    	displayWarningAlter();
	}

	private void HandleQVComplete(String qvFile)
	{
		ContentValues values = new ContentValues(2);
        File jpgFile=new File(qvFile) ;

      	values.put(Images.Media.TITLE, jpgFile.getName());
        values.put(Images.Media.DISPLAY_NAME, jpgFile.getName());
        values.put(Images.Media.DATE_TAKEN, jpgFile.lastModified());
        values.put(Images.Media.MIME_TYPE, getMimeType(qvFile));
        values.put(Images.Media.DATA, qvFile );
        values.put(Images.Media.SIZE, jpgFile.length());
        Uri uri=getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
    	sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));		
	}
	
    class DownloadObjectTask extends AsyncTask<Integer, Integer, String> {

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			 TakeImageBtn.setImageResource(R.drawable.lcd_btn_shutter_record);
			 DisplayVideoLVOSD(true);	
	     	 ImageRemainCountText.setText(ImageRemainCountStr.toCharArray(), 0, ImageRemainCountStr.length());
	         ImageRemainCountText.setVisibility(View.VISIBLE);	
			 ProgressBar.dismiss();	
			 IsIRPFinish=true;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
  			 ProgressBar.setProgress(values[0]);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
  			 ProgressBar.setMessage(getResources().getString(R.string.VideoDownload));
			 ProgressBar.show();
			 ProgressBar.setProgress(0);
			 ProgressBar.setMax(100);
			 TakeImageBtn.setImageResource(R.drawable.lcd_btn_shutter_record);
			 ProgressBar.setCancelable(false);			
		}

		@Override
		protected String doInBackground(Integer... params) {
			int objId=params[0];
			try{
				CubicCamera.ObjectInfo info = camera.getObjectInfoById(objId);
	    		String root = Environment.getExternalStorageDirectory().toString();
	    		if(myDir.isDirectory() == false) myDir.mkdir();
	            File file = new File (myDir, info.getFileName());	            
	            if (file.exists ()) file.delete (); 
	    		FileOutputStream out = new FileOutputStream(file);	    		
	    		long fileSize = info.getCompressedSize() ;
	    		int offset=0;
	    		int loopCount = (int)(fileSize/DATA_SIZE_WHEN_GET_PARTITAL)+1 ;
	    		for (int i=0;i<loopCount;i++)
	            {
	            	camera.getPartialByIdAndSaveFile(objId, offset, DATA_SIZE_WHEN_GET_PARTITAL, out);
	            	fileSize-=DATA_SIZE_WHEN_GET_PARTITAL ;
	            	offset+=DATA_SIZE_WHEN_GET_PARTITAL ;
	            	publishProgress( i*100/loopCount);
	            }
	    		out.flush();
				out.close();
				ContentValues values = new ContentValues(2);
				if (info.isVideoFile()) {
					values.put(MediaStore.Video.Media.TITLE, file.getName() ) ;
				    values.put(MediaStore.Video.Media.DISPLAY_NAME, file.getName());
				    values.put(MediaStore.Video.Media.DATE_TAKEN, file.lastModified());
				    values.put(MediaStore.Video.Media.MIME_TYPE, getMimeType(file.getPath()));
				    values.put(MediaStore.Video.Media.DATA, file.getPath() );
				    values.put(MediaStore.Video.Media.SIZE, info.getCompressedSize());
				    values.put(MediaStore.Video.Media.RESOLUTION, info.getResolutionString());
				}
				else
				{
					values.put(MediaStore.Images.Media.TITLE, file.getName() ) ;
				    values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
				    values.put(MediaStore.Images.Media.DATE_TAKEN, file.lastModified());
				    values.put(MediaStore.Images.Media.MIME_TYPE, getMimeType(file.getPath()));
				    values.put(MediaStore.Images.Media.DATA, file.getPath() );
				    values.put(MediaStore.Images.Media.SIZE, info.getCompressedSize());
				    values.put(MediaStore.Images.Media.WIDTH, info.getImageWidth());
					values.put(MediaStore.Images.Media.HEIGHT, info.getImageHeight());
				}
	             Uri uri = getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		    	sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            	publishProgress( 100 );
            	return file.getPath();
			}
			catch(Exception e)
			{
				
			}
			return null;
		}
    
    }
	private void HandleAddObject(int objId)
	{
	    imageDownloadButton.setEnabled(IsNoSDcard?false:true);
	    imageVideoModeButton.setEnabled(true);
	    CubicCamera.TakeMode takeMode = camera.getTakeMode() ;
	    if (takeMode.isNowStillMode())
	    {
			IsIRPFinish = true;
			return;	    	
	    }
	    if (takeMode.currentVideoMode() == CubicCamera.TakeMode.VIDEOSIZE_FULLHD)
	    {
			 TakeImageBtn.setImageResource(R.drawable.lcd_btn_shutter_record);
			 DisplayVideoLVOSD(true);
				IsIRPFinish = true;
				return;	    				 
	    }
	    
	    new DownloadObjectTask().execute(objId);	    
	}
	private void HandleCaptureComplete()
	{
		CubicCamera.TakeMode takeMode= camera.getTakeMode() ;
		if  (takeMode.isNowStillMode() &&	bPlayShutterSound == true)
		{
			bPlayShutterSound = false;
			PlaySoundAction PlaySoundThread = new PlaySoundAction();										
			PlaySoundThread.PlaySoundActionParam(Capture_Setting_Define.ShutterSound);									
			new Thread(PlaySoundThread,"remote playsound").start();
			
		}        			
		IsIRPFinish=false;
		if  (takeMode.isNowStillMode())
		{
			if(RemainCount == 0)
				bAddTakeObjectHandle = false;
			else
				bAddTakeObjectHandle = true;
		}
		imageDownloadButton.setEnabled(false);
		imageVideoModeButton.setEnabled(false);		
	}
	
	@Override
	public void statusChanged(int status, Object value) {
		switch(status)
		{
			case DSCStatusUpdateListener.DSC_STATUS_INIT_FINISH:
				SetRemainCountString();
				progressDialog.dismiss() ;
				SetButtonEnable(true);
				UpdateLVIcon();						
			break;
			case DSCStatusUpdateListener.DSC_STATUS_TAKEMODE_CHANGE:
				UpdateTakeModeIcon();
				SetRemainCountString();				
			break;
			case DSCStatusUpdateListener.DSC_STATUS_FLASHMODE_CHANGE:
				UpdateFlashIcon((Integer)value);
			break;
			case DSCStatusUpdateListener.DSC_STATUS_CAPTURE_COMPLETE:
				HandleCaptureComplete();
			break;	
			case DSCStatusUpdateListener.DSC_STATUS_QV_COMPLETE:
				if (value!=null) HandleQVComplete((String)value) ;
			break;
			case DSCStatusUpdateListener.DSC_STATUS_ADD_OBJECT_EVENT:
				HandleAddObject((Integer)value) ;				
				SetRemainCountString();
			break;
			case DSCStatusUpdateListener.DSC_STATUS_MACROMODE_CHANGE:
				macroIcon.setVisibility((Boolean)value ? View.VISIBLE : View.INVISIBLE) ;
				break;
			case DSCStatusUpdateListener.DSC_STATUS_TOUCHSHOT_ENABLE_CHANGE:
				TouchShotIcon.setVisibility((Boolean)value ? View.VISIBLE : View.INVISIBLE) ;
				break;
		}
	}

	@Override
	public void turnoffNotify(int powerOffType) {
		switch(powerOffType)
		{
			case DSCStatusUpdateListener.POWEROFF_TYPE_POWEROFF:
	 			Alter_Error_Type = AlertType.DisConnect_Error;
	 			bAlter_Show = false;
	 			displayErrorAlter();			
			break;
			case DSCStatusUpdateListener.POWEROFF_TYPE_AUTOPOWEROFF:
 		 		bAlter_Message_Show = false;
    	    	Alter_Message_Type = MessageType.AutoPowerOffAlter;			    	
    	    	displayMessageAlter();
			break;
		}
	}
}
