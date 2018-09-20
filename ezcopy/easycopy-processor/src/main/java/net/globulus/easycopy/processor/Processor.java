package net.globulus.easycopy.processor;

import net.globulus.easycopy.annotation.EasyCopy;
import net.globulus.easycopy.annotation.Skip;
import net.globulus.easycopy.processor.codegen.CopierCodeGen;
import net.globulus.easycopy.processor.codegen.CopierListCodeGen;
import net.globulus.easycopy.processor.codegen.MergeFileCodeGen;
import net.globulus.easycopy.processor.util.FrameworkUtil;
import net.globulus.easycopy.processor.util.ProcessorLog;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class Processor extends AbstractProcessor {

	private static final List<Class<? extends Annotation>> ANNOTATIONS = Arrays.asList(
			EasyCopy.class,
			Skip.class
	);

	private Elements mElementUtils;
	private Types mTypeUtils;
	private Filer mFiler;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);

		ProcessorLog.init(env);

		mElementUtils = env.getElementUtils();
		mTypeUtils = env.getTypeUtils();
		mFiler = env.getFiler();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> types = new LinkedHashSet<>();
		for (Class<? extends Annotation> annotation : ANNOTATIONS) {
			types.add(annotation.getCanonicalName());
		}
		return types;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		List<String> annotatedClasses = new ArrayList<>();
		List<String> parcelerNames = new ArrayList<>();
		CopierCodeGen copierCodeGen = new CopierCodeGen(mElementUtils, mFiler);
		Element lastElement = null;
		Boolean shouldMerge = null;
		for (Element element : roundEnv.getElementsAnnotatedWith(EasyCopy.class)) {
			if (!isValid(element)) {
				continue;
			}

			List<CopyField> fields = new ArrayList<>();
			lastElement = element;

			EasyCopy annotation = element.getAnnotation(EasyCopy.class);
			boolean deep = annotation.deep();
			if (annotation.bottom()) {
				shouldMerge = true;
			} else {
				shouldMerge = false;
			}

			List<? extends Element> memberFields = mElementUtils.getAllMembers((TypeElement) element);

			if (memberFields != null) {
				for (Element member : memberFields) {
					if (member.getKind() != ElementKind.FIELD || !(member instanceof VariableElement)) {
						continue;
					}

					Skip fieldAnnotated = member.getAnnotation(Skip.class);
					if (fieldAnnotated != null) {
						continue;
					}

					Set<Modifier> modifiers = member.getModifiers();
					if (modifiers.contains(Modifier.STATIC)
							|| modifiers.contains(Modifier.FINAL)
							|| modifiers.contains(Modifier.PRIVATE)) {
						continue;
					}

					fields.add(new CopyField((VariableElement) member, mElementUtils, mTypeUtils));
				}
			}

			try {
				TypeElement classElement = (TypeElement) element;
				String name = copierCodeGen.generate(classElement, fields, deep);
				annotatedClasses.add(classElement.getQualifiedName().toString());
				parcelerNames.add(name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (shouldMerge == null) {
			return true;
		}
		CopierListCodeGen.Input input = new CopierListCodeGen.Input(shouldMerge ? lastElement : null, annotatedClasses, parcelerNames);
		if (shouldMerge) {
			ProcessorLog.note(lastElement, "MERGING");
			ByteBuffer buffer = ByteBuffer.allocate(50_000);
			try {
				for (int i = 0; i < Integer.MAX_VALUE; i++) {
					Class mergeClass = Class.forName(FrameworkUtil.getEasyCopyPackageName() + "." + MergeFileCodeGen.CLASS_NAME + i, true, getClass().getClassLoader());

					ProcessorLog.note(lastElement, "FOUND MERGE CLASS");
					buffer.put((byte[]) mergeClass.getField(MergeFileCodeGen.MERGE_FIELD_NAME).get(null));
					if (!mergeClass.getField(MergeFileCodeGen.NEXT_FIELD_NAME).getBoolean(null)) {
						break;
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			try {
				CopierListCodeGen.Input merge = CopierListCodeGen.Input.fromBytes(buffer.array());
				input = input.mergedUp(merge);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			new CopierListCodeGen().generate(mFiler, input);
		} else {
			ProcessorLog.note(lastElement, "WRITING MERGE");
			new MergeFileCodeGen().generate(mFiler, input);
		}

		return true;
	}

	private boolean isValid(Element element) {
		if (element.getKind() == ElementKind.CLASS) {
//			if (element.getModifiers().contains(Modifier.ABSTRACT)) {
//				ProcessorLog.error(element,
//						"Element %s is annotated with @%s but is an abstract class. "
//								+ "Abstract classes can not be annotated. Annotate the concrete class "
//								+ "that implements all abstract methods with @%s", element.getSimpleName(),
//						EasyCopy.class.getSimpleName(), EasyCopy.class.getSimpleName());
//				return false;
//			}

			if (element.getModifiers().contains(Modifier.PRIVATE)) {
				ProcessorLog.error(element, "The private class %s is annotated with @%s. "
								+ "Private classes are not supported because of lacking visibility.",
						element.getSimpleName(), EasyCopy.class.getSimpleName());
				return false;
			}

			return true;
		} else {
			ProcessorLog.error(element,
					"Element %s is annotated with @%s but is not a class. Only Classes are supported",
					element.getSimpleName(), EasyCopy.class.getSimpleName());
			return false;
		}
	}
}
