package net.appositedesigns.muplusplus.muUtil;

import java.util.Comparator;

import net.appositedesigns.muplusplus.activity.MuFilesArrayActivity;
import net.appositedesigns.muplusplus.model.FileArrayEntry;
import net.appositedesigns.muplusplus.muUtil.SettingsHelper.SortField;

public class FileArraySorter implements Comparator<FileArrayEntry> {

	private MuFilesArrayActivity mContext;
	
	private boolean dirsOnTop = false;
	private SortField sortField;
	private int dir;
	
	public FileArraySorter(MuFilesArrayActivity context){
		
		mContext = context;
		SettingsHelper util = new SettingsHelper(mContext);
		
		dirsOnTop = util.isShowDirsOnTop();
		sortField = util.getSortField();
		dir =  util.getSortDir();
	}
	
	@Override
	public int compare(FileArrayEntry file1, FileArrayEntry file2) {

		if(dirsOnTop)
		{
			if(file1.getPath().isDirectory() && file2.getPath().isFile())
			{
				return -1;
			}
			else if(file2.getPath().isDirectory() && file1.getPath().isFile())
			{
				return 1;
			}
		}
		
		switch (sortField) {
		case NAME:
			return dir * file1.getName().compareToIgnoreCase(file2.getName());
			
		case MTIME:
			return dir * file1.getLastModified().compareTo(file2.getLastModified());

		case SIZE:
			return dir * Long.valueOf(file1.getSize()).compareTo(file2.getSize());
			
		default:
			break;
		}
		
		return 0;
	}
	
	

}
