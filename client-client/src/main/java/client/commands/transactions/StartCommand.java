package client.commands.transactions;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class StartCommand extends Command {

	@Override
	public int minArgs() {
		return 0;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		int id = middleware.start();
		System.out.println("Started transaction with id " + id);
	}

	@Override
	public String description() {
		return "Start a transaction";
	}

	@Override
	public String purpose() {
		return "Get the system to generate a unique transaction id.";
	}

	@Override
	public String argsDescription() {
		return "";
	}

}
