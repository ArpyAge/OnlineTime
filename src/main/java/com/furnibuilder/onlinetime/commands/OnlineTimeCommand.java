package com.furnibuilder.onlinetime.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OnlineTimeCommand extends Command {

    public static String ONLLINE_KEY = "online.onlinetime";

    public OnlineTimeCommand(String permission, String[] keys)
    {
        super(permission, keys);
    }

    @Override
    public boolean handle(GameClient gameClient, String[] strings) throws Exception {
        Integer onlineTime = 0;
        
        if (!gameClient.getHabbo().getHabboStats().cache.containsKey(ONLLINE_KEY))
        {
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT online_time FROM users_settings WHERE user_id = ? LIMIT 1")) {
                statement.setInt(1, gameClient.getHabbo().getHabboInfo().getId());
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        onlineTime = set.getInt(1);
                    }
                }
            } catch (SQLException e) {
                Emulator.getLogging().logSQLException(e);
            }
        }
        else {
            onlineTime = (int)gameClient.getHabbo().getHabboStats().cache.get(ONLLINE_KEY);
        }

        Integer days;
        Integer hours;
        Integer minutes;
        Integer seconds;

        Object obj = 0;
        try {
            Field f = gameClient.getHabbo().getHabboStats().getClass().getDeclaredField("timeLoggedIn");
            f.setAccessible(true);
            obj = f.get(gameClient.getHabbo().getHabboStats());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Integer loggedInSeconds = Emulator.getIntUnixTimestamp() - (int)obj;

        Integer totalLoggedInTime = loggedInSeconds + onlineTime;

        days = (int)Math.floor(totalLoggedInTime/86400);
        hours = (int)Math.floor((totalLoggedInTime%86400)/3600);
        minutes = (int)Math.floor((totalLoggedInTime%3600)/60);
        seconds = (int)Math.floor(totalLoggedInTime%60);

        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("online.cmd_onlinetime.text").replace("%days%", ""+days).replace("%hours%", ""+hours).replace("%minutes%", ""+minutes).replace("%seconds%", ""+seconds), RoomChatMessageBubbles.ALERT);
        return true;
    }
}
