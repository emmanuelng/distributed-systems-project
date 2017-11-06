package common.locks;

import java.util.BitSet;
import java.util.Vector;

public class LockManager {

	public static final int READ = 0;
	public static final int WRITE = 1;

	private static int TABLE_SIZE = 2039;
	private static int DEADLOCK_TIMEOUT = 10000;

	private static TPHashTable lockTable = new TPHashTable(LockManager.TABLE_SIZE);
	private static TPHashTable stampTable = new TPHashTable(LockManager.TABLE_SIZE);
	private static TPHashTable waitTable = new TPHashTable(LockManager.TABLE_SIZE);

	public LockManager() {
		super();
	}

	public boolean lock(int xid, String strData, int lockType) throws DeadlockException {

		// If any parameter is invalid, then return false
		if (xid < 0 || strData == null) {
			return false;
		}

		if ((lockType != TrxnObj.READ) && (lockType != TrxnObj.WRITE)) {
			return false;
		}

		// Two objects in lock table for easy lookup.
		TrxnObj trxnObj = new TrxnObj(xid, strData, lockType);
		DataObj dataObj = new DataObj(xid, strData, lockType);

		// Return true when there is no lock conflict or throw a deadlock exception.
		try {
			boolean bConflict = true;
			BitSet bConvert = new BitSet(1);
			while (bConflict) {
				synchronized (LockManager.lockTable) {
					// check if this lock request conflicts with existing locks
					bConflict = lockConflict(dataObj, bConvert);
					if (!bConflict) {
						// no lock conflict
						synchronized (LockManager.stampTable) {
							// remove the time stamp (if any) for this lock request
							TimeObj timeObj = new TimeObj(xid);
							LockManager.stampTable.remove(timeObj);
						}
						synchronized (LockManager.waitTable) {
							// remove the entry for this transaction from waitTable (if it
							// is there) as it has been granted its lock request
							WaitObj waitObj = new WaitObj(xid, strData, lockType);
							LockManager.waitTable.remove(waitObj);
						}

						if (bConvert.get(0) == true) {
							// lock conversion
							// *** ADD CODE HERE *** to carry out the lock conversion in the
							// lock table
						} else {
							// a lock request that is not lock conversion
							LockManager.lockTable.add(trxnObj);
							LockManager.lockTable.add(dataObj);
						}
					}
				}
				if (bConflict) {
					// lock conflict exists, wait
					WaitLock(dataObj);
				}
			}
		} catch (DeadlockException deadlock) {
			throw deadlock;
		} catch (RedundantLockRequestException redundantlockrequest) {
			// just ignore the redundant lock request
			return true;
		}

		return true;
	}

