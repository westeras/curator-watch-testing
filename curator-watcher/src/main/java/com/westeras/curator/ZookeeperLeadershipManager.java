package com.westeras.curator;

import java.io.Closeable;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;

import lombok.extern.slf4j.Slf4j;

/*
 * This class encapsulates a Curator Leader Selector, a Curator ZK client, and a Curator ZK Node Watch Utility (PathChildrenCache)
 * An instance of this class locks on a given ZK lock node, and if elected leader, reacts to changes on a given ZK watch node
 */
@Slf4j
public class ZookeeperLeadershipManager extends LeaderSelectorListenerAdapter implements Closeable {

    private final CuratorFramework client;
    private final LeaderSelector leaderSelector;
    private final PathChildrenCache cache;

    private final String hostname;

    public ZookeeperLeadershipManager(String zkConnect, String zkLockNode, String zkWatchNode) throws UnknownHostException {

        // setup Curator ZK client
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.newClient(zkConnect, retryPolicy);

        // setup ZK leader selector
        this.leaderSelector = new LeaderSelector(client, zkLockNode, this);
        this.leaderSelector.autoRequeue();

        this.cache = new PathChildrenCache(client, zkWatchNode, true);

        this.hostname = Inet4Address.getLocalHost().getHostName();
    }

    @Override
    public void close() throws IOException {
        this.leaderSelector.close();
        this.cache.close();
        this.client.close();
    }

    @Override
    public void takeLeadership(CuratorFramework curatorFramework) {
        log.info("[{}] taking leadership of zookeeper quorum...", hostname);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.error("Leadership thread interrupted while sleeping!", e);
        }
        log.info("[{}] relinquishing leadership...", hostname);
    }

    public void start() throws Exception {
        this.client.start();
        this.leaderSelector.start();
        this.cache.start();
        setupListener();
    }

    private void setupListener() {
        PathChildrenCacheListener listener = (client, event) -> {

            if (!leaderSelector.hasLeadership()) {
                return;
            }

            switch (event.getType()) {
                case CHILD_UPDATED:
                case CHILD_ADDED: {
                    log.info("{}: " + ZKPaths.getNodeFromPath(event.getData().getPath()), event.getType().name());
                    log.info("Node data: " + new String(event.getData().getData(), StandardCharsets.UTF_8));
                    break;
                }
                case CHILD_REMOVED: {
                    log.info("Node removed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                    break;
                }
            }
        };

        cache.getListenable().addListener(listener);
    }
}