/*
 * Copyright (C) 2008 OpenIntents.org
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

/*
 * Based on AndDev.org's file browser V 2.0.
 */

package org.openintents.filemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openintents.filemanager.intents.FileManagerIntents;
import org.openintents.filemanager.util.FileUtils;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.robert.maps.applib.R;

/**
 * 文件选择
 * 
 * @author DRH
 *
 */
public class FileManagerActivity extends ListActivity {
	private int mState;

	private static final int STATE_BROWSE = 1;
	private static final int STATE_PICK_FILE = 2;
	private static final int STATE_PICK_DIRECTORY = 3;

	private static final String BUNDLE_CURRENT_DIRECTORY = "current_directory";
	private static final String BUNDLE_CONTEXT_FILE = "context_file";
	private static final String BUNDLE_CONTEXT_TEXT = "context_text";
	private static final String BUNDLE_SHOW_DIRECTORY_INPUT = "show_directory_input";
	private static final String BUNDLE_STEPS_BACK = "steps_back";

	/** Contains directories and files together */
     private ArrayList<IconifiedText> directoryEntries = new ArrayList<IconifiedText>();

     /** Dir separate for sorting */
     List<IconifiedText> mListDir = new ArrayList<IconifiedText>();

     /** Files separate for sorting */
     List<IconifiedText> mListFile = new ArrayList<IconifiedText>();

     /** SD card separate for sorting */
     List<IconifiedText> mListSdCard = new ArrayList<IconifiedText>();

     private File currentDirectory = new File("");

     //private MimeTypes mMimeTypes;

     private String mContextText;
     private File mContextFile = new File("");

     /** How many steps one can make back using the back key. */
     private int mStepsBack;

     private EditText mEditFilename;
     private Button mButtonPick;
//     private LinearLayout mDirectoryButtons;

     private LinearLayout mDirectoryInput;
     private EditText mEditDirectory;
     private ImageButton mButtonDirectoryPick;

     private TextView mEmptyText;
     private TextView mPathText;
//     private ProgressBar mProgressBar;

     private DirectoryScanner mDirectoryScanner;
     private File mPreviousDirectory;
//     private ThumbnailLoader mThumbnailLoader;

     private Handler currentHandler;

 	 static final public int MESSAGE_SHOW_DIRECTORY_CONTENTS = 500;	// List of contents is ready, obj = DirectoryContents

     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle icicle) {
          super.onCreate(icicle);

          currentHandler = new Handler() {
			public void handleMessage(Message msg) {
				FileManagerActivity.this.handleMessage(msg);
			}
		};

		  requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
          setContentView(R.layout.filelist);

          mEmptyText = (TextView) findViewById(R.id.empty_text);
          mPathText = (TextView) findViewById(R.id.path);

		  getListView().setEmptyView(findViewById(R.id.empty));
	      getListView().setTextFilterEnabled(true);
	      getListView().requestFocus();
	      getListView().requestFocusFromTouch();

//          mDirectoryButtons = (LinearLayout) findViewById(R.id.directory_buttons);
//          mEditFilename = (EditText) findViewById(R.id.filename);
	      
          mButtonPick = (Button) findViewById(R.id.button_pick);
          mButtonPick.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {
					pickFileOrDirectory();
				}
          });

          // Initialize only when necessary:
          mDirectoryInput = null;

          // Create map of extensions:
          mState = STATE_BROWSE;

          Intent intent = getIntent();
          String action = intent.getAction();

          File browseto = new File("/");

          if (action != null) {
        	  if (action.equals(FileManagerIntents.ACTION_PICK_FILE)) {
        		  mState = STATE_PICK_FILE;
        		  mButtonPick.setVisibility(View.GONE);
        	  } else if (action.equals(FileManagerIntents.ACTION_PICK_DIRECTORY)) {
        		  mState = STATE_PICK_DIRECTORY;

        		  // Remove edit text and make button fill whole line
//        		  mEditFilename.setVisibility(View.GONE);
//        		  mButtonPick.setLayoutParams(new LinearLayout.LayoutParams(
//        				  LinearLayout.LayoutParams.FILL_PARENT,
//        				  LinearLayout.LayoutParams.WRAP_CONTENT));
        	  }
          }

          if (mState == STATE_BROWSE) {
        	  // Remove edit text and button.
        	  //mEditFilename.setVisibility(View.GONE);
        	  mButtonPick.setVisibility(View.GONE);
          }

          // Set current directory and file based on intent data.
    	  File file = FileUtils.getFile(intent.getData());
    	  if (file != null) {
    		  File dir = FileUtils.getPathWithoutFilename(file);
    		  if (dir.isDirectory()) {
    			  browseto = dir;
    		  }
//    		  if (!file.isDirectory()) {
//    			  mEditFilename.setText(file.getName());
//    		  }
    	  }
