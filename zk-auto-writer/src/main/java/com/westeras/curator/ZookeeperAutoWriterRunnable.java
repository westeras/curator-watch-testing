package com.westeras.curator;

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
import com.westeras.curator.api.SampleZNodeObject;

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

    public ZookeeperAutoWriterRunnable(String zkConnect, String zkWatchNode) {
        this.zkWatchNode = zkWatchNode;

        // setup Curator ZK client
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.newClient(zkConnect, retryPolicy);
        client.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                int option = random.nextInt(3);
                if (childNodes.isEmpty() || option == 0) {
                    // add new node
                    String childNode = "/" + UUID.randomUUID().toString();
                    childNodes.add(childNode);

                    SampleZNodeObject sampleObject = new SampleZNodeObject(childNode, System.currentTimeMillis(), Inet4Address.getLocalHost().getHostName());
                    byte[] bytes = serializeObject(sampleObject);

                    client.create().forPath(zkWatchNode + childNode, bytes);
                } else if (option == 1) {
                    // delete a random node
                    int toDeleteIndex = random.nextInt(childNodes.size());
                    String toDelete = childNodes.get(toDeleteIndex);
                    childNodes.remove(toDeleteIndex);

                    client.delete().forPath(zkWatchNode + toDelete);
                } else {
                    // update a random node
                    String toUpdate = childNodes.get(random.nextInt(childNodes.size()));

                    byte[] bytes = client.getData().forPath(zkWatchNode + toUpdate);
                    SampleZNodeObject sampleObject = deserializeObject(bytes);
                    sampleObject = SampleZNodeObject.builder()
                            .timestamp(System.currentTimeMillis())
                            .build();
                    client.setData().forPath(zkWatchNode + toUpdate, serializeObject(sampleObject));
                }

                Thread.sleep(random.nextInt(10000));
            } catch (Exception e) {
                log.info("thread was interrupted while sleeping");
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
