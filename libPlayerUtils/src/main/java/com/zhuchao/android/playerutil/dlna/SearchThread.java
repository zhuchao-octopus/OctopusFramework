package com.zhuchao.android.playerutil.dlna;

import com.zhuchao.android.libfileutils.MMLog;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;

public class SearchThread extends Thread {
	private boolean flag = true;
	private ControlPoint mControlPoint;
	private boolean mStartComplete;
	private int mSearchTimes;
	private static final int mFastInternalTime = 15000;
	private static final int mNormalInternalTime = 3600000;
	private static final String TAG = "SearchThread";

	public SearchThread(ControlPoint mControlPoint) {
		super();
		this.mControlPoint = mControlPoint;
		this.mControlPoint.addDeviceChangeListener(mDeviceChangeListener);
	}

	@Override
	public void run() {
		while (flag) {
			if (mControlPoint == null) {
				break;
			}
			searchDevices();
		}
	}

	/**
	 * Search for the DLNA devices.
	 */
	private void searchDevices() {
		try {
			if (mStartComplete) {
				mControlPoint.search();
				MMLog.d(TAG, "control point search...");
			}
			else
			{
				mControlPoint.stop();
				boolean startRet = mControlPoint.start();
				MMLog.d(TAG, "control point start:" + startRet);
				if (startRet) {
					mStartComplete = true;
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			MMLog.log(TAG,e.toString());
		}
		// Search the devices five times fast, and after that we can make it
		// search lowly to save the power.
		synchronized (this) {
			try {
				mSearchTimes++;
				if (mSearchTimes >= 5) {
					wait(mNormalInternalTime);
				} else {
					wait(mFastInternalTime);
				}
			} catch (Exception e) {
				MMLog.log(TAG,e.toString());
			}
		}
	}

	/**
	 * Set the search times, set this to 0 to make it search fast.
	 * 
	 * @param searchTimes
	 *            The times we have searched.
	 */
	public synchronized void setSearchTimes(int searchTimes) {
		this.mSearchTimes = searchTimes;
	}

	/**
	 * Notify all the thread.
	 */
	public void awake() {
		synchronized (this) {
			notifyAll();
		}
	}

	/**
	 * Stop the thread, if quit this application we should use this method to
	 * stop the thread.
	 */
	public void stopThread() {
		flag = false;
		awake();
	}

	private DeviceChangeListener mDeviceChangeListener = new DeviceChangeListener() {

		@Override
		public void deviceRemoved(Device dev) {
			MMLog.d(TAG, "control point remove a device");
			DLNAContainer.getInstance().removeDevice(dev);
		}

		@Override
		public void deviceAdded(Device dev) {
			MMLog.d(TAG, "control point add a device..." + dev.getDeviceType() + dev.getFriendlyName());
			DLNAContainer.getInstance().addDevice(dev);
		}
	};
}