package net.appositedesigns.muplusplus;

import android.app.Application;
import android.content.Intent;

public class MuPlusPlusApp extends Application {

	public static final int THEME_WHITE = R.style.Theme_Muplusplus_Light;
	
	public static final String OPENBOOKMARK_ACTION = "net.appositedesigns.MuPlusPlusApp.action.OPEN_BOOKMARKS";
	public static final String INSTANCE_IS_PICKER = "net.appositedesigns.MuPlusPlusApp.extra.IS_PICKER";
	public static final int BOOKMARK_REQPICK = 11;
	public static final String BOOKMARK_ADDITIONAL_SELECTED = "net.appositedesigns.MuPlusPlusApp.extra.SELECTED_BOOKMARK";
	public static final String ADDITIONAL_FOLDER = "net.appositedesigns.MuPlusPlusApp.extra.FOLDER";
	
	private Intent fileAttachIntent;

	public Intent getFileAttachIntent() {
		return fileAttachIntent;
	}

	public void setFileAttachIntent(Intent fileAttachIntent) {
		this.fileAttachIntent = fileAttachIntent;
	}
	
	

}
