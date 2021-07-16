package com.mtkj.cnpc.broadcast;

import com.mtkj.cnpc.activity.MapManagerActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OpenMapBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
//		showMapId.ShowMap(intent.getStringExtra("MapName"), intent.getStringExtra("center"), intent.getStringExtra("zoom"));
		
		Intent mainIntent = new Intent(context, MapManagerActivity.class);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(mainIntent);
	}
	
	public static IShowMapId showMapId;
	
	public static void setShowMapId(IShowMapId showMapId) {
		OpenMapBroadcast.showMapId = showMapId;
	}

	public interface IShowMapId {
		public void ShowMap(String mapId, String center, String zoom);
	}

}