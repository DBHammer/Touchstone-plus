package org.example.dbconnector;

import org.example.utils.exception.MainException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class DbConnector {
    private final Connection conn;

    protected abstract String[] preExecutionCommands();

    private final int[] sqlInfoColumns;

    protected DbConnector(DatabaseConnectorConfig config, String dbType, String databaseConnectionConfig) throws MainException, SQLException {
        String url;
        if (config.getDatabaseName() != null) {
            url = String.format("jdbc:%s://%s:%s/%s?%s", dbType, config.getDatabaseIp(), config.getDatabasePort(),
                    config.getDatabaseName(), databaseConnectionConfig);
        } else {
            url = String.format("jdbc:%s://%s:%s/?%s", dbType, config.getDatabaseIp(), config.getDatabasePort(),
                    databaseConnectionConfig);
        }
        // 数据库的用户名与密码
        String user = config.getDatabaseUser();
        String pass = config.getDatabasePwd();
        conn = DriverManager.getConnection(url, user, pass);
        try (Statement stmt = conn.createStatement()) {
            for (String command : preExecutionCommands()) {
                stmt.execute(command);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new MainException(String.format("无法建立数据库连接,连接信息为: '%s'", url));
        }
        sqlInfoColumns = getSqlInfoColumns();
    }

    protected abstract int[] getSqlInfoColumns();

    public List<String> getAllDistinctString(String colName, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(String.format("select distinct %s from %s where %s is not null", colName, tableName, colName));
            List<String> infos = new ArrayList<>();
            while (rs.next()) {
                infos.add(rs.getString(1));
            }
            return infos;
        }
    }

    public int getSqlResult(String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            return rs.getInt(1);
        }
    }

    public int getTableSize(String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String countQuery = String.format("select count(*) as cnt from %s", tableName);
            ResultSet rs = stmt.executeQuery(countQuery);
            if (rs.next()) {
                return rs.getInt("cnt");
            }
            throw new SQLException(String.format("table'%s'的size为0", tableName));
        }
    }
}
