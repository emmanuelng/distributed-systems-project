package client.commands.delete;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class DeleteRoomCommand extends Command {

	@Override
	public int minArgs() {
		return 2;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nDeleting all rooms from a particular location  using id: " + arguments.get(0));
		System.out.println("Room Location: " + arguments.get(1) + "\n");

		int id = Integer.parseInt(arguments.get(0));
		String location = arguments.get(1);

		if (middleware.deleteRooms(id, location)) {
			System.out.println("Rooms deleted.");
		} else {
			System.out.println("Rooms could not be deleted.");
		}
	}

	@Override
	public String description() {
		return "Deleting a Room";
	}

	@Override
	public String purpose() {
		return "Delete all rooms from a location.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<location>,<numRooms>";
	}

}
