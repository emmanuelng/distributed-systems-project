package common.locks;

class LockManagerTest {
	public static void main(String[] args) {
		MyThread t1, t2;
		LockManager lm = new LockManager();
		t1 = new MyThread(lm, 1);
		t2 = new MyThread(lm, 2);
		t1.start();
		t2.start();
	}
}

class MyThread extends Thread {
	LockManager lm;
	int threadId;

	public MyThread(LockManager lm, int threadId) {
		this.lm = lm;
		this.threadId = threadId;
	}

	public void run() {
		if (threadId == 1) {
			try {
				lm.lock(1, "a", LockManager.READ);
			} catch (DeadlockException e) {
				System.out.println("Deadlock.... ");
			}

			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
			}

			try {
				lm.lock(1, "b", LockManager.WRITE);
			} catch (DeadlockException e) {
				System.out.println("Deadlock.... ");
			}

			lm.unlockAll(1);
		} else if (threadId == 2) {
			try {
				lm.lock(2, "b", LockManager.READ);
			} catch (DeadlockException e) {
				System.out.println("Deadlock.... ");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			try {
				lm.lock(2, "a", LockManager.WRITE);
			} catch (DeadlockException e) {
				System.out.println("Deadlock.... ");
			}

			lm.unlockAll(2);
		}
	}
}