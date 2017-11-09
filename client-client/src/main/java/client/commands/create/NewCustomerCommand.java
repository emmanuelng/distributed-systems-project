package client.commands.create;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class NewCustomerCommand extends Command {

	@Override
	public int minArgs() {
		return 1;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nAdding a new Customer using id:" + arguments.get(0) + "\n");

		int id = Integer.parseInt(arguments.get(0));
		int customerId = middleware.newCustomer(id);

		System.out.println("New customer id:" + customerId);
	}

	@Override
	public String description() {
		return "Adding a new Customer.";
	}

	@Override
	public String purpose() {
		return "Get the system to provide a new customer id. (same as adding a new customer)";
	}

	@Override
	public String argsDescription() {
		return "<id>";
	}

}
