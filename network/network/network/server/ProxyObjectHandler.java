package network.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Socket;

import network.common.MethodInvocation;

public class ProxyObjectHandler implements InvocationHandler, Serializable {

	private static final long serialVersionUID = 8693829967258722091L;

	private String rmiServerHost;
	private int rmiServerPort;

	private Socket socket = null;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;

	public ProxyObjectHandler(String rmiServerHost, int rmiServerPort) {
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
			System.out.println("[Client] Connecting to " + rmiServerHost + ":" + rmiServerPort + "...");

			socket = new Socket(rmiServerHost, rmiServerPort);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		}
	}

}
