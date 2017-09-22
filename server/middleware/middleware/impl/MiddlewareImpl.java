package middleware.impl;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import middleware.Middleware;

@SuppressWarnings("deprecation")
public class MiddlewareImpl implements Middleware {

	public static void main(String[] args) {
		// Figure out where server is running
		String server = "localhost";
		int port = 1099;

		if (args.length == 1) {
			server = server + ":" + args[0];
			port = Integer.parseInt(args[0]);
		} else if (args.length != 0 && args.length != 1) {
			System.err.println("Wrong usage");
			System.exit(1);
		}

		try {
			// Create a new server object and dynamically generate the stub (client proxy)
			MiddlewareImpl obj = new MiddlewareImpl();
			Middleware rm = (Middleware) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry(port);
			registry.rebind("middleware.group20", rm);

			System.out.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}

		// Create and install a security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int newCustomer(int id) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean newCustomer(int id, int cid) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteRooms(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCustomer(int id, int customer) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String queryCustomerInfo(int id, int customer) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean reserveFlight(int id, int customer, int flightNumber) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveCar(int id, int customer, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveRoom(int id, int customer, String locationd) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean itinerary(int id, int customer, Vector flightNumbers, String location, boolean Car, boolean Room)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

}
