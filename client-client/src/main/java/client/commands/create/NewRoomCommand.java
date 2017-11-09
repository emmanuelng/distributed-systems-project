package client.commands.create;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class NewRoomCommand extends Command {

	@Override
	public int minArgs() {
		return 4;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nAdding a new Room using id: " + arguments.get(0));
		System.out.println("Room Location: " + arguments.get(1));
		System.out.println("Add Number of Rooms: " + arguments.get(2));
		System.out.println("Set Price: " + arguments.get(3) + "\n");
		
		int id = Integer.parseInt(arguments.get(0));
		String location = arguments.get(1);
		int numRooms = Integer.parseInt(arguments.get(2));
		int price = Integer.parseInt(arguments.get(3));
		
		if (middleware.addRooms(id, location, numRooms, price)) {
			System.out.println("Rooms added.");
		} else {
			System.out.println("Rooms could not be added.");
		}
	}

	@Override
	public String description() {
		return "Adding a new Room.";
	}

	@Override
	public String purpose() {
		return "Add information about a new room location.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<location>,<numberofrooms>,<priceperroom>";
	}

}
