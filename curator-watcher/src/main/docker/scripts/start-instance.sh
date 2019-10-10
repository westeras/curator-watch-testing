#!/usr/bin/env bash

exec java -jar /curator-watcher/curator-watcher.jar "$ZK_CONNECT" "$ZK_LOCK_NODE" "$ZK_WATCH_NODE"