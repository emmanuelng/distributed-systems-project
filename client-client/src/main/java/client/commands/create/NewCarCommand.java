package client.commands.create;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class NewCarCommand extends Command {

	@Override
	public int minArgs() {
		return 4;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nAdding a new Car using id: " + arguments.get(0));
		System.out.println("Car Location: " + arguments.get(1));
		System.out.println("Add Number of Cars: " + arguments.get(2));
		System.out.println("Set Price: " + arguments.get(3) + "\n");

		int id = Integer.parseInt(arguments.get(0));
		String location = arguments.get(1);
		int numCars = Integer.parseInt(arguments.get(2));
		int price = Integer.parseInt(arguments.get(3));

		if (middleware.addCars(id, location, numCars, price)) {
			System.out.println("Car added.");
		} else {
			System.out.println("Car could not be added.");
		}
	}

	@Override
	public String description() {
		return "Adding a new Car.";
	}

	@Override
	public String purpose() {
		return "Add information about a new car location.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<location>,<numberofcars>,<pricepercar>";
	}

}
