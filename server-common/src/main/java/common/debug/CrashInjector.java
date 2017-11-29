package common.debug;

public class CrashInjector {

	public static enum When {
		BEFORE, AFTER
	}

	public static enum Operation {
		VOTE, SAVE
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
			return false;
		}
	}

	public void beforeVote() {
		if (when == When.BEFORE && operation == Operation.VOTE) {
			crash();
		}
	}

	public void afterVote() {
		if (when == When.AFTER && operation == Operation.VOTE) {
			crash();
		}
	}

	public void beforeSave() {
		if (when == When.BEFORE && operation == Operation.SAVE) {
			crash();
		}
	}

	private void crash() {
		System.out.println("[CrashInjector] Crashing server " + when + " " + operation);
		System.exit(1);
	}

}
