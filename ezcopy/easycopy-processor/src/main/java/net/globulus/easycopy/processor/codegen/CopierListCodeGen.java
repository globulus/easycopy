package net.globulus.easycopy.processor.codegen;

import net.globulus.easycopy.annotation.EasyCopy;
import net.globulus.easycopy.processor.util.FrameworkUtil;

import java.io.Writer;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import javawriter.JavaWriter;

/**
 * Created by gordanglavas on 01/10/16.
 */
public class CopierListCodeGen {

	public void generate(Filer filer,
						 Element lastElement,
						 List<String> annotatedClasses,
						 List<String> copierNames) {
		try {

			String packageName = FrameworkUtil.getEasyCopyPackageName();
			String className = FrameworkUtil.getCopierListImplClassName();
			String innerClassName = className + "Inner";

			JavaFileObject jfo = filer.createSourceFile(packageName + "." + className, lastElement);
			Writer writer = jfo.openWriter();
			JavaWriter jw = new JavaWriter(writer);
			jw.emitPackage(packageName);
			jw.emitImports("java.util.Map");
			jw.emitImports("java.util.HashMap");
			jw.emitImports(FrameworkUtil.getQualifiedName(FrameworkUtil.getCopierClassName()));
			jw.emitImports(FrameworkUtil.getQualifiedName(FrameworkUtil.getCopyUtilClassName()));
			jw.emitImports(FrameworkUtil.getQualifiedName(FrameworkUtil.getCopierListClassName()));
			jw.emitEmptyLine();

			jw.emitJavadoc("Generated class by @%s . Do not modify this code!",
					EasyCopy.class.getSimpleName());
			jw.beginType(className, "class", EnumSet.of(Modifier.PUBLIC), null);
			jw.emitEmptyLine();

			jw.beginType(innerClassName, "class", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC),
					null, FrameworkUtil.getCopierListClassName());
			jw.emitField("Map<Class<?>, " + FrameworkUtil.getCopierClassName() + ">", "map");
			jw.beginConstructor(EnumSet.of(Modifier.PUBLIC));
			jw.emitStatement("map = new HashMap<>()");
			for (int i = 0; i < annotatedClasses.size(); i++) {
				jw.emitStatement("map.put(" + annotatedClasses.get(i) + ".class, new " + copierNames.get(i) + "())");
			}
			jw.emitStatement(FrameworkUtil.getCopyUtilClassName() + ".setCopierList(this)");
			jw.endConstructor();

			jw.beginMethod("<T> " + FrameworkUtil.getCopierClassName() + "<T>",
					"getCopierForClass",
					EnumSet.of(Modifier.PUBLIC), "Class<T>", "clazz");
			jw.emitStatement("return map.get(clazz)");
			jw.endMethod();
			jw.endType();

			jw.emitField(innerClassName, "INSTANCE", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
					"new " + innerClassName + "()");


			jw.endType();
			jw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
