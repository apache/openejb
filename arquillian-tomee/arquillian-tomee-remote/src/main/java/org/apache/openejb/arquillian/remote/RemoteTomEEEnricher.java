package org.apache.openejb.arquillian.remote;

import org.apache.openejb.cdi.ThreadSingletonServiceImpl;
import org.jboss.arquillian.test.spi.TestEnricher;

import javax.ejb.EJB;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class RemoteTomEEEnricher implements TestEnricher {

	private static final String ANNOTATION_NAME = "javax.ejb.EJB";

	public void enrich(Object testCase) {
		BeanManager mgr = getBeanManager();
		if (mgr != null) {
			AnnotatedType<?> type = mgr.createAnnotatedType(getClass());
			InjectionTarget<Object> injectionTarget = (InjectionTarget<Object>) mgr.createInjectionTarget(type);
			CreationalContext<Object> context = mgr.createCreationalContext(null);

			injectionTarget.inject(testCase, context);
			injectionTarget.postConstruct(this);
		}

		try {
			if (SecurityActions.isClassPresent(ANNOTATION_NAME)) {
				@SuppressWarnings("unchecked")
				Class<? extends Annotation> ejbAnnotation = (Class<? extends Annotation>) SecurityActions.getThreadContextClassLoader().loadClass(ANNOTATION_NAME);

				List<Field> annotatedFields = SecurityActions.getFieldsWithAnnotation(testCase.getClass(), ejbAnnotation);

				for (Field field : annotatedFields) {
					if (field.get(testCase) == null) {
						EJB fieldAnnotation = (EJB) field.getAnnotation(ejbAnnotation);
						Object ejb = lookupEJB(field.getType(), fieldAnnotation.mappedName());
						field.set(testCase, ejb);
					}
				}

				List<Method> methods = SecurityActions.getMethodsWithAnnotation(testCase.getClass(), ejbAnnotation);

				for (Method method : methods) {
					if (method.getParameterTypes().length != 1) {
						throw new RuntimeException("@EJB only allowed on single argument methods");
					}
					if (!method.getName().startsWith("set")) {
						throw new RuntimeException("@EJB only allowed on 'set' methods");
					}
					EJB parameterAnnotation = null; // method.getParameterAnnotations()[0]
					for (Annotation annotation : method.getParameterAnnotations()[0]) {
						if (EJB.class.isAssignableFrom(annotation.annotationType())) {
							parameterAnnotation = (EJB) annotation;
						}
					}
					String mappedName = parameterAnnotation == null ? null : parameterAnnotation.mappedName();
					Object ejb = lookupEJB(method.getParameterTypes()[0], mappedName);
					method.invoke(testCase, ejb);
				}

			}
		} catch (Exception e) {

		}

	}

	protected Object lookupEJB(Class<?> fieldType, String mappedName) throws Exception {
		// TODO: figure out test context ?
		Context initcontext = new InitialContext();

		// TODO: These names are not spec compliant; fieldType needs to be a
		// bean type here,
		// but usually is just an interface of a bean. These seldom work.
		String[] jndiNames = { "openejb:global/global/test/test.jar/" + fieldType.getSimpleName() + "Bean",
				"openejb:global/global/test/test.jar/" + fieldType.getSimpleName(),
				"java:global/test/test.jar/" + fieldType.getSimpleName() + "Bean", 
				"java:global/test/test.jar/" + fieldType.getSimpleName(), 
				"java:global/test.ear/test/" + fieldType.getSimpleName() + "Bean", 
				"java:global/test.ear/test/" + fieldType.getSimpleName(), 
				"java:global/test/" + fieldType.getSimpleName(), 
				"java:global/test/" + fieldType.getSimpleName() + "Bean", 
				"java:global/test/" + fieldType.getSimpleName() + "/no-interface", 
				"test/" + fieldType.getSimpleName() + "Bean/local", 
				"test/" + fieldType.getSimpleName() + "Bean/remote", 
				"test/" + fieldType.getSimpleName() + "/no-interface", 
				fieldType.getSimpleName() + "Bean/local", 
				fieldType.getSimpleName() + "Bean/remote", 
				fieldType.getSimpleName() + "/no-interface",
				"ejblocal:" + fieldType.getCanonicalName(),
				fieldType.getCanonicalName() };
		if ((mappedName != null) && (!mappedName.equals(""))) {
			// Use only the mapped name to lookup this EJB
			jndiNames = new String[] { mappedName };
		}

		for (String jndiName : jndiNames) {
			try {
				return initcontext.lookup(jndiName);
			} catch (NamingException e) {
				// no-op, try next
			}
		}
		throw new NamingException("No EJB found in JNDI");
	}

	@Override
	public Object[] resolve(Method method) {
		return new Object[method.getParameterTypes().length];
	}

	public BeanManager getBeanManager() {
		return ThreadSingletonServiceImpl.get().getBeanManagerImpl();
	}
}
