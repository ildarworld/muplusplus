package ru.innopolis.muplusplus.workers;

import java.io.File;

import ru.innopolis.muplusplus.R;
import ru.innopolis.muplusplus.activity.FilesArrayActivity;
import ru.innopolis.muplusplus.callbacks.ActionCallback;
import ru.innopolis.muplusplus.muUtil.MuUtil;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class Trasher extends AsyncTask<File, Integer, Boolean>
{
	private static final String TAG = Trasher.class.getName();
	
	private File fileToBeDeleted;
	private FilesArrayActivity caller;
	private ProgressDialog waitDialog;

	private ActionCallback<Void> callback;
	
	public Trasher(FilesArrayActivity caller, ActionCallback<Void> callback) {

		this.caller = caller;
		if(callback!=null)
		{
			this.callback = callback;
		}
		else
		{
			this.callback = new ActionCallback<Void>() {

				@Override
				public Void onSuccess() {return null;}

				@Override
				public void onFailure(Throwable e) {Log.e(TAG, "Error occurred", e);}
			};
		}
	}
	@Override
	protected void onPostExecute(Boolean result) {
		
		Log.v(TAG, "In post execute. Result of deletion was - "+result);
		if(result)
		{
			Log.i(TAG, fileToBeDeleted.getAbsolutePath()+" deleted successfully");
			caller.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					waitDialog.dismiss();
					Toast.makeText(caller.getApplicationContext(), "Deleted", Toast.LENGTH_LONG);
					if(callback!=null)
					{
						callback.onSuccess();
					}
					caller.refresh();
					
				}
			});
		}
		else
		{
			MuUtil.setPasteSrcFile(fileToBeDeleted, MuUtil.getPasteMode());
			caller.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
					if(callback!=null)
					{
						callback.onFailure(new Exception());
					}
					waitDialog.dismiss();
					new Builder(caller)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(caller.getString(R.string.error))
					.setMessage(caller.getString(R.string.delete_failed, fileToBeDeleted.getName()))
					.show();
					
					
				}
			});
		}
	}
	@Override
	protected Boolean doInBackground(File... params) {
		
		fileToBeDeleted = params[0];
		
		caller.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				waitDialog = new ProgressDialog(caller);
				waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				waitDialog.setMessage(caller.getString(R.string.deleting_path,fileToBeDeleted.getName()));
				waitDialog.setCancelable(false);
				
				waitDialog.show();
			}
		});
		
		try
		{
			Log.v(TAG, "Checking if file on clipboard is same as that being deleted");
			if(MuUtil.getFileToPaste() != null && MuUtil.getFileToPaste().getCanonicalPath().equals(fileToBeDeleted.getCanonicalPath()))
			{
				Log.v(TAG, "File on clipboard is being deleted");
				MuUtil.setPasteSrcFile(null, MuUtil.getPasteMode());
			}
			return MuUtil.delete(fileToBeDeleted);
		}
		catch (Exception e) {
			Log.e(TAG, "Error occured while deleting file "+fileToBeDeleted.getAbsolutePath(),e);
			return false;
		}
		
	}

}
