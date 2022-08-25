package ca.tweetzy.core.compatibility;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public enum MethodMapping {
    MC_ITEM_STACK__GET_TAG("getTag", "getTag", "s", "t"),
    MC_ITEM_STACK__SET_TAG("setTag", "setTag", "c", "c", ClassMapping.NBT_TAG_COMPOUND.getClazz()),

    MC_NBT_TAG_COMPOUND__SET("set", "set", "a", "a", String.class, ClassMapping.NBT_BASE.getClazz()),
    MC_NBT_TAG_COMPOUND__SET_SHORT("setShort", "setShort", "a", "a", String.class, short.class),
    MC_NBT_TAG_COMPOUND__SET_STRING("setString", "setString", "a", "a", String.class, String.class),
    MC_NBT_TAG_COMPOUND__REMOVE("remove", "remove", "r", "r", String.class),

    MC_NBT_TAG_LIST__ADD("add", "a", "add", "a", "add", ClassMapping.NBT_BASE.getClazz()),

    MC_CHUNK__GET_WORLD("getWorld", "getWorld", "D"),

    CB_GENERIC__GET_HANDLE("getHandle"),

    CB_BLOCK__GET_NMS("getNMSBlock", "getNMS", "getNMS"),
    CB_BLOCK__GET_POSITION("getPosition"),

    CB_BLOCK_DATA__GET_STATE("getState"),

    CB_ITEM_STACK__AS_NMS_COPY("asNMSCopy", ItemStack.class),
    CB_ITEM_STACK__AS_CRAFT_MIRROR("asCraftMirror", ClassMapping.ITEM_STACK.getClazz()),

    CRAFT_MAGIC_NUMBERS__GET_BLOCK__MATERIAL("getBlock", Material.class),

    I_BLOCK_DATA__GET_BLOCK("getBlock", "b", "b"),

    BLOCK__GET_BLOCK_DATA("getBlockData", "n", "n"),

    CHUNK__SET_BLOCK_STATE("setType", "setBlockState", "a", ClassMapping.BLOCK_POSITION.getClazz(), ClassMapping.I_BLOCK_DATA.getClazz(), boolean.class, boolean.class),

    ITEM_STACK__SAVE("save", "b", "b", ClassMapping.NBT_TAG_COMPOUND.getClazz()),
    ITEM_STACK__GET_ITEM("getItem", "c", "c"),
    ITEM_STACK__GET_MAX_STACK_SIZE("getMaxStackSize", "l", "e"),

    WORLD__UPDATE_ADJACENT_COMPARATORS("updateAdjacentComparators", "c", "c", ClassMapping.BLOCK_POSITION.getClazz(), ClassMapping.BLOCK.getClazz()),
    WORLD__GET_CHUNK_AT("getChunkAt", "d", "a", int.class, int.class),

    WORLD_BOARDER__SET_CENTER("setCenter", "setCenter", "setCenter", "c", "c", double.class, double.class),
    WORLD_BOARDER__SET_SIZE("setSize", "setSize", "setSize", "a", "a", double.class),
    WORLD_BOARDER__SET_WARNING_TIME("setWarningTime", "setWarningTime", "setWarningTime", "b", "b", int.class),
    WORLD_BOARDER__SET_WARNING_DISTANCE("setWarningDistance", "setWarningDistance", "setWarningDistance", "c", "c", int.class),
    WORLD_BOARDER__TRANSITION_SIZE_BETWEEN("transitionSizeBetween", "transitionSizeBetween", "transitionSizeBetween", "a", "a", double.class, double.class, long.class),

    MOJANGSON_PARSER__PARSE("parse", "a", "a", String.class);

    private final String saneFallback;
    private final String _1_14;
    private final String _1_17;
    private final String _1_18;
    private final String _1_18_2;
    private final Class<?>[] parameters;

    MethodMapping(String saneFallback, String _1_14, String _1_17, String _1_18, String _1_18_2, Class<?>... parameters) {
        this.saneFallback = saneFallback;

        this._1_14 = _1_14;
        this._1_17 = _1_17;
        this._1_18 = _1_18;
        this._1_18_2 = _1_18_2;
        this.parameters = parameters;
    }

    MethodMapping(String saneFallback, String _1_17, String _1_18, String _1_18_2, Class<?>... parameters) {
        this.saneFallback = saneFallback;

        this._1_14 = null;
        this._1_17 = _1_17;
        this._1_18 = _1_18;
        this._1_18_2 = _1_18_2;
        this.parameters = parameters;
    }

    MethodMapping(String saneFallback, String _1_18, String _1_18_2, Class<?>... parameters) {
        this.saneFallback = saneFallback;

        this._1_14 = null;
        this._1_17 = null;
        this._1_18 = _1_18;
        this._1_18_2 = _1_18_2;
        this.parameters = parameters;
    }

    MethodMapping(String saneFallback, Class<?>... parameters) {
        this.saneFallback = saneFallback;

        this._1_14 = null;
        this._1_17 = null;
        this._1_18 = null;
        this._1_18_2 = null;
        this.parameters = parameters;
    }

    public Method getMethod(Class<?> clazz) {
        try {
            String methodName = _1_18;
            switch (ServerVersion.getServerVersion()) {
                case V1_14:
                    if (_1_14 != null) {
                        methodName = _1_14;
                    }

                    break;
                case V1_17:
                    if (_1_17 != null) {
                        methodName = _1_17;
                    }

                    break;
                case V1_18:
                    if (_1_18_2 != null) {
                        methodName = _1_18_2;
                    }
            }

            try {
                Method method = clazz.getMethod(methodName, parameters);
                method.setAccessible(true);

                return method;
            } catch (NullPointerException | NoSuchMethodException ex) {
                if (saneFallback != null && !saneFallback.equals(methodName)) {
                    try {
                        Method method = clazz.getMethod(saneFallback, parameters);
                        method.setAccessible(true);

                        return method;
                    } catch (NoSuchMethodException innerEx) {
                        ex.printStackTrace();
                        innerEx.printStackTrace();
                    }
                } else {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}