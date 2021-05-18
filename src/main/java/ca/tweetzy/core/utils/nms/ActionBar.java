package ca.tweetzy.core.utils.nms;

import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/13/2020
 * Time Created: 4:45 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class ActionBar {

    public static void send(Player player, String msg) {
        try {
            Constructor<?> constructor = NMSUtils.getNMSClass("PacketPlayOutChat").getConstructor(NMSUtils.getNMSClass("IChatBaseComponent"), byte.class);
            Object icbc = NMSUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + TextUtils.formatText(msg) + "\"}");
            Object packet = constructor.newInstance(icbc, (byte) 2);
            NMSUtils.sendPacket(player, packet);
        } catch (Exception e) {

        }
    }
}
