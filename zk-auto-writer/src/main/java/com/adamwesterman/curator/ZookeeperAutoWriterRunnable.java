package com.adamwesterman.curator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.adamwesterman.curator.api.SampleZNodeObject;

import lombok.extern.slf4j.Slf4j;

/*
 * Client used to randomly write and delete data from a watched Zookeeper node
 */
@Slf4j
public class ZookeeperAutoWriterRunnable implements Runnable {

    private final String zkWatchNode;

    private final CuratorFramework client;

    private final List<String> childNodes = new ArrayList<>();
    private static final Random random = new Random();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ZookeeperAutoWriterRunnable(String zkConnect, String zkWatchNode) throws Exception {
        log.info("setting up auto writer runnable...");
        this.zkWatchNode = zkWatchNode;

        // setup Curator ZK client
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.newClient(zkConnect, retryPolicy);
        client.start();
    }

    private String[] pathParts(String fullPath) {
        fullPath = fullPath.startsWith("/") ? fullPath.substring(1) : fullPath;
        fullPath = fullPath.endsWith("/") ? fullPath.substring(0, fullPath.length() - 1) : fullPath;

        return fullPath.split("/");
    }

    @Override
    public void run() {
        log.info("running auto writer runnable...");
        while (true) {
            try {
                int option = random.nextInt(3);
                if (childNodes.isEmpty() || option == 0) {
                    String childNode = "/" + UUID.randomUUID().toString();
                    log.debug("adding a new node {} to {}", childNode, zkWatchNode);

                    SampleZNodeObject sampleObject = new SampleZNodeObject(childNode, System.currentTimeMillis(), Inet4Address.getLocalHost().getHostName());
                    byte[] bytes = serializeObject(sampleObject);

                    childNodes.add(childNode);

                    client.create().forPath(zkWatchNode + childNode, bytes);
                } else if (option == 1) {
                    int toDeleteIndex = random.nextInt(childNodes.size());
                    String toDelete = childNodes.get(toDeleteIndex);
                    log.debug("deleting node {} from {}", toDelete, zkWatchNode);

                    client.delete().forPath(zkWatchNode + toDelete);

                    childNodes.remove(toDeleteIndex);
                } else {
                    String toUpdate = childNodes.get(random.nextInt(childNodes.size()));
                    log.debug("updating node {} in {} with a current timestamp", toUpdate, zkWatchNode);
                    byte[] bytes = client.getData().forPath(zkWatchNode + toUpdate);
                    SampleZNodeObject sampleObject = deserializeObject(bytes);
                    sampleObject.setTimestamp(System.currentTimeMillis());
                    client.setData().forPath(zkWatchNode + toUpdate, serializeObject(sampleObject));
                }

            } catch (Exception e) {
                log.error("caught an exception while processing reading/writing data from ZK", e);
            }

            try {
                Thread.sleep(random.nextInt(50));
            } catch (InterruptedException e) {
                log.error("thread was interrupted while sleeping", e);
            }
        }
    }

    private byte[] serializeObject(SampleZNodeObject sampleObject) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        objectMapper.writeValue(os, sampleObject);
        return os.toByteArray();
    }

    private SampleZNodeObject deserializeObject(byte[] bytes) throws IOException {
        return objectMapper.readValue(bytes, SampleZNodeObject.class);
    }
}
