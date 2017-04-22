package net.appositedesigns.muplusplus.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.appositedesigns.muplusplus.MuPlusPlusApp;
import net.appositedesigns.muplusplus.R;
import net.appositedesigns.muplusplus.muAdapters.MuBookmarksAdapter;
import net.appositedesigns.muplusplus.model.FileArrayEntry;
import net.appositedesigns.muplusplus.muUtil.SettingsHelper;
import net.appositedesigns.muplusplus.workers.BookmarkLoader;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MuBookmarksActivity extends MuFilesArrayActivity {

	protected static final String TAG = MuBookmarksActivity.class.getName();
	private MuBookmarksAdapter adapter;
	private ArrayList<FileArrayEntry> bookmarks;
	private ListView bookmarkListView;
	private boolean isPicker = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setTheme(new SettingsHelper(this).getTheme());
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.main);
		isPicker = getIntent().getBooleanExtra(MuPlusPlusApp.INSTANCE_IS_PICKER, false);
		bookmarks = new ArrayList<FileArrayEntry>();
		initBookmarksList();
		refresh();
		bookmarkListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				return false;
			}
			
			
		});
		bookmarkListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				
				if(!isPicker)
				{
					FileArrayEntry bookmark = (FileArrayEntry)bookmarkListView.getAdapter().getItem(position);
					removeBookmark(bookmark);
					return true;
				}
				return false;
			}
			
		});
		registerForContextMenu(bookmarkListView);	

	}
	
	protected void removeBookmark(final FileArrayEntry bookmark) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setMessage(getString(R.string.confirm_remove_bookmark, bookmark.getName()))
	       .setCancelable(false)
	       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {

	        	  bookmarker.removeBookmark(bookmark.getPath().getAbsolutePath());
	           }
	       })
	       .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	                
	           }
	       }).setTitle(R.string.confirm);
		
		AlertDialog confirm = builder.create();
		confirm.show();		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(!isPicker)
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.bookmarks_options_menu, menu);
			return true;
		}
		return false;
	}

	private void initBookmarksList() {
		bookmarkListView = (ListView) getListView();
		adapter = new MuBookmarksAdapter(this, bookmarks);
		bookmarkListView.setAdapter(adapter);
		bookmarkListView.setTextFilterEnabled(true);
		bookmarkListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (bookmarkListView.isClickable()) {
					FileArrayEntry file = (FileArrayEntry) bookmarkListView
							.getAdapter().getItem(position);
					select(file.getPath());
				}
			}

		});

	}
	
	
	protected void select(File path) {
		Intent intent = new Intent();
		intent.putExtra(MuPlusPlusApp.BOOKMARK_ADDITIONAL_SELECTED, path.getAbsolutePath());
		intent.putExtra(MuPlusPlusApp.INSTANCE_IS_PICKER, isPicker);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getItemId()== android.R.id.home)
		{
			onBackPressed();
			return true;
		}
		else if(R.id.menu_settings == item.getItemId())
		{
			Intent prefsIntent = new Intent(this,
					PreferencesActivity.class);
			startActivity(prefsIntent);
			return true;
		}
		else if(R.id.menu_add_bookmark == item.getItemId())
		{
			promptBookmarkPath();
			return true;
		}
		return false;
	}

	private void promptBookmarkPath() {
		final EditText input = new EditText(this);
		
		input.setSingleLine();
		input.setHint(getString(R.string.add_bookmark_prompt_hint));
		input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		new Builder(this)
		.setTitle(getString(R.string.add_bookmark_prompt))
		.setView(input)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  
			CharSequence bookmarkPath = input.getText();
			try
			{
				File toDir = new File(bookmarkPath.toString());
				if(toDir.isDirectory() && toDir.exists())
				{
					bookmarker.addBookmark(bookmarkPath.toString());
				}
				else
				{
					throw new FileNotFoundException();
				}
				
			}
			catch (Exception e) {
				Log.e(TAG, "Error bookmarking path"+bookmarkPath, e);
				new Builder(MuBookmarksActivity.this)
				.setTitle(getString(R.string.error))
				.setMessage(getString(R.string.path_not_exist))
				.show();
			}
		  }
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		   
			  dialog.dismiss();
		  }
		})
		.show();
	}

	public void setBookmarks(List<FileArrayEntry> childFiles) {
		TextView emptyText = (TextView) findViewById(android.R.id.empty);
		if (emptyText != null) {
			emptyText.setText(R.string.no_bookmarks);
		}
		bookmarks.clear();
		bookmarks.addAll(childFiles);
		adapter.notifyDataSetChanged();		
	}

	public void refresh() {
		new BookmarkLoader(this).execute();		
	}
}
