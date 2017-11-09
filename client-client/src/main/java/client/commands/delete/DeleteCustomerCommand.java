package client.commands.delete;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class DeleteCustomerCommand extends Command {

	@Override
	public int minArgs() {
		return 2;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		System.out.println("\nDeleting a customer from the database using id: " + arguments.get(0));
		System.out.println("Customer id: " + arguments.get(1) + "\n");

		int id = Integer.parseInt(arguments.get(0));
		int customer = Integer.parseInt(arguments.get(1));

		if (middleware.deleteCustomer(id, customer)) {
			System.out.println("Customer deleted.");
		} else {
			System.out.println("Customer could not be deleted.");
		}
	}

	@Override
	public String description() {
		return "Deleting a Customer";
	}

	@Override
	public String purpose() {
		return "Remove a customer from the database.";
	}

	@Override
	public String argsDescription() {
		return "<id>,<customerid>";
	}

}
