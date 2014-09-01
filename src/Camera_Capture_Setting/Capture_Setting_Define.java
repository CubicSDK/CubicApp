package Camera_Capture_Setting;

public class Capture_Setting_Define {
	public static final int IMG_REOLUTION_12M = 1;	
	public static final int IMG_REOLUTION_8M  = 2;
	public static final int IMG_REOLUTION_4M  = 3;
	public static final int IMG_REOLUTION_2M  = 4;
	
	//CaptureSetting Select
	public static final int SceneSetting	  = 1;
	public static final int FlashSetting	  = 2;
	public static final int ISOSetting		  = 3;
	public static final int ShutterSetting	  = 4;
	public static final int EVCompSetting	  = 5;	
	public static final int VideoModeSetting  = 6;	
	public static final int SoundVolumeSetting  = 7;	
	public static final int TouchShotSetting  = 8;
	public static final int FunctionModeSetting  = 9;
	public static final int AutoPowerOffSetting  = 10;
	public static final int MarcoModeSetting  = 11;	
	//Mode Select
	public static final int AutoMode		  = 1;
	public static final int FireWorkMode	  = 2;
	public static final int VideoMode	  	  = 2;
	//Flash Select
	public static final int FlashAuto		  = 1;
	public static final int FlashOff	  	  = 2;
	public static final int FlashFill		  = 3;
	
	//Speaker Select
	public static final int SpeakerOn		  = 1;
	public static final int SpeakerOff	  	  = 2;
	
	//ISO Select
	public static final int ISOAuto		 	  = 1;
	public static final int ISO100	  	      = 2;	
	public static final int ISO200		 	  = 3;
	public static final int ISO400	  	      = 4;	
	public static final int ISO800		 	  = 5;
	public static final int ISO1600	  	      = 6;	
	public static final int ISO3200		 	  = 7;
	
	//ISO Select
	public static final int ExpoTimeAuto			= 1;//Auto
	public static final int ExpoTime4000			= 2;//4 sec	
	public static final int ExpoTime2000			= 3;//2 sec
	public static final int ExpoTime1000			= 4;//1 sec	
	public static final int ExpoTime500		 	  	= 5;//1/2 sec
	public static final int ExpoTime250	  	      	= 6;//1/4 sec	
	public static final int ExpoTime125		 	  	= 7;//1/8 sec
	public static final int ExpoTime67	  	      	= 8;//1/15 sec	
	public static final int ExpoTime33		 	  	= 9;//1/30 sec
	public static final int ExpoTime17	  	      	= 10;//1/60 sec	
	public static final int ExpoTime8		 	  	= 11;//1/125 sec
	public static final int ExpoTime4		 	  	= 12;//1/250 sec
	public static final int ExpoTime2		 	  	= 13;//1/500 sec
	public static final int ExpoTime1		 	  	= 14;//1/1000 sec
	public static final int ExpoTime8000		 	  	= 15;//1/500 sec
	public static final int ExpoTime16000		 	  	= 16;//1/1000 sec
	
	//EV COMP Select
	public static final int EVCOMPP63				= 1;//EV 6/3
	public static final int EVCOMPP53				= 2;//EV 5/3	
	public static final int EVCOMPP43				= 3;//EV 4/3	
	public static final int EVCOMPP33				= 4;//EV 3/3	
	public static final int EVCOMPP23				= 4;//EV 2/3
	public static final int EVCOMPP13				= 4;//EV 1/3
	public static final int EVCOMPP0		 	  	= 5;//EV 0	
	public static final int EVCOMPM13	  	      	= 6;//EV -1/3		
	public static final int EVCOMPM23		 	  	= 7;//EV -2/3	
	public static final int EVCOMPM33	  	      	= 8;//EV -3/3	
	public static final int EVCOMPM43		 	  	= 9;//EV -4/3	
	public static final int EVCOMPM53	  	      	= 10;//EV -5/3	
	public static final int EVCOMPM63		 	  	= 11;//EV -6/3	
	
	//Flash Select
	public static final int Video_Full_HD		  	= 1;//Full HD
	public static final int Video_YouTube	  	  	= 2;//VGA
	public static final int Video_Instagram		  	= 3;//1:1
	
	//Flash Select
	public static final int ImageRemainCount		= 1;//Full HD
	public static final int VideoRemainCount		= 2;//VGA
	
	//Max Record Time
	public static final int YoutubeMaxRexTime		= 60+1;//sec +1 for newwork lag
	public static final int InstagramRexTime		= 15+1;//sec +1 for newwork lag

	//Volume setting
	public static final int SoundVolumeHigh			= 1;
	public static final int SoundVolumeMedium		= 2;
	public static final int SoundMute				= 3;
	
	//sound effect 
	public static final int PowerOnSound			= 1;
	public static final int ShutterSound			= 2;

	//Touch shot 
	public static final int TouchShotOn				= 1;
	public static final int TouchShotOff		= 2;

	//Touch shot 
	public static final int FunctionBrowse				= 3;
	public static final int FunctionControl			= 4;
	
	//AutoPowerOff Time
	public static final int AutoPowerOff1Min		= 1*60;//1 min	
	public static final int AutoPowerOff2Min		= 2*60;//2 min
	public static final int AutoPowerOff5Min		= 5*60;//5 min	
	public static final int AutoPowerOff10Min		= 10*60;//10 min
	public static final int AutoPowerOffOFF			= 16*60*60;//60*60 min
	
	public static final int MarcoModeOn				= 1;
	public static final int MarcoModeOff			= 0;
}
