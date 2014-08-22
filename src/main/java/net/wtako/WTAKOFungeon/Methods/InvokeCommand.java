package net.wtako.WTAKOFungeon.Methods;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class InvokeCommand {

    public static int addCommand(int fungeonID, String command) throws SQLException {
        final PreparedStatement insStmt = Database.getConn().prepareStatement(
                "INSERT INTO invoke_commands (`fungeon_id`, `command`) VALUES (?, ?)");
        insStmt.setInt(1, fungeonID);
        insStmt.setString(2, command);
        insStmt.execute();
        final ResultSet result = insStmt.getGeneratedKeys();
        final int rowID = result.getInt(1);
        result.close();
        insStmt.close();
        return rowID;
    }

    public static void deleteAllCommands(int fungeonID) throws SQLException {
        final PreparedStatement delStmt = Database.getConn().prepareStatement(
                "DELETE FROM invoke_commands WHERE fungeon_id = ?");
        delStmt.setInt(1, fungeonID);
        delStmt.execute();
        delStmt.close();
    }

    public static void deleteCommand(int commandID) throws SQLException {
        final PreparedStatement delStmt = Database.getConn().prepareStatement(
                "DELETE FROM invoke_commands WHERE row_id = ?");
        delStmt.setInt(1, commandID);
        delStmt.execute();
        delStmt.close();
    }

    public static ArrayList<String> getCommands(int fungeonID) throws SQLException {
        final ArrayList<String> commands = new ArrayList<String>();
        final PreparedStatement selStmt = Database.getConn().prepareStatement(
                "SELECT * FROM invoke_commands WHERE fungeon_id = ?");
        selStmt.setInt(1, fungeonID);
        final ResultSet result = selStmt.executeQuery();
        while (result.next()) {
            commands.add(result.getString("command"));
        }
        result.close();
        selStmt.close();
        return commands;
    }

}
