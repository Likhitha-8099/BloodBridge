package com.bloodbridge.entity;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class UserRolesSchemaGenerationTest {

    @Test
    @DisplayName("Verify Hibernate schema metadata assigns composite PRIMARY KEY (user_id, role) to user_roles table")
    void testUserRolesTableHasPrimaryKey() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
        registryBuilder.applySettings(settings);

        MetadataSources metadataSources = new MetadataSources(registryBuilder.build());
        metadataSources.addAnnotatedClass(User.class);

        MetadataImplementor metadata = (MetadataImplementor) metadataSources.buildMetadata();

        Table userRolesTable = StreamSupport.stream(metadata.getDatabase().getNamespaces().spliterator(), false)
                .flatMap(ns -> ns.getTables().stream())
                .filter(table -> "user_roles".equalsIgnoreCase(table.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Table 'user_roles' not found in Hibernate metadata"));

        assertNotNull(userRolesTable, "Table user_roles should exist");

        PrimaryKey primaryKey = userRolesTable.getPrimaryKey();
        assertNotNull(primaryKey, "Table user_roles MUST have a primary key configured");

        List<String> pkColumnNames = primaryKey.getColumns().stream()
                .map(Column::getName)
                .toList();

        System.out.println("user_roles Primary Key columns: " + pkColumnNames);

        assertEquals(2, pkColumnNames.size(), "Primary key must consist of 2 columns");
        assertTrue(pkColumnNames.contains("user_id"), "Primary key must include 'user_id'");
        assertTrue(pkColumnNames.contains("role"), "Primary key must include 'role'");
    }
}
