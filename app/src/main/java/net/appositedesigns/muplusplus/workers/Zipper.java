package net.appositedesigns.muplusplus.workers;

import java.io.File;

import net.appositedesigns.muplusplus.R;
import net.appositedesigns.muplusplus.activity.FilesArrayActivity;
import net.appositedesigns.muplusplus.exception.PathInvalidException;
import net.appositedesigns.muplusplus.muUtil.AbortionFlag;
import net.appositedesigns.muplusplus.muUtil.MuZipUtil;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

public class Zipper extends AsyncTask<File, Integer, String> {

	private static final String TAG = Zipper.class.getName();
	private AbortionFlag flag;
	private FilesArrayActivity caller;
	private String zipName;
	
	private ProgressDialog zipProgress;
	private boolean zippedAtleastOne;
	private File destination;
	
	public Zipper(String zipName, File destination, FilesArrayActivity mContext) {

		this.destination = destination;
		this.flag = new AbortionFlag();
		this.caller = mContext;
		this.zipName = zipName;
	}
	
	@Override
	protected void onPostExecute(String result) {

		final String zipFile = result;
		
		
		caller.runOnUiThread(new Runnable() {

			@Override
			public void run() {

				if (zipProgress != null && zipProgress.isShowing()) {
					zipProgress.dismiss();
				}
			}
		});
		
		if(zipFile!=null)
		{
			caller.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
	
					caller.refresh();
					new Builder(caller)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(caller.getString(R.string.success))
					.setMessage(caller.getString(R.string.zip_success, zipFile))
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
	
							dialog.dismiss();
						}
					})
					.show();
				}
			});
		}
		
	}
	@Override
	protected String doInBackground(File... params) {
		
		try
		{
			File zipDest = destination;
			File zipFile = new File(zipDest, zipName+".zip");
			zippedAtleastOne = false;
			for(File fileToBeZipped : params)
			{
				if(flag.isAborted())
				{
					caller.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
			
							String message = zippedAtleastOne?caller.getString(R.string.zip_abort_partial):caller.getString(R.string.zip_aborted);
							new Builder(caller)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(caller.getString(R.string.abort))
							.setMessage(message)
							.setPositiveButton(android.R.string.ok, new OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
			
									dialog.dismiss();
								}
							})
							.show();
						}
					});
				}
				else
				{
					if(fileToBeZipped.isDirectory())
					{
						if(fileToBeZipped.listFiles().length==0)
						{
							caller.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
					
									new Builder(caller)
									.setIcon(android.R.drawable.ic_dialog_alert)
									.setTitle(caller.getString(R.string.zip))
									.setMessage(caller.getString(R.string.zip_dir_empty))
									.setPositiveButton(android.R.string.ok, new OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
					
											dialog.dismiss();
										}
									})
									.show();
								}
							});
							
							continue;
						}
						else
						{
							MuZipUtil.zipFolder(fileToBeZipped.getAbsolutePath(), zipFile.getAbsolutePath(),flag);
							zippedAtleastOne = true;
						}
						
					}
					else
					{
						MuZipUtil.zipFile(fileToBeZipped.getAbsolutePath(), zipFile.getAbsolutePath(),flag);
						zippedAtleastOne = true;
					}
				}
				
				if(zippedAtleastOne)
				{
					return zipFile.getAbsolutePath();
				}
			}
			
			
		}
		catch (PathInvalidException e) {
			Log.e(TAG, "Zip destination was invalid", e);
			caller.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
	
					new Builder(caller)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(caller.getString(R.string.error))
					.setMessage(caller.getString(R.string.zip_dest_invalid))
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
	
							dialog.dismiss();
						}
					})
					.show();
				}
			});
		}
		catch(Exception e)
		{
			Log.e(TAG, "An error occured while running in background", e);
			caller.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
	
					new Builder(caller)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(caller.getString(R.string.zip))
					.setMessage(caller.getString(R.string.zip_dir_empty))
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
	
							dialog.dismiss();
						}
					})
					.show();
				}
			});
		}

		return null;
	}

	@Override
	protected void onPreExecute() {
		caller.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {

				zipProgress = new ProgressDialog(caller);
				zipProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				zipProgress.setMessage(caller.getString(R.string.zip_in_progress));
				zipProgress.setButton(caller.getString(R.string.run_in_background), new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						
					}
				});
				zipProgress.setButton2(caller.getString(R.string.cancel), new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						zipProgress.getButton(which).setText(R.string.cancelling);
						Zipper.this.flag.abort();
					}
				});
				zipProgress.show();
				
			}
		});
	}


}
