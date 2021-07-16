package com.mtkj.cnpc.protocol.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;

import com.mtkj.cnpc.R;

public class DialogUtils {
	
	/**
	 * 显示圆型加载 ProgressBar
	 * 
	 * @param activity
	 * @return
	 */
	public static Dialog alertProgress(Activity activity) {
		final Dialog lDialog = new Dialog(activity,
				android.R.style.Theme_Translucent_NoTitleBar);
		lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		lDialog.setContentView(R.layout.progress);
		return lDialog;
	}


}
