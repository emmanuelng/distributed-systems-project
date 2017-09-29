package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class RMIClient {

	public static Object getProxyObjectFrom(String host, int port) {
		Object proxyObj = null;

		try {
			Socket socket = new Socket(host, port);
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			proxyObj = in.readObject();
			socket.close();
		} catch (IOException e) {
			System.err.println("[Client] Failed to connect to the registry at " + host + ":" + port);
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("[Client] Invalid proxy object received from " + host + ":" + port);
			System.exit(1);
		}

		return proxyObj;
	}

}
