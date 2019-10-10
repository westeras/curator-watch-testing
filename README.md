# curator-watch-testing

Contains a test app illustrating the use of Apache Curator to 1) elect a leader amongst a group of running instances and 2) watch a given Zookeeper node and (if leader) react to any changes.

Another app (zk-auto-writer) randomly writes, deletes, and updates the watched Zookeeper node to demostrate the functionality of the first app.

Spotify's `dockerfile-maven-plugin` is used to build Docker images out of each application, and docker-compose is used to deploy the entire stack.

## Usage

Requirements: Maven, Docker

Basic usage, which deploys a 3 node Zookeeper quorum, and one instance each of `curator-watcher` and `zk-auto-writer`:

```bash
mvn clean install
docker-compose up
```

`--scale` can be used to deploy extra instances of `curator-watcher` and `zk-auto-writer`:

```bash
mvn clean install
docker-compose up --scale curator-watcher=5 --scale auto-writer=3
```

Each instance of `curator-watcher` will take leadership of the quorum for 60 seconds before relinquishing it, which is displayed in the logs:

```bash
INFO  c.w.c.ZookeeperLeadershipManager: [8c92cc99d5a5] relinquishing leadership...
INFO  c.w.c.ZookeeperLeadershipManager: [8ff16e1c0aa9] taking leadership of zookeeper quorum.
```

Each instance of `curator-watcher` is also equipped with a Zookeeper node watcher which implements a callback in order to react to child nodes written within the watched node:

```bash
INFO  c.w.c.ZookeeperLeadershipManager: CHILD_UPDATED: 67b21275-ce11-4051-afce-8f4a4b6207f8
INFO  c.w.c.ZookeeperLeadershipManager: Node data: {"id":"/67b21275-ce11-4051-afce-8f4a4b6207f8","timestamp":1570716241467,"hostname":"dc61baba1b97"}
```

This callback ensures the given instance is the leader of the quorum before processing the event:

```
if (!leaderSelector.hasLeadership()) {
    return;
}
```

`zk-auto-writer` implements a Runnable which on each invocation of `run()` does one of three things, chosen randomly at runtime: 1) adds a new, serialized instance of `SampleZNodeObject` to the watched ZNode, 2) deletes an already written node, or 3) updates the timestamp of an already written node.