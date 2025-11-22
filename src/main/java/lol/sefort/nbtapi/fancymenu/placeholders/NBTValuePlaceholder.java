package lol.sefort.nbtapi.fancymenu.placeholders;

import de.keksuccino.fancymenu.customization.placeholder.DeserializedPlaceholderString;
import de.keksuccino.fancymenu.customization.placeholder.Placeholder;
import lol.sefort.nbtapi.tags.*;
import lol.sefort.nbtapi.world.MinecraftWorld;
import lol.sefort.nbtapi.world.WorldScanner;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class NBTValuePlaceholder extends Placeholder {

    public NBTValuePlaceholder() {
        super("nbt_value");
    }

    @Override
    public String getReplacementFor(DeserializedPlaceholderString dps) {
        try {
            String worldName = dps.values.getOrDefault("world", "current");
            String path = dps.values.get("path");
            String defaultValue = dps.values.getOrDefault("default", "null");

            if (path == null || path.trim().isEmpty()) {
                return "ERROR: path not specified";
            }

            MinecraftWorld world = getWorld(worldName);
            if (world == null) {
                return defaultValue;
            }

            NBTTag value = getValueByPath(world.getLevelDat().getRoot(), path);

            if (value == null) {
                return defaultValue;
            }

            return tagToString(value);

        } catch (Exception e) {
            System.err.println("[NBTApi FancyMenu] Error in placeholder: " + e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    @Nullable
    @Override
    public List<String> getValueNames() {
        return List.of("world", "path", "default");
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "NBT Value";
    }

    @Nullable
    @Override
    public List<String> getDescription() {
        return List.of(
                "Placeholder for retrieving values from NBT",
                "",
                "Parameters:",
                "  world - world folder",
                "  path - path to NBT",
                "",
                "Examples:",
                "{\"placeholder\":\"nbt_value\",\"values\":{\"path\":\"Data.LevelName\"}}",
                "{\"placeholder\":\"nbt_value\",\"values\":{\"world\":\"My World\",\"path\":\"Data.GameType\"}}"
        );
    }

    @Override
    public String getCategory() {
        return "NBT API";
    }

    @NotNull
    @Override
    public DeserializedPlaceholderString getDefaultPlaceholderString() {
        HashMap<String, String> values = new HashMap<>();
        values.put("path", "Data.LevelName");
        return new DeserializedPlaceholderString("nbt_value", values, "{\"placeholder\":\"nbt_value\",\"values\":{\"path\":\"Data.LevelName\"}}");
    }

    private MinecraftWorld getWorld(String worldName) throws Exception {
        if ("current".equalsIgnoreCase(worldName)) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world != null && mc.getServer() != null) {
                String levelName = mc.getServer().getSaveProperties().getLevelName();
                File savesFolder = new File(mc.runDirectory, "saves");
                File worldFolder = new File(savesFolder, levelName);
                return new MinecraftWorld(worldFolder);
            }
            return null;
        }

        WorldScanner scanner = WorldScanner.createDefault();
        scanner.scan();

        MinecraftWorld world = scanner.findWorldByName(worldName);
        if (world == null) {
            world = scanner.findWorldByFolder(worldName);
        }

        return world;
    }

    private NBTTag getValueByPath(NBTTagCompound root, String path) {
        String[] parts = path.split("\\.");
        NBTTag current = root;

        for (String part : parts) {
            if (current instanceof NBTTagCompound) {
                current = ((NBTTagCompound) current).getTag(part);
                if (current == null) {
                    return null;
                }
            } else if (current instanceof NBTTagList) {
                try {
                    int index = Integer.parseInt(part);
                    current = ((NBTTagList) current).get(index);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return current;
    }

    private String tagToString(NBTTag tag) {
        if (tag instanceof NBTTagByte) {
            return String.valueOf(((NBTTagByte) tag).getValue());
        } else if (tag instanceof NBTTagShort) {
            return String.valueOf(((NBTTagShort) tag).getValue());
        } else if (tag instanceof NBTTagInt) {
            return String.valueOf(((NBTTagInt) tag).getValue());
        } else if (tag instanceof NBTTagLong) {
            return String.valueOf(((NBTTagLong) tag).getValue());
        } else if (tag instanceof NBTTagFloat) {
            return String.valueOf(((NBTTagFloat) tag).getValue());
        } else if (tag instanceof NBTTagDouble) {
            return String.valueOf(((NBTTagDouble) tag).getValue());
        } else if (tag instanceof NBTTagString) {
            return ((NBTTagString) tag).getValue();
        } else if (tag instanceof NBTTagCompound) {
            return "{compound}";
        } else if (tag instanceof NBTTagList) {
            return "[list:" + ((NBTTagList) tag).size() + "]";
        }
        return tag.toSNBT();
    }
}