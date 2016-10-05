package net.globulus.easycopy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.globulus.easycopy.processor.util.FrameworkUtil;

/**
 * Created by gordanglavas on 29/09/16.
 */
public final class CopyUtil {

	private static CopierList sCopierList;

	private CopyUtil() { }

	static void setCopierList(CopierList copierList) {
		sCopierList = copierList;
	}

	@Nullable
	private static Copier getCopierForClass(@NonNull Class clazz) {
		if (sCopierList == null) {
			try {
				// Initiate class loading for the ParcelerList implementation class
				Class.forName(FrameworkUtil.getQualifiedName(FrameworkUtil.getCopierListImplClassName()));
			} catch (ClassNotFoundException e) {
				throw new AssertionError(e);  // Can't happen
			}
		}
		return sCopierList.getCopierForClass(clazz);
	}

	public static <T> boolean copy(@NonNull T from, @NonNull T to) {
		Class<?> clazz = from.getClass();
		do {
			Copier copier = getCopierForClass(clazz);
			if (copier != null) {
				copier.copy(from, to);
				return true;
			}
			clazz = clazz.getSuperclass();
		} while (clazz != null);
		return false;
	}
}
