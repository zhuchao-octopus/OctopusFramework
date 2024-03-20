package com.common.util;

import com.common.util.MyCmd;

public class ProtocolAk47 {

	// msg receive
	public final static byte TYPE_COMMON_RECEIVE = 0x01;
	public final static byte TYPE_RADIO_RECEIVE = 0x02;
	public final static byte TYPE_RDS_RECEIVE = 0x03;
	public final static byte TYPE_AUDIO_RECEIVE = 0x04;
	public final static byte TYPE_CAN_RECEIVE = 0x05;
	public final static byte TYPE_SETTINGS_RECEIVE = 0x06;
	public final static byte TYPE_IAP_RECEIVE = 0x07; // 升级暂时延续旧的方法,在linux
														// kernel处理.
	public final static byte TYPE_SWC_RECEIVE = 0x09;
	public final static byte TYPE_PANEL_KEY_RECEIVE = 0x0b;
	public final static byte TYPE_SECRET_RECEIVE = 0x0c;
	public final static byte TYPE_DVD_RECEIVE = 0x0d;
	// msg send
	public final static byte TYPE_COMMON_SEND = 0x01;
	public final static byte TYPE_RADIO_SEND = 0x02;
	public final static byte TYPE_RDS_SEND = 0x03;
	public final static byte TYPE_AUDIO_SEND = 0x04;
	public final static byte TYPE_CAN_SEND = 0x05;
	public final static byte TYPE_IAP_SEND = 0x06;
	public final static byte TYPE_SWC_SEND = 0x07;
	public final static byte TYPE_TV_SEND = 0x09;
	public final static byte TYPE_SETTINGS_SEND = 0x0A;
	public final static byte TYPE_PANEL_KEY_SEND = 0x0b;
	public final static byte TYPE_SECRET_SEND = 0x0c;
	public final static byte TYPE_DVD_SEND = 0x0d;
	

	/** Host --> MCU subId:Power 电源相关数据*/
	public  final static byte SEND_COMMON_SUB_POWER = 0x01;
	/** Host --> MCU subId:Front Source  前台通道选择*/
	public  final static byte SEND_COMMON_SUB_FRONT_SOURCE = 0x02;
	/** Host --> MCU subId:Rear L Source  左后台通道选择*/
	public  final static byte SEND_COMMON_SUB_REAL_L_SOURCE = 0x03;
	/** Host --> MCU subId:Brightness*/
	public  final static byte SEND_COMMON_SUB_BRIGHTNESS = 0x04;
	/** Host --> MCU subId:Host Current Source 主机当前通道, MCU询问时下发*/
	public  final static byte SEND_COMMON_SUB_CURRENT_SOURCE = 0x05;
	/** Host --> MCU subId:Bluetooth 蓝牙相关数据*/
	public  final static byte SEND_COMMON_SUB_BLUETOOTH = 0x06;
	/** Host --> MCU subId:Beep Control 蜂鸣器控制*/
	public  final static byte SEND_COMMON_SUB_BEEP_CONTROL = 0x08;
	/** Host --> MCU subId:System Setting info query 查询系统设置信息*/
	public  final static byte SEND_COMMON_SUB_SETTINGS_QUERY = 0x09;
	/** Host --> MCU subId:Version info query*/
	public  final static byte SEND_COMMON_SUB_VERSION_QUERY = 0x0a;
	/** Host --> MCU subId:Load Factory Setting 恢复出厂设置*/
	public  final static byte SEND_COMMON_SUB_FACTORY_RESET = 0x0b;
	/** Host --> MCU subId:**/
	public  final static byte SEND_COMMON_SUB_DVD_LED = 0x0;
	public  final static byte SEND_COMMON_SUB_DVD_POWER = 0x0e;
	public  final static byte SEND_COMMON_SUB_SCREEN0 = 0x17;
	/** Host --> MCU subId:MCU Current Source Query*/
	public  final static byte SEND_COMMON_SUB_CURRENT_SOURCE_QUERY = 0x10;
	/** Host --> MCU subId:Car Status Query查询汽车状态*/
	public  final static byte SEND_COMMON_SUB_CAR_STATUS_QUERY = 0x12;
	
	
	/** Host --> MCU subId:Volume Control 音量控制*/
	public final static byte SEND_VOLUME_SUB_VOLUME_CONTROL = 0x04;
	
