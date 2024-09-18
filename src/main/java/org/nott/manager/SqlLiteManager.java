package org.nott.manager;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author Nott
 * @date 2024-9-9
 */
public class SqlLiteManager {

    public static Connection getConnect() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:plugins/Sw-flag-war/database.db");
    }
}
