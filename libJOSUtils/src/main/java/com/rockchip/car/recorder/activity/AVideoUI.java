package com.rockchip.car.recorder.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rockchip.car.recorder.camera2.CameraHolder;
import com.rockchip.car.recorder.model.CameraInfo;
import com.rockchip.car.recorder.service.CameraService;
import com.rockchip.car.recorder.service.Config;
import com.rockchip.car.recorder.service.IService;
import com.rockchip.car.recorder.service.ParametersSet;
import com.rockchip.car.recorder.service.ServiceImpl;
import com.rockchip.car.recorder.utils.SLog;

import java.util.List;

/**
 * Created by Administrator on 2016/8/9.
 */
public abstract class AVideoUI implements View.OnClickListener {

	private static final String TAG = "CAM_AVideoUI";
	protected View mRootView;

	protected CameraService mCameraService;
	protected View[] mSurfaces;
	protected boolean mSwitchDefault;
	protected boolean mNeedStartRecord;

	// ======== Views ========
	private ImageButton mRecorderButton;

	private TextView mRecordingTimeView;

	protected View mAdasSettings;
	protected ImageView mADASResultImageView;
	protected Bitmap mAdasResultBitMap;

	private View mListViewLayout;
	private ListView mListView;
	private ImageView mListItemView;
	private View mCurrentActionView;

	protected boolean mFromReverse;

	private long mStartRecordTime;
	private long mMaxRecordTime;
	private static final int MSG_UPDATE_RECORDING_TIME = 0x00000001;
	public static final int DRAW_ADASRESULT = 2;

	public AVideoUI() {
		// this.mVideomController = controller;
		// this.mCameraSettings = new CameraSettings(activity, this);
		this.mSurfaces = new View[2];

		initViews();
	}

	private void initViews() {
	}

	public void initializeSurfaceView() {
		// if (!CameraSettings.isPreviewSwitched()) {
		// mSurfaces[0] = mRootView.findViewById(R.id.preview_content0);
		// mSurfaces[0].setOnClickListener(this);
		// mSurfaces[1] = mRootView.findViewById(R.id.preview_content1);
		// mSurfaces[1].setOnClickListener(this);
		// mSwitchDefault = false;
		// } else {
		// mSurfaces[0] = mRootView.findViewById(R.id.preview_content1);
		// mSurfaces[0].setOnClickListener(this);
		// mSurfaces[1] = mRootView.findViewById(R.id.preview_content0);
		// mSurfaces[1].setOnClickListener(this);
		// mSwitchDefault = true;
		// }
	}

	public void setService(CameraService service) {
		this.mCameraService = service;
	}

	public <T> boolean startPreviewDirect(int id, T t) {
		return mCameraService.startPreviewDirect(id, t);
	}

	public boolean startPreviewDirect(int id, int channel) {
		ParametersSet.mChannel = channel;
		return mCameraService.startPreviewDirect(id, null);
	}
	
	public <T> void startRecordingDirect(int id, T t) {
		mCameraService.startRecordingDirect(id, t);
	}

	private void startOrStopRecord() {
		if (mCurrentActionView != null) {
			mCurrentActionView.setVisibility(View.GONE);
		}
		if (mCameraService == null) {
			SLog.d(TAG, "AVideoUI.startOrStopRecord(). mCameraService is null");
			return;
		}
		mRecorderButton.setEnabled(false);
		if (mCameraService.getCameraInfos().size() == 0) {
			SLog.e(TAG, "CameraInfo is null");
		}

		if (mCameraService != null && mCameraService.isRecording()) {
			if (SystemClock.uptimeMillis() - mStartRecordTime <= 1000) {
				SLog.d(TAG,
						"it's less than one minute from record start, don't allowed to stop now");
				mRecorderButton.setEnabled(true);
				return;
			}

			// if (CameraSettings.isSingleRecord()) {
			// mCameraService.stopRecord(CameraSettings.getSingleRecordCameraId());
			// } else {
			mCameraService.stopRecord();
			// }
		} else {
			// if (CameraSettings.isSingleRecord()) {
			// mCameraService.startRecord(CameraSettings.getSingleRecordCameraId());
			// } else {
			mCameraService.startRecord();
			// }
		}
		// mRecorderButton.setEnabled(true);
	}

