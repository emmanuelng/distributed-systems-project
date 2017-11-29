package client.commands.transactions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import client.commands.Command;
import client.helpers.UnclosableInputStream;
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
			Scanner scanner = new Scanner(new UnclosableInputStream(System.in));

			System.out.print("Commit failed. Abort transaction? (Y/N) ");
			String answer = scanner.nextLine();

			while (!answer.equals("Y") && !answer.equals("N")) {
				System.out.print("Please enter Y or N: ");
				answer = scanner.nextLine();
			}

			scanner.close();

			if (answer.equals("Y")) {
				if (middleware.abort(id)) {
					System.out.println("The transaction was aborted.");
				} else {
					System.out.println("The transaction could not abort.");
				}
			}
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
