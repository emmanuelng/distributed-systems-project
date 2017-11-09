package client.commands;

import java.util.List;

import client.Client;
import middleware.Middleware;

public class QuitCommand extends Command {

	@Override
	public int minArgs() {
		return 0;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) {
		Client.exit = true;
	}

	@Override
	public String description() {
		return "Exit the application.";
	}

	@Override
	public String purpose() {
		return "Closes the program.";
	}

	@Override
	public String argsDescription() {
		return "";
	}

}
