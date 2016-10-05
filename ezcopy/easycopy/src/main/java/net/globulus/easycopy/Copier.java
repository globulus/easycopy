package net.globulus.easycopy;

/**
 * Created by gordanglavas on 29/09/16.
 */

public interface Copier<T> {

	void copy(T from, T to);
}
