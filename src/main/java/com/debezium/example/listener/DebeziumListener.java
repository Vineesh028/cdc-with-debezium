package com.debezium.example.listener;

import static io.debezium.data.Envelope.FieldName.AFTER;
import static io.debezium.data.Envelope.FieldName.BEFORE;
import static io.debezium.data.Envelope.FieldName.OPERATION;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.debezium.example.service.UserService;

import io.debezium.config.Configuration;
import io.debezium.data.Envelope.Operation;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DebeziumListener {
	
	@Autowired
	private UserService userService;
	

    private final Executor executor = Executors.newSingleThreadExecutor();    
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;

    public DebeziumListener(Configuration customerConnectorConfiguration) {
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
            .using(customerConnectorConfiguration.asProperties())
            .notifying(this::handleChangeEvent)
            .build();
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        var sourceRecord = sourceRecordRecordChangeEvent.record();
        log.info("Key = {}, Value = {}", sourceRecord.key(), sourceRecord.value());
        var sourceRecordChangeValue= (Struct) sourceRecord.value();
        log.info("SourceRecordChangeValue = '{}'", sourceRecordChangeValue);
        
         if (sourceRecordChangeValue != null) {
             Operation operation = Operation.forCode((String) sourceRecordChangeValue.get(OPERATION));

             if(operation != Operation.READ) {
                 String record = operation == Operation.DELETE ? BEFORE : AFTER; 

                 Struct struct = (Struct) sourceRecordChangeValue.get(record);
                 Map<String, Object> payload = struct.schema().fields().stream()
                     .map(Field::name)
                     .filter(fieldName -> struct.get(fieldName) != null)
                     .map(fieldName -> Pair.of(fieldName, struct.get(fieldName)))
                     .collect(toMap(Pair::getKey, Pair::getValue));

                 userService.replicateData(payload, operation);
                 log.info("Updated Data: {} with Operation: {}", payload, operation.name());
             }
         }
    }

    @PostConstruct
    private void start() {
        this.executor.execute(debeziumEngine);
    }

    @PreDestroy
    private void stop() throws IOException {
        if (Objects.nonNull(this.debeziumEngine)) {
            this.debeziumEngine.close();
        }
    }

}