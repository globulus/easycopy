package net.globulus.easycopy.processor.codegen;

import net.globulus.easycopy.annotation.EasyCopy;
import net.globulus.easycopy.processor.CopyField;
import net.globulus.easycopy.processor.util.FrameworkUtil;
import net.globulus.easycopy.processor.util.TypeUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

import javawriter.JavaWriter;

import static net.globulus.easycopy.processor.util.FrameworkUtil.PARAM_FROM;
import static net.globulus.easycopy.processor.util.FrameworkUtil.PARAM_TO;

public class CopierCodeGen {

    private Filer filer;
    private Elements elementUtils;

    public CopierCodeGen(Elements elementUtils, Filer filer) {
        this.filer = filer;
        this.elementUtils = elementUtils;
    }

    public String generate(TypeElement classElement, List<CopyField> fields, boolean shallow) throws Exception {

        String classSuffix = FrameworkUtil.getCopierClassExtension();
        String packageName = TypeUtils.getPackageName(elementUtils, classElement);
        String binaryName = TypeUtils.getBinaryName(elementUtils, classElement);

     	 String originalClassName = classElement.getSimpleName().toString();

        String originFullQualifiedName = classElement.getQualifiedName().toString();
		String copierClassName = originalClassName + classSuffix;
        String className;
        if (packageName.length() > 0) {
            className = binaryName.substring(packageName.length() + 1) + classSuffix;
        } else {
            className = binaryName + classSuffix;
        }
        String qualifiedName = binaryName + classSuffix;

        JavaFileObject jfo = filer.createSourceFile(qualifiedName, classElement);
		Writer writer = jfo.openWriter();
		JavaWriter jw = new JavaWriter(writer);
		jw.emitPackage(packageName);
		jw.emitImports(FrameworkUtil.getQualifiedName(FrameworkUtil.getCopierClassName()));
		jw.emitImports(FrameworkUtil.getQualifiedName(FrameworkUtil.getCopyUtilClassName()));
		jw.emitImports(originFullQualifiedName);
		jw.emitEmptyLine();
		jw.emitJavadoc("Generated class by @%s . Do not modify this code!",
                EasyCopy.class.getSimpleName());
		jw.beginType(className, "class", EnumSet.of(Modifier.PUBLIC), null,
				FrameworkUtil.getCopierClassName() + "<" + originalClassName + ">");
		jw.emitEmptyLine();

		jw.beginMethod("void", "copy", EnumSet.of(Modifier.PUBLIC), originalClassName, PARAM_FROM, originalClassName, PARAM_TO);
		for (CopyField field : fields) {
			if (shallow || field.isPrimitive) {
				emitAssignmentStatement(jw, field);
			} else {
				String type = field.getType();
				String name = field.getFieldName();
				String auxName = PARAM_TO + name;
				jw.emitStatement("%s %s = new %s()", type, auxName, type);
				jw.beginControlFlow("if (%s.copy(%s.%s, %s))", FrameworkUtil.getCopyUtilClassName(),
						PARAM_FROM, name, auxName);
				jw.emitStatement("%s.%s = %s", PARAM_TO, name, auxName);
				jw.nextControlFlow("else");
				emitAssignmentStatement(jw, field);
				jw.endControlFlow();
			}
		}
		jw.endMethod();
		jw.endType();
		jw.close();

		return packageName + "." + copierClassName;
    }

	private void emitAssignmentStatement(JavaWriter jw, CopyField field) throws IOException {
		String name = field.getFieldName();
		jw.emitStatement("%s.%s = %s.%s", PARAM_TO, name, PARAM_FROM, name);
	}
}
