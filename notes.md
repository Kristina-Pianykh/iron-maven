# Pub/Sub Centralized vs. Pub/Sub Distributed

## Pub/Sub Centralized :

### Benefits:
<!-- - performance and scalability (can broadcast to multiple clients at once) -->
- low latency (immediate action with one hop as opposed to a control message taking the longest path in the network worst case) --> faster mitigation of increasing transmission costs
- higher chance at atomicity (due to low latencies between the initial control message from the coordinator and the acks from the edge clients as opposed to the distributed approach where a control message can tak the longest path in the network). If an edge broker has first sent an ack but in the time between sending an ack and recieving a commit message, it becomes overloaded, automicity is violated --> data loss since the broker failes to commit and make the necessary updates --> unexpected system behavior
- better consistency and synchronized action between the brokers. Roughly simultaneous delivery of control messages which will help to prevent data loss and ensure event buferring at V_multi simultaneously. In the distributed approach, delivering a control message to one broker instructing it to start buffering events at time t_0 can arrive faster than to a more remote broker. E.g. a more remote broker will receive this message at time t_1 > t_0, which means the events that arrived during the gap between t_0 and t_1 might end up lost.
- easier implementation and debugging

### Disadvantages:
- single point of failure (coordinator) but this is out of scope

### Ideas and Inspirations:

- broker network overlay
- Have a transaction manager at each client (i.e. node) to control the initiation and execution of a transaction. The nodes don't have the knowledge of the network graph as they recieve the instructions from the coordinator. The coordinator is responsible for keeping a representation of the network and computing the shortest path between V_multi to V_fallback
- Use a 2-PC protocol to ensure atomicity (the coordinator sends out control messages and only on receiving the acks, sends out the commit message with instructions)


## Distributed Pub/Sub:

<details>
  <summary>Components and Concepts</summary>

    Network overlay (brokers, message queues, topics, etc.)

    transaction control message: a special type of publication used to trigger operations at a receiving client

    coordinator

    every client has a transaction manager to control initiation and execution of a transaction. Before any operation is processed, the TXManager must complete the initialization phase. Similarly, in order to terminate a transaction, the TXManager waits for all operations being acknowledged.

    2-PC (two-phase commit) protocol

    intermediary brokers

    downstream brokers

    Due to the tree structure of the overlay, message propagation is acyclic

    when the initial broker receives acks from the edge brokers, it means that all the brokers have initialized the transaction.

    atomicity is guaranteed by adapting the 2-PC algorithm
</details>

### Benefits:

- truly distributed
- no single point of failure (fault tolerance, the initial control message can be sent by any broker)

### Disadvantages:

- higher latency (a control message can take the longest path in the network)
- due to network latencies, higher risk of accummulating transmission costs until updates are propagates through the network and a repair is initiaded
- because of high latencies, the atomicity of a transaction (i.e. rapair) is jeopardized. Between the acks and the commit message a broker that has already agreed to commit can become overloaded by the increasing influx of incoming events. This can lead to a situation where the broker is unable to commit the transaction (atomicity is violated), which can result in data loss and unexpected system behavior.
- because of the complex nature of implementation, debugging can be challenging
