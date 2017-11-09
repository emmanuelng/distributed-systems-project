package client.commands.reserve;

import java.util.List;
import java.util.Vector;

import client.commands.Command;
import middleware.Middleware;

public class ReserveItineraryCommand extends Command {

	@Override
	public int minArgs() {
		return 6;
	}

	@Override
	public int maxArgs() {
		return -1;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("Reserving an Itinerary using id:" + arguments.get(0));
		System.out.println("Customer id:" + arguments.get(1));

		for (int i = 0; i < arguments.size() - 6; i++) {
			System.out.println("Flight number" + arguments.get(2 + i));
		}

		System.out.println("Location for Car/Room booking:" + arguments.get(arguments.size() - 3));
		System.out.println("Car to book?:" + arguments.get(arguments.size() - 2));
		System.out.println("Room to book?:" + arguments.get(arguments.size() - 1));

		int id = Integer.parseInt(arguments.get(0));
		int customer = Integer.parseInt(arguments.get(1));
		Vector<Integer> flightNumbers = new Vector<>();

		for (int i = 0; i < arguments.size() - 6; i++) {
			flightNumbers.add(Integer.parseInt(arguments.get(2 + i)));
		}

		String location = arguments.get(arguments.size() - 3);
		boolean car = Boolean.parseBoolean(arguments.get(arguments.size() - 2));
		boolean room = Boolean.parseBoolean(arguments.get(arguments.size() - 1));

		if (middleware.itinerary(id, customer, flightNumbers, location, car, room)) {
			System.out.println("Itinerary reserved.");
		} else {
			System.out.println("Itinerary could not be reserved.");
		}
	}

	@Override
	public String description() {
		return "Reserving an Itinerary.";
	}

	@Override
	public String purpose() {
		return "Book one or more flights.Also book zero or more cars/rooms at a location.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<customerid>,<flightnumber1>....<flightnumberN>,<LocationToBookCarsOrRooms>,<NumberOfCars>,<NumberOfRoom>";
	}

}
