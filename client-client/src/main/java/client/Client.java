package client;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import client.commands.Command;
import middleware.Middleware;

public class Client {

	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 1099;

	static {
		CommandRegistry.initialize();
	}

	public static boolean exit = false;

	public static void main(String[] args) {
		Client c = null;

		if (args.length == 0) {
			c = new Client();
		} else if (args.length == 1) {
			c = new Client(args[0]);
		} else if (args.length == 2) {
			c = new Client(args[0], Integer.parseInt(args[1]));
		} else {
			System.err.println("Usage: java client [rmihost [rmiport]]");
			System.exit(1);
		}

		c.start();
	}

	private String host;
	private int port;
	private BufferedReader stdin;
	private Middleware middleware;
	private String command;

	private Client(String host, int port) {
		this.host = host;
		this.port = port;
		this.stdin = new BufferedReader(new InputStreamReader(System.in));
	}

	private Client(String host) {
		this(host, DEFAULT_PORT);
	}

	private Client() {
		this(DEFAULT_HOST);
	}

	private void start() {
		try {
			// Connect to the Server
			connect();
			if (middleware != null) {
				System.out.println("Successfully connected!\n");
				loop();
			} else {
				System.err.println("Error: The middleware is null");
			}
		} catch (IOException e) {
			System.err.println("Error: Unnable to read commands");
		}
	}

	private void connect() {
		try {
			Registry registry = LocateRegistry.getRegistry(host, port);
			middleware = (Middleware) registry.lookup("middleware.group20");
		} catch (NotBoundException e) {
			System.err.println("Error: Unable to find the middleware in the registry");
			System.exit(1);
		} catch (RemoteException e) {
			System.err.println("Error: Impossible to connect to " + host + ":" + port);
			System.exit(1);
		}
	}

	private void loop() throws IOException {
		System.out.println("======== Client Interface ========\n");

		while (!exit) {
			System.out.print("\n> ");
			command = stdin.readLine();
			execute(command.trim(), 0);
		}

		System.out.println("Exiting client.");
	}

	private void execute(String input, int attempts) {
		if (input.isEmpty()) {
			return;
		}

		List<String> args = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(command, " ");

		while (tokenizer.hasMoreElements()) {
			args.add(tokenizer.nextToken().trim());
		}

		String commandName = args.remove(0);
		Command cmd = CommandRegistry.COMMANDS.get(commandName);

		if (cmd != null) {
			if (args.size() >= cmd.minArgs() && (cmd.maxArgs() < 0 || args.size() <= cmd.maxArgs())) {
				try {
					cmd.execute(middleware, args);
				} catch (ConnectException e) {
					// Try to reconnect and to re-execute the command
					if (attempts < 5) {
						connect();
						execute(input, attempts + 1);
					} else {
						System.out.println("Error: Connection lost.");
						System.exit(1);
					}
				} catch (EOFException e) {
					System.out.println("Error: Server internal error. Please try again.");
				} catch (Exception e) {
					System.out.print("Error");
					if (e.getMessage() != null) {
						System.out.println(": " + e.getMessage());
					} else {
						System.out.println();
					}
				}
			} else {
				System.out.println(cmd.invalidArgsNbMsg(commandName));
			}
		} else {
			System.out.println("The interface does not support this command\n");
		}
	}
}
