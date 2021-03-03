package ca.tweetzy.core.core;

import ca.tweetzy.core.TweetyCore;
import ca.tweetzy.core.commands.AbstractCommand;
import ca.tweetzy.core.gui.GuiManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * The current file has been created by Kiran Hart
 * Date Created: 5/13/2020
 * Time Created: 5:08 PM
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise.
 */
public class TweetzyCoreCommand extends AbstractCommand {

    protected GuiManager guiManager;

    public TweetzyCoreCommand() {
        super(CommandType.CONSOLE_OK, "tweetzy");
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (sender instanceof Player) {
            if (guiManager == null || guiManager.isClosed()) {
                guiManager = new GuiManager(TweetyCore.getHijackedPlugin());
            }
            guiManager.showGUI((Player) sender, new TweetzyCoreOverviewGUI());
        } else {
            sender.sendMessage("/tweetzy info");
        }
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "tweetzy.admin";
    }

    @Override
    public String getSyntax() {
        return "/tweetzy";
    }

    @Override
    public String getDescription() {
        return "View current data about Tweetzy plugins.";
    }
}
