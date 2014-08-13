package net.wtako.WTAKOFungeon.Methods;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import net.wtako.WTAKOFungeon.Main;
import net.wtako.WTAKOFungeon.Utils.Lang;

public class Database {

    private static Database instance;
    private static int      latestVersion = 1;
    public Connection       conn;

    public Database() throws SQLException {
        Database.instance = this;
        final String path = MessageFormat.format("jdbc:sqlite:{0}/{1}", Main.getInstance().getDataFolder()
                .getAbsolutePath(), Main.getInstance().getName() + ".db");
        conn = DriverManager.getConnection(path);
        check();
    }

    private void addConfig(String config, String value) throws SQLException {
        final PreparedStatement stmt = conn.prepareStatement("INSERT INTO `configs` (`config`, `value`) VALUES (?, ?)");
        stmt.setString(1, config);
        stmt.setString(2, value);
        stmt.execute();
        stmt.close();
    }

    public void createTables() throws SQLException {
        final Statement cur = conn.createStatement();
        String stmt = "CREATE TABLE `funegons` (" + "`row_id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`funegon_name` TEXT NOT NULL, " + "`time_limit` INT NOT NULL," + "`min_players` INT NOT NULL,"
                + "`max_players` INT NOT NULL," + "`wait_time` INT NOT NULL," + "`lobby_loc_id` INT NULL,"
                + "`area_p1_loc_id` INT NULL," + "`area_p2_loc_id` INT NULL," + "`wait_rm_loc_id` INT NULL,"
                + "`start_pt_loc_id` INT NULL," + "`run_command` TEXT NULL" + ")";
        cur.execute(stmt);
        stmt = "CREATE TABLE `prizes` (" + "`row_id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "`funegon_id` INT NOT NULL," + "`cash_amount` INT NULL," + "`item_json` TEXT NULL" + ")";
        cur.execute(stmt);
        stmt = "CREATE TABLE `costs` (" + "`row_id` INTEGER PRIMARY KEY AUTOINCREMENT, " + "`funegon_id` INT NOT NULL,"
                + "`cost_type` TEXT NOT NULL," + "`item_meta_json` TEXT NULL," + "`amount` INT NULL" + ")";
        cur.execute(stmt);
        stmt = "CREATE TABLE `locations` (" + "`row_id` INTEGER PRIMARY KEY AUTOINCREMENT, " + "`world` TEXT NOT NULL,"
                + "`x` INT NOT NULL," + "`y` INT NOT NULL," + "`z` INT NOT NULL" + ")";
        cur.execute(stmt);
        cur.execute("CREATE TABLE `configs` (`config` TEXT PRIMARY KEY, `value` TEXT NULL)");
        cur.close();
        addConfig("database_version", String.valueOf(Database.latestVersion));
    }

    private boolean areTablesExist() {
        try {
            final Statement cur = conn.createStatement();
            cur.execute("SELECT * FROM `configs` LIMIT 0");
            cur.close();
            return true;
        } catch (final SQLException ex) {
            return false;
        }
    }

    public void check() throws SQLException {
        Main.log.info(Lang.TITLE.toString() + "Checking database...");
        if (!areTablesExist()) {
            Main.log.info(Lang.TITLE.toString() + "Creating tables...");
            createTables();
            Main.log.info(Lang.TITLE.toString() + "Done.");
        }
    }

    public static Connection getConn() {
        return Database.instance.conn;
    }

    public static Database getInstance() {
        return Database.instance;
    }

}