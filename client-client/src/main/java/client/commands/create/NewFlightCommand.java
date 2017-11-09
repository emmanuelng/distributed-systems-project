package client.commands.create;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class NewFlightCommand extends Command {

	@Override
	public int minArgs() {
		return 4;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nAdding a new Flight using id: " + arguments.get(0));
		System.out.println("Flight number: " + arguments.get(1));
		System.out.println("Add Flight Seats: " + arguments.get(2));
		System.out.println("Set Flight Price: " + arguments.get(3) + "\n");
		
		int id = Integer.parseInt(arguments.get(0));
		int flightNum = Integer.parseInt(arguments.get(1));
		int flightSeats = Integer.parseInt(arguments.get(2));
		int flightPrice = Integer.parseInt(arguments.get(3));
		
		if (middleware.addFlight(id, flightNum, flightSeats, flightPrice)) {
			System.out.println("Flight added.");
		} else {
			System.out.println("Flight could not be added.");
		}
		
	}

	@Override
	public String description() {
		return "Adding a new Flight.";
	}

	@Override
	public String purpose() {
		return "Add information about a new flight.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<flightnumber>,<flightSeats>,<flightprice>";
	}

}
