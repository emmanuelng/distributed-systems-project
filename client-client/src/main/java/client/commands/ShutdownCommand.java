package client.commands;

import java.util.List;

import middleware.Middleware;

public class ShutdownCommand extends Command {

	@Override
	public int minArgs() {
		return 0;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		if (middleware.shutdown()) {
			System.out.println("System was shut down");
		} else {
			System.out.println("The system could not shut down.Please try again.");
		}
	}

	@Override
	public String description() {
		return "Shut down the system";
	}

	@Override
	public String purpose() {
		return "Send a shutdown request to the server.";
	}

	@Override
	public String argsDescription() {
		return "";
	}

}
