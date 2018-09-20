package net.globulus.easycopy.processor.codegen;

import net.globulus.easycopy.annotation.EasyCopy;
import net.globulus.easycopy.processor.util.FrameworkUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
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

	public void generate(Filer filer, Input input) {
		try {

			String packageName = FrameworkUtil.getEasyCopyPackageName();
			String className = FrameworkUtil.getCopierListImplClassName();
			String innerClassName = className + "Inner";

			JavaFileObject jfo = filer.createSourceFile(packageName + "." + className, input.lastElement);
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
			for (int i = 0; i < input.annotatedClasses.size(); i++) {
				jw.emitStatement("map.put(" + input.annotatedClasses.get(i) + ".class, new " + input.copierNames.get(i) + "())");
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

	public static class Input implements Serializable {

		public Element lastElement;
		public final List<String> annotatedClasses;
		public final List<String> copierNames;

		public Input(Element lastElement, List<String> annotatedClasses, List<String> copierNames) {
			this.lastElement = lastElement;
			this.annotatedClasses = annotatedClasses;
			this.copierNames = copierNames;
		}

		public Input mergedUp(Input other) {
			Element lastElement = (other.lastElement != null) ? other.lastElement : this.lastElement;
			List<String> annotatedClasses = new ArrayList<>(other.annotatedClasses);
			annotatedClasses.addAll(this.annotatedClasses);
			List<String> copierNames = new ArrayList<>(other.copierNames);
			copierNames.addAll(this.copierNames);
			return new Input(lastElement, annotatedClasses, copierNames);
		}

		public static Input fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
			try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				 ObjectInput in = new ObjectInputStream(bis)) {
				return (Input) in.readObject();
			}
		}
	}
}
