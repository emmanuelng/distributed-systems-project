package client.commands.query;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class QueryFlightPriceCommand extends Command {

	@Override
	public int minArgs() {
		return 2;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nQuerying a flight Price using id: " + arguments.get(0));
		System.out.println("Flight number: " + arguments.get(1) + "\n");

		int id = Integer.parseInt(arguments.get(0));
		int flightNumber = Integer.parseInt(arguments.get(1));
		int price = middleware.queryFlightPrice(id, flightNumber);

		System.out.println("Price of a seat:" + price);
	}

	@Override
	public String description() {
		return "Querying flight price.";
	}

	@Override
	public String purpose() {
		return "Obtain price information about a certain flight.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<flightnumber>";
	}

}
