package lol.sefort.nbtapi.fancymenu.actions;

import de.keksuccino.fancymenu.customization.action.Action;
import lol.sefort.nbtapi.tags.*;
import lol.sefort.nbtapi.world.MinecraftWorld;
import lol.sefort.nbtapi.world.WorldScanner;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetNBTAction extends Action {

    public SetNBTAction() {
        super("set_nbt");
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public void execute(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            System.err.println("[NBTApi FancyMenu] No value specified for action!");
            return;
        }

        try {
            String[] parts = value.split("\\|");

            if (parts.length < 3) {
                System.err.println("[NBTApi FancyMenu] Invalid format! Use: world|path|value|type");
                return;
            }

            String worldName = parts[0].trim();
            String path = parts[1].trim();
            String newValue = parts[2].trim();
            String type = parts.length > 3 ? parts[3].trim().toLowerCase() : "auto";

            WorldScanner scanner = WorldScanner.createDefault();
            scanner.scan();

            MinecraftWorld world = scanner.findWorldByName(worldName);
            if (world == null) {
                world = scanner.findWorldByFolder(worldName);
            }

            if (world == null) {
                System.err.println("[NBTApi FancyMenu] World not found: " + worldName);
                return;
            }

            NBTTagCompound root = world.getLevelDat().getRoot();
            setValueByPath(root, path, newValue, type);

            world.save();

            System.out.println("[NBTApi FancyMenu] Value changed: " + worldName + " -> " + path + " = " + newValue);

        } catch (Exception e) {
            System.err.println("[NBTApi FancyMenu] Error when changing NBT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public Text getActionDisplayName() {
        return Text.literal("Set NBT Value");
    }

    @NotNull
    @Override
    public Text[] getActionDescription() {
        return new Text[] {
                Text.literal("Action to change values in NBT"),
                Text.literal(""),
                Text.literal("Format: world|path|value"),
                Text.literal(""),
                Text.literal("Examples:"),
                Text.literal("My World|Data.LevelName|New Name"),
                Text.literal("My World|Data.GameType|1")
        };
    }

    @Nullable
    @Override
    public Text getValueDisplayName() {
        return Text.literal("NBT Parameters");
    }

    @Nullable
    @Override
    public String getValueExample() {
        return "My World|Data.LevelName|New Name";
    }

    private void setValueByPath(NBTTagCompound root, String path, String value, String type) {
        String[] parts = path.split("\\.");

        NBTTagCompound current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];

            if (!current.hasKey(part)) {
                NBTTagCompound newCompound = new NBTTagCompound();
                current.setTag(part, newCompound);
                current = newCompound;
            } else {
                NBTTag tag = current.getTag(part);
                if (tag instanceof NBTTagCompound) {
                    current = (NBTTagCompound) tag;
                } else {
                    throw new IllegalArgumentException("The path contains a non-compound element: " + part);
                }
            }
        }

        String finalKey = parts[parts.length - 1];
        NBTTag newTag = createTagFromString(value, type, current.getTag(finalKey));
        current.setTag(finalKey, newTag);
    }

    private NBTTag createTagFromString(String value, String type, NBTTag existingTag) {
        if ("auto".equals(type) && existingTag != null) {
            type = getTypeNameFromTag(existingTag);
        }

        switch (type.toLowerCase()) {
            case "byte":
            case "bool":
            case "boolean":
                byte b = value.equalsIgnoreCase("true") ? (byte) 1 :
                        value.equalsIgnoreCase("false") ? (byte) 0 :
                                Byte.parseByte(value);
                return new NBTTagByte(b);

            case "short":
                return new NBTTagShort(Short.parseShort(value));

            case "int":
            case "integer":
                return new NBTTagInt(Integer.parseInt(value));

            case "long":
                return new NBTTagLong(Long.parseLong(value));

            case "float":
                return new NBTTagFloat(Float.parseFloat(value));

            case "double":
                return new NBTTagDouble(Double.parseDouble(value));

            case "string":
            case "auto":
            default:
                return new NBTTagString(value);
        }
    }

    private String getTypeNameFromTag(NBTTag tag) {
        if (tag instanceof NBTTagByte) return "byte";
        if (tag instanceof NBTTagShort) return "short";
        if (tag instanceof NBTTagInt) return "int";
        if (tag instanceof NBTTagLong) return "long";
        if (tag instanceof NBTTagFloat) return "float";
        if (tag instanceof NBTTagDouble) return "double";
        if (tag instanceof NBTTagString) return "string";
        return "string";
    }
}