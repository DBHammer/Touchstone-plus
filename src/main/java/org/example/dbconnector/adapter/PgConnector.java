package org.example.dbconnector.adapter;

import org.example.dbconnector.DatabaseConnectorConfig;
import org.example.dbconnector.DbConnector;
import org.example.utils.exception.MainException;

import java.sql.SQLException;

public class PgConnector extends DbConnector {
    private static final String DB_DRIVER_TYPE = "postgresql";
    private static final String JDBC_PROPERTY = "";

    public PgConnector(DatabaseConnectorConfig config) throws MainException, SQLException {
        super(config, DB_DRIVER_TYPE, JDBC_PROPERTY);
    }

    @Override
    protected String[] preExecutionCommands() {
        return new String[]{"SET max_parallel_workers_per_gather = 0;", "SET join_collapse_limit = 1;"};
    }

    @Override
    protected int[] getSqlInfoColumns() {
        return new int[]{1};
    }
}
