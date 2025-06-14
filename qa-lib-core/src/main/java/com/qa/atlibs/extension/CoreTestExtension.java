package com.qa.atlibs.extension;

import com.qa.atlibs.manager.EnumSessionProcessor;
import com.qa.atlibs.session.Session;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import java.lang.reflect.Method;
import java.util.stream.IntStream;

public class CoreTestExtension implements BeforeEachCallback, InvocationInterceptor {

	/**
	 *
	 * @param context the current extension context; never {@code null}
	 */
	@Override
	public void beforeEach(ExtensionContext context) {
		Session.initNewSession();
	}

	/**
	 *
	 * @param invocation the invocation that is being intercepted; never
	 * {@code null}
	 * @param invocationContext the context of the invocation that is being
	 * intercepted; never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @throws Throwable
	 */
	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
	                                        ExtensionContext extensionContext) throws Throwable {
		Method testMethod = invocationContext.getExecutable();
		Object[] arguments = invocationContext.getArguments().toArray();

		IntStream.range(0, arguments.length).forEach(i -> {
			String parameterName = testMethod.getParameters()[i].getName();
			Object parameterValue = arguments[i];
			if (parameterValue instanceof EnumSessionProcessor sessionVar) {
				Session.setSessionValue(parameterName, sessionVar.getValue());
			} else {
				Session.setSessionValue(parameterName, parameterValue);
			}
		});
		invocation.proceed();
	}

}
