package lol.sefort.nbtapi.fancymenu.requirements;

import de.keksuccino.fancymenu.customization.loadingrequirement.LoadingRequirement;
import de.keksuccino.fancymenu.util.rendering.ui.screen.texteditor.TextEditorFormattingRule;
import lol.sefort.nbtapi.tags.*;
import lol.sefort.nbtapi.world.MinecraftWorld;
import lol.sefort.nbtapi.world.WorldScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class NBTValueRequirement extends LoadingRequirement {

    public NBTValueRequirement() {
        super("nbt_value_check");
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public boolean isRequirementMet(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            System.err.println("[NBTApi FancyMenu] No value specified for requirement!");
            return false;
        }

        try {
            String[] parts = value.split("\\|");

            if (parts.length < 4) {
                System.err.println("[NBTApi FancyMenu] Invalid format! Use: world|path|operator|value");
                return false;
            }

            String worldName = parts[0].trim();
            String path = parts[1].trim();
            String operator = parts[2].trim();
            String expectedValue = parts[3].trim();

            WorldScanner scanner = WorldScanner.createDefault();
            scanner.scan();

            MinecraftWorld world = scanner.findWorldByName(worldName);
            if (world == null) {
                world = scanner.findWorldByFolder(worldName);
            }

            if (world == null) {
                System.err.println("[NBTApi FancyMenu] World not found: " + worldName);
                return false;
            }

            NBTTagCompound root = world.getLevelDat().getRoot();
            String actualValue = getValueByPath(root, path);

            if (actualValue == null) {
                return false;
            }

            return compareValues(actualValue, operator, expectedValue);

        } catch (Exception e) {
            System.err.println("[NBTApi FancyMenu] Error checking NBT: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "NBT Value Check";
    }

    @Nullable
    @Override
    public List<String> getDescription() {
        return Arrays.asList(
                "Checks the value in the world's level.dat NBT file",
                "",
                "Format: world|path|operator|value",
                "",
                "Operators:",
                "  ==  - equally",
                "  !=  - not equal",
                "  >   - greater",
                "  <   - less",
                "  >=  - greater than or equal to",
                "  <=  - less than or equal to",
                "  contains - (for strings)",
                "",
                "Examples:",
                "My World|Data.GameType|==|1",
                "My World|Data.LevelName|contains|Test",
                "My World|Data.Difficulty|>|0"
        );
    }

    @Nullable
    @Override
    public String getCategory() {
        return "NBT API";
    }

    @Nullable
    @Override
    public String getValueDisplayName() {
        return "NBT Requirement parameters";
    }

    @Nullable
    @Override
    public String getValuePreset() {
        return "My World|Data.GameType|==|1";
    }

    @Nullable
    @Override
    public List<TextEditorFormattingRule> getValueFormattingRules() {
        return null;
    }

    private String getValueByPath(NBTTagCompound root, String path) {
        String[] parts = path.split("\\.");

        NBTTagCompound current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];

            if (!current.hasKey(part)) {
                return null;
            }

            NBTTag tag = current.getTag(part);
            if (tag instanceof NBTTagCompound) {
                current = (NBTTagCompound) tag;
            } else {
                return null;
            }
        }

        String finalKey = parts[parts.length - 1];
        if (!current.hasKey(finalKey)) {
            return null;
        }

        NBTTag tag = current.getTag(finalKey);
        return tagToString(tag);
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
        }
        return tag.toString();
    }

    private boolean compareValues(String actual, String operator, String expected) {
        switch (operator.toLowerCase()) {
            case "==":
            case "equals":
                return actual.equals(expected);

            case "!=":
            case "notequals":
                return !actual.equals(expected);

            case "contains":
                return actual.toLowerCase().contains(expected.toLowerCase());

            case ">":
            case "greater":
                try {
                    return Double.parseDouble(actual) > Double.parseDouble(expected);
                } catch (NumberFormatException e) {
                    return false;
                }

            case "<":
            case "less":
                try {
                    return Double.parseDouble(actual) < Double.parseDouble(expected);
                } catch (NumberFormatException e) {
                    return false;
                }

            case ">=":
            case "greaterorequals":
                try {
                    return Double.parseDouble(actual) >= Double.parseDouble(expected);
                } catch (NumberFormatException e) {
                    return false;
                }

            case "<=":
            case "lessorequals":
                try {
                    return Double.parseDouble(actual) <= Double.parseDouble(expected);
                } catch (NumberFormatException e) {
                    return false;
                }

            default:
                System.err.println("[NBTApi FancyMenu] Unknown operator: " + operator);
                return false;
        }
    }
}