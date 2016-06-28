/**
 * Created by Zorro on 5/28 028.
 */
package me.mzorro.concurrency.bank.impl;

import me.mzorro.concurrency.bank.Bank;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockBank extends Bank {
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();
    private final Condition sufficientAmount = writeLock.newCondition();

    public ReentrantReadWriteLockBank(int n, double initialAmount) {
        super(n, initialAmount);
    }

    public boolean transfer(int from, int to, double amount) {
        // obtain lock
        writeLock.lock();
        try {
            // wait for account 'from' has sufficient amount
            while (accounts[from] < amount) {
                if (!sufficientAmount.await(5, TimeUnit.SECONDS)) {
                    return false;
                }
            }

            doTransfer(from, to, amount);
            // notify "all the other threads waiting for the condition" to recheck
            sufficientAmount.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        return true;
    }

    public double getTotalAmount() {
        readLock.lock();
        try {
            return doGetTotalAmount();
        } finally {
            readLock.unlock();
        }
    }
}
