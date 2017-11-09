package client.commands.query;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class QueryCustomerCommand extends Command {

	@Override
	public int minArgs() {
		return 2;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("Querying Customer information using id: " + arguments.get(0));
		System.out.println("Customer id: " + arguments.get(1) + "\n");

		int id = Integer.parseInt(arguments.get(0));
		int customer = Integer.parseInt(arguments.get(1));
		String bill = middleware.queryCustomerInfo(id, customer);

		System.out.println("Customer info:" + bill);
	}

	@Override
	public String description() {
		return "Querying Customer Information.";
	}

	@Override
	public String purpose() {
		return "Obtain information about a customer.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<customerid>";
	}

}
