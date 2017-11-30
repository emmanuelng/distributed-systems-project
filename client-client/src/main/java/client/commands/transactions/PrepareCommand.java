package client.commands.transactions;

import java.util.List;
import java.util.Scanner;

import client.commands.Command;
import client.helpers.UnclosableInputStream;
import middleware.Middleware;

public class PrepareCommand extends Command {

	@Override
	public int minArgs() {
		return 1;
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) throws Exception {
		int id = Integer.parseInt(arguments.get(0));
		Scanner scanner = new Scanner(new UnclosableInputStream(System.in));

		if (middleware.prepare(id)) {
			System.out.print("Prepare succeeded. Commit?(Y/N) ");
			String answer = scanner.nextLine();

			while (!answer.equals("Y") && !answer.equals("N")) {
				System.out.print("Please enter Y or N: ");
				answer = scanner.nextLine();
			}

			if (answer.equals("Y")) {
				if (middleware.commit(id)) {
					System.out.println("Committed!");
				} else {
					System.out.println("Could not commit. Please try again.");
				}
			}

		} else {
			System.out.print("Prepare failed. Retry?(Y/N) ");
			String answer = scanner.nextLine();

			while (!answer.equals("Y") && !answer.equals("N")) {
				System.out.print("Please enter Y or N: ");
				answer = scanner.nextLine();
			}

			if (answer.equals("Y")) {
				execute(middleware, arguments);
			}

		}

		scanner.close();
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
