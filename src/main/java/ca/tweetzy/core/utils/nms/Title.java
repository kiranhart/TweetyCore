package ca.tweetzy.core.utils.nms;

import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/13/2020
 * Time Created: 4:36 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class Title {

    public enum TitleType {
        TITLE, SUBTITLE;
    }

    public static void send(Player player, TitleType type, String msg, int fadeIn, int stay, int fadeOut) {
        try {
            Object enumTitle = NMSUtils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField(type == TitleType.TITLE ? "TITLE" : "SUBTITLE").get(null);
            Object titleChat = NMSUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + TextUtils.formatText(msg) + "\"}");
            Constructor<?> titleConstructor = NMSUtils.getNMSClass("PacketPlayOutTitle").getConstructor(NMSUtils.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], NMSUtils.getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object titlePacket = titleConstructor.newInstance(enumTitle, titleChat, fadeIn, stay, fadeOut);
            NMSUtils.sendPacket(player, titlePacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
