package com.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

public class ResourceUtil {

	public static int getLayoutId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "layout",
				paramContext.getPackageName());
	}

	public static int getStringId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "string",
				paramContext.getPackageName());
	}

	public static int getDrawableId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString,
				"drawable", paramContext.getPackageName());
	}

	public static int getStyleId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "style",
				paramContext.getPackageName());
	}

	public static int getId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "id",
				paramContext.getPackageName());
	}

	public static int getColorId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "color",
				paramContext.getPackageName());
	}

	public static int getArrayId(Context paramContext, String paramString) {
		return paramContext.getResources().getIdentifier(paramString, "array",
				paramContext.getPackageName());
	}
	
	public static void setActivityAnim(Activity c, int anim) {
		View v = ((ViewGroup) c.findViewById(android.R.id.content))
				.getChildAt(0);// getWindow().getDecorView();

		Animation animation = (AnimationSet) AnimationUtils.loadAnimation(c,
				anim);

		v.clearAnimation();
		v.setAnimation(animation);

		animation.start();
	}

	// for change ui
	// public static final String[] CAR_TYPE = new String[] {
	// MachineConfig.VALUE_CAR_TYPE_BENZ,
	// MachineConfig.VALUE_CAR_TYPE_AUDIQ5,
	// MachineConfig.VALUE_CAR_TYPE_AUDIA4L, MachineConfig.VALUE_CAR_TYPE_BMW };
	//
	// public static final int[] CAR_RES_SWDP = new int[] { 0, 481, 481, 0 };
	// public static final int[] CAR_RES_WDP = new int[] { 0, 801, 802, 0 };

	// it must call before setContentView
	public static String updateUi(Context context) { // only launcher use now

		String value = MachineConfig
				.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);

		// if (value != null) {
		int sw = 0;
		int w = 0;
		int h = 0;
		int type = 0; // deault 800X480

		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		if (dm.widthPixels == 1024 && dm.heightPixels == 600) {
			type = 1;
			sw = 321;
		} else if(dm.widthPixels == 1280 && dm.heightPixels == 480) {
			type = 2;
		}

		if (MachineConfig.VALUE_SYSTEM_UI_KLD1.equals(value)) {
			if (type == 0) {
				sw = 322;
				// w = 801;
				// h = 479;
			} else {
				sw = 323;
				// w = 1025;
				// h = 601;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD2.equals(value)) {
			if (type == 0) {
				sw = 324;
			} else {
				sw = 325;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD3.equals(value)) {
			if (type == 0) {
				sw = 326;
			} else {
				sw = 327;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD5.equals(value)) {
			if (type == 0) {
				sw = 328;
			} else {
				sw = 329;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD7_1992.equals(value)) {
			if (type == 0) {
				sw = 330;
			} else {
				sw = 331;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD3_8702.equals(value)) {
			if (type == 0) {
				sw = 332;
			} else {
				sw = 333;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD10_887.equals(value)) {
			if (type == 0) {
				sw = 334;
			} else if (type == 2) {
				sw = 335;
			} else {
				sw = 336;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD12_80.equals(value)) {
			if (type == 0) {
				sw = 340;
			} else {
				sw = 341;
			}
		}

//		Log.d("ResourceUtil", "sw:" + sw);

		Configuration c = context.getResources().getConfiguration();
		if (sw != 0) {
			c.smallestScreenWidthDp = sw;
		}
		if (w != 0) {
			c.screenWidthDp = w;
		}
		if (h != 0) {
			c.screenHeightDp = h;
		}
		context.getResources().updateConfiguration(c, null);

		// }

		return value;
	}

	public static void updateAppUi(Context context) { // app used except
														// launcher

		String value = MachineConfig
				.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);

		// if (value != null) {
		int sw = 0;
		int w = 0;
		int h = 0;
		int type = 0; // deault 800X480

		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		if (dm.widthPixels == 1024 && dm.heightPixels == 600) {
			type = 1;
			sw = 321;
		} else if(dm.widthPixels == 1280 && dm.heightPixels == 480) {//1280X480?
			type = 2;
		}

//		if (MachineConfig.VALUE_SYSTEM_UI_KLD1.equals(value)) {
//			if (type == 0) {
//				sw = 322;
//				// w = 801;
//				// h = 479;
//			} else {
//				sw = 323;
//				// w = 1025;
//				// h = 601;
//			}
//		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD2.equals(value)) {
//			if (type == 0) {
//				sw = 324;
//			} else {
//				sw = 325;
//			}
//		} else 
			
		if (MachineConfig.VALUE_SYSTEM_UI_KLD3.equals(value)) {
			if (type == 0) {
				sw = 326;
			} else {
				sw = 327;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD3_8702.equals(value)) {
			if (type == 0) {
				sw = 332;
			} else {
				sw = 333;
			}
		} else if (MachineConfig.VALUE_SYSTEM_UI_KLD10_887.equals(value)) {
			if (type == 0) {
				sw = 334;
			} else if (type == 2) {
				sw = 335;
			} else {
				sw = 336;
			}

		}

//		Log.d("ResourceUtil", "sw:" + sw);

		Configuration c = context.getResources().getConfiguration();
		if (sw != 0) {
			c.smallestScreenWidthDp = sw;
		}
		if (w != 0) {
			c.screenWidthDp = w;
		}
		if (h != 0) {
			c.screenHeightDp = h;
		}
		context.getResources().updateConfiguration(c, null);

		// }

	}
	
	public static String updateSingleUi(Context context) { // only launcher use now

		String value = MachineConfig
				.getPropertyReadOnly(MachineConfig.KEY_SYSTEM_UI);

		int sw = 0;
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		if (dm.widthPixels == 1024 && dm.heightPixels == 600) {	
			sw = 321;
		}

		Configuration c = context.getResources().getConfiguration();
		if (sw != 0) {
			c.smallestScreenWidthDp = sw;
		}
		
		context.getResources().updateConfiguration(c, null);
		return value;
	}
}
