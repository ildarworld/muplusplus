package net.appositedesigns.muplusplus.muAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.appositedesigns.muplusplus.R;
import net.appositedesigns.muplusplus.activity.FilesArrayActivity;
import net.appositedesigns.muplusplus.model.FileArrayEntry;
import net.appositedesigns.muplusplus.muUtil.MuUtil;

import java.util.List;

public class FilesArrayAdapter extends BaseAdapter {

	public static class ViewHolder 
	{
	  public TextView resName;
	  public ImageView resIcon;
	  public TextView resMeta;
	}

	private static final String TAG = FilesArrayAdapter.class.getName();
	  
	private FilesArrayActivity mContext;
	private List<FileArrayEntry> files;
	private LayoutInflater mInflater;
	
	public FilesArrayAdapter(FilesArrayActivity context, List<FileArrayEntry> files) {
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
            convertView = mInflater.inflate(R.layout.explorer_item, parent, false);
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
        holder.resIcon.setImageDrawable(MuUtil.getIcon(mContext, currentFile.getPath()));
        String meta = MuUtil.prepareMeta(currentFile, mContext);
        holder.resMeta.setText(meta);

        
        return convertView;
	}

}
