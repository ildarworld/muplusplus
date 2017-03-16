package net.appositedesigns.muplusplus.muUtil;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.appositedesigns.muplusplus.activity.MuBookmarksActivity;
import net.appositedesigns.muplusplus.model.FileArrayEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public final class BookmarksHelper {

	private Activity mContext;
	private static List<String> bookmarkedPaths = new ArrayList<String>();
	public static final String BOOKMARKS = "bookmarks";
	public static final String BOOKMARKS_FILE = "bookmarks_2";
	
	public BookmarksHelper(Activity activity)
	{
		mContext = activity;
		refreshBookmarkCache();
	}
	
	public void addBookmark(final String path) {
	
		new Thread(new Runnable() {
	
			@Override
			public void run() {
				final String bookmarkCsv = mContext.getSharedPreferences(
						BOOKMARKS_FILE, Context.MODE_PRIVATE).getString(
						BOOKMARKS, "");
	
				StringTokenizer tokens = new StringTokenizer(bookmarkCsv, "\n");
				boolean found = false;
				while (tokens.hasMoreTokens()) {
					String bookmark = tokens.nextToken();
	
					if (bookmark != null && bookmark.equalsIgnoreCase(path)) {
						found = true;
						break;
					}
				}
	
				if (!found) {
	
					SharedPreferences.Editor editor = mContext
							.getSharedPreferences(BOOKMARKS_FILE,
									Context.MODE_PRIVATE).edit();
					editor.putString(BOOKMARKS, bookmarkCsv + "\n" + path);
					editor.commit();
	
					refreshBookmarkCache();
					mContext.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							
							if(mContext instanceof MuBookmarksActivity)
							{
								MuBookmarksActivity muBookmarksActivity = (MuBookmarksActivity)mContext;
								muBookmarksActivity.refresh();
							}
							
						}
					});
				}
	
			}
		}).start();
	}

	public List<FileArrayEntry> getBookmarks() {
	
		String bookmarkCsv = mContext.getSharedPreferences(BOOKMARKS_FILE,
				Context.MODE_PRIVATE).getString(BOOKMARKS, "");
	
		StringTokenizer tokens = new StringTokenizer(bookmarkCsv, "\n");
		List<FileArrayEntry> files = new ArrayList<FileArrayEntry>();
	
		while (tokens.hasMoreTokens()) {
			String bookmark = tokens.nextToken();
	
			File dir = new File(bookmark);
			if (dir.exists() && dir.isDirectory()) {
				FileArrayEntry entry = new FileArrayEntry(bookmark);
				files.add(entry);
			}
		}
		return files;
	
	}

	public boolean isBookmarked(String path) {
	
		return bookmarkedPaths.contains(path);
	}

	private void refreshBookmarkCache() {
		String bookmarkCsv = mContext.getSharedPreferences(BOOKMARKS_FILE,
				Context.MODE_PRIVATE).getString(BOOKMARKS, "");
	
		StringTokenizer tokens = new StringTokenizer(bookmarkCsv, "\n");
		bookmarkedPaths.clear();
		while (tokens.hasMoreTokens()) {
			String bookmark = tokens.nextToken();
	
			File dir = new File(bookmark);
			if (dir.exists() && dir.isDirectory()) {
				synchronized (bookmarkedPaths) {
				
					bookmarkedPaths.add(bookmark);
				}
				
			}
		}
	
	}

	public void removeBookmark(final String path) {

        AsyncTask<String, String, String> task = new AsyncTask<String, String, String>(){

            @Override
            protected String doInBackground(String... strings) {
                String bookmarkCsv = mContext.getSharedPreferences(
                        BOOKMARKS_FILE, Context.MODE_PRIVATE).getString(
                        BOOKMARKS, "");

                StringTokenizer tokens = new StringTokenizer(bookmarkCsv, "\n");
                final StringBuffer buffer = new StringBuffer();

                while (tokens.hasMoreTokens()) {
                    String bookmark = tokens.nextToken();

                    if (bookmark != null && !bookmark.equals(path)) {
                        buffer.append("\n");
                        buffer.append(bookmark);
                    }
                }

                SharedPreferences.Editor editor = mContext
                        .getSharedPreferences(BOOKMARKS_FILE,
                                Context.MODE_PRIVATE).edit();
                editor.putString(BOOKMARKS, buffer.toString());
                editor.commit();

                refreshBookmarkCache();
                mContext.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if(mContext instanceof MuBookmarksActivity)
                        {
                            MuBookmarksActivity muBookmarksActivity = (MuBookmarksActivity)mContext;
                            muBookmarksActivity.refresh();
                        }

                    }
                });

                return null;
            }
        }.execute();
	}

}
