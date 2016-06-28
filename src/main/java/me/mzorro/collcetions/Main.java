package me.mzorro.collcetions;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Zorro on 3/11 011.
 */
public class Main {

    public static void main(String[] args) {
        // 集合类
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        Queue<Integer> linkedList = new LinkedList<Integer>();
        SortedSet<Integer> treeSet = new TreeSet<Integer>();
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<Integer>();

        // 非阻塞同步集合
        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<String, String>();
        ConcurrentLinkedQueue<String> concurrentLinkedQueue = new ConcurrentLinkedQueue<String>();

        // 阻塞队列
        LinkedBlockingQueue<String> linkedBlockingQueue = new LinkedBlockingQueue<String>();
        ArrayBlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<String>(10);
        PriorityBlockingQueue<String> priorityBlockingQueue = new PriorityBlockingQueue<String>();
        DelayQueue<Delayed> delayQueue = new DelayQueue<Delayed>();
        LinkedTransferQueue<String> linkedTransferQueue = new LinkedTransferQueue<String>();
        LinkedBlockingDeque<String> linkedBlockingDeque = new LinkedBlockingDeque<String>();


        // 线程池
        Executors.newCachedThreadPool();
        Executors.newFixedThreadPool(100);
        Executors.newSingleThreadExecutor();

        Executors.newScheduledThreadPool(100);
        Executors.newSingleThreadScheduledExecutor();

        ForkJoinPool forkJoinPool = new ForkJoinPool();
    }
}
