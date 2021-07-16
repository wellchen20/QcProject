/* 
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.filemanager.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.robert.maps.applib.utils.Ut;

/**
 * @version 2009-07-03
 * 
 * @author Peli
 *
 */
public class FileUtils {
	/** TAG for log messages. */
	static final String TAG = "FileUtils";

	/**
	 * Whether the filename is a video file.
	 * 
	 * @param filename
	 * @return
	 *//*
	public static boolean isVideo(String filename) {
		String mimeType = getMimeType(filename);
		if (mimeType != null && mimeType.startsWith("video/")) {
			return true;
		} else {
			return false;
		}
	}*/

	/**
	 * Whether the URI is a local one.
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isLocal(String uri) {
		if (uri != null && !uri.startsWith("http://")) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the extension of a file name, like ".png" or ".jpg".
	 * 
	 * @param uri
	 * @return Extension including the dot("."); "" if there is no extension;
	 *         null if uri was null.
	 */
	public static String getExtension(String uri) {
		if (uri == null) {
			return null;
		}

		int dot = uri.lastIndexOf(".");
		if (dot >= 0) {
			return uri.substring(dot);
		} else {
			// No extension.
			return "";
		}
	}

	/**
	 * Returns true if uri is a media uri.
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isMediaUri(String uri) {
		if (uri.startsWith(Audio.Media.INTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(Audio.Media.EXTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(Video.Media.INTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(Video.Media.EXTERNAL_CONTENT_URI.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Convert File into Uri.
	 * @param file
	 * @return uri
	 */
	public static Uri getUri(Context context, File file) {
		if (file != null) {
			Uri uriGraph = null;
			if (Build.VERSION.SDK_INT >= 24) {
//				uriGraph = FileProvider.getUriForFile(context, "com.mtkj.cnpc.photo.fileprovider", file);
				uriGraph = Uri.fromFile(file);
			} else {
				uriGraph = Uri.fromFile(file);
			}
			return uriGraph;
		}
		return null;
	}
	
	/**
	 * Convert Uri into File.
	 * @param uri
	 * @return file
	 */
	public static File getFile(Uri uri) {
		if (uri != null) {
			String filepath = uri.getPath();
			if (filepath != null) {
				return new File(filepath);
			}
		}
		return null;
	}
	
	/**
	 * Returns the path only (without file name).
	 * @param file
	 * @return
	 */
	public static File getPathWithoutFilename(File file) {
		 if (file != null) {
			 if (file.isDirectory()) {
				 // no file to be split off. Return everything
				 return file;
			 } else {
				 String filename = file.getName();
				 String filepath = file.getAbsolutePath();
	  
				 // Construct path without file name.
				 String pathwithoutname = filepath.substring(0, filepath.length() - filename.length());
				 if (pathwithoutname.endsWith("/")) {
					 pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
				 }
				 return new File(pathwithoutname);
			 }
		 }
		 return null;
	}

	/**
	 * Constructs a file from a path and file name.
	 * 
	 * @param curdir
	 * @param file
	 * @return
	 */
	public static File getFile(String curdir, String file) {
		String separator = "/";
		  if (curdir.endsWith("/")) {
			  separator = "";
		  }
		   File clickedFile = new File(curdir + separator
		                       + file);
		return clickedFile;
	}
	
	public static File getFile(File curdir, String file) {
		return getFile(curdir.getAbsolutePath(), file);
	}
	
	/**
	 * 遍历文件下的所有文件
	 * 
	 * @param rootPath
	 * @return
	 */
	public static List<File> getFileList(String rootPath) {
		List<File> fileList = new ArrayList<File>();
		File root = new File(rootPath);
		File[] files = root.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (files[i].isDirectory()) {
					getFileList(files[i].getAbsolutePath());
				} else if (fileName.endsWith(".sqlitedb")) {
					fileList.add(files[i]);
				} else if (fileName.endsWith(".csv")) {
					fileList.add(files[i]);
				} else if (fileName.endsWith(".gpx")) {
					fileList.add(files[i]);
				} else if (fileName.endsWith(".kml")) {
					fileList.add(files[i]);
				} else {
					continue;
				}
			}
		}
		return fileList;
	}
	
	/**
	 * 遍历所有离线地图
	 * 
	 * @param rootPath
	 * @return
	 */
	public static List<File> getOutlineMapList(Context context) {
		List<File> fileList = new ArrayList<File>();
		File root = Ut.getRMapsMapsDir(context);
		File[] files = root.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				 if (fileName.endsWith(".sqlitedb")) {
					fileList.add(files[i]);
				} else {
					continue;
				}
			}
		}
		return fileList;
	}
	
	/**
	 * 遍历所有项目文件
	 * 
	 * @param rootPath
	 * @return
	 */
	public static List<File> getProjectList(Context context) {
		List<File> fileList = new ArrayList<File>();
		File root = Ut.getRMapsProjectPublicDir(context);
		File[] files = root.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (files[i].isDirectory()) {
//					getFileList(files[i].getAbsolutePath());
				}  else {
					fileList.add(files[i]);
				}
			}
		}
		return fileList;
	}
	
	/**
	 * 遍历所有兴趣点文件
	 * 
	 * @param rootPath
	 * @return
	 */
	public static List<File> getInsterestsList(Context context) {
		List<File> fileList = new ArrayList<File>();
		File root = Ut.getRMapsProjectPrivatePointsInputDir(context);
		File[] files = root.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (fileName.endsWith(".gpx")) {
					fileList.add(files[i]);
				} else if (fileName.endsWith(".kml")) {
					fileList.add(files[i]);
				} else {
					continue;
				}
			}
		}
		return fileList;
	}
	
	/**
	 * 遍历所有轨迹文件
	 * 
	 * @param rootPath
	 * @return
	 */
	public static List<File> getTracksList(Context context) {
		List<File> fileList = new ArrayList<File>();
		File root = Ut.getRMapsProjectPrivateTracksInputDir(context);
		File[] files = root.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (fileName.endsWith(".gpx")) {
					fileList.add(files[i]);
				} else if (fileName.endsWith(".kml")) {
					fileList.add(files[i]);
				} else {
					continue;
				}
			}
		}
		return fileList;
	}
	
	/**
	 * 遍历所有炮点文件
	 * 
	 * @param rootPath
	 * @return
	 */
	public static List<File> getTasksList(Context context) {
		List<File> fileList = new ArrayList<File>();
		File root = Ut.getRMapsProjectPrivateTasksInputDir(context);
		File[] files = root.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (fileName.endsWith(".csv")) {
					fileList.add(files[i]);
				} else if (fileName.endsWith(".CSV")) {
					fileList.add(files[i]);
				} else {
					continue;
				}
			}
		}
		return fileList;
	}

	public static String getRealFilePath( final Context context, final Uri uri ) {
		if ( null == uri ) return null;
		final String scheme = uri.getScheme();
		String data = null;
		if ( scheme == null )
			data = uri.getPath();
		else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
			data = uri.getPath();
		} else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
			Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
			if ( null != cursor ) {
				if ( cursor.moveToFirst() ) {
					int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
					if ( index > -1 ) {
						data = cursor.getString( index );
					}
				}
				cursor.close();
			}
		}
		return data;
	}
}
