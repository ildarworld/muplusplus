package net.appositedesigns.muplusplus.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.appositedesigns.muplusplus.R;
import net.appositedesigns.muplusplus.activity.FilesArrayActivity;
import net.appositedesigns.muplusplus.model.FileArrayEntry;
import net.appositedesigns.muplusplus.model.FileWaiting;
import net.appositedesigns.muplusplus.muUtil.FileArraySorter;
import net.appositedesigns.muplusplus.muUtil.MuUtil;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class Finder extends AsyncTask<File, Integer, FileWaiting>
{
	
	private static final String TAG = Finder.class.getName();
	
	private FilesArrayActivity caller;
	private ProgressDialog waitDialog;
	
	private File currentDir;
	
	public Finder(FilesArrayActivity caller) {
		
		this.caller = caller;
	}

	@Override
	protected void onPostExecute(FileWaiting result) {

		FileWaiting childFilesList = result;
		Log.v(TAG, "Children for "+currentDir.getAbsolutePath()+" received");
		
		if(waitDialog!=null && waitDialog.isShowing())
		{
			waitDialog.dismiss();
		}
		Log.v(TAG, "Children for "+currentDir.getAbsolutePath()+" passed to caller");
		caller.setCurrentDirAndChilren(currentDir,childFilesList);
	
	}
	@Override
	protected FileWaiting doInBackground(File... params) {
		
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
		
		currentDir = params[0];
		Log.v(TAG, "Received directory to list paths - "+currentDir.getAbsolutePath());
		
		String[] children = currentDir.list();
		FileWaiting listing = new FileWaiting(new ArrayList<FileArrayEntry>());
		List<FileArrayEntry> childFiles = listing.getChildren();
		
		boolean showHidden = caller.getPreferenceHelper().isShowHidden();
		boolean showSystem = caller.getPreferenceHelper().isShowSystemFiles();
		Map<String, Long> dirSizes = MuUtil.getDirSizes(currentDir);
		

		for(String fileName : children)
		{
			if(".nomedia".equals(fileName))
			{
				listing.setExcludeFromMedia(true);
			}
			File f = new File(currentDir.getAbsolutePath()+File.separator+fileName);
			
			if(!f.exists())
			{
				continue;
			}
			if(MuUtil.isProtected(f) && !showSystem)
			{
				continue;
			}
			if(f.isHidden() && !showHidden)
			{
				continue;
			}
			
			String fname = f.getName();
			
			FileArrayEntry child = new FileArrayEntry();
			child.setName(fname);
			child.setPath(f);
			if(f.isDirectory())
			{
				try
				{
					Long dirSize = dirSizes.get(f.getCanonicalPath());
					child.setSize(dirSize);
				}
				catch (Exception e) {

					Log.w(TAG, "Could not find size for "+child.getPath().getAbsolutePath());
					child.setSize(0);
				}
			}
			else
			{
				child.setSize(f.length());
			}
			child.setLastModified(new Date(f.lastModified()));
			childFiles.add(child);
		}
		
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
		return listing;
	}
}
