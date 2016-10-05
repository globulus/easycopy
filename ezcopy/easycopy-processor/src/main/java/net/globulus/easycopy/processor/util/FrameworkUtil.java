package net.globulus.easycopy.processor.util;

public final class FrameworkUtil {

	public static final String PARAM_FROM = "from";
	public static final String PARAM_TO = "to";

	private FrameworkUtil() { }

	public static String getEasyCopyPackageName() {
		return "net.globulus.easycopy";
	}

	public static String getCopyUtilClassName() {
		return "CopyUtil";
	}

	public static String getCopierClassName() {
		return "Copier";
	}

	public static String getCopierClassExtension() {
		return "_EasyCopy" + getCopierClassName();
	}

	public static String getCopierListClassName() {
		return "CopierList";
	}

	public static String getCopierListImplClassName() {
		return "EasyCopy" + getCopierListClassName();
	}

	public static String getQualifiedName(String className) {
		return String.format("%s.%s", getEasyCopyPackageName(), className);
	}
}
