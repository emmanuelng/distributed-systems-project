package middleware.impl.debug;

public class CrashInjector {

	public static enum When {
		BEFORE, IN, AFTER
	}

	public static enum Operation {
		PREPARE, DECISION
	}

	private When when;
	private Operation operation;

	public CrashInjector() {
		this.when = null;
		this.operation = null;
	}

	public boolean inject(String when, String operation) {
		try {
			this.when = Enum.valueOf(When.class, when.toUpperCase());
			this.operation = Enum.valueOf(Operation.class, operation.toUpperCase());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void beforePrepare() {
		if (when == When.BEFORE && operation == Operation.PREPARE) {
			crash();
		}

	}

	public void inPrepare(int received, int over) {
		if (when == When.IN && operation == Operation.PREPARE) {
			if (received == Math.ceil(over / 2)) {
				crash();
			}
		}
	}

	public void afterPrepare() {
		if (when == When.AFTER && operation == Operation.PREPARE) {
			crash();
		}
	}

	public void beforeDecision() {
		if (when == When.BEFORE && operation == Operation.DECISION) {
			crash();
		}
	}

	public void inDecision(int received, int over) {
		if (when == When.IN && operation == Operation.DECISION) {
			if (received == Math.ceil(over / 2)) {
				crash();
			}
		}
	}

	public void afterDecision() {
		if (when == When.AFTER && operation == Operation.DECISION) {
			crash();
		}
	}

	private void crash() {
		System.out.println("[CrashInjector] Crashing server " + when + " " + operation);
		System.exit(1);
	}

}
