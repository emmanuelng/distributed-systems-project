package common.tcp.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

import common.tcp.client.MethodInvocation;

public class ProxyObjectHandler implements InvocationHandler {

	public static Object generateProxyObj(String rmiServerHost, int rmiServerPort, Object object) {
		ClassLoader cl = object.getClass().getClassLoader();
		Class<?>[] interfaces = object.getClass().getInterfaces();
		return Proxy.newProxyInstance(cl, interfaces, new ProxyObjectHandler(rmiServerHost, rmiServerPort));
	}

	private String rmiServerHost;
	private int rmiServerPort;

	private Socket socket = null;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;

	private ProxyObjectHandler(String rmiServerHost, int rmiServerPort) {
		this.rmiServerHost = rmiServerHost;
		this.rmiServerPort = rmiServerPort;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		connect();
		out.writeObject(new MethodInvocation(method, args));
		return in.readObject();

	}

	private void connect() throws Throwable {
		if (socket == null || out == null || in == null) {
			socket = new Socket(rmiServerHost, rmiServerPort);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		}
	}

}