	private void takePicture() {
		if (mCurrentActionView != null) {
			mCurrentActionView.setVisibility(View.GONE);
		}
		if (mCameraService == null) {
			SLog.d(TAG, "AVideoUI.takePicture(). mCameraService is null");
			return;
		}
		for (CameraInfo info : mCameraService.getCameraInfos().values()) {
			if (info != null && info.getCameraState() != null
					&& info.getCameraState().equals(IService.CAMERA_PREVIEWING)) {
				mCameraService.takePicture(info.getCameraId());

			} else {
				SLog.d(TAG,
						"AVideo::takePictiure. "
								+ (info == null ? "CameraInfo is null."
										: (info.getCameraState() == null ? "CameraState is null."
												: "Camera is not Previewing now.")));
			}
		}
	}

	public abstract void setPreviewFormat(int id, int format);

	public abstract void setRenderSize(int id, int width, int height);

	public abstract void requestRender(int id, byte[] data);

	public abstract void reverse(int state);

	public void updateRecordIcon(boolean recording, boolean lock) {

	}

	private static String millisecondToTimeString(long milliSeconds,
			boolean displayCentiSeconds) {
		long seconds = milliSeconds / 1000; // round down to compute seconds
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long remainderMinutes = minutes - (hours * 60);
		long remainderSeconds = seconds - (minutes * 60);

		StringBuilder timeStringBuilder = new StringBuilder();

		// Hours
		if (hours > 0) {
			if (hours < 10) {
				timeStringBuilder.append('0');
			}
			timeStringBuilder.append(hours);

			timeStringBuilder.append(':');
		}

		// Minutes
		if (remainderMinutes < 10) {
			timeStringBuilder.append('0');
		}
		timeStringBuilder.append(remainderMinutes);
		timeStringBuilder.append(':');

		// Seconds
		if (remainderSeconds < 10) {
			timeStringBuilder.append('0');
		}
		timeStringBuilder.append(remainderSeconds);

		// Centi seconds
		if (displayCentiSeconds) {
			timeStringBuilder.append('.');
			long remainderCentiSeconds = (milliSeconds - seconds * 1000) / 10;
			if (remainderCentiSeconds < 10) {
				timeStringBuilder.append('0');
			}
			timeStringBuilder.append(remainderCentiSeconds);
		}

		return timeStringBuilder.toString();
	}

	private void showCameraSettingUI() {
		SLog.i(TAG, "showCameraSettingsUI");
		if (mCurrentActionView != null)
			mCurrentActionView.setVisibility(View.GONE);

	}

	private void showRecordSettingsUI() {

	}

	public ImageView getListItemView() {
		return this.mListItemView;
	}

	public ListView getListView() {
		return mListView;
	}

	public void updateCurrentView(Class cla) {
		if (cla.equals(mListView.getClass())) {
			mCurrentActionView.setVisibility(View.GONE);
			mCurrentActionView = mListViewLayout;
			mCurrentActionView.setVisibility(View.VISIBLE);
		}
	}

	public void setCurrentActionViewVisibility(boolean visibility) {
		if (mCurrentActionView != null) {
			mCurrentActionView.setVisibility(visibility ? View.VISIBLE
					: View.GONE);
		}
	}

	public abstract void scaleSurface(int id);

	public void showPicInPic(boolean show) {

	}

	public void setNeedStartRecord(boolean needStartRecord) {
		this.mNeedStartRecord = needStartRecord;
	}

	public boolean isNeedStartRecord() {
		return mNeedStartRecord;
	}

	public abstract void surfaceVisible(int id, int visible);

	public abstract void previewSwitch();

	// mCameraService.switchPreview();
	// }
	public abstract void open();

	public abstract void close();

	public abstract void checkEvent();

	public void setFromReverse(boolean reverse) {
		this.mFromReverse = reverse;
	}

	public void setRender(int id, boolean render) {
	};

	@Override
	public void onClick(View v) {

	}

	public interface ICallbackRender {
		void addCallBackBuffer(int id, byte[] data);
	}

	protected class Surfaces {
		int id;
		int visible;
		int width;
		int height;
		float z;

		public Surfaces(int id, int visible, int width, int height, float z) {
			this.id = id;
			this.visible = visible;
			this.width = width;
			this.height = height;
			this.z = z;
		}

		public int getId() {
			return id;
		}

		public int getVisible() {
			return visible;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public float getZ() {
			return z;
		}

		public void setId(int id) {
			this.id = id;
		}

		public void setVisible(int visible) {
			this.visible = visible;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public void setZ(float z) {
			this.z = z;
		}
	}

	public abstract boolean switchAadas();

	public void drawAdasResult(Bitmap bitmap) {

	}

	public void enableCameraButton(boolean enable) {

	}
}
