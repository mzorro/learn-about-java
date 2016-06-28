/**
 * Created by Zorro on 5/28 028.
 */
package me.mzorro.concurrency.bank.impl;

import me.mzorro.concurrency.bank.Bank;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LinkedQueueBank extends Bank {
    protected static class Transfer {
        private int from, to;
        private double amount;
        public Transfer(int from, int to, double amount) {
            this.from = from;
            this.to = to;
            this.amount = amount;
        }
    }

    private final BlockingQueue<Transfer> transferQueue = new LinkedBlockingQueue<Transfer>();
    private final Thread worker = new Thread(new Runnable() {
        public void run() {
            try {
                while (true) {
                    Transfer transfer = transferQueue.take();
                    doTransfer(transfer.from, transfer.to, transfer.amount);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    public LinkedQueueBank(int n, double initialAmount) {
        super(n, initialAmount);
        worker.setDaemon(true);
        worker.start();
    }

    public boolean transfer(int from, int to, double amount) {
        // obtain lock
        return true;
    }

    public double getTotalAmount() {
        return doGetTotalAmount();
    }
}
