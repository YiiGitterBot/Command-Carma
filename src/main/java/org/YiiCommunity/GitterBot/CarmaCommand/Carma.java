package org.YiiCommunity.GitterBot.CarmaCommand;

import com.amatkivskiy.gitter.sdk.model.response.message.MessageResponse;
import com.amatkivskiy.gitter.sdk.model.response.room.RoomResponse;
import org.YiiCommunity.GitterBot.api.Command;
import org.YiiCommunity.GitterBot.containers.Gitter;
import org.YiiCommunity.GitterBot.models.database.User;
import org.YiiCommunity.GitterBot.utils.L;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Carma extends Command {
    private List<String> commands = new ArrayList<>();

    public Carma() {
        commands = getConfig().getStringList("commands");
    }

    @Override
    public void onMessage(RoomResponse room, MessageResponse message) {
        try {
            for (String item : commands) {
                if (message.text.equalsIgnoreCase(item)) {
                    User user = User.getUser(message.fromUser.username);
                    Gitter.sendMessage(room,
                            getConfig()
                                    .getString("messages.yourCarma", "@{username} your carma right now is **{carma}**\n" +
                                            "You said thanks **{thanks}** times\n" +
                                            "{achievements}")
                                    .replace("{username}", user.getUsername())
                                    .replace("{carma}", (user.getCarma() >= 0 ? "+" : "-") + user.getCarma())
                                    .replace("{thanks}", user.getThanks().toString())
                                    .replace("{achievements}", getAchievements(user))
                    );
                    return;
                }
            }
            Pattern p = Pattern.compile("(?:" + String.join("|", commands) + ")\\s+@([0-9a-zA-Z-_]+)\\b");
            Matcher m = p.matcher(message.text.trim());

            while (m.find()) {
                User receiver = User.getUser(m.group(1));
                Gitter.sendMessage(room,
                        getConfig()
                                .getString("messages.userCarma", "User @{username} have **{carma}** carma right now\n" +
                                        "He said thanks **{thanks}** times\n" +
                                        "{achievements}")
                                .replace("{username}", receiver.getUsername())
                                .replace("{carma}", (receiver.getCarma() >= 0 ? "+" : "-") + receiver.getCarma())
                                .replace("{thanks}", receiver.getThanks().toString())
                                .replace("{achievements}", getAchievements(receiver))
                );
            }
        } catch (Exception e) {
            L.$(e.getMessage());
        }
    }

    private String getAchievements(User user) {
        if (user.getAchievements().isEmpty())
            return getConfig().getString("messages.noAchievements", "No achievements yet.");

        List<String> list = user.getAchievements().stream().map(item -> item.getAchievement().getTitle()).collect(Collectors.toList());
        return getConfig().getString("messages.achievements", "Achievements: {list}").replace("{list}", String.join(", ", list));
    }
}
