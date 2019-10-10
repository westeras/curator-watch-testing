#!/usr/bin/env bash

exec java -jar /zk-auto-writer/zk-auto-writer.jar "$ZK_CONNECT" "$ZK_WATCH_NODE"