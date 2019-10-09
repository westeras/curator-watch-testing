#!/usr/bin/env bash

"$JAVA_HOME"/bin/java -jar /zk-auto-writer/zk-auto-writer.jar "$ZK_CONNECT" "$ZK_WATCH_NODE"