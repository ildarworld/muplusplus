package net.appositedesigns.muplusplus.model;

import java.util.List;

public class FileWaiting {

	
	private List<FileArrayEntry> children;
	private boolean isExcludeFromMedia = false;
	public List<FileArrayEntry> getChildren() {
		return children;
	}
	public void setChildren(List<FileArrayEntry> children) {
		this.children = children;
	}
	public boolean isExcludeFromMedia() {
		return isExcludeFromMedia;
	}
	public void setExcludeFromMedia(boolean isExcludeFromMedia) {
		this.isExcludeFromMedia = isExcludeFromMedia;
	}
	public FileWaiting(List<FileArrayEntry> children) {
		super();
		this.children = children;
	}
	
	
}
