package ca.tweetzy.core.commands;

import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MainCommand extends AbstractCommand {

	String header = null;
	String description;
	boolean sortHelp = false;
	final String command;
	final Plugin plugin;
	protected final SimpleNestedCommand nestedCommands;

	public MainCommand(Plugin plugin, String command) {
		super(CommandType.CONSOLE_OK, command);
		this.command = command;
		this.plugin = plugin;
		this.description = "Shows the command help page for /" + command;
		this.nestedCommands = new SimpleNestedCommand(this);
	}


	public MainCommand setHeader(String header) {
		this.header = header;
		return this;
	}

	public MainCommand setDescription(String description) {
		this.description = description;
		return this;
	}

	public MainCommand setSortHelp(boolean sortHelp) {
		this.sortHelp = sortHelp;
		return this;
	}

	public MainCommand addSubCommand(AbstractCommand command) {
		nestedCommands.addSubCommand(command);
		return this;
	}

	public MainCommand addSubCommands(AbstractCommand... commands) {
		nestedCommands.addSubCommands(commands);
		return this;
	}

	@Override
	protected ReturnType runCommand(CommandSender sender, String... args) {
		sender.sendMessage("");
		if (header != null) {
			sender.sendMessage(header);
		} else {
			sender.sendMessage(TextUtils.formatText(String.format("#00ce74&l%s &8» &7Version %s", plugin.getDescription().getName(), plugin.getDescription().getVersion())));
		}
		sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.YELLOW + "/tweetzy" + ChatColor.GRAY + " - Opens the Tweetzy Config Editor");
		sender.sendMessage("");

		if (nestedCommands != null) {
			List<String> commands = nestedCommands.children.values().stream().distinct().map(c -> c.getCommands().get(0)).collect(Collectors.toList());
			if (sortHelp) {
				Collections.sort(commands);
			}
			boolean isPlayer = sender instanceof Player;
			// todo? pagation if commands.size is too large? (player-only)
			sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.YELLOW + getSyntax() + ChatColor.GRAY + " - " + getDescription());
			for (String cmdStr : commands) {
				final AbstractCommand cmd = nestedCommands.children.get(cmdStr);
				if (cmd == null) continue;
				if (!isPlayer) {
					sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.YELLOW + cmd.getSyntax() + ChatColor.GRAY + " - " + cmd.getDescription());
				} else if (cmd.getPermissionNode() == null || sender.hasPermission(cmd.getPermissionNode())) {
					final String c = "/" + command + " ";
                    sender.sendMessage(TextUtils.formatText("&8- &e" + c + "&7- &E" + cmd.getDescription()));
				}
			}
		}

		sender.sendMessage("");

		return ReturnType.SUCCESS;
	}

	@Override
	protected List<String> onTab(CommandSender sender, String... args) {
		// don't need to worry about tab for a root command - handled by the manager
		return null;
	}

	@Override
	public String getPermissionNode() {
		// permissions for a root command should be handled in the plugin.yml
		return null;
	}

	@Override
	public String getSyntax() {
		return "/" + command;
	}

	@Override
	public String getDescription() {
		return description;
	}

}