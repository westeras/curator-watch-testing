package com.westeras.curator;

import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZookeeperAutoWriterApplication {

    public static void main(String[] args) {

        if (args.length < 2) {
            log.error("Arguments: [zk connect] [zk watch node]");
            System.exit(1);
        }

        String zkConnect = args[0];
        String zkWatchNode = args[1];

        Runnable zookeeperAutoWriter = new ZookeeperAutoWriterRunnable(zkConnect, zkWatchNode);

        Executors.newSingleThreadExecutor().submit(zookeeperAutoWriter);
    }
}
