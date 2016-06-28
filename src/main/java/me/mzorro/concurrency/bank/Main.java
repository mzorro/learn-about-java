/**
 * Created by Zorro on 3/11 011.
 */
package me.mzorro.concurrency.bank;

import me.mzorro.concurrency.bank.impl.ReentrantLockBank;
import me.mzorro.concurrency.bank.impl.ReentrantReadWriteLockBank;
import me.mzorro.concurrency.bank.impl.SynchronizedBank;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    static final int N = 10000;
    static final double INITIAL_AMOUNT = 1000.0;
    static int TRANSFER_COUNT = N;
    public static void main(String[] args) throws InterruptedException {
        Bank[] banks = new Bank[]{
                new ReentrantLockBank(N, INITIAL_AMOUNT),
                new ReentrantReadWriteLockBank(N, INITIAL_AMOUNT),
                new SynchronizedBank(N, INITIAL_AMOUNT)
        };
        for (Bank bank : banks) {
            TryBankWithLatch(bank);
            TryBankWithBarrier(bank);
        }
    }

    public static void TryBankWithLatch(final Bank bank) throws InterruptedException {
        String message = bank.getClass().getSimpleName() + " with CountDownLatch";
        System.out.println("Started " + message);
        final Random random = new Random();

        // 使用AtomicInteger来统计转账成功的数量
        final AtomicInteger successCount = new AtomicInteger();

        // 使用CountDownLatch来控制线程的结束
        final CountDownLatch finishedLatch = new CountDownLatch(TRANSFER_COUNT);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < TRANSFER_COUNT; i++) {
            final int from = i%N;
            Runnable transferJob = new Runnable() {
                public void run() {
                    try {
                        int to = random.nextInt(N);
                        while (to == from) to = random.nextInt(N); // so that to != from
                        double amount = random.nextDouble() * (INITIAL_AMOUNT/10);
                        if (bank.transfer(from, to, amount)) successCount.incrementAndGet();
                        Thread.sleep(random.nextInt(3000));

                        // 任务执行完成，使finishedLatch的计数器减一，表示有一个线程执行完毕。
                        finishedLatch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            new Thread(transferJob, String.format("thread-%05d", i)).start();
        }

        // 主线程阻塞，等待finishedLatch中的计数器为0，此时所有线程执行完毕
        finishedLatch.await();
        long duringTime = System.currentTimeMillis() - startTime;
        System.out.printf("Finished %s: cost %.03fs, %d success, %d failed\n", message,
                duringTime/1000.0, successCount.get(), TRANSFER_COUNT - successCount.get());
    }

    public static void TryBankWithBarrier(final Bank bank) throws InterruptedException {
        final String message = bank.getClass().getSimpleName() + " with CyclicBarrier";
        System.out.println("Started " + message);
        final Random random = new Random();

        // 使用AtomicInteger来统计转账成功的数量
        final AtomicInteger successCount = new AtomicInteger();
        final long startTime = System.currentTimeMillis();

        // 用CyclicBarrier来控制线程的结束
        final CyclicBarrier finishBarrier = new CyclicBarrier(TRANSFER_COUNT, new Runnable() {
            // 所有线程都达到barrier后执行的任务
            public void run() {
                long duringTime = System.currentTimeMillis() - startTime;
                System.out.printf("Finished %s: cost %.03fs, %d success, %d failed\n", message,
                        duringTime/1000.0, successCount.get(), TRANSFER_COUNT - successCount.get());
            }
        });
        for (int i = 0; i < TRANSFER_COUNT; i++) {
            final int from = i%N;
            Runnable transferJob = new Runnable() {
                public void run() {
                    try {
                        int to = random.nextInt(N);
                        while (to == from) to = random.nextInt(N); // so that to != from
                        double amount = random.nextDouble() * (INITIAL_AMOUNT/10);
                        if (bank.transfer(from, to, amount)) successCount.incrementAndGet();
                        Thread.sleep(random.nextInt(3000));

                        // 任务完成，达到了barrier。线程在这里会阻塞，直到指定数量的线程到达barrier后继续。
                        // 所以说在这里使用barrier是不那么合适的，因为线程执行完成工作之后会阻塞而不是被销毁。
                        // 这样内存中会存在大量的阻塞线程，并不高效。这里只是为了演示CyclicBarrier的功能。
                        finishBarrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            };
            new Thread(transferJob, String.format("thread-%05d", i)).start();
        }
    }
}
