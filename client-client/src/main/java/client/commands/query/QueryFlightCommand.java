package client.commands.query;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class QueryFlightCommand extends Command {

	@Override
	public int minArgs() {
		return 2;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nQuerying a flight using id: " + arguments.get(0));
		System.out.println("Flight number: " + arguments.get(1) + "\n");

		int id = Integer.parseInt(arguments.get(0));
		int flightNumber = Integer.parseInt(arguments.get(1));
		int seats = middleware.queryFlight(id, flightNumber);

		System.out.println("Number of seats available:" + seats);
	}

	@Override
	public String description() {
		return "Querying flight.";
	}

	@Override
	public String purpose() {
		return "Obtain Seat information about a certain flight.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<flightnumber>";
	}

}
