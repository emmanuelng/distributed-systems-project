package client.commands;

import java.util.List;

import middleware.Middleware;

public abstract class Command {

	public abstract int minArgs();

	public int maxArgs() {
		return minArgs();
	}

	public String invalidArgsNbMsg(String commandName) {
		return "Syntax error. \nCorrect usage: " + commandName + " " + argsDescription();
	}

	public abstract void execute(Middleware middleware, List<String> arguments) throws Exception;

	public abstract String description();

	public abstract String purpose();

	public abstract String argsDescription();

}
