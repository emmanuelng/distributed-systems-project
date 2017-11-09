package client.commands.reserve;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class ReserveRoomCommand extends Command {

	@Override
	public int minArgs() {
		return 3;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("Reserving a room at a location using id: " + arguments.get(0));
		System.out.println("Customer id: " + arguments.get(1));
		System.out.println("Location: " + arguments.get(2));

		int id = Integer.parseInt(arguments.get(0));
		int customer = Integer.parseInt(arguments.get(1));
		String location = arguments.get(2);

		if (middleware.reserveRoom(id, customer, location)) {
			System.out.println("Room reserved.");
		} else {
			System.out.println("Room could not be reserved");
		}

	}

	@Override
	public String description() {
		return "Reserving a Room.";
	}

	@Override
	public String purpose() {
		return "Reserve a given number of rooms for a customer at a particular location.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<customerid>,<location>,<nummberofRooms>";
	}

}