//
//    	  String title = intent.getStringExtra(FileManagerIntents.EXTRA_TITLE);
//    	  if (title != null) {
//    		  setTitle(title);
//    	  }
//
//    	  String buttontext = intent.getStringExtra(FileManagerIntents.EXTRA_BUTTON_TEXT);
//    	  if (buttontext != null) {
//    		  mButtonPick.setText(buttontext);
//    	  }

          mStepsBack = 0;

          if (icicle != null) {
        	  browseto = new File(icicle.getString(BUNDLE_CURRENT_DIRECTORY));
        	  mContextFile = new File(icicle.getString(BUNDLE_CONTEXT_FILE));
        	  mContextText = icicle.getString(BUNDLE_CONTEXT_TEXT);

        	  boolean show = icicle.getBoolean(BUNDLE_SHOW_DIRECTORY_INPUT);
        	  showDirectoryInput(show);

        	  mStepsBack = icicle.getInt(BUNDLE_STEPS_BACK);
          }

          browseTo(browseto);
     }

     public void onDestroy() {
    	 super.onDestroy();

    	 // Stop the scanner.
    	 DirectoryScanner scanner = mDirectoryScanner;

    	 if (scanner != null) {
    		 scanner.cancel = true;
    	 }

    	 mDirectoryScanner = null;

//    	 ThumbnailLoader loader = mThumbnailLoader;
//
//    	 if (loader != null) {
//    		 loader.cancel = true;
////    		 mThumbnailLoader = null;
//    	 }
     }

     private void handleMessage(Message message) {
//    	 Log.v(TAG, "Received message " + message.what);

    	 switch (message.what) {
    	 case MESSAGE_SHOW_DIRECTORY_CONTENTS:
    		 showDirectoryContents((DirectoryContents) message.obj);
    		 break;
    	 }
     }

//     private void notifyIconChanged(IconifiedText text) {
//    	 if (getListAdapter() != null) {
//    		 ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
//    	 }
//     }
//
//     private void setProgress(int progress, int maxProgress) {
//    	 mProgressBar.setMax(maxProgress);
//    	 mProgressBar.setProgress(progress);
//    	 mProgressBar.setVisibility(View.VISIBLE);
//     }
//
     private void showDirectoryContents(DirectoryContents contents) {
    	 mDirectoryScanner = null;

    	 mListSdCard = contents.listSdCard;
    	 mListDir = contents.listDir;
    	 mListFile = contents.listFile;

    	 directoryEntries.ensureCapacity(mListSdCard.size() + mListDir.size() + mListFile.size());

         addAllElements(directoryEntries, mListSdCard);
         addAllElements(directoryEntries, mListDir);
         addAllElements(directoryEntries, mListFile);

         IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this);
         itla.setListItems(directoryEntries, getListView().hasTextFilter());
         setListAdapter(itla);
	     getListView().setTextFilterEnabled(true);

         selectInList(mPreviousDirectory);
         refreshDirectoryPanel();
         setProgressBarIndeterminateVisibility(false);

//    	 mProgressBar.setVisibility(View.GONE);
    	 mEmptyText.setVisibility(View.VISIBLE);

//    	 mThumbnailLoader = new ThumbnailLoader(currentDirectory, mListFile, currentHandler);
//    	 mThumbnailLoader.start();
     }

     private void onCreateDirectoryInput() {
//    	 mDirectoryInput = (LinearLayout) findViewById(R.id.directory_input);
//         mEditDirectory = (EditText) findViewById(R.id.directory_text);
//
//         mButtonDirectoryPick = (ImageButton) findViewById(R.id.button_directory_pick);
//
//         mButtonDirectoryPick.setOnClickListener(new View.OnClickListener() {
//
//				public void onClick(View arg0) {
//					goToDirectoryInEditText();
//				}
//         });
     }

     //private boolean mHaveShownErrorMessage;
     private File mHaveShownErrorMessageForFile = null;

     private void goToDirectoryInEditText() {
    	 File browseto = new File(mEditDirectory.getText().toString());

    	 if (browseto.equals(currentDirectory)) {
    		 showDirectoryInput(false);
    	 } else {
    		 if (mHaveShownErrorMessageForFile != null
    				 && mHaveShownErrorMessageForFile.equals(browseto)) {
    			 // Don't let user get stuck in wrong directory.
    			 mHaveShownErrorMessageForFile = null;
        		 showDirectoryInput(false);
    		 } else {
	    		 if (!browseto.exists()) {
	    			 // browseTo() below will show an error message,
	    			 // because file does not exist.
	    			 // It is ok to show this the first time.
	    			 mHaveShownErrorMessageForFile = browseto;
	    		 }
				 browseTo(browseto);
    		 }
    	 }
     }

     /**
      * Show the directory line as input box instead of button row.
      * If Directory input does not exist yet, it is created.
      * Since the default is show == false, nothing is created if
      * it is not necessary (like after icicle).
      * @param show
      */
     private void showDirectoryInput(boolean show) {
    	 if (show) {
    		 if (mDirectoryInput == null) {
        		 onCreateDirectoryInput();
        	 }
    	 }
    	 if (mDirectoryInput != null) {
	    	 mDirectoryInput.setVisibility(show ? View.VISIBLE : View.GONE);
//	    	 mDirectoryButtons.setVisibility(show ? View.GONE : View.VISIBLE);
    	 }

    	 refreshDirectoryPanel();
     }

 	/**
 	 *
 	 */
 	private void refreshDirectoryPanel() {
		String path = currentDirectory.getAbsolutePath();
 		if (isDirectoryInputVisible()) {
 			// Set directory path
 			mEditDirectory.setText(path);

 			// Set selection to last position so user can continue to type:
 			mEditDirectory.setSelection(path.length());
 		} else {
 			//setDirectoryButtons();
 			mPathText.setText(path);
 		}
 	}
     /*
     @Override
	protected void onResume() {
		super.onResume();
	}
*/


 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);

 		// remember file name
 		outState.putString(BUNDLE_CURRENT_DIRECTORY, currentDirectory.getAbsolutePath());
 		outState.putString(BUNDLE_CONTEXT_FILE, mContextFile.getAbsolutePath());
 		outState.putString(BUNDLE_CONTEXT_TEXT, mContextText);
 		boolean show = isDirectoryInputVisible();
 		outState.putBoolean(BUNDLE_SHOW_DIRECTORY_INPUT, show);
 		outState.putInt(BUNDLE_STEPS_BACK, mStepsBack);
 	}

	/**
	 * @return
	 */
	private boolean isDirectoryInputVisible() {
		return ((mDirectoryInput != null) && (mDirectoryInput.getVisibility() == View.VISIBLE));
	}

	private void pickFileOrDirectory() {
		File file = null;
		if (mState == STATE_PICK_FILE) {
			String filename = mEditFilename.getText().toString();
			file = FileUtils.getFile(currentDirectory.getAbsolutePath(), filename);
		} else if (mState == STATE_PICK_DIRECTORY) {
			file = currentDirectory;
		}

    	Intent intent = getIntent();
    	if (Build.VERSION.SDK_INT > 23) {
    		Uri fileUri = FileProvider.getUriForFile(FileManagerActivity.this, FileManagerActivity.this.getPackageName(), file);
    		Toast.makeText(FileManagerActivity.this, fileUri.getPath(), Toast.LENGTH_SHORT).show();
    		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    		intent.setData(fileUri);
		} else {
			intent.setData(FileUtils.getUri(FileManagerActivity.this, file));
		}
    	setResult(RESULT_OK, intent);
    	finish();
     }

	/**
      * This function browses up one level
      * according to the field: currentDirectory
      */
     private void upOneLevel(){
    	 if (mStepsBack > 0) {
    		 mStepsBack--;
    	 }
         if(currentDirectory.getParent() != null)
               browseTo(currentDirectory.getParentFile());
     }

     /**
      * Jump to some location by clicking on a
      * directory button.
      *
      * This resets the counter for "back" actions.
      *
      * @param aDirectory
      */
     private void jumpTo(final File aDirectory) {
    	 mStepsBack = 0;
    	 browseTo(aDirectory);
     }

     /**
      * Browse to some location by clicking on a list item.
      * @param aDirectory
      */
     private void browseTo(final File aDirectory){
          // setTitle(aDirectory.getAbsolutePath());

          if (aDirectory.isDirectory()){
        	  if (aDirectory.equals(currentDirectory)) {
        		  // Switch from button to directory input
        		  showDirectoryInput(true);
        	  } else {
        		   mPreviousDirectory = currentDirectory;
	               currentDirectory = aDirectory;
	               refreshList();
//	               selectInList(previousDirectory);
	//               refreshDirectoryPanel();
        	  }
          }
     }


     private void refreshList() {

    	  // Cancel an existing scanner, if applicable.
    	  DirectoryScanner scanner = mDirectoryScanner;

    	  if (scanner != null) {
    		  scanner.cancel = true;
    	  }

    	  directoryEntries.clear();
          mListDir.clear();
          mListFile.clear();
          mListSdCard.clear();

          setProgressBarIndeterminateVisibility(true);

          // Don't show the "folder empty" text since we're scanning.
          mEmptyText.setVisibility(View.GONE);

          setListAdapter(null);

		  mDirectoryScanner = new DirectoryScanner(currentDirectory, this, currentHandler);
		  mDirectoryScanner.start();

     }

     private void selectInList(File selectFile) {
    	 String filename = selectFile.getName();
    	 IconifiedTextListAdapter la = (IconifiedTextListAdapter) getListAdapter();
    	 int count = la.getCount();
    	 for (int i = 0; i < count; i++) {
    		 IconifiedText it = (IconifiedText) la.getItem(i);
    		 if (it.getText().equals(filename)) {
    			 getListView().setSelection(i);
    			 break;
    		 }
    	 }
     }

     private void addAllElements(List<IconifiedText> addTo, List<IconifiedText> addFrom) {
    	 int size = addFrom.size();
    	 for (int i = 0; i < size; i++) {
    		 addTo.add(addFrom.get(i));
    	 }
     }

     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
          super.onListItemClick(l, v, position, id);

          IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();

          if (adapter == null) {
        	  return;
          }

          IconifiedText text = (IconifiedText) adapter.getItem(position);

          String file = text.getText();

          if (file.equals("...")) {
			upOneLevel();
		} else {
			String curdir = currentDirectory.getAbsolutePath();
			File clickedFile = FileUtils.getFile(curdir, file);
			if (clickedFile != null) {
				if (clickedFile.isDirectory()) {
					// If we click on folders, we can return later by the "back" key.
					mStepsBack++;

					browseTo(clickedFile);
				}
				else if(mState == STATE_BROWSE || mState == STATE_PICK_FILE) {
			    	Intent intent = getIntent();
			    	if (Build.VERSION.SDK_INT > 23) {
//			    		Uri fileUri = FileProvider.getUriForFile(FileManagerActivity.this, FileManagerActivity.this.getPackageName(),  clickedFile);
//			    		Toast.makeText(FileManagerActivity.this, fileUri.getPath(), Toast.LENGTH_SHORT).show();
//			    		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//			    		intent.setData(fileUri);
						intent.setData(FileUtils.getUri(FileManagerActivity.this, clickedFile));
					} else {
						intent.setData(FileUtils.getUri(FileManagerActivity.this, clickedFile));
					}
			    	setResult(RESULT_OK, intent);
			    	finish();
				}

			}
		}
     }
}