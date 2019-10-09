package com.westeras.curator;

import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZookeeperAutoWriterApplication {
    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            log.error("Arguments: [zk connect] [zk watch node]");
            System.exit(1);
        }

        String zkConnect = args[0];
        String zkWatchNode = args[1];

        log.info("Arguments passed: zk connect: {}, zk watch node: {}", zkConnect, zkWatchNode);

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            log.error("thread interrupted while sleeping", e);
        }

        Runnable zookeeperAutoWriter = new ZookeeperAutoWriterRunnable(zkConnect, zkWatchNode);
        Executors.newFixedThreadPool(1).submit(zookeeperAutoWriter);
    }
}
