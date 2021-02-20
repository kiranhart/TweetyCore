package ca.tweetzy.core.commands;

import ca.tweetzy.core.compatibility.EntityNamespace;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectorArguments {

    static Pattern selectorPattern = Pattern.compile("^(@[apers])(\\[(.*?)\\])?$");
    static Pattern selectorRangePattern = Pattern.compile("^([0-9]{1,9}(\\.[0-9]{1,9})?)?(\\.\\.)?([0-9]{1,9}(\\.[0-9]{1,9})?)?$");

    /**
     * Parse a command selector using Minecraft's selector format. <br>
     * Currently only supports distance and entity type
     *
     * @param sender   CommandBlock or Player running the command
     * @param argument argument with the selector to parse
     * @return SelectorArguments Object for grabbing the list of entities, or null if the selector is invalid
     */
    @Nullable
    public static SelectorArguments parseSelector(@NotNull CommandSender sender, @NotNull String argument) {
        if (!(sender instanceof BlockCommandSender || sender instanceof Player)) {
            return null;
        }
        Matcher m = selectorPattern.matcher(argument);
        if (!m.find()) {
            return null;
        }
        SelectorType type = SelectorType.getType(m.group(1));
        if (type == null) {
            return null;
        }
        SelectorArguments selector = new SelectorArguments(sender, type);

        if (m.group(3) != null) {
            selector.parseArguments(m.group(3));
        }

        return selector;
    }

    protected final CommandSender sender;
    protected final SelectorType selector;
    protected double rangeMin = 0, rangeMax = Double.POSITIVE_INFINITY;
    protected EntityType entityType;

    public SelectorArguments(CommandSender sender, SelectorType type) {
        this.sender = sender;
        this.selector = type;
    }

    private void parseArguments(String selectorArgs) {
        String[] args = selectorArgs.split(",");
        for (String s : args) {
            if (s.contains("=")) {
                String[] v = s.split("=");
                if (v[0].equals("distance")) {
                    // 10 = d == 10
                    // 10..12 = d > 10 && d <= 12
                    // 5.. = d >= 5
                    // ..5 = d <= 15
                    Matcher distGroup = selectorRangePattern.matcher(v[1]);
                    if (distGroup.find()) {
                        if (distGroup.group(1) != null) {
                            rangeMin = Double.parseDouble(distGroup.group(1));
                        }
                        if (distGroup.group(3) == null) {
                            rangeMax = rangeMin;
                        } else if (distGroup.group(4) != null) {
                            rangeMax = Double.parseDouble(distGroup.group(4));
                        }
                    }
                } else if (v[0].equals("type")) {
                    entityType = EntityNamespace.minecraftToBukkit(v[1]);
                }
                // more arguments can be parsed here (TODO)
            }
        }
        /*
         advancements 	Advancement earned by entity.
         distance 	Distance to entity.
         dx         Entities between x and x + dx.
         dy         Entities between y and y + dy.
         dz         Entities between z and z + dz.
         gamemode 	Players with gamemode. It can be one of the following values: adventure, creative, spectator, survival, !adventure, !creative, !spectator, !survival
         level      Experience level. It must be an integer value that is 0 or greater.
         limit      Maximum number of entities to target. It must be an integer value that is 1 or greater.
         name       Entity name.
         nbt        NBT tag.
         scores 	Score.
         sort       Sort the entities. It must be one of the following values: arbitrary, furthest, nearest, random
         tag        Scoreboard tag.
         team       Entities on team.
         type       Entity type (target must be the specified entity type - https://www.digminecraft.com/lists/entity_list_pc.php ).
         x          Entity's x-coordinate position.
         x_rotation Entity's x rotation (vertical rotation).
         y          Entity's y-coordinate position.
         y_rotation Entity's y rotation (horizontal rotation).
         z          Entity's z-coordinate position.
         target selector arguments are case-sensitive
         @e[type=cow,limit=5]
         */
    }

    public Collection<Entity> getSelection() {
        final Location location = sender instanceof Player ? ((Player) sender).getLocation() : ((BlockCommandSender) sender).getBlock().getLocation();
        Collection<Entity> list = preSelect(location);
        if (list.isEmpty()) {
            return list;
        }
        List<Entity> list2 = filter(location, list);
        if (list2.isEmpty()) {
            return list2;
        }
        switch (selector) {
            case PLAYER:
                Collections.sort(list2, (o1, o2) -> (int) (o1.getLocation().distanceSquared(location) - o2.getLocation().distanceSquared(location)));
                return Arrays.asList(list2.get(0));
            case RANDOM_PLAYER:
                Collections.shuffle(list2);
                return Arrays.asList(list2.get(0));
            case ALL_PLAYER:
            case ALL_ENTITIES:
            case SELF:
                return list2;
        }
        return list2;
    }

    protected Collection<Entity> preSelect(Location location) {
        switch (selector) {
            case PLAYER:
            case RANDOM_PLAYER:
            case ALL_PLAYER:
                return rangeMax == Double.POSITIVE_INFINITY
                        ? location.getWorld().getEntitiesByClasses(Player.class)
                        : location.getWorld().getNearbyEntities(location, rangeMax * 2, rangeMax * 2, rangeMax * 2).stream()
                        .filter(e -> e instanceof Player).collect(Collectors.toSet());
            case ALL_ENTITIES:
                return rangeMax == Double.POSITIVE_INFINITY
                        ? location.getWorld().getEntities()
                        : location.getWorld().getNearbyEntities(location, rangeMax * 2, rangeMax * 2, rangeMax * 2);
            case SELF:
                return sender instanceof Entity ? Arrays.asList((Entity) sender) : Collections.EMPTY_LIST;
        }
        return Collections.EMPTY_LIST;
    }

    protected List<Entity> filter(Location location, Collection<Entity> list) {
        Stream<Entity> stream = list.stream()
                .filter(p -> rangeMin == 0 || p.getLocation().distance(location) > rangeMin)
                .filter(e -> entityType == null || e.getType() == entityType);
        return stream.collect(Collectors.toList());
    }

    public static enum SelectorType {

        PLAYER, RANDOM_PLAYER, ALL_PLAYER, ALL_ENTITIES, SELF;

        public static SelectorType getType(String str) {
            if (str != null) {
                switch (str.toLowerCase()) {
                    case "@p":
                        return PLAYER;
                    case "@r":
                        return RANDOM_PLAYER;
                    case "@a":
                        return ALL_PLAYER;
                    case "@e":
                        return ALL_ENTITIES;
                    case "@s":
                        return SELF;
                }
            }
            return null;
        }
    }
}