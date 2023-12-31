package com.debezium.example.config;

import java.io.File;
import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DebeziumConnectorConfig {

    @Bean
    public io.debezium.config.Configuration customerConnector(Environment env) throws IOException {
        var offsetStorageTempFile = File.createTempFile("offsets_", ".dat");
        return io.debezium.config.Configuration.create()
            .with("name", "user_postgres_connector")
            .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
            .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
            .with("offset.storage.file.filename", offsetStorageTempFile.getAbsolutePath())
            .with("offset.flush.interval.ms", "60000")
            .with("database.hostname", env.getProperty("user.datasource.host"))
            .with("database.port", env.getProperty("user.datasource.port")) // defaults to 5432
            .with("database.user", env.getProperty("user.datasource.username"))
            .with("database.password", env.getProperty("user.datasource.password"))
            .with("database.dbname", env.getProperty("user.datasource.database"))
            .with("database.server.id", "10181")
            .with("database.server.name", "user-postgres-db-server")
            .with("database.history", "io.debezium.relational.history.MemoryDatabaseHistory")
            .with("table.include.list", "public.users")
            .with("publication.autocreate.mode", "filtered")
            .with("plugin.name", "pgoutput")
            .with("slot.name", "dbz_userdb_listener")
            .build();
    }
}