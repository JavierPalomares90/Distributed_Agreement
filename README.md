# Distributed Agreement 
## Javier Palomares and Matt Molter

This is an implementation of the Weighted Distributed Agreement using Byzantine Paxos. For complete details on the algorithm, please see Distributed_Agreement.pdf and Distributed_Agreement_Presentation.pdf. Both are attached to this repo. Additionally, visit www.javierpalomares.net/my-blog/2020/4/5/the-byzantine-distributed-agreement-problem for a quick run down of the Paxos and Byzantine Paxos algorithms

What follows are the instructions for running the server and client.
The project is built and executed with maven.
### Running Server

The server is implemented in distributed/DistributedAgreement.java.
The class takes 2 arguments:
1. Path to servers list
2. Server Id

Run it with:

``` mvn clean compile exec:java -Dexec.mainClass="distributed.DistributedAgreement" -Dlog4j.configuration=file:"./src/main/resources/log4j.properties" -Dexec.args="<path_to_servers_list> <server_id>"```


For example:

``` mvn clean compile exec:java -Dexec.mainClass="distributed.DistributedAgreement" -Dlog4j.configuration=file:"./src/main/resources/log4j.properties" -Dexec.args="./src/main/resources/hosts.yaml 1"```



The servers list defines the ip address, port, server id, and weight given to each server. The weights must add up to 1.0

For example:
```---
ipAddress: "127.0.0.1"
port: 8080
serverId: 1
weight : .6
---
ipAddress: "127.0.0.1"
port: 8081
serverId : 2
weight : .4
---
```

### Running Client

The client is implemented in distributed/client/Client.java. The class takes the `path to serverst list` as argument.

Run it with:

``` mvn clean compile exec:java -Dexec.mainClass="distributed.client.Client" -Dlog4j.configuration=file:"./src/main/resources/log4j.properties" -Dexec.args="<path_to_servers_list>"```

For example:

``` mvn clean compile exec:java -Dexec.mainClass="distributed.client.Client" -Dlog4j.configuration=file:"./src/main/resources/log4j.properties" -Dexec.args="./src/main/resources/hosts.yaml"```


## Malicious Implementations

The algorithm is Byzantine, so it can handle malicious servers and clients that lie. Again, take a look at the attached pdfs for full details on how this works. 

### Running Malicious Server

Run it with:

``` mvn clean compile exec:java -Dexec.mainClass="distributed.malicious.MaliciousDistributedAgreement" -Dlog4j.configuration=file:"./src/main/resources/log4j.properties" -Dexec.args="<path_to_servers_list> <server_id>"```


### Running Malicious Client

Run it with:

``` mvn clean compile exec:java -Dexec.mainClass="distributed.malicious.client.MaliciousClient" -Dlog4j.configuration=file:"./src/main/resources/log4j.properties" -Dexec.args="<path_to_servers_list>"```
