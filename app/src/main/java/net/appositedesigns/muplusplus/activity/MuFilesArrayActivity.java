package net.appositedesigns.muplusplus.activity;

import net.appositedesigns.muplusplus.util.BookmarksHelper;
import net.appositedesigns.muplusplus.util.SettingsHelper;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

public abstract class MuFilesArrayActivity extends ListActivity {

	protected SettingsHelper prefs;
	protected BookmarksHelper bookmarker;
	private OnSharedPreferenceChangeListener listener;
	protected boolean shouldRestartApp = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		prefs = new SettingsHelper(this);
		bookmarker = new BookmarksHelper(this);
		setTheme(prefs.getTheme());
		super.onCreate(savedInstanceState);

		listenToThemeChange();
	}
	
	private void listenToThemeChange() {

		listener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (SettingsHelper.PREF_THEME.equals(key)) {

					shouldRestartApp = true;

				}
				if (SettingsHelper.PREF_USE_QUICKACTIONS.equals(key)) {

					shouldRestartApp = true;

				}
			}
		};

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(listener);
	}
	public synchronized SettingsHelper getPreferenceHelper()
	{
		return prefs;
	}
	
	public BookmarksHelper getBookmarker()
	{
		return bookmarker;
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(listener);
	}
}
