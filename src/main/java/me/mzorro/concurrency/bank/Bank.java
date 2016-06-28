/**
 * Created by Zorro on 5/28 028.
 */
package me.mzorro.concurrency.bank;

import java.util.Arrays;

public abstract class Bank {
    protected final double[] accounts;
    public Bank(int n, double initialAmount) {
        this.accounts = new double[n];
        Arrays.fill(accounts, initialAmount);
    }

    protected final void doTransfer(int from, int to, double amount) {
        // we assume accounts[from] >= amount
        //System.out.printf("%s %05.2f from %02d to %02d", Thread.currentThread().getName(), amount, from, to);
        accounts[from] -= amount;
        accounts[to] += amount;
        //System.out.printf(" total: %8.2f\n", getTotalAmount());
    }

    public abstract boolean transfer(int from, int to, double amount);

    protected final double doGetTotalAmount() {
        double sum = 0;
        for (double m : accounts) {
            sum += m;
        }
        return sum;
    }
    protected abstract double getTotalAmount();
}
