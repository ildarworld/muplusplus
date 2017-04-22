package ru.innopolis.muplusplus.muUtil;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.innopolis.muplusplus.MuPlusPlusApp;
import ru.innopolis.muplusplus.exception.PathInvalidException;

import java.io.File;

public final class SettingsHelper {

	public enum SortField {
		NAME, MTIME, SIZE
	}

	private Activity mContext;
	public static final String ENDUSERLICENCEAGGREMENT_ACCEPTED = "eula_accepted_v2.5";
	public static final String ENDUSERLICENCEAGGREMENT_MARKER = "eula_marker_file_v2.5";

	public static final String PREFERENCES_DIR = "homeDir";
	public static final String PREFERENCES_SDCARD_OPTIONS = "sdCardOptions";
	public static final String PREFERENCES_SHOW_DIRS_FIRST = "showDirsFirst";
	public static final String PREFERENCES_SHOW_HIDDEN = "showHidden";
	public static final String PREFERENCES_SHOW_SYSFILES = "showSysFiles";
	public static final String PREFERENCES_SORT_DIR = "sort.dir";
	public static final String PREFERENCES_SORT_FIELD = "sort.field";
	public static final String PREFERENCES_THEME = "theme";
	public static final String PREFERENCES_USE_BACK_BUTTON = "useBackButton";
	private static final String PREFERENCES_NAVIGATE_FOCUS_ON_PARENT = "focusOnParent";
	public static final String PREFERENCES_USE_QUICKACTIONS = "useQuickActions";
	public static final String PREFERENCES_ZIP_ENABLE = "zipEnable";
	public static final String PREFERENCES_USE_ZIP_FOLDER = "useZipFolder";
	public static final String PREFERENCES_ZIP_LOCATION = "zipLocation";
	public static final String PREFERENCES_MEDIA_EXCLUSIONS = "media_exclusions";

	public static final String SORT_DIR_ASC = "asc";
	public static final String SORT_FIELD_M_TIME = "mtime";
	public static final String SORT_FIELD_NAME = "name";
	public static final String SORT_FIELD_SIZE = "size";


	public SettingsHelper(Activity context) {
		mContext = context;

	}

	public boolean isShowDirsOnTop() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREFERENCES_SHOW_DIRS_FIRST, true);
	}

	public boolean isShowHidden() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREFERENCES_SHOW_HIDDEN, false);
	}

	public boolean useBackNavigation() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREFERENCES_USE_BACK_BUTTON, false);
	}

	public boolean useQuickActions() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREFERENCES_USE_QUICKACTIONS, true);
	}

	public SortField getSortField() {
		String field = PreferenceManager.getDefaultSharedPreferences(mContext)
				.getString(PREFERENCES_SORT_FIELD, SORT_FIELD_NAME);
		if (SORT_FIELD_NAME.equalsIgnoreCase(field)) {
			return SortField.NAME;
		} else if (SORT_FIELD_M_TIME.equalsIgnoreCase(field)) {
			return SortField.MTIME;
		} else if (SORT_FIELD_SIZE.equalsIgnoreCase(field)) {
			return SortField.SIZE;
		} else {
			return SortField.NAME;
		}
	}

	public int getSortDir() {
		String field = PreferenceManager.getDefaultSharedPreferences(mContext)
				.getString(PREFERENCES_SORT_DIR, "asc");
		if (SORT_DIR_ASC.equalsIgnoreCase(field)) {
			return 1;
		} else {
			return -1;
		}
	}

	public File getStartDir() {
		String dirPath = PreferenceManager
				.getDefaultSharedPreferences(mContext).getString(PREFERENCES_DIR,
						"/");
		File homeDir = new File(dirPath);

		if (homeDir.exists() && homeDir.isDirectory()) {
			return homeDir;
		} else {
			return new File("/");
		}
	}


	public boolean isShowSystemFiles() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREFERENCES_SHOW_SYSFILES, true);
	}

	public File getZipDestinationDir() throws PathInvalidException {
		String dirPath = PreferenceManager
				.getDefaultSharedPreferences(mContext).getString(
						PREFERENCES_ZIP_LOCATION, "/sdcard/zipped");
		Boolean useZipFolder = PreferenceManager.getDefaultSharedPreferences(
				mContext).getBoolean(PREFERENCES_USE_ZIP_FOLDER, false);

		if (!useZipFolder) {
			return null;
		}
		File dir = new File(dirPath);

		if (dir.exists() && dir.isDirectory()) {
			return dir;
		} else if (!dir.exists()) {
			dir.mkdirs();
			return dir;
		} else {
			throw new PathInvalidException(dirPath);
		}
	}

	public boolean isEnableSdCardOptions() {

		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREFERENCES_SDCARD_OPTIONS, true);
	}

	public boolean focusOnParent() {
		
		return PreferenceManager.getDefaultSharedPreferences(mContext)
		.getBoolean(PREFERENCES_NAVIGATE_FOCUS_ON_PARENT, true);
	}
	public boolean isZipEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREFERENCES_ZIP_ENABLE, false);
	}
	
	public boolean isMediaExclusionEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getBoolean(PREFERENCES_MEDIA_EXCLUSIONS, false);
	}

	public boolean isEulaAccepted() {
		return mContext.getSharedPreferences(ENDUSERLICENCEAGGREMENT_MARKER, Context.MODE_PRIVATE)
				.getBoolean(ENDUSERLICENCEAGGREMENT_ACCEPTED, false);
	}

	public void markEulaAccepted() {
		SharedPreferences.Editor editor = mContext.getSharedPreferences(
				ENDUSERLICENCEAGGREMENT_MARKER, Context.MODE_PRIVATE).edit();
		editor.putBoolean(ENDUSERLICENCEAGGREMENT_ACCEPTED, true);
		editor.commit();
	}

	public int getTheme() {

        return MuPlusPlusApp.THEME_WHITE;
        /*
		String theme = PreferenceManager.getDefaultSharedPreferences(mContext)
				.getString(PREFERENCES_THEME, VALUE_THEME_WHITE);
		if (VALUE_THEME_BLACK.equalsIgnoreCase(theme)) {
			return MuPlusPlusApp.THEME_BLACK;
		} else if (VALUE_THEME_WHITE_BLACK.equalsIgnoreCase(theme)) {
			return MuPlusPlusApp.THEME_WHITE_BLACK;
		} else {
			return MuPlusPlusApp.THEME_WHITE;
		}
		*/
	}
}
