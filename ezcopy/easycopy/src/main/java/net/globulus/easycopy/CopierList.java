package net.globulus.easycopy;

/**
 * Created by gordanglavas on 30/09/16.
 */
public interface CopierList {

	<T> Copier<T> getCopierForClass(Class<T> clazz);
}
