package com.robert.maps.applib.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.robert.maps.applib.R;

public class DialogUtils {

	/**
	 * 弹出对话框，只有OK按钮，点击按钮对话框消失，按钮点击事件自行处理
	 */
	public static void alertInfo(Context context, String title, String message,
			final OnClickListener click) {
		final Dialog lDialog = new Dialog(context,
				R.style.MyDialogStyleFullText);
		lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		lDialog.setContentView(R.layout.dialog_r_okcancel);
		TextView d_Title = (TextView) lDialog.findViewById(R.id.dialog_title);
		d_Title.setText(title);
		TextView d_Message = (TextView) lDialog
				.findViewById(R.id.dialog_message);
		d_Message.setText(message);
		Button ok = (Button) lDialog.findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lDialog.dismiss();
				if (null != click) {
					click.onClick(null);
				}
			}
		});
		Button cancel = (Button) lDialog.findViewById(R.id.cancel);
		cancel.setVisibility(View.GONE);
		lDialog.show();
	}

	/**
	 * 弹出对话框，只有OK按钮，点击按钮对话框消失，按钮点击事件自行处理
	 */
	public static void alertInfo(Context context, String title, String message) {
		final Dialog lDialog = new Dialog(context,
				R.style.MyDialogStyleFullText);
		lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		lDialog.setContentView(R.layout.dialog_r_okcancel);
		TextView d_Title = (TextView) lDialog.findViewById(R.id.dialog_title);
		d_Title.setText(title);
		TextView d_Message = (TextView) lDialog
				.findViewById(R.id.dialog_message);
		d_Message.setText(message);
		Button ok = (Button) lDialog.findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lDialog.dismiss();
			}
		});
		Button cancel = (Button) lDialog.findViewById(R.id.cancel);
		cancel.setVisibility(View.GONE);
		lDialog.show();
	}

	public static void alertInfo(Context context, String title, int message) {
		final Dialog lDialog = new Dialog(context,
				R.style.MyDialogStyleFullText);
		lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		lDialog.setContentView(R.layout.dialog_r_okcancel);
		TextView d_Title = (TextView) lDialog.findViewById(R.id.dialog_title);
		d_Title.setText(title);
		TextView d_Message = (TextView) lDialog
				.findViewById(R.id.dialog_message);
		d_Message.setText(message);
		Button ok = (Button) lDialog.findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				lDialog.dismiss();
			}
		});
		Button cancel = (Button) lDialog.findViewById(R.id.cancel);
		cancel.setVisibility(View.GONE);
		lDialog.show();
	}

	/**
	 * 弹出对话框，一到两个按钮，按钮点击事件自行处理
	 */
	public static Dialog Alert(Context context, String title, String message,
			String[] btns, OnClickListener[] listeners) {
		final Dialog lDialog = new Dialog(context,
				R.style.MyDialogStyleFullText);
		lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		lDialog.setContentView(R.layout.dialog_r_okcancel);
		TextView d_Title = (TextView) lDialog.findViewById(R.id.dialog_title);
		d_Title.setText(title);
		TextView d_Message = (TextView) lDialog
				.findViewById(R.id.dialog_message);
		d_Message.setText(message);
		Button ok = (Button) lDialog.findViewById(R.id.ok);
		Button cancel = (Button) lDialog.findViewById(R.id.cancel);
		if (btns != null) {
			if (btns.length > 0)
				ok.setText(btns[0]);
			if (btns.length > 1)
				cancel.setText(btns[1]);
		}
		if (listeners != null) {
			if (listeners.length > 0)
				ok.setOnClickListener(listeners[0]);
			if (listeners.length > 1)
				cancel.setOnClickListener(listeners[1]);
		}
//		lDialog.show();
		return lDialog;
	}
}
