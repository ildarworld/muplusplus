package ru.innopolis.muplusplus.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.innopolis.muplusplus.R;
import ru.innopolis.muplusplus.activity.MuBookmarksActivity;
import ru.innopolis.muplusplus.model.FileArrayEntry;
import ru.innopolis.muplusplus.muUtil.FileArraySorter;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class BookmarkLoader extends AsyncTask<File, Integer, List<FileArrayEntry>>
{
	
	private static final String TAG = BookmarkLoader.class.getName();
	
	private MuBookmarksActivity caller;
	private ProgressDialog waitDialog;
	
	public BookmarkLoader(MuBookmarksActivity caller) {
		
		this.caller = caller;
	}

	@Override
	protected void onPostExecute(List<FileArrayEntry> result) {

		final List<FileArrayEntry> childFiles = result;
		caller.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				if(waitDialog!=null && waitDialog.isShowing())
				{
					waitDialog.dismiss();
				}
				Log.v(TAG, "Bookmarks for passed to caller");
				caller.setBookmarks(childFiles);
				if(childFiles.size()>0)
				{
					caller.getActionBar().setSubtitle(caller.getString(R.string.bookmarks_count, childFiles.size()));
				}
				else
				{
					caller.getActionBar().setSubtitle(caller.getString(R.string.bookmarks_count_0));
				}
			}
		});
	
	}
	@Override
	protected List<FileArrayEntry> doInBackground(File... params) {
		
		Thread waitForASec = new Thread() {
			
			@Override
			public void run() {
				
				waitDialog = new ProgressDialog(caller);
				waitDialog.setTitle("");
				waitDialog.setMessage(caller.getString(R.string.querying_filesys));
				waitDialog.setIndeterminate(true);
				
				try {
					Thread.sleep(100);
					if(this.isInterrupted())
					{
						return;
					}
					else
					{
						caller.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {

								if(waitDialog!=null)
									waitDialog.show();
							}
						});

					}
				} catch (InterruptedException e) {
					
					Log.e(TAG, "Progressbar waiting thread encountered exception ",e);
					e.printStackTrace();
				}

				
			}
		};
		caller.runOnUiThread(waitForASec);
		
		
		List<FileArrayEntry> childFiles = new ArrayList<FileArrayEntry>(caller.getBookmarker().getBookmarks());
		
		FileArraySorter sorter = new FileArraySorter(caller);
		Collections.sort(childFiles, sorter);
		
		Log.v(TAG, "Will now interrupt thread waiting to show progress bar");
		if(waitForASec.isAlive())
		{
			try
			{
				waitForASec.interrupt();
			}
			catch (Exception e) {
				
				Log.e(TAG, "Error while interrupting thread",e);
			}
		}
		return childFiles;
	}
	
	
}
