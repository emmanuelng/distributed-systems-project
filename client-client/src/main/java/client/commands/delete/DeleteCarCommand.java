package client.commands.delete;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class DeleteCarCommand extends Command {

	@Override
	public int minArgs() {
		return 2;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nDeleting the cars from a particular location using id: " + arguments.get(0));
		System.out.println("Car Location: " + arguments.get(1));
		
		int id = Integer.parseInt(arguments.get(0));
		String location = arguments.get(1);
		
		if (middleware.deleteCars(id, location)) {
			System.out.println("Cars deleted.");
		} else {
			System.out.println("Cars could not be deleted.");
		}
	}

	@Override
	public String description() {
		return "Deleting a Car";
	}

	@Override
	public String purpose() {
		return "Delete all cars from a location.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<location>,<numCars>";
	}

}
