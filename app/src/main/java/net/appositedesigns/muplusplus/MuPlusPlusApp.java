package net.appositedesigns.muplusplus;

import android.app.Application;
import android.content.Intent;

public class MuPlusPlusApp extends Application {

	public static final int THEME_BLACK = R.style.Theme_Muplusplus;
	public static final int THEME_WHITE = R.style.Theme_Muplusplus_Light;
	public static final int THEME_WHITE_BLACK = android.R.style.Theme_Holo_Light_DarkActionBar;
	
	
	public static final String ACTION_OPEN_BOOKMARK = "net.appositedesigns.MuPlusPlusApp.action.OPEN_BOOKMARKS";
	public static final String ACTION_OPEN_FOLDER = "net.appositedesigns.MuPlusPlusApp.action.OPEN_FOLDER";
	public static final String EXTRA_IS_PICKER = "net.appositedesigns.MuPlusPlusApp.extra.IS_PICKER";
	public static final int REQ_PICK_FILE = 10;
	public static final int REQ_PICK_BOOKMARK = 11;
	public static final String EXTRA_SELECTED_BOOKMARK = "net.appositedesigns.MuPlusPlusApp.extra.SELECTED_BOOKMARK";
	public static final String EXTRA_FOLDER = "net.appositedesigns.MuPlusPlusApp.extra.FOLDER";
	
	private Intent fileAttachIntent;

	public Intent getFileAttachIntent() {
		return fileAttachIntent;
	}

	public void setFileAttachIntent(Intent fileAttachIntent) {
		this.fileAttachIntent = fileAttachIntent;
	}
	
	

}
