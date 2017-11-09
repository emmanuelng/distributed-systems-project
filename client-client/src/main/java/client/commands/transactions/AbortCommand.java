package client.commands.transactions;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class AbortCommand extends Command {

	@Override
	public int minArgs() {
		return 1;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		int id = Integer.parseInt(arguments.get(0));

		if (middleware.abort(id)) {
			System.out.println("Transaction " + id + " aborted sucessfully.");
		} else {
			System.out.println("Transaction " + id + " could not abort.");
		}
	}

	@Override
	public String description() {
		return "Abort a transaction";
	}

	@Override
	public String purpose() {
		return "Cancel all operations of the transaction and close it";
	}

	@Override
	public String argsDescription() {
		return "<id>";
	}

}
