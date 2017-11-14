package client.performance;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import middleware.Middleware;

public class PerformanceTest {

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 1099;

	public static boolean exit = false;

	public static void main(String[] args) {
		PerformanceTest p = null;

		if (args.length == 0) {
			p = new PerformanceTest();
		} else if (args.length == 1) {
			p = new PerformanceTest(args[0]);
		} else if (args.length == 2) {
			p = new PerformanceTest(args[0], Integer.parseInt(args[1]));
		} else {
			System.err.println("Usage: java client [rmihost [rmiport]]");
			System.exit(1);
		}

		p.start();
	}

	private String host;
	private int port;
	private Middleware middleware;

	private PerformanceTest(String host, int port) {
		this.host = host;
		this.port = port;
	}

	private PerformanceTest(String host) {
		this(host, DEFAULT_PORT);
	}

	private PerformanceTest() {
		this(DEFAULT_HOST);
	}

	private void start() {
		try {
			// Connect to the Server
			Registry registry = LocateRegistry.getRegistry(host, port);
			middleware = (Middleware) registry.lookup("middleware.group20");

			if (middleware != null) {
				System.out.println("Successfully connected!\n");
				startTest();
			} else {
				System.err.println("Error: The middleware is null");
			}

		} catch (RemoteException e) {
			System.err.println("Error: Impossible to connect to " + host + ":" + port);
		} catch (NotBoundException e) {
			System.err.println("Error: Unable to find the middleware in the registry");
		}
	}

	private void startTest() {
		System.out.println("== Test with 1 client ==\n");
		System.out.println("Average response time with one client: " + sendTransactions() + "ms");
		
		System.out.println("== Test with 10 clients ==\n");
		for (int i=0; i<10; i++) {
			clientStub(i);
		}
	}

	private long sendTransactions() {
		List<Long> responseTimes = new ArrayList<>();
		long start;

		for (int i = 0; i < 1000; i++) {
			start = System.currentTimeMillis();
			transaction1();
			responseTimes.add(System.currentTimeMillis() - start);

			start = System.currentTimeMillis();
			transaction2();
			responseTimes.add(System.currentTimeMillis() - start);

			start = System.currentTimeMillis();
			transaction3();
			responseTimes.add(System.currentTimeMillis() - start);
		}

		long avg = 0;
		for (long time : responseTimes) {
			avg += time;
		}
		avg /= responseTimes.size();

		return avg;
	}

	private void clientStub(int id) {
		Runnable stub = new Runnable() {
			@Override
			public void run() {
				System.out.println("Average response time with one client " + id + ": " + sendTransactions() + "ms");
			}
		};
		
		new Thread(stub).start();
	}

	private void transaction1() {
		try {
			int id = middleware.start();
			middleware.addCars(id, "montreal", 50, 100);
			middleware.queryCars(id, "montreal");
			middleware.commit(id);
		} catch (Exception e) {
			// Stop
		}
	}

	private void transaction2() {
		try {
			int id = middleware.start();
			middleware.addFlight(id, 0, 50, 100);
			middleware.queryFlight(id, 0);
			middleware.commit(id);
		} catch (Exception e) {
			// Stop
		}
	}

	private void transaction3() {
		try {
			int id = middleware.start();
			middleware.addRooms(id, "montreal", 50, 100);
			middleware.queryRooms(id, "montreal");
			middleware.commit(id);
		} catch (Exception e) {
			// Stop
		}
	}
}
