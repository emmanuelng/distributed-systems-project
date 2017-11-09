package client.commands.query;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class QueryRoomCommand extends Command {

	@Override
	public int minArgs() {
		return 2;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nQuerying a room location using id: " + arguments.get(0));
		System.out.println("Room location: " + arguments.get(1) + "\n");

		int id = Integer.parseInt(arguments.get(0));
		String location = arguments.get(1);
		int numRooms = middleware.queryRooms(id, location);

		System.out.println("Number of Rooms at this location:" + numRooms);
	}

	@Override
	public String description() {
		return "Querying a room location.";
	}

	@Override
	public String purpose() {
		return "Obtain number of rooms at a certain room location.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<location>";
	}

}
