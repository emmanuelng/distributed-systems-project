package network.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

import network.common.MethodInvocation;

public abstract class RMIServer {

	private boolean running = false;
	private ServerSocket registrySocket, rmiSocket;
	private Object proxyObject;
	private Object object;

	public RMIServer(int port, Object object) {
		try {
			this.registrySocket = new ServerSocket(port);
			this.rmiSocket = new ServerSocket(0); // Use any free port
			this.object = object;
		} catch (IOException e) {
			System.err.println("Unable to initialize the server: Port " + port + " is used.");
			System.exit(1);
		}

		// Initialize the proxy object
		String rmiHost = rmiSocket.getInetAddress().getHostAddress();
		int rmiPort = rmiSocket.getLocalPort();
		this.proxyObject = ProxyObjectHandler.generateProxyObj(rmiHost, rmiPort, this);
	}

	public void start() {
		Thread registryThread = new Thread(registryRunnable());
		Thread rmiThread = new Thread(rmiRunnable());
		running = true;

		registryThread.start();
		rmiThread.start();
	}

	private Runnable registryRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				String host = registrySocket.getInetAddress().getHostName();
				int port = registrySocket.getLocalPort();
				System.out.println("[Registry] Starting server at " + host + ":" + port);

				while (running) {
					try {
						Socket socket = registrySocket.accept();

						String clientAddress = socket.getInetAddress().getHostAddress();
						System.out.println("[Registry] sending proxy object to " + clientAddress + "...");

						ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
						out.writeObject(proxyObject);

						socket.close();
					} catch (IOException e) {
						System.err.println("[Registry] Server error: " + e.getMessage());
					}
				}
			}
		};
	}

	private Runnable rmiRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				String host = rmiSocket.getInetAddress().getHostName();
				int port = rmiSocket.getLocalPort();
				System.out.println("[RMI] Starting server at " + host + ":" + port);

				try {
					while (running) {
						Socket socket = rmiSocket.accept();
						Thread rmiChannelThread = new Thread(rmiChannelRunnable(socket));
						rmiChannelThread.start();
					}
				} catch (IOException e) {
					System.err.println("[RMI] " + e.getMessage());
				}
			}
		};
	}

	protected Runnable rmiChannelRunnable(Socket socket) {
		return new Runnable() {

			@Override
			public void run() {
				String clientAdress = socket.getInetAddress().getHostAddress();
				System.out.println("[RMI] Starting a new RMI channel with client" + clientAdress);

				try {
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
					String className = object.getClass().getName();

					while (running) {
						MethodInvocation mi = (MethodInvocation) in.readObject();
						System.out.println("[RMI]" + className + "::" + mi.methodName() + " called");

						Method method = Class.forName(className).getMethod(mi.methodName(), mi.parameterTypes());
						Object result = method.invoke(object, mi.args());
						System.out.println("[RMI] Returning " + result);

						out.writeObject(result);
					}

				} catch (IOException e) {
					System.err.println("[RMI] I/O error: " + e.getMessage());
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
					System.err.println("[RMI] Invalid method invocation command received from " + clientAdress);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					System.err.println("[RMI] Cannot execute method invoked by client with address " + clientAdress);
				}

			}
		};
	}

}
