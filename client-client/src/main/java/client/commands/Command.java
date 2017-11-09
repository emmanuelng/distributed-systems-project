package client.commands;

import java.util.List;

import middleware.Middleware;

public abstract class Command {

	public abstract int minArgs();

	public int maxArgs() {
		return minArgs();
	}

	public String invalidArgsNbMsg() {
		return "The number of arguments provided in this command are wrong.\n"
				+ "Type help, <commandname> to check usage of this command.\n";
	}

	public abstract void execute(Middleware middleware, List<String> arguments) throws Exception;

	public abstract String description();

	public abstract String purpose();

	public abstract String argsDescription();

}
