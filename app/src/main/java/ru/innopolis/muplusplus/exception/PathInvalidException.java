package ru.innopolis.muplusplus.exception;

public class PathInvalidException extends Exception {

	private static final long VersionUID = -4046926680600982016L;
	
	private String location;

	public PathInvalidException(String location) {
		super();
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	
}
