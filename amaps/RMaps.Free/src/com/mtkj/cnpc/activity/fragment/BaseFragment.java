package com.mtkj.cnpc.activity.fragment;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import com.mtkj.cnpc.protocol.constants.SysContants;
import com.robert.maps.applib.utils.LogFileUtil;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;


/***
 * 
 * 
 * @author TNT
 * 
 */
public class BaseFragment extends Fragment {
	
	private SharedPreferences mPreferences;
	
	public Handler handler = null;
	
	// 重启处理
	private UncaughtExceptionHandler m_handler = new UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			// 异常信息存储
			LogFileUtil.saveFileToSDCard(FormatStackTrace(ex));
			
			System.exit(0);
		}
	};
	
	public String FormatStackTrace(Throwable throwable) {
		if (throwable == null)
			return "";
		String rtn = throwable.getStackTrace().toString();
		try {
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			throwable.printStackTrace(printWriter);
			printWriter.flush();
			writer.flush();
			rtn = writer.toString();
			rtn = rtn.replaceAll("at ", "\r\n" + "at ");
			rtn = rtn.replaceAll("Caused by", "\r\n" + "Caused by");
			printWriter.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception ex) {
		}
		return rtn;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getActivity().getSharedPreferences(SysContants.SYSCONFIG, getActivity().MODE_PRIVATE);
		
//		Thread.setDefaultUncaughtExceptionHandler(m_handler);
	}
	
	public void hideFragment(Fragment f) {
		if (f != null && f.isAdded() && !f.isHidden()) {
			FragmentTransaction transaction = getActivity().getSupportFragmentManager()
					.beginTransaction().setCustomAnimations(
							android.R.anim.fade_in, android.R.anim.fade_out);
			transaction.hide(f);
			transaction.commitAllowingStateLoss();
		}
	}
	
	public void showMessage(String msg) {
		Toast.makeText(getActivity(), msg,Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * @return the handler
	 */
	public Handler getHandler() {
		return handler;
	}

	/**
	 * @param handler
	 *            the handler to set
	 */
	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	
	// ---------------------------------------------------------------------------
	public String getData(String key, String defValue) {
		return mPreferences.getString(key, defValue);
	}

	public int getData(String key, int defValue) {
		return mPreferences.getInt(key, defValue);
	}

	public long getData(String key, long defValue) {
		return mPreferences.getLong(key, defValue);
	}

	public float getData(String key, float defValue) {
		return mPreferences.getFloat(key, defValue);
	}

	public boolean getData(String key, boolean defValue) {
		return mPreferences.getBoolean(key, defValue);
	}

	public void setData(String key, Object o) {
		if (o != null) {
			SharedPreferences.Editor editor = mPreferences.edit();
			if (o instanceof Boolean) {
				editor.putBoolean(key, (Boolean) o);
			} else if (o instanceof Integer) {
				editor.putInt(key, (Integer) o);
			} else if (o instanceof Long) {
				editor.putLong(key, (Long) o);
			} else if (o instanceof Float) {
				editor.putFloat(key, (Float) o);
			} else if (o instanceof String) {
				editor.putString(key, (String) o);
			}
			editor.commit();
		}
	}
}
