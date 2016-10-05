package net.globulus.easycopy.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by gordanglavas on 30/09/16.
 */
public class CopyField {

	private String mFieldName;
	private String type;
	private Element element;
	public boolean isPrimitive;

	public CopyField(VariableElement element, Elements elementUtils, Types typeUtils) {
		this.element = element;
		mFieldName = element.getSimpleName().toString();
		TypeMirror tm = element.asType();
		type = tm.toString();
		isPrimitive = tm.getKind().isPrimitive();
		if (!isPrimitive) {
			Class<?> classesToCheck[] = { Byte.class, Short.class, Integer.class, Float.class, Double.class, Boolean.class };
			for (Class<?> clazz : classesToCheck) {
				if (isAssignable(tm, clazz, elementUtils, typeUtils)) {
					isPrimitive = true;
					break;
				}
			}
		}

//		CodeGenInfo res = SupportedTypes.getCodeGenInfo(element, elementUtils, typeUtils);
//			codeGenerator = res.getCodeGenerator();
//			genericsTypeArgument = res.getGenericsType();
//
//			// Check if type is supported
//			if (codeGenerator == null) {
//				ProcessorLog.error(element,
//						"Unsupported type %s for field %s.",
//						element.asType().toString(), element.getSimpleName());
//			}
	}

	private boolean isAssignable(TypeMirror tm, Class<?> type, Elements elementUtil, Types typeUtil) {
		TypeMirror typeMirror = elementUtil.getTypeElement(type.getName()).asType();
		return typeUtil.isAssignable(tm, typeMirror);
	}

	public Element getElement() {
		return element;
	}

	public String getFieldName() {
		return mFieldName;
	}

	public String getType() {
		return type;
	}
}
