package com.adamwesterman.curator;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LeaderLatchAndWatchApplication {

    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            log.error("Arguments: [zk connect] [zk lock node] [zk watch node]");
            System.exit(1);
        }

        String zookeeperConnect = args[0];
        String zkLockNode = args[1];
        String zkWatchNode = args[2];

        try (ZookeeperLeadershipManager zookeeperLeadershipManager = new ZookeeperLeadershipManager(zookeeperConnect, zkLockNode, zkWatchNode)) {
            zookeeperLeadershipManager.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("received a shutdown hook, stopping leadership manager");
                try {
                    zookeeperLeadershipManager.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            while (true) {
                Thread.sleep(1000);
            }
        }
    }
}
