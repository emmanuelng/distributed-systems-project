package client.commands.transactions;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class PrepareCommand extends Command {

	@Override
	public int minArgs() {
		return 1;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		int id = Integer.parseInt(arguments.get(0));

		if (middleware.prepare(id)) {
			System.out
					.println("The transaction is ready for commit.\nUse the commit command to commit the transaction");
		} else {
			System.out.println("The transaction cannot commit now. Please try later.");
		}
	}

	@Override
	public String description() {
		return "Initiates the first phase of the two phase commit (2PC)";
	}

	@Override
	public String purpose() {
		return "Verifies that the transaction can commit before actually commiting it.";
	}

	@Override
	public String argsDescription() {
		return "<id>";
	}

}
