package client.commands.reserve;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class ReserveFlightCommand extends Command {

	@Override
	public int minArgs() {
		return 3;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nReserving a seat on a flight using id: " + arguments.get(0));
		System.out.println("Customer id: " + arguments.get(1));
		System.out.println("Flight number: " + arguments.get(2) + "\n");
		
		int id = Integer.parseInt(arguments.get(0));
		int customer = Integer.parseInt(arguments.get(1));
		int flightNumber = Integer.parseInt(arguments.get(2));
		
		if (middleware.reserveFlight(id, customer, flightNumber)) {
			System.out.println("Flight reserved");
		} else {
			System.out.println("Flight could not be reserved");
		}
	}

	@Override
	public String description() {
		return "Reserving a flight.";
	}

	@Override
	public String purpose() {
		return "Reserve a flight for a customer.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<customerid>,<flightnumber>";
	}

}
