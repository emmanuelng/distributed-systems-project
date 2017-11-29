package client.commands.debug;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class InjectCrashCommand extends Command {

	@Override
	public int minArgs() {
		return 3;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		String where = arguments.get(0);
		String when = arguments.get(1);
		String operation = arguments.get(2);

		if (middleware.injectCrash(where, when, operation)) {
			System.out.println("Crash was injected successfully!");
		} else {
			System.out.println("Invalid arguments");
		}

	}

	@Override
	public String description() {
		return "Inject a crash";
	}

	@Override
	public String purpose() {
		return "Adds a crash at a specific place. When this place is reached, the system is stopped.";
	}

	@Override
	public String argsDescription() {
		return "{middleware [before|in|after] [prepare|decision]} or "
				+ "{[cars|flights|hotels|customers] [before|after] [vote|save]}";
	}

}
