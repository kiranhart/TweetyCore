package ca.tweetzy.core.commands;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

public class SimpleNestedCommand {

    final AbstractCommand parent;
    final LinkedHashMap<String, AbstractCommand> children = new LinkedHashMap();

    protected SimpleNestedCommand(AbstractCommand parent) {
        this.parent = parent;
    }

    public SimpleNestedCommand addSubCommand(AbstractCommand command) {
        command.getCommands().stream().forEach(cmd -> children.put(cmd.toLowerCase(), command));
        return this;
    }

    public SimpleNestedCommand addSubCommands(AbstractCommand... commands) {
        Stream.of(commands).forEach(command -> command.getCommands().stream().forEach(cmd -> children.put(cmd.toLowerCase(), command)));
        return this;
    }

}