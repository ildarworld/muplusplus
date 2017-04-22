package net.appositedesigns.muplusplus.activity;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.appositedesigns.muplusplus.MuPlusPlusApp;
import net.appositedesigns.muplusplus.R;
import net.appositedesigns.muplusplus.muAdapters.FilesArrayAdapter;
import net.appositedesigns.muplusplus.callbacks.CancelOperationCallback;
import net.appositedesigns.muplusplus.callbacks.FileOperationsCallback;
import net.appositedesigns.muplusplus.model.FileArrayEntry;
import net.appositedesigns.muplusplus.model.FileWaiting;
import net.appositedesigns.muplusplus.muUtil.FileOperationsHelper;
import net.appositedesigns.muplusplus.muUtil.MuUtil;
import net.appositedesigns.muplusplus.workers.FileMover;
import net.appositedesigns.muplusplus.workers.Finder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesArrayActivity extends MuFilesArrayActivity {

	private static final String TAG = FilesArrayActivity.class.getName();
	
	private static final String CURRENT_DIR = "current-dir";
	
	private ListView muListView;
	private File currentDir;
	private List<FileArrayEntry> files;
	private FilesArrayAdapter adapter;
	protected Object mCurrentActionMode;
	private ArrayAdapter<CharSequence> mSpinnerAdapter;
	private CharSequence[] gotoLocations;
	private boolean isPicker = false;
	private MuPlusPlusApp app;
	private File previousOpenDirChild;
	private boolean focusOnParent;
	private boolean excludeFromMedia  = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		app = (MuPlusPlusApp)getApplication();
		isPicker = getIntent().getBooleanExtra(MuPlusPlusApp.INSTANCE_IS_PICKER, false);
		if(Intent.ACTION_GET_CONTENT.equals(getIntent().getAction()))
		{
			isPicker  = true;
			app.setFileAttachIntent(getIntent());
		}
		
		initUi();
		initGotoLocations();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		prepareActionBar();
		initRootDir(savedInstanceState);

		files = new ArrayList<FileArrayEntry>();

		initFileListView();
		focusOnParent = getPreferenceHelper().focusOnParent();
		if (getPreferenceHelper().isEulaAccepted()) {
			listContents(currentDir);
		} else {
			MuPopupBuilder.create(this).show();
		}

	}

	private void initUi() {
		if(isPicker)
		{
			getWindow().setUiOptions(0);
		}
		
	}

	private void initGotoLocations() {
		
		gotoLocations = getResources().getStringArray(R.array.goto_locations);
	}

	private void initFileListView() {
		muListView = (ListView) getListView();
		adapter = new FilesArrayAdapter(this, files);
		muListView.setAdapter(adapter);
		muListView.setTextFilterEnabled(true);
		muListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (muListView.isClickable()) {
					FileArrayEntry file = (FileArrayEntry) muListView
							.getAdapter().getItem(position);
					select(file.getPath());
				}
			}

		});

		muListView.setOnItemLongClickListener(getLongPressListener());
		registerForContextMenu(muListView);		
	}

	private OnItemLongClickListener getLongPressListener() {
		return new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0,
					final View view, int arg2, long arg3) {

				if(!muListView.isLongClickable())
					return true;
				if(isPicker)
				{
					return false;
				}
				 view.setSelected(true);

				final FileArrayEntry fileArrayEntry = (FileArrayEntry) adapter
						.getItem(arg2);
				
				
				if (mCurrentActionMode != null) {
					return false;
				}
				if (MuUtil.isProtected(fileArrayEntry
						.getPath())) {
					return false;
				}
				muListView.setEnabled(false);

				mCurrentActionMode = FilesArrayActivity.this
						.startActionMode(new FileOperationsCallback(
								FilesArrayActivity.this, fileArrayEntry) {

							@Override
							public void onDestroyActionMode(
									ActionMode mode) {
								view.setSelected(false);
								mCurrentActionMode = null;
								muListView.setEnabled(true);
							}

						});
				view.setSelected(true);
				return true;
			}

		};
	}

	private void initRootDir(Bundle savedInstanceState) {
		// If app was restarted programmatically, find where the user last left
		// it
		String restartDirPath = getIntent().getStringExtra(MuPlusPlusApp.ADDITIONAL_FOLDER);
		
		if (restartDirPath != null) 
		{
			File restartDir = new File(restartDirPath);
			if (restartDir.exists() && restartDir.isDirectory()) {
				currentDir = restartDir;
				getIntent().removeExtra(MuPlusPlusApp.ADDITIONAL_FOLDER);
			}
		}
		else if (savedInstanceState!=null && savedInstanceState.getSerializable(CURRENT_DIR) != null) {
			
			currentDir = new File(savedInstanceState
					.getSerializable(CURRENT_DIR).toString());
		} 
		else 
		{
			currentDir = getPreferenceHelper().getStartDir();
		}		
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(CURRENT_DIR, currentDir.getAbsolutePath());

	}

	private void prepareActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		mSpinnerAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, gotoLocations);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, getActionbarListener(actionBar));
		
	}

	private OnNavigationListener getActionbarListener(final ActionBar actionBar) {
		return new OnNavigationListener() {
			
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				
				int selectedIndex = actionBar.getSelectedNavigationIndex();
				
				if(selectedIndex == 0)
				{
					return false;
				}
				switch (selectedIndex) {
					
				case 1:
					listContents(getPreferenceHelper().getStartDir());
					break;
					
					
				case 2:
					listContents(new File("/sdcard"));
					break;
					
				case 3:
					listContents(MuUtil.getDownloadsFolder());
					break;
					
				case 4:
					listContents(MuUtil.getDcimFolder());
					break;
					
				case 5:
					openBookmarks(actionBar);
					break;
				case 6:
					MuUtil.gotoPath(currentDir.getAbsolutePath(), FilesArrayActivity.this, new CancelOperationCallback() {
						
						@Override
						public void onCancel() {
							 actionBar.setSelectedNavigationItem(0);
							
						}
					});
					break;

				default:
					break;
				}
				
				
				return true;
			}

		};
	}
	private void openBookmarks(final ActionBar actionBar) {
		Intent intent = new Intent();
		intent.setAction(MuPlusPlusApp.OPENBOOKMARK_ACTION);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra(MuPlusPlusApp.INSTANCE_IS_PICKER, isPicker);
		actionBar.setSelectedNavigationItem(0);
		startActivityForResult(intent, MuPlusPlusApp.BOOKMARK_REQPICK);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
		case MuPlusPlusApp.BOOKMARK_REQPICK:
			if(resultCode == RESULT_OK)
			{
				String selectedBookmark = data.getStringExtra(MuPlusPlusApp.BOOKMARK_ADDITIONAL_SELECTED);
				listContents(new File(selectedBookmark));
			}
			break;

		default:
			break;
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (shouldRestartApp) {
			shouldRestartApp = false;
			restartApp();
		}
	}

	@Override
	public void onBackPressed() {

		if(isPicker)
		{
			super.onBackPressed();
			return;
		}
		if (getPreferenceHelper().useBackNavigation()) {
			if (MuUtil.isRoot(currentDir)) {
				finish();
			} else {
				gotoParent();
			}
		} else {
			super.onBackPressed();
		}

	}

	void select(File file) {
		if (MuUtil.isProtected(file)){
			new Builder(this)
					.setTitle(getString(R.string.access_denied))
					.setMessage(
							getString(R.string.cant_open_dir, file.getName()))
					.show();
		} else if (file.isDirectory()) {
			
			listContents(file);
			
		} else {
			doFileAction(file);
		}
	}

	private void doFileAction(File file) {
		if (MuUtil.isProtected(file) || file.isDirectory()) {
			return;
		}
		
		if(isPicker)
		{
			pickFile(file);
			return;
		}
		else
		{
			openFile(file);
			return;
		}
	}

	private void openFile(File file) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(file);
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
		intent.setDataAndType(uri, type == null ? "*/*" : type);
		startActivity((Intent.createChooser(intent,
				getString(R.string.open_using))));
	}

	private void pickFile(File file) {
		Intent fileAttachIntent = app.getFileAttachIntent();
		fileAttachIntent.setData(Uri.fromFile(file));
		setResult(Activity.RESULT_OK, fileAttachIntent);
		finish();
		return;
	}

	public void listContents(File dir)
	{
		listContents(dir, null);
	}
	public void listContents(File dir, File previousOpenDirChild) {
		if (!dir.isDirectory() || MuUtil.isProtected(dir)) {
			return;
		}
		if(previousOpenDirChild!=null)
		{
			this.previousOpenDirChild = new File(previousOpenDirChild.getAbsolutePath());
		}
		else
		{
			this.previousOpenDirChild = null;
		}
		new Finder(this).execute(dir);
	}

	private void gotoParent() {

		if (MuUtil.isRoot(currentDir)) {
			// Do nothing finish();
		} else {
			listContents(currentDir.getParentFile(), currentDir);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		
		if(isPicker)
		{
			inflater.inflate(R.menu.picker_options_menu, menu);
		}
		else 
		{
			inflater.inflate(R.menu.options_menu, menu);
		}
		return true;
		
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if(!isPicker)
		{
			if(getPreferenceHelper().isMediaExclusionEnabled())
			{
				menu.findItem(R.id.menu_media_exclusion).setVisible(true);
				menu.findItem(R.id.menu_media_exclusion).setChecked(excludeFromMedia);
			}
			else
			{
				menu.findItem(R.id.menu_media_exclusion).setVisible(false);
			}
			menu.findItem(R.id.menu_bookmark_toggle).setChecked(bookmarker.isBookmarked(currentDir.getAbsolutePath()));
			if (MuUtil.canPaste(currentDir)) {
				menu.findItem(R.id.menu_paste).setVisible(true);
			} else {
				menu.findItem(R.id.menu_paste).setVisible(false);
			}	
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		switch (item.getItemId()) {

		case android.R.id.home:
			gotoParent();
			return true;

		case R.id.menu_cancel:
			setResult(RESULT_CANCELED);
			finish();
			return true;
			
		case R.id.menu_bookmark_toggle:
			boolean setBookmark = item.isChecked();
			item.setChecked(!setBookmark);
			if(!setBookmark)
			{
				bookmarker.addBookmark(currentDir.getAbsolutePath());
			}
			else
			{
				bookmarker.removeBookmark(currentDir.getAbsolutePath());
			}
			return true;
			
		case R.id.menu_media_exclusion:
			item.setChecked(!excludeFromMedia);
			setMediaExclusionForFolder();
			return true;
			
		case R.id.menu_goto:
			MuUtil.gotoPath(currentDir.getAbsolutePath(), this);
			return true;

		case R.id.menu_paste:
			confirmPaste();
			return true;

		case R.id.menu_refresh:
			refresh();
			return true;
			
		case R.id.menu_newfolder:
			confirmCreateFolder();
			return true;

		case R.id.menu_settings:
			Intent prefsIntent = new Intent(FilesArrayActivity.this,
					PreferencesActivity.class);
			startActivity(prefsIntent);
			return true;
		default:
			super.onOptionsItemSelected(item);
			break;
		}

		return true;
	}

	private void setMediaExclusionForFolder() {

		if(excludeFromMedia)
		{
			//Now include folder in media
			FileUtils.deleteQuietly(new File(currentDir, ".nomedia"));
			excludeFromMedia = false;
		}
		else
		{
			try
			{
				FileUtils.touch(new File(currentDir, ".nomedia"));
				excludeFromMedia = true;
			}
			catch(Exception e)
			{
				Log.e(TAG, "Error occurred while creating .nomedia file", e);
			}
		}
		FileOperationsHelper.rescanMedia(this);
		refresh();
	}

	private void confirmPaste() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getString(R.string.confirm));
		alert.setMessage(getString(R.string.confirm_paste_text,
				MuUtil.getFileToPaste().getName()));

		alert.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						dialog.dismiss();
						new FileMover(FilesArrayActivity.this, MuUtil
								.getPasteMode()).execute(currentDir);
					}
				});

		alert.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						dialog.dismiss();
					}
				});

		alert.show();

	}

	private void confirmCreateFolder() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getString(R.string.create_folder));
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setHint(getString(R.string.enter_folder_name));
		input.setSingleLine();
		alert.setView(input);

		alert.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						CharSequence newDir = input.getText();
						if (MuUtil.mkDir(
								currentDir.getAbsolutePath(), newDir)) {
							listContents(currentDir);
						}
					}
				});

		alert.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						dialog.dismiss();
					}
				});

		alert.show();

	}

	public synchronized void setCurrentDirAndChilren(File dir, FileWaiting folderListing) {
		currentDir = dir;

		List<FileArrayEntry> children = folderListing.getChildren();
		excludeFromMedia   = folderListing.isExcludeFromMedia();
		TextView emptyText = (TextView) findViewById(android.R.id.empty);
		if (emptyText != null) {
			emptyText.setText(R.string.empty_folder);
		}
		files.clear();
		files.addAll(children);
		adapter.notifyDataSetChanged();
		getActionBar().setSelectedNavigationItem(0);
		
		if(MuUtil.isRoot(currentDir))
		{
			gotoLocations[0] = getString(R.string.filesystem);	
		}
		else
		{
			gotoLocations[0] = currentDir.getName();
		}
		
		if(previousOpenDirChild!=null && focusOnParent)
		{
			int position = files.indexOf(new FileArrayEntry(previousOpenDirChild.getAbsolutePath()));
			if(position>=0)
			muListView.setSelection(position);
		}
		else
		{
			muListView.setSelection(0);
		}
		mSpinnerAdapter.notifyDataSetChanged();
		
		ActionBar ab = getActionBar();
		ab.setSelectedNavigationItem(0);
		
		ab.setSubtitle(getString(R.string.item_count_subtitle, children.size()));				
		if(MuUtil.isRoot(currentDir) || currentDir.getParentFile()==null)
    	{
			ab.setDisplayHomeAsUpEnabled(false);
			ab.setTitle(getString(R.string.filesystem));
    	}
    	else
    	{
    		ab.setTitle(currentDir.getName());
    		ab.setDisplayHomeAsUpEnabled(true);
    	}
	}

	public void refresh() {
		listContents(currentDir);
	}

	private void restartApp() {
		Intent i = getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra(MuPlusPlusApp.ADDITIONAL_FOLDER, currentDir.getAbsolutePath());
		startActivity(i);
	}
	
	public boolean isInPickMode()
	{
		return isPicker;
	}

	public File getCurrentDir() {
		return currentDir;
	}

}