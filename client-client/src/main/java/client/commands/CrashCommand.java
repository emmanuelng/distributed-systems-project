package client.commands;

import java.util.List;

import middleware.Middleware;

public class CrashCommand extends Command {

	@Override
	public int minArgs() {
		return 1;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		String which = arguments.get(0);

		if (middleware.crash(which)) {
			System.out.println("The " + which + " server was stopped successfully");
		} else {
			System.out.println("The " + which + " does not exist or could not be stopped");
		}
	}

	@Override
	public String description() {
		return "Simulate a crash";
	}

	@Override
	public String purpose() {
		return "Indicates which server should destroy itself";
	}

	@Override
	public String argsDescription() {
		return "[cars|flights|hotels|middleware]";
	}

}
