package ca.tweetzy.core.chat;

import ca.tweetzy.core.compatibility.ServerVersion;
import ca.tweetzy.core.utils.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The current file has been created by Kiran Hart
 * Date Created: November 29 2020
 * Time Created: 8:23 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class ChatMessage {

    private static final Gson gson = (new GsonBuilder()).create();
    private List<JsonObject> textList = new ArrayList();
    private static boolean enabled;
    private static Class<?> mc_ChatMessageType;
    private static Method mc_IChatBaseComponent_ChatSerializer_a;
    private static Method cb_craftPlayer_getHandle;
    private static Method mc_playerConnection_sendPacket;
    private static Constructor mc_PacketPlayOutChat_new;
    private static Field mc_entityPlayer_playerConnection;
    private static Field mc_chatMessageType_Chat;

    public ChatMessage() {
    }

    public void clear() {
        this.textList.clear();
    }

    public ChatMessage fromText(String text) {
        return this.fromText(text, false);
    }

    public ChatMessage fromText(String text, boolean noHex) {
        Pattern pattern = Pattern.compile("(.*?)(?!&([omnlk]))(?=(&([123456789abcdefr#])|$)|#([a-f]|[A-F]|[0-9]){6})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            ColorContainer color = null;
            String firstGroup = matcher.group(1);

            if (matcher.groupCount() == 0 || firstGroup.length() == 0) continue;

            char colorChar = '-';

            if (matcher.start() != 0)
                colorChar = text.substring(matcher.start() - 1, matcher.start()).charAt(0);

            if (colorChar != '-') {
                if (colorChar == '#') {
                    color = new ColorContainer(firstGroup.substring(0, 6), noHex);
                    firstGroup = firstGroup.substring(5);
                } else if (colorChar == '&') {
                    color = new ColorContainer(ColorCode.getByChar(Character.toLowerCase(firstGroup.charAt(0))));
                }
            }

            Pattern subPattern = Pattern.compile("(.*?)(?=&([omnlk])|$)");
            Matcher subMatcher = subPattern.matcher(firstGroup);

            List<ColorCode> stackedCodes = new ArrayList<>();
            while (subMatcher.find()) {
                String secondGroup = subMatcher.group(1);
                if (secondGroup.length() == 0) continue;

                ColorCode code = ColorCode.getByChar(Character.toLowerCase(secondGroup.charAt(0)));

                if (code != null && code != ColorCode.RESET)
                    stackedCodes.add(code);

                if (color != null)
                    secondGroup = secondGroup.substring(1);

                if (secondGroup.length() == 0) continue;

                addMessage(secondGroup, color, stackedCodes);
            }
        }

        return this;
    }

    public String toText() {
        return this.toText(false);
    }

    public String toText(boolean noHex) {
        StringBuilder text = new StringBuilder();

        for (JsonObject object : this.textList) {
            if (object.has("color")) {
                String color = object.get("color").getAsString();
                text.append("&");
                if (color.length() == 7) {
                    text.append((new ColorContainer(color, noHex)).getColor().getCode());
                } else {
                    text.append(ColorCode.valueOf(color.toUpperCase()).getCode());
                }
            }

            for (ColorCode code : ColorCode.values()) {
                if (!code.isColor()) {
                    String c = code.name().toLowerCase();
                    if (object.has(c) && object.get(c).getAsBoolean()) {
                        text.append("&").append(code.getCode());
                    }
                }
            }

            text.append(object.get("text").getAsString());
        }

        return text.toString();
    }

    public ChatMessage addMessage(String s) {
        JsonObject txt = new JsonObject();
        txt.addProperty("text", s);
        this.textList.add(txt);
        return this;
    }

    public ChatMessage addMessage(String text, ColorContainer color) {
        return this.addMessage(text, color, Collections.emptyList());
    }

    public ChatMessage addMessage(String text, ColorContainer color, List<ColorCode> colorCodes) {
        JsonObject txt = new JsonObject();
        txt.addProperty("text", text);
        if (color != null) {
            txt.addProperty("color", color.getHexCode() != null ? "#" + color.getHexCode() : color.getColorCode().name().toLowerCase());
        }

        ColorCode[] colors = ColorCode.values();
        int size = colors.length;

        for (int i = 0; i < size; ++i) {
            ColorCode code = colors[i];
            if (!code.isColor()) {
                txt.addProperty(code.name().toLowerCase(), colorCodes.contains(code));
            }
        }

        this.textList.add(txt);
        return this;
    }

    public ChatMessage addRunCommand(String text, String hoverText, String cmd) {
        JsonObject txt = new JsonObject();
        txt.addProperty("text", text);
        JsonObject hover = new JsonObject();
        hover.addProperty("action", "show_text");
        hover.addProperty("value", hoverText);
        txt.add("hoverEvent", hover);
        JsonObject click = new JsonObject();
        click.addProperty("action", "run_command");
        click.addProperty("value", cmd);
        txt.add("clickEvent", click);
        this.textList.add(txt);
        return this;
    }

    public ChatMessage addPromptCommand(String text, String hoverText, String cmd) {
        JsonObject txt = new JsonObject();
        txt.addProperty("text", text);
        JsonObject hover = new JsonObject();
        hover.addProperty("action", "show_text");
        hover.addProperty("value", hoverText);
        txt.add("hoverEvent", hover);
        JsonObject click = new JsonObject();
        click.addProperty("action", "suggest_command");
        click.addProperty("value", cmd);
        txt.add("clickEvent", click);
        this.textList.add(txt);
        return this;
    }

    public ChatMessage addURL(String text, String hoverText, String url) {
        JsonObject txt = new JsonObject();
        txt.addProperty("text", text);
        JsonObject hover = new JsonObject();
        hover.addProperty("action", "show_text");
        hover.addProperty("value", hoverText);
        txt.add("hoverEvent", hover);
        JsonObject click = new JsonObject();
        click.addProperty("action", "open_url");
        click.addProperty("value", url);
        txt.add("clickEvent", hover);
        this.textList.add(txt);
        return this;
    }

    public String toString() {
        return gson.toJson(this.textList);
    }

    public void sendTo(CommandSender sender) {
        this.sendTo((ChatMessage) null, sender);
    }

    public void sendTo(ChatMessage prefix, CommandSender sender) {
        if (sender instanceof Player && enabled) {
            try {
                List<JsonObject> textList = prefix == null ? new ArrayList() : new ArrayList(prefix.textList);
                textList.addAll(this.textList);
                Object packet;
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_16)) {
                    packet = mc_PacketPlayOutChat_new.newInstance(mc_IChatBaseComponent_ChatSerializer_a.invoke((Object) null, gson.toJson(textList)), mc_chatMessageType_Chat.get((Object) null), ((Player) sender).getUniqueId());
                } else {
                    packet = mc_PacketPlayOutChat_new.newInstance(mc_IChatBaseComponent_ChatSerializer_a.invoke((Object) null, gson.toJson(textList)));
                }

                Object cbPlayer = cb_craftPlayer_getHandle.invoke(sender);
                Object mcConnection = mc_entityPlayer_playerConnection.get(cbPlayer);
                mc_playerConnection_sendPacket.invoke(mcConnection, packet);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                Bukkit.getLogger().log(Level.WARNING, "Problem preparing raw chat packets (disabling further packets)", e);
                enabled = false;
            }
        } else {
            sender.sendMessage(TextUtils.formatText((prefix == null ? "" : prefix.toText(true) + " ") + this.toText(true)));
        }

    }

    static void init() {
        if (enabled) {
            try {
                String version = ServerVersion.getServerVersionString();
                Class<?> cb_craftPlayerClazz = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
                cb_craftPlayer_getHandle = cb_craftPlayerClazz.getDeclaredMethod("getHandle");
                Class<?> mc_entityPlayerClazz = Class.forName("net.minecraft.server." + version + ".EntityPlayer");
                mc_entityPlayer_playerConnection = mc_entityPlayerClazz.getDeclaredField("playerConnection");
                Class<?> mc_playerConnectionClazz = Class.forName("net.minecraft.server." + version + ".PlayerConnection");
                Class<?> mc_PacketInterface = Class.forName("net.minecraft.server." + version + ".Packet");
                mc_playerConnection_sendPacket = mc_playerConnectionClazz.getDeclaredMethod("sendPacket", mc_PacketInterface);
                Class<?> mc_IChatBaseComponent = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
                Class<?> mc_IChatBaseComponent_ChatSerializer = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
                mc_IChatBaseComponent_ChatSerializer_a = mc_IChatBaseComponent_ChatSerializer.getMethod("a", String.class);
                Class<?> mc_PacketPlayOutChat = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_16)) {
                    mc_ChatMessageType = Class.forName("net.minecraft.server." + version + ".ChatMessageType");
                    mc_chatMessageType_Chat = mc_ChatMessageType.getField("CHAT");
                    mc_PacketPlayOutChat_new = mc_PacketPlayOutChat.getConstructor(mc_IChatBaseComponent, mc_ChatMessageType, UUID.class);
                } else {
                    mc_PacketPlayOutChat_new = mc_PacketPlayOutChat.getConstructor(mc_IChatBaseComponent);
                }
            } catch (Throwable e) {
                Bukkit.getLogger().log(Level.WARNING, "Problem preparing raw chat packets (disabling further packets)", e);
                enabled = false;
            }
        }

    }

    public ChatMessage replaceAll(String toReplace, String replaceWith) {
        for (JsonObject object : this.textList) {
            String text = object.get("text").getAsString().replaceAll(toReplace, replaceWith);
            object.remove("text");
            object.addProperty("text", text);
        }

        return this;
    }

    static {
        enabled = ServerVersion.isServerVersionAtLeast(ServerVersion.V1_8);
        init();
    }
}