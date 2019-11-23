# Distributed Agreement 
## Javier Palomares and Matt Molter

### Running Server
```mvn clean compile```
``` mvn exec:java -Dexec.mainClass="distributed.DistributedAgreement" -Dlog4j.configuration=file:"./src/main/resources/log4j.properties" -Dexec.args="./src/main/resources/hosts.yaml <serverId>"```


### Running Client
```mvn clean compile```

``` mvn exec:java -Dexec.mainClass="distributed.client.Client" -Dlog4j.configuration=file:"./src/main/resources/log4j.properties" -Dexec.args="./src/main/resources/hosts.yaml"```
