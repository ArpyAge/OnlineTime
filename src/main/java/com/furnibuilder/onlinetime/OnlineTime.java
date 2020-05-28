package com.furnibuilder.onlinetime;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.CommandHandler;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import com.furnibuilder.onlinetime.commands.OnlineTimeCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class OnlineTime extends HabboPlugin implements EventListener {
    public static OnlineTime INSTANCE = null;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Emulator.getPluginManager().registerEvents(this, this);

        if (Emulator.isReady) {
            this.checkDatabase();
        }

        Emulator.getLogging().logStart("[Online] Started Online Command Plugin!");
    }

    @Override
    public void onDisable() {
        Emulator.getLogging().logShutdownLine("[Online] Stopped Online Command Plugins!");
    }

    @EventHandler
    public static void onEmulatorLoaded(EmulatorLoadedEvent event) {
        INSTANCE.checkDatabase();
    }

    @Override
    public boolean hasPermission(Habbo habbo, String s) {
        return false;
    }

    private void checkDatabase() {
        boolean reloadPermissions = false;

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE  `emulator_texts` CHANGE  `value`  `value` VARCHAR( 4096 ) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL");
        } catch (SQLException e) {
        }
        Emulator.getTexts().register("online.cmd_onlinetime.keys", "online;onlinetime");
        Emulator.getTexts().register("commands.description.cmd_onlinetime", ":online");
        Emulator.getTexts().register("online.cmd_onlinetime.text", "You have been online for %days% day(s), %hours% hour(s), %minutes% minute(s) and %seconds% second(s)");
        reloadPermissions = this.registerPermission("cmd_onlinetime", "'0', '1'", "1", reloadPermissions);

        if (reloadPermissions) {
            Emulator.getGameEnvironment().getPermissionsManager().reload();
        }

        CommandHandler.addCommand(new OnlineTimeCommand("cmd_onlinetime", Emulator.getTexts().getValue("online.cmd_onlinetime.keys").split(";")));
    }

    private boolean registerPermission(String name, String options, String defaultValue, boolean defaultReturn) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("ALTER TABLE  `permissions` ADD  `" + name + "` ENUM(  " + options + " ) NOT NULL DEFAULT  '" + defaultValue + "'")) {
                statement.execute();
                return true;
            }
        } catch (SQLException e) {
        }

        return defaultReturn;
    }

    public static void main(String[] args) {
        System.out.println("Don't run this seperately");
    }
}