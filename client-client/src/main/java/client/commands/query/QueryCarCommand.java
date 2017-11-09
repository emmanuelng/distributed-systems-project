package client.commands.query;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class QueryCarCommand extends Command {

	@Override
	public int minArgs() {
		return 2;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nQuerying a car location using id: " + arguments.get(0));
		System.out.println("Car location: " + arguments.get(1) + "\n");

		int id = Integer.parseInt(arguments.get(0));
		String location = arguments.get(1);
		int numCars = middleware.queryCars(id, location);

		System.out.println("number of Cars at this location:" + numCars);
	}

	@Override
	public String description() {
		return "Querying a car location.";
	}

	@Override
	public String purpose() {
		return "Obtain number of cars at a certain car location.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<location>";
	}
}