	// remove all locks for this transaction in the lock table.
	public boolean UnlockAll(int xid) {

		// if any parameter is invalid, then return false
		if (xid < 0) {
			return false;
		}

		TrxnObj trxnQueryObj = new TrxnObj(xid, "", -1); // Only used in elements() call below.
		synchronized (LockManager.lockTable) {
			Vector<XObj> vect = LockManager.lockTable.elements(trxnQueryObj);

			TrxnObj trxnObj;
			Vector<XObj> waitVector;
			WaitObj waitObj;
			int size = vect.size();

			for (int i = (size - 1); i >= 0; i--) {

				trxnObj = (TrxnObj) vect.elementAt(i);
				LockManager.lockTable.remove(trxnObj);

				DataObj dataObj = new DataObj(trxnObj.getXId(), trxnObj.getDataName(), trxnObj.getLockType());
				LockManager.lockTable.remove(dataObj);

				// check if there are any waiting transactions.
				synchronized (LockManager.waitTable) {
					// get all the transactions waiting on this dataObj
					waitVector = LockManager.waitTable.elements(dataObj);
					int waitSize = waitVector.size();
					for (int j = 0; j < waitSize; j++) {
						waitObj = (WaitObj) waitVector.elementAt(j);
						if (waitObj.getLockType() == LockManager.WRITE) {
							if (j == 0) {
								// get all other transactions which have locks on the
								// data item just unlocked.
								Vector<XObj> vect1 = LockManager.lockTable.elements(dataObj);

								// remove interrupted thread from waitTable only if no
								// other transaction has locked this data item
								if (vect1.size() == 0) {
									LockManager.waitTable.remove(waitObj);

									try {
										synchronized (waitObj.getThread()) {
											waitObj.getThread().notify();
										}
									} catch (Exception e) {
										System.out.println("Exception on unlock\n" + e.getMessage());
									}
								} else {
									// some other transaction still has a lock on
									// the data item just unlocked. So, WRITE lock
									// cannot be granted.
									break;
								}
							}

							// stop granting READ locks as soon as you find a WRITE lock
							// request in the queue of requests
							break;
						} else if (waitObj.getLockType() == LockManager.READ) {
							// remove interrupted thread from waitTable.
							LockManager.waitTable.remove(waitObj);

							try {
								synchronized (waitObj.getThread()) {
									waitObj.getThread().notify();
								}
							} catch (Exception e) {
								System.out.println("Exception e\n" + e.getMessage());
							}
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 * Returns true if the lock request on dataObj conflicts with already existing
	 * locks. If the lock request is a redundant one (for eg: if a transaction holds
	 * a read lock on certain data item and again requests for a read lock), then
	 * this is ignored. This is done by throwing RedundantLockRequestException which
	 * is handled appropriately by the caller. If the lock request is a conversion
	 * from READ lock to WRITE lock, then bitset is set.
	 */
	private boolean lockConflict(DataObj dataObj, BitSet bitset)
			throws DeadlockException, RedundantLockRequestException {
		Vector<XObj> vect = LockManager.lockTable.elements(dataObj);
		DataObj dataObj2;
		int size = vect.size();

		// As soon as a lock that conflicts with the current lock request is found,
		// return true
		for (int i = 0; i < size; i++) {
			dataObj2 = (DataObj) vect.elementAt(i);
			if (dataObj.getXId() == dataObj2.getXId()) {
				// the transaction already has a lock on this data item which means that it is
				// either re locking it or is converting the lock
				if (dataObj.getLockType() == DataObj.READ) {
					// Since transaction already has a lock (may be READ, may be WRITE. we don't
					// care) on this data item and it is requesting a READ lock, this lock request
					// is redundant.
					throw new RedundantLockRequestException(dataObj.getXId(), "Redundant READ lock request");
				} else if (dataObj.getLockType() == DataObj.WRITE) {
					// transaction already has a lock and is requesting a WRITE lock
					// now there are two cases to analyze here
					// (1) transaction already had a READ lock
					// (2) transaction already had a WRITE lock
					// Seeing the comments at the top of this function might be helpful
					// *** ADD CODE HERE *** to take care of both these cases
				}
			} else {
				if (dataObj.getLockType() == DataObj.READ) {
					if (dataObj2.getLockType() == DataObj.WRITE) {
						// transaction is requesting a READ lock and some other transaction
						// already has a WRITE lock on it ==> conflict
						System.out.println("Want READ, someone has WRITE");
						return true;
					}
				} else if (dataObj.getLockType() == DataObj.WRITE) {
					// transaction is requesting a WRITE lock and some other transaction has either
					// a READ or a WRITE lock on it ==> conflict
					System.out.println("Want WRITE, someone has READ or WRITE");
					return true;
				}
			}
		}

		// no conflicting lock found, return false
		return false;

	}

	private void WaitLock(DataObj dataObj) throws DeadlockException {
		// Check timestamp or add a new one.
		// Will always add new timestamp for each new lock request since
		// the timeObj is deleted each time the transaction succeeds in
		// getting a lock (see Lock() )

		TimeObj timeObj = new TimeObj(dataObj.getXId());
		TimeObj timestamp = null;
		long timeBlocked = 0;
		Thread thisThread = Thread.currentThread();
		WaitObj waitObj = new WaitObj(dataObj.getXId(), dataObj.getDataName(), dataObj.getLockType(), thisThread);

		synchronized (LockManager.stampTable) {
			Vector<XObj> vect = LockManager.stampTable.elements(timeObj);
			if (vect.size() == 0) {
				// add the time stamp for this lock request to stampTable
				LockManager.stampTable.add(timeObj);
				timestamp = timeObj;
			} else if (vect.size() == 1) {
				// lock operation could have timed out; check for deadlock
				TimeObj prevStamp = (TimeObj) vect.firstElement();
				timestamp = prevStamp;
				timeBlocked = timeObj.getTime() - prevStamp.getTime();
				if (timeBlocked >= LockManager.DEADLOCK_TIMEOUT) {
					// the transaction has been waiting for a period greater than the timeout period
					cleanupDeadlock(prevStamp, waitObj);
				}
			} else {
				// should never get here. shouldn't be more than one time stamp per transaction
				// because a transaction at a given time the transaction can be blocked on just
				// one lock
				// request.
			}
		}

		// suspend thread and wait until notified...

		synchronized (LockManager.waitTable) {
			if (!LockManager.waitTable.contains(waitObj)) {
				// register this transaction in the waitTable if it is not already there
				LockManager.waitTable.add(waitObj);
			} else {
				// else lock manager already knows the transaction is waiting.
			}
		}

		synchronized (thisThread) {
			try {
				thisThread.wait(LockManager.DEADLOCK_TIMEOUT - timeBlocked);
				TimeObj currTime = new TimeObj(dataObj.getXId());
				timeBlocked = currTime.getTime() - timestamp.getTime();
				if (timeBlocked >= LockManager.DEADLOCK_TIMEOUT) {
					// the transaction has been waiting for a period greater than the timeout period
					cleanupDeadlock(timestamp, waitObj);
				} else {
					return;
				}
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted?");
			}
		}
	}

	// cleanupDeadlock cleans up stampTable and waitTable, and throws
	// DeadlockException
	private void cleanupDeadlock(TimeObj tmObj, WaitObj waitObj) throws DeadlockException {
		synchronized (LockManager.stampTable) {
			synchronized (LockManager.waitTable) {
				LockManager.stampTable.remove(tmObj);
				LockManager.waitTable.remove(waitObj);
			}
		}
		throw new DeadlockException(waitObj.getXId(), "Sleep timeout...deadlock.");
	}
}
