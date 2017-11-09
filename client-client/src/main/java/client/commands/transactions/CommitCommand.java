package client.commands.transactions;

import java.util.List;

import client.commands.Command;
import middleware.Middleware;

public class CommitCommand extends Command {

	@Override
	public int minArgs() {
		return 1;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		int id = Integer.parseInt(arguments.get(0));
		
		if (middleware.commit(id)) {
			System.out.println("Transaction " + id + " committed successfully.");
		} else {
			System.out.println("Transaction " + id + " could not commit.");
		}
	}

	@Override
	public String description() {
		return "Commit a transaction";
	}

	@Override
	public String purpose() {
		return "Close a transction and save the data";
	}

	@Override
	public String argsDescription() {
		return "<id>";
	}

}
