#!/usr/bin/env bash

"$JAVA_HOME"/bin/java -jar /curator-watcher/curator-watcher.jar "$ZK_CONNECT" "$ZK_LOCK_NODE" "$ZK_WATCH_NODE"