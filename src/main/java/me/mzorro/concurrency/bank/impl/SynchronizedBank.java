/**
 * Created by Zorro on 5/28 028.
 */
package me.mzorro.concurrency.bank.impl;

import me.mzorro.concurrency.bank.Bank;

public class SynchronizedBank extends Bank {

    public SynchronizedBank(int n, double initialAmount) {
        super(n, initialAmount);
    }

    public synchronized boolean transfer(int from, int to, double amount) {
        try {
            // wait for account 'from' has sufficient amount
            int retries = 5;
            while (retries-- > 0 && accounts[from] < amount) {
                wait(1000);
            }
            if (accounts[from] < amount) return false;
            doTransfer(from, to, amount);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public synchronized double getTotalAmount() {
        return doGetTotalAmount();
    }
}
