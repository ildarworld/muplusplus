package ru.innopolis.muplusplus.callbacks;

public interface ActionCallback<T> {

	T onSuccess();
	void onFailure(Throwable e);
}
