package org.YiiCommunity.GitterBot.CarmaCommand;

import com.amatkivskiy.gitter.rx.sdk.model.response.message.MessageResponse;
import org.YiiCommunity.GitterBot.api.Command;
import org.YiiCommunity.GitterBot.containers.Gitter;
import org.YiiCommunity.GitterBot.models.database.User;
import org.YiiCommunity.GitterBot.utils.L;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Alex on 10/23/15.
 */
public class Carma extends Command {
    private List<String> commands = new ArrayList<>();

    public Carma() {
        commands = getConfig().getStringList("commands");
    }

    @Override
    public void onMessage(MessageResponse message) {
        try {
            for (String item : commands) {
                if (message.text.equalsIgnoreCase(item)) {
                    User user = User.getUser(message.fromUser.username);
                    Gitter.sendMessage("@" + user.getUsername() + " твоя карма нынче **" + (user.getCarma() >= 0 ? "+" : "-") + user.getCarma() + "**\n" + getThanks(user) + "\n" + getAchievements(user));
                    return;
                }
            }
            Pattern p = Pattern.compile("(?:" + String.join("|", commands) + ")\\s+@([0-9a-zA-Z-_]+)\\b");
            Matcher m = p.matcher(message.text.trim());

            while (m.find()) {
                User receiver = User.getUser(m.group(1));
                Gitter.sendMessage("Карма @" + receiver.getUsername() + " нынче **" + (receiver.getCarma() >= 0 ? "+" : "-") + receiver.getCarma() + "**\n" + getThanks(receiver) + "\n" + getAchievements(receiver));
                receiver.updateAchievements();
            }
        } catch (Exception e) {
            L.$(e.getMessage());
        }
    }

    private String getThanks(User user) {
        return "Сказал спасибо **" + user.getThanks() + "** раз";
    }

    private String getAchievements(User user) {
        if (user.getAchievements().isEmpty())
            return "Пока достижений нет.";

        List<String> list = user.getAchievements().stream().map(item -> item.getAchievement().getTitle()).collect(Collectors.toList());
        return "Достижения: " + String.join(", ", list);
    }
}
