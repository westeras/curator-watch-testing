package com.westeras.curator;

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

            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                thread.join();
            }
        }
    }
}
