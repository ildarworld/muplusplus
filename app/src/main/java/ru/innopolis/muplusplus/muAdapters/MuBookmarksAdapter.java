package ru.innopolis.muplusplus.muAdapters;

import java.util.List;

import ru.innopolis.muplusplus.R;
import ru.innopolis.muplusplus.activity.MuBookmarksActivity;
import ru.innopolis.muplusplus.model.FileArrayEntry;
import ru.innopolis.muplusplus.muUtil.MuUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MuBookmarksAdapter extends BaseAdapter {

	public static class ViewHolder 
	{
	  public TextView resName;
	  public ImageView resIcon;
	  public TextView resMeta;
	}

	private static final String TAG = MuBookmarksAdapter.class.getName();
	  
	private MuBookmarksActivity mContext;
	private List<FileArrayEntry> files;
	private LayoutInflater mInflater;
	
	public MuBookmarksAdapter(MuBookmarksActivity context, List<FileArrayEntry> files) {
		super();
		mContext = context;
		this.files = files;
		mInflater = mContext.getLayoutInflater();
		
	}

	
	@Override
	public int getCount() {
		if(files == null)
		{
			return 0;
		}
		else
		{
			return files.size();
		}
	}

	@Override
	public Object getItem(int arg0) {

		if(files == null)
			return null;
		else
			return files.get(arg0);
	}

	public List<FileArrayEntry> getItems()
	{
	  return files;
	}
	
	@Override
	public long getItemId(int position) {

		return position;
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
        if (convertView == null) 
        {
        	convertView = mInflater.inflate(R.layout.bookmark_list_item, parent, false);
            holder = new ViewHolder();
            holder.resName = (TextView)convertView.findViewById(R.id.explorer_resName);
            holder.resMeta = (TextView)convertView.findViewById(R.id.explorer_resMeta);
            holder.resIcon = (ImageView)convertView.findViewById(R.id.explorer_resIcon);
            convertView.setTag(holder);
        } 
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        final FileArrayEntry currentFile = files.get(position);
        holder.resName.setText(currentFile.getName());
        if(MuUtil.isRoot(currentFile.getPath()))
        {
        	holder.resName.setText(mContext.getString(R.string.filesystem_root));
        }
        holder.resIcon.setImageDrawable(MuUtil.getIcon(mContext, currentFile.getPath()));
        holder.resMeta.setText(currentFile.getPath().getAbsolutePath());
        
        return convertView;
	}

}
