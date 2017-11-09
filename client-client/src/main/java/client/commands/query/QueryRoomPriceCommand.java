package client.commands.query;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class QueryRoomPriceCommand extends Command {

	@Override
	public int minArgs() {
		return 2;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("Querying a room price using id: " + arguments.get(0));
		System.out.println("Room Location: " + arguments.get(1) + "\n");
		
		int id = Integer.parseInt(arguments.get(0));
		String location = arguments.get(1);
		int price = middleware.queryRoomsPrice(id, location);
		
		System.out.println("Price of Rooms at this location:" + price);
	}

	@Override
	public String description() {
		return "Querying a room price at a location.";
	}

	@Override
	public String purpose() {
		return "Obtain price information about a certain room location.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<location>";
	}

}
