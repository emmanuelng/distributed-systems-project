package client.commands;

import java.util.List;

import client.CommandRegistry;
import middleware.Middleware;

public class HelpCommand extends Command {

	@Override
	public int minArgs() {
		return 0;
	}

	@Override
	public int maxArgs() {
		return 1;
	}

	@Override
	public String invalidArgsNbMsg(String commandName) {
		return "Improper use of help command. Type help or help, <commandname>";
	}

	@Override
	public void execute(Middleware middleware, List<String> arguments) {
		if (arguments.size() == 0) {
			listAll();
		} else if (arguments.size() == 1) {
			listSpecific(arguments.get(0));
		}
	}

	private void listAll() {
		System.out.println("\nWelcome to the client interface provided to test your project.");
		System.out.println("Commands accepted by the interface are:\n");

		for (String commandName : CommandRegistry.COMMANDS.keySet()) {
			System.out.println("- " + commandName + ": " + CommandRegistry.COMMANDS.get(commandName).description());
		}

		System.out.println("\nType help, <commandname> for detailed info (NOTE the use of comma).");
	}

	private void listSpecific(String commandName) {
		if (CommandRegistry.COMMANDS.containsKey(commandName)) {
			Command cmd = CommandRegistry.COMMANDS.get(commandName);

			System.out.println("\nHelp on: " + commandName);
			System.out.println("\t" + cmd.description() + "\n");
			System.out.println("Purpose:");
			System.out.println("\t" + cmd.purpose() + "\n");
			System.out.println("Usage");
			System.out.println("\t" + commandName + " " + cmd.argsDescription() + "\n");
		} else {
			System.out.println(commandName);
			System.out.println("The interface does not support this command.");
		}
	}

	@Override
	public String description() {
		return "Get help";
	}

	@Override
	public String purpose() {
		return null;
	}

	@Override
	public String argsDescription() {
		return null;
	}

}
