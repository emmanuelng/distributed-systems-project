package client;

import java.util.Hashtable;
import java.util.Map;

import client.commands.Command;
import client.commands.HelpCommand;
import client.commands.QuitCommand;
import client.commands.create.NewCarCommand;
import client.commands.create.NewCustomerCommand;
import client.commands.create.NewFlightCommand;
import client.commands.create.NewRoomCommand;
import client.commands.delete.DeleteCarCommand;
import client.commands.delete.DeleteCustomerCommand;
import client.commands.delete.DeleteFlightCommand;
import client.commands.delete.DeleteRoomCommand;
import client.commands.query.QueryCarCommand;
import client.commands.query.QueryCarPriceCommand;
import client.commands.query.QueryCustomerCommand;
import client.commands.query.QueryFlightCommand;
import client.commands.query.QueryFlightPriceCommand;
import client.commands.query.QueryRoomCommand;
import client.commands.query.QueryRoomPriceCommand;
import client.commands.reserve.ReserveCarCommand;
import client.commands.reserve.ReserveFlightCommand;
import client.commands.reserve.ReserveRoomCommand;
import client.commands.transactions.AbortCommand;
import client.commands.transactions.CommitCommand;
import client.commands.transactions.StartCommand;

public class CommandRegistry {

	public static final Map<String, Command> COMMANDS = new Hashtable<>();

	public static void initialize() {
		COMMANDS.put("help", new HelpCommand());
		COMMANDS.put("quit", new QuitCommand());

		COMMANDS.put("newflight", new NewFlightCommand());
		COMMANDS.put("newcar", new NewCarCommand());
		COMMANDS.put("newroom", new NewRoomCommand());
		COMMANDS.put("newcustomer", new NewCustomerCommand());

		COMMANDS.put("deleteflight", new DeleteFlightCommand());
		COMMANDS.put("deletecar", new DeleteCarCommand());
		COMMANDS.put("deleteroom", new DeleteRoomCommand());
		COMMANDS.put("deletecustomer", new DeleteCustomerCommand());

		COMMANDS.put("queryflight", new QueryFlightCommand());
		COMMANDS.put("querycar", new QueryCarCommand());
		COMMANDS.put("queryroom", new QueryRoomCommand());
		COMMANDS.put("querycustomer", new QueryCustomerCommand());
		COMMANDS.put("queryflightprice", new QueryFlightPriceCommand());
		COMMANDS.put("querycarprice", new QueryCarPriceCommand());
		COMMANDS.put("queryroomprice", new QueryRoomPriceCommand());

		COMMANDS.put("reserveflight", new ReserveFlightCommand());
		COMMANDS.put("reservecar", new ReserveCarCommand());
		COMMANDS.put("reserveroom", new ReserveRoomCommand());

		COMMANDS.put("start", new StartCommand());
		COMMANDS.put("commit", new CommitCommand());
		COMMANDS.put("abort", new AbortCommand());
	}

}