	public final static byte SEND_VOLUME_SUB_DATA1_SET_VOLUME = 0x00;

	/** MCU --> Host subId: */
	public final static byte RECEVE_COMMON_KEY_INFO = 0x01;	
	/** MCU --> Host subId:当前播放电台信息*/
	public final static byte RECEVE_RADIO_SUB_RADIO_CURRENT_INFO = 0x02;
	/** MCU --> Host subId:当前播放电台信息*/
	public final static byte RECEVE_RADIO_SUB_PRESET_LIST_FREQUENCY_INFO = 0x03;
	/** MCU --> Host subId:当前播放电台信息*/
	public final static byte RECEVE_RADIO_SUB_CURRENT_RADIO_REGION_INFO = 0x04;
	
	/** MCU --> Host subId:rds标志*/
	public final static byte RECEVE_RDS_SUB_RDS_FLAGS = 0x02;
	/** MCU --> Host subId:pty*/
	public final static byte RECEVE_RDS_SUB_PTY_INFO = 0x03;
	/** MCU --> Host subId:ps*/
	public final static byte RECEVE_RDS_SUB_PS_INFO = 0x04;
	/** MCU --> Host subId:rt*/
	public final static byte RECEVE_RDS_SUB_RT_INFO = 0x05;
	/** MCU --> Host subId:预设列表ps*/
	public final static byte RECEVE_RDS_SUB_PRESET_LIST_PS_INFO = 0x06;

	/** Host --> MCU subId:Radio Operation 收音操作*/
	public final static byte SEND_RADIO_SUB_RADIO_OPERATION = 0x01;	
	/** Host --> MCU subId:Set Radio Frequency 播放特定收音频点*/
	public final static byte SEND_RADIO_SUB_SET_CURRENT_FREQUENCY = 0x02;
	/** Host --> MCU subId:Radio Info Query查询收音信息*/
	public final static byte SEND_RADIO_SUB_QUERY_RADIO_INFO = 0x04;
	

	public final static byte SEND_RDS_SUB_OPT = 0x01;	
	public final static byte SEND_RDS_SUB_QUERY = 0x03;
	


	/** Host --> MCU subId:Audio Info Query查询信息 */
	public  final static byte SEND_AUDIO_CMD_SET = 0x02;
	public  final static byte SEND_AUDIO_CMD_SET2 = 0x03;
	public  final static byte SEND_AUDIO_SUB_QUERYAUDIO_INFO = 0x06;
	

	/** Host --> MCU subId:Audio Info Query查询信息 */
	public  final static byte SEND_DVD_SUB_DVD_POWER = 0x01;

	public final static byte RECEVE_AUDIO_VOLUME_INFO = 0x05;
	public final static byte RECEVE_AUDIO_MUTE_INFO = 0x06;
	public final static byte RECEVE_AUDIO_EQ_INFO = 0x03;
	
	private byte resultType = 0;


	public final static byte getGroupId(byte[] protocol) {
		return protocol[0];
	}

	public final static byte getSubId(byte[] protocol) {
		return protocol[1];
	}

	public static int getDataOffset() {
		return 2;
	}

	public static int getProtocolLength(byte[] protocol) {
		return protocol.length;
	}

	public static int getDataLength(byte[] protocol) {
		return getProtocolLength(protocol) - 2;
	}

	public static byte[] generateNullProtocol(byte groupId, byte subId
			) {
		byte[] protocol = new byte[ 2];
		protocol[0] = groupId;
		protocol[1] = subId;
		return protocol;
	}
	public static byte[] generateProtocol1(byte groupId, byte subId, byte data1) {
		byte[] protocol = new byte[3];
		protocol[0] = groupId;
		protocol[1] = subId;
		protocol[2] = data1;
		return protocol;
	}
	public static byte[] generateProtocol2(byte groupId, byte subId, byte data1, byte data2) {
		byte[] protocol = new byte[4];
		protocol[0] = groupId;
		protocol[1] = subId;
		protocol[2] = data1;
		protocol[3] = data2;
		return protocol;
	}

}
