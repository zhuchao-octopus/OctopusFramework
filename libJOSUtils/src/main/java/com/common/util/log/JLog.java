package com.common.util.log;

import android.util.Log;

/**
 * Log输出类
 * 
 * @author sky
 *
 */
public class JLog {
	
	final static public int TYPE_VERBOSE = 0;
	final static public int TYPE_INFO = 1;
	final static public int TYPE_ERROR = 2;
	final static public int TYPE_DEBUG = 3;
	boolean isPrint;
	int mDefaultType = TYPE_DEBUG;
	private String iTag = "JLog";

	public JLog() {
	}

	public JLog(String aTag, boolean isOn, int aDefaultType) {
		setPrint(isOn);
		setTag(aTag);

	}

	/**
	 * 设置控制台输出开关
	 * 
	 * @param isOn
	 *            - boolean
	 * */
	public void setPrint(boolean isOn) {
		isPrint = isOn;
	}

	/**
	 * 设置输出时的tag标签
	 * 
	 * @param aTag
	 *            标签名
	 * */
	public void setTag(String aTag) {
		iTag = aTag;
	}

	/**
	 * 设置默认输出类型
	 * 
	 * @param aType
	 */
	public void setDefaultType(int aType) {
		mDefaultType = aType;
	}

	/**
	 * 输出到控制台
	 * 
	 * @param str
	 *            - String
	 * */
	public void print(String str) {
		print(str, mDefaultType);
	}

	public void print(String str, int aType) {
		if (isPrint) {
			switch (aType) {
			case TYPE_ERROR:
				Log.e(iTag, str);
				break;
			case TYPE_INFO:
				Log.i(iTag, str);
				break;
			case TYPE_VERBOSE:
				Log.v(iTag, str);
				break;
			case TYPE_DEBUG:
				Log.d(iTag, str);
				break;
			}
		}
	}

	public void print(int value) {
		print(String.valueOf(value));
	}

	public void print(Object o) {
		print(o.toString());
	}

	public void print(boolean b) {
		print(b ? "true" : "false");
	}

	public void print() {
		print("");
	}

	public void print(long l) {
		print(String.valueOf(l));
	}

	public void print(byte[] b) {
		print(b, 0, b.length);
	}
	
	public void print(String str, byte[] b)
	{
		print(str + byteArray2String(b, 0, b.length));
	}

	public void print(byte[] b, int offset, int len) {
		
		print(byteArray2String(b, offset, len));
	}
	
	private String byteArray2String(byte[] b, int offset, int len)
	{
		StringBuffer strBuf = new StringBuffer();
		for (int i = offset; i < len; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			strBuf.append(hex.toUpperCase());
		}
		return strBuf.toString();
	}
}
