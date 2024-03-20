//package com.common.util;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 公共部分静态常量，这里的所有常量与具体某套协议无关，ui层只使用这些常量，不使用任何与协议相关的东西
// * 
// * 
// * 
// *
// */
//public class CommonConstant {
//
//	/** 关闭应用程序 --String EXTRAS_PACKAGE_NAME */
//	public final static String ACTION_STOP_APP = "com.seanovo.android.ACTION_STOP_APP";
//
//	/**
//	 * 每次切换Activity的时候都会发出些广播，包括非正常结束的应用程序，在Framework中的ActivityStack.
//	 * java中resumeTopActivityInnerLocked方法增加广播发出
//	 */
//	public final static String ACTION_RESUME_TOP_ACTIVITY = "com.seanovo.android.action.RESUME_TOP_ACTIVITY";
//
//	/** 应用启动广播 */
//	public final static String ACTION_APP_IN = "com.seanovo.android.ACTION_APP_IN";
//	/** 应用退出广播 */
//	public final static String ACTION_APP_OUT = "com.seanovo.android.ACTION_APP_OUT";
//	/**
//	 * 应用启动与退出时广播，附件 源ID--byte
//	 * 
//	 * @see CommonConstant.SourceId
//	 */
//	public final static String EXTRAS_APP_SOURCE_ID = "extras_app_source_id";
//	/**
//	 * 虚拟声音源变化时通知，附件 源ID--byte 即将要切换到的源：EXTRAS_APP_SOURCE_ID
//	 * 当前被切换掉的源：EXTRAS_APP_SOURCE_ID_CURRENT
//	 * 
//	 * @see CommonConstant.SourceId
//	 */
//	public final static String ACTION_APP_SOURCE_ID_CHANGE = "com.car.service.common.CommonConstant.ACTION_APP_SOURCE_ID_CHANGE";
//	/**
//	 * 应用启动与退出时广播，附件 当前正在播放源ID--byte
//	 * 
//	 * @see CommonConstant.SourceId
//	 */
//	public final static String EXTRAS_APP_SOURCE_ID_CURRENT = "extras_app_source_id_current";
//	/**
//	 * 键值广播 EXTRAS_KEY_PRESS_TYPE EXTRAS_KEY_CODE EXTRAS_IS_NEED_DELAY
//	 * 
//	 * @see Keycode
//	 */
//
//	/** 设置屏幕亮度广播 -- int EXTRAS_BRIGHTNESS */
//	public final static String ACTION_SET_BRIGHTNESS = "com.seanovo.android.ACTION_SET_BRIGHTNESS";
//	/** 亮度改变通知广播 -- int EXTRAS_BRIGHTNESS */
//	public final static String ACTION_BRIGHTNESS_CHANGE = "com.seanovo.android.ACTION_BRIGHTNESS_CHANGE";
//	/** 屏幕亮度广播附件 -- int */
//	public final static String EXTRAS_BRIGHTNESS = "extras_brightness";
//	// /** 屏幕亮度广播附件
//	// * @see com.car.model.BrightnessInfo*/ 暂时不用这个，后面要拓展再看情况吧
//	// public final static String EXTRAS_BRIGHTNESS = "extras_brightness";
//
//	/** 版本信息改变广播 */
//	public final static String ACTION_VERSION_INFO_CHANGE = "com.seanovo.android.ACTION_VERSION_INFO_CHANGE";
//	/**
//	 * 版本信息改变广播附件
//	 * 
//	 * @see com.car.model.VersionInfo
//	 */
//	public final static String EXTRAS_VERSION_INFO = "extras_version_info";
//
//	/** 设备状态改变广播，带一个附件int EXTRAS_DEVICE_STATE */
//	public final static String ACTION_DEVICE_STATE_CHANGE = "com.seanovo.android.ACTION_DEVICE_STATE_CHANGE";
//	public final static String EXTRAS_DEVICE_STATE = "extras_device_state";
//
//	/** beep声音广播，带一个附件int EXTRAS_BEEP_TYPE */
//	public final static String ACTION_BEEP_CONTROL = "com.seanovo.android.ACTION_BEEP_CONTROL";
//	/** 2:表示短按声音 3：表示长按声音 */
//	public final static String EXTRAS_BEEP_TYPE = "extras_beep_type";
//
//	/** 导航软件改变，带两个附件 EXTRAS_PACKAGE_NAME， EXTRAS_CLAZZ_NAME */
//	public final static String ACTION_NAVIGATION_CHANGE = "android.intent.action.NAVI_CHANGED";
//	/** 包名 -- String */
//	public final static String EXTRAS_PACKAGE_NAME = "extras_package_name";
//	/** 类名 -- String */
//	public final static String EXTRAS_CLAZZ_NAME = "extras_clazz_name";
//
//	/** 导航声音改变，带1个附件 EXTRAS_IS_NAVIGATION_PLAYING */
//	public final static String ACTION_NAVIGATION_SOUND_CHANGE = "android.intent.action.ACTION_NAVIGATION_SOUND_CHANGE";
//	/** 导航是否发声 -- boolean */
//	public final static String EXTRAS_IS_NAVIGATION_PLAYING = "extras_is_navigation_playing";
//
//	/**
//	 * 第三方APP声音改变，带3个附件 EXTRAS_IS_THIRD_APP_PLAYING，EXTRAS_IS_THIRD_APP_PACKAGE，
//	 * EXTRAS_AUDIO_TYPE
//	 */
//	public final static String ACTION_THIRD_SOUND_CHANGE = "android.intent.action.ACTION_THIRD_SOUND_CHANGE";
//	/** 第三方APP是否正在播放 */
//	public final static String EXTRAS_IS_THIRD_APP_PLAYING = "extras_is_third_app_playing";
//	/** 第三方APP包名 */
//	public final static String EXTRAS_IS_THIRD_APP_PACKAGE = "extras_is_third_app_package";
//	/** 1:audioTrack 2:mediaPlayer */
//	public final static String EXTRAS_AUDIO_TYPE = "extras_audio_type";
//
//	/** 倒车状态改变，带2个附件 EXTRAS_IS_REVERSE, EXTRAS_IS_REVERSE_MUTE */
//	public final static String ACTION_REVERSE_STATUS_CHANGE = "com.seanovo.android.ACTION_REVERSE_STATUS_CHANGE";
//	/** 是否在倒车 -- boolean */
//	public final static String EXTRAS_IS_REVERSE = "extras_is_reverse";
//	/** 是否在倒车静音 -- boolean */
//	public final static String EXTRAS_IS_REVERSE_MUTE = "extras_is_reverse_mute";
//
//	/** 音量设置，带2个附件 EXTRAS_SET_VOLUME_TYPE, EXTRAS_SET_VOLUME_VALUE */
//	public final static String ACTION_SET_VOLUME = "com.seanovo.android.ACTION_SET_VOLUME";
//	/** 设置音量类型 -- int 1：加减 2：具体值 3:静音/解静音 */
//	public final static String EXTRAS_SET_VOLUME_TYPE = "extras_set_volume_type";
//	/** 值 -- int -4：静音 -3：解静音 -2：减 -1：加 其他：音量值 */
//	public final static String EXTRAS_SET_VOLUME_VALUE = "extras_set_volume_value";
//
//	/** 音量条控制，带1个附件 EXTRAS_IS_SHOW */
//	public final static String ACTION_VOLUME_BAR_CONTROL = "com.seanovo.android.ACTION_VOLUME_BAR_CONTROL";
//	/** 是否显示音量条 -- boolean true:显示 false:隐藏 */
//	public final static String EXTRAS_IS_SHOW = "extras_is_show";
//
//	/**
//	 * SystemUI最近程序中结束指定的应用，会发出这个广播，收到广播后停止音乐等操作，带1个附件 EXTRAS_PACKAGE_NAME --
//	 * string
//	 */
//	public final static String ACTION_FINISH_PACKAGE = "com.seanovo.android.action.ACTION_FINISH_PACKAGE";
//
//	/** 关机机器，--EXTRAS_SHUTDOWN_TYPE */
//	public final static String ACTION_SEANOVO_SHUTDOWN = "com.seanovo.android.ACTION_SEANOVO_SHUTDOWN";
//	/** 关机类型 --int 0:关机 1：重启 */
//	public final static String EXTRAS_SHUTDOWN_TYPE = "extras_shutdown_type";
//
//	/**
//	 * 控制本地音乐<br>
//	 * 三个附件，EXTRAS_CONTROL_TYPE --int 控制类型必填类型<br>
//	 * EXTRAS_ALBUM --String 可选<br>
//	 * EXTRAS_ARTIST --String 可选<br>
//	 * EXTRAS_SONG_NAME --String 可选
//	 */
//	public final static String ACTION_SEANOVO_MUSIC_CONTROL = "com.seanovo.android.action.ACTION_SEANOVO_MUSIC_CONTROL";
//	/** 控制类型--int：1：播放 2：查找 */
//	public final static String EXTRAS_CONTROL_TYPE = "extras_control_type";
//	/** 专辑--String */
//	public final static String EXTRAS_ALBUM = "extras_album";
//	/** 艺术家--String */
//	public final static String EXTRAS_ARTIST = "extras_artist";
//	/** 歌曲名字--String */
//	public final static String EXTRAS_SONG_NAME = "extras_song_name";
//
//	/** 任意类型的信息，实际使用中自行区别 */
//	public final static String EXTRAS_INFO_1 = "extras_info_1";
//	/** 任意类型的信息，实际使用中自行区别 */
//	public final static String EXTRAS_INFO_2 = "extras_info_2";
//	/** 任意类型的信息，实际使用中自行区别 */
//	public final static String EXTRAS_INFO_3 = "extras_info_3";
//
//	// --------------------以上是通用广播定义-----------------
//
//	// -----------以下BT 相关----------------
//	/**
//	 * BT下载广播，带两个附件，一个是下载类型byte -- EXTRAS_BT_DOWNLOAD_TYPE，一个是下载状态byte --
//	 * EXTRAS_BT_DOWNLOAD_STATUS
//	 */
//	public final static String ACTION_BT_DOWNLOAD = "com.seanovo.android.ACTION_BT_DOWNLOAD";
//	/** BT下载广播附件1,byte 1：电话本， 2：所有通话记录， 3：未接电话记录， 4:呼入电话 记录, 5:呼出电话记录 */
//	public final static String EXTRAS_BT_DOWNLOAD_TYPE = "extras_bt_download_type";
//	/** BT下载广播附件2,byte 0： 下载异常， 1：开始下载， 2：下载结束 */
//	public final static String EXTRAS_BT_DOWNLOAD_STATUS = "extras_bt_download_status";
//
//	/**
//	 * BT下载成功广播，带两个附件，一个是类型byte--EXTRAS_BT_DOWNLOAD_TYPE , 一个boolean类型 --
//	 * EXTRAS_BT_DOWNLOAD_IS_SUCCESS
//	 */
//	public final static String ACTION_BT_DOWNLOAD_SUCCESS = "com.seanovo.android.ACTION_BT_DOWNLOAD_SUCCESS";
//	/** BT下载广播附件,boolean true:同步成功 false:同步失败 */
//	public final static String EXTRAS_BT_DOWNLOAD_IS_SUCCESS = "extras_bt_download_is_success";
//
//	/** BT电话状态改变广播，带1个附件，一个是类型byte--EXTRAS_BT_PHONE_STATE */
//	public final static String ACTION_BT_PHONE_STATE_CHANGE = "com.seanovo.android.ACTION_BT_PHONE_STATE_CHANGE";
//	/** BT电话当前所处状态：1：正常待机， 2：来电铃声， 3：去电， 4:通话中 */
//	public final static String EXTRAS_BT_PHONE_STATE = "extras_bt_phone_state";
//
//	/** BT电话状态改变广播，带1个附件，一个是类型byte--EXTRAS_BT_HFP_STATE */
//	public final static String ACTION_BT_HFP_STATE_CHANGE = "com.seanovo.android.ACTION_BT_HFP_STATE_CHANGE";
//	/** 蓝牙电话HFP状态：0:初启化 1：未连接， 2：正在连接， 3：已连接， 4：拨出状态 5:来电状态 6:通话状态 */
//	public final static String EXTRAS_BT_HFP_STATE = "extras_bt_hfp_state";
//
//	/** BT电话声音通道改变广播，带1个附件，一个是类型boolean--EXTRAS_BT_VOICE_CHANNEL_AT_CAR */
//	public final static String ACTION_BT_VOICE_CHANNEL_CHANGE = "com.seanovo.android.ACTION_BT_VOICE_CHANNEL_CHANGE";
//	/** BT电话声音通道是否在车机上--boolean true:车机上 false:手机上 */
//	public final static String EXTRAS_BT_VOICE_CHANNEL_AT_CAR = "extras_bt_voice_channel_at_car";
//
//	/**
//	 * BT电话通话信息广播，带2个String类型附件，--EXTRAS_BT_CALL_ONLINE_NAME --
//	 * EXTRAS_BT_CALL_ONLINE_NUMBER
//	 */
//	public final static String ACTION_BT_CALL_ONLINE_INFO = "com.seanovo.android.ACTION_BT_CALL_ONLINE_INFO";
//	/** BT电话通话姓名--String */
//	public final static String EXTRAS_BT_CALL_ONLINE_NAME = "extras_bt_call_online_name";
//	/** BT电话通话号码--String */
//	public final static String EXTRAS_BT_CALL_ONLINE_NUMBER = "extras_bt_call_online_number";
//
//	/**
//	 * BT电话speek和mic音量改变，带2个int类型附件，--EXTRAS_BT_SPEEK_VOLUME --
//	 * EXTRAS_BT_MIC_VOLUME
//	 */
//	public final static String ACTION_BT_SPEEK_MIC_VOLUME_CHANGE = "com.seanovo.android.ACTION_BT_SPEEK_MIC_VOLUME_CHANGE";
//	/** BT speek音量--int */
//	public final static String EXTRAS_BT_SPEEK_VOLUME = "extras_bt_speek_volume";
//	/** BT mic音量--int */
//	public final static String EXTRAS_BT_MIC_VOLUME = "extras_bt_mic_volume";
//
//	/** BT mic状态改变 ，带1个boolean类型附件，--EXTRAS_BT_MIC_STATE */
//	public final static String ACTION_BT_MIC_STATE_CHANGE = "com.seanovo.android.ACTION_BT_MIC_STATE_CHANGE";
//	/** BT speek音量--boolean */
//	public final static String EXTRAS_BT_MIC_STATE = "extras_bt_speek_volume";
//
//	// -----------以上BT相关-----------------
//}
