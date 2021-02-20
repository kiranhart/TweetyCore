package ca.tweetzy.core.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCommand {

    private final CommandType _cmdType;
    private final boolean _hasArgs;
    private final List<String> _handledCommands = new ArrayList<>();

    protected AbstractCommand(CommandType type, String... command) {
        this._handledCommands.addAll(Arrays.asList(command));
        this._hasArgs = false;
        this._cmdType = type;
    }

    public final List<String> getCommands() {
        return Collections.unmodifiableList(_handledCommands);
    }

    public final void addSubCommand(String command) {
        _handledCommands.add(command);
    }

    protected abstract ReturnType runCommand(CommandSender sender, String... args);

    protected abstract List<String> onTab(CommandSender sender, String... args);

    public abstract String getPermissionNode();

    public abstract String getSyntax();

    public abstract String getDescription();

    public boolean isNoConsole() {
        return _cmdType == CommandType.PLAYER_ONLY;
    }

    public static enum ReturnType {SUCCESS, NEEDS_PLAYER, FAILURE, SYNTAX_ERROR}
    public static enum CommandType {PLAYER_ONLY, CONSOLE_OK}
}