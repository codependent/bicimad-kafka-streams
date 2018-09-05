# bicimad-kafka-streams

[![Build Status](https://semaphoreci.com/api/v1/codependent/bicimad-kafka-streams/branches/master/badge.svg)](https://semaphoreci.com/codependent/bicimad-kafka-streams)

Kafka Streams used to track BiciMAD usage

## Instructions

1. Configure BiciMAD key/secret properties (`bicimad.code`, `bicimad.passkey`) with values gotten
from the [emtmadrid.es registration website](https://opendata.emtmadrid.es/Formulario.aspx). 

2. Start Kafka:

```
/opt/apache/kafka_2.11-2.0.0/bin/zookeeper-server-start.sh /opt/apache/kafka_2.11-2.0.0/config/zookeeper.properties
```
```
/opt/apache/kafka_2.11-2.0.0/bin/kafka-server-start.sh /opt/apache/kafka_2.11-2.0.0/config/server.properties 
```

3. Start the microservice as a normal Spring Boot application

4. Check the status of the different topics:

For instance, example of bicimad-low-capacity-stations topic:

```
/opt/apache/kafka_2.11-2.0.0/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
--topic bicimad-low-capacity-stations --from-beginning --formatter kafka.tools.DefaultMessageFormatter \
--property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer \
--property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```
```
7	{"id":7,"latitude":"40.4241480","longitude":"-3.6984470","name":"Colegio Arquitectos","dock_bikes":0,"free_bases":23,"availabilityPercentage":0.0}
8	{"id":8,"latitude":"40.4251906","longitude":"-3.6977715","name":"Hortaleza","dock_bikes":1,"free_bases":18,"availabilityPercentage":4.761904761904762}
9	{"id":9,"latitude":"40.4278682","longitude":"-3.6954403","name":"Alonso Martínez","dock_bikes":2,"free_bases":20,"availabilityPercentage":8.333333333333334}
11	{"id":11,"latitude":"40.4250863","longitude":"-3.6918807","name":"Marqués de la Ensenada","dock_bikes":0,"free_bases":24,"availabilityPercentage":0.0}
...
```

## Topology:

The diagram below illustrates the whole processing topology of the application.

![Topology](https://raw.githubusercontent.com/codependent/bicimad-kafka-streams/master/topology.png)
