package com.lsandor.fixme.core.database;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class Database {
    private static final String DATABASE_URL = "jdbc:sqlite:".concat(System.getProperty("user.dir")).concat("/transactions.db");
    private static final String INSERT_TRANSACTION = "INSERT INTO transactions " +
            "(market_name, broker_id, broker_name, op_type, instrument, price, quantity, result, comment)" +
            " VALUES (?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_QUERY = "SELECT * FROM transactions";
    private static Connection connection;
    private static Statement statement;

    static {
        try {
            connection = DriverManager.getConnection(DATABASE_URL);
            statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS " +
                    "'transactions' ('market_name' text, 'broker_id' text, 'broker_name' text, 'op_type' text," +
                    "'instrument' text, 'price' text, 'quantity' text, 'result' text, 'comment' text);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insert(String marketName, String brokerId, String brokerName, String operationType, String instrument,
                              String price, String quantity, String result, String comment) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_TRANSACTION)) {
            statement.setString(1, marketName);
            statement.setString(2, brokerId);
            statement.setString(3, brokerName);
            statement.setString(4, operationType);
            statement.setString(5, instrument);
            statement.setString(6, price);
            statement.setString(7, quantity);
            statement.setString(8, result);
            statement.setString(9, comment);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public static void selectAll() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_QUERY)) {
            printResultSet(resultSet);
        } catch (SQLException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    private static void printResultSet(ResultSet resultSet) {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnsNumber = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(", ");
                    String columnValue = resultSet.getString(i);
                    System.out.print(metaData.getColumnName(i) + ": '" + columnValue + "'");
                }
                System.out.println();
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
