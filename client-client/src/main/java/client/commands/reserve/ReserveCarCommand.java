package client.commands.reserve;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class ReserveCarCommand extends Command {

	@Override
	public int minArgs() {
		return 3;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("Reserving a car at a location using id: " + arguments.get(0));
		System.out.println("Customer id: " + arguments.get(1));
		System.out.println("Location: " + arguments.get(2) + "\n");
		
		int id = Integer.parseInt(arguments.get(0));
		int customer = Integer.parseInt(arguments.get(1));
		String location = arguments.get(2);
		
		if (middleware.reserveCar(id, customer, location)) {
			System.out.println("Car reserved.");
		} else {
			System.out.println("Car could not be reserved.");
		}
	}

	@Override
	public String description() {
		return "Reserving a Car.";
	}

	@Override
	public String purpose() {
		return "Reserve a given number of cars for a customer at a particular location.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<customerid>,<location>,<nummberofCars>";
	}

}
