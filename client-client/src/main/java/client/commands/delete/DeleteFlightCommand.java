package client.commands.delete;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class DeleteFlightCommand extends Command {

	@Override
	public int minArgs() {
		return 2;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nDeleting a flight using id: " + arguments.get(0));
		System.out.println("Flight Number: " + arguments.get(1) + "\n");

		int id = Integer.parseInt(arguments.get(0));
		int flightNum = Integer.parseInt(arguments.get(1));

		if (middleware.deleteFlight(id, flightNum)) {
			System.out.println("Flight deleted.");
		} else {
			System.out.println("Flight could not be deleted.");
		}
	}

	@Override
	public String description() {
		return "Deleting a flight";
	}

	@Override
	public String purpose() {
		return "Delete a flight's information.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<flightnumber>";
	}

}
