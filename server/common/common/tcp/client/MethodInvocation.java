package common.tcp.client;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodInvocation implements Serializable {

	private static final long serialVersionUID = 6811339128438830739L;

	private String methodName;
	private Class<?>[] parametersTypes;
	private Object[] args;

	public MethodInvocation(Method method, Object[] args) throws NotSerializableException {
		this.methodName = method.getName();
		this.parametersTypes = method.getParameterTypes();
		this.args = args;
	}

	public String methodName() {
		return methodName;
	}

	public Class<?>[] parameterTypes() {
		return parametersTypes;
	}

	public Object[] args() {
		return Arrays.copyOf(args, args.length);
	}

}
