package ca.tweetzy.core.locale;

import ca.tweetzy.core.configuration.Config;
import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;

/**
 * The current file has been created by Kiran Hart
 * Date Created: June 16 2021
 * Time Created: 2:19 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class Locale {

	final String EXTENSION = ".lang";

	private JavaPlugin plugin;
	private final Config config;

	private String message;

	public Locale(JavaPlugin plugin, String language) {
		this.plugin = plugin;
		this.config = new Config(plugin, "/locales/", language + EXTENSION);
		this.config.load();
	}

	public Locale getMessage(String node) {
		this.message = this.config.getString(node);
		return this;
	}

	public Locale processPlaceholder(String placeholder, Object replacement) {
		final String place = Matcher.quoteReplacement(placeholder);
		this.message = message.replaceAll("%" + place + "%|\\{" + place + "\\}", replacement == null ? "" : Matcher.quoteReplacement(replacement.toString()));
		return this;
	}

	public Locale newMessage(String message) {
		this.message = message;
		return this;
	}

	public void sendPrefixedMessage(CommandSender sender) {
		final String prefix = TextUtils.formatText(this.config.getString("general.prefix"));
		sender.sendMessage(TextUtils.formatText(prefix.length() == 0 ? "" + this.message : prefix + " " + this.message));
	}

	public String getMessage() {
		return this.message;
	}

	public Config getConfig() {
		return config;
	}
}
