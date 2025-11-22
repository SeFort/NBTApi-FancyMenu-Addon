package lol.sefort.nbtapi.fancymenu;

import lol.sefort.nbtapi.fancymenu.placeholders.*;
import lol.sefort.nbtapi.fancymenu.actions.*;
import lol.sefort.nbtapi.fancymenu.requirements.*;
import de.keksuccino.fancymenu.customization.placeholder.PlaceholderRegistry;
import de.keksuccino.fancymenu.customization.action.ActionRegistry;
import de.keksuccino.fancymenu.customization.loadingrequirement.LoadingRequirementRegistry;
import net.fabricmc.api.ClientModInitializer;

public class NBTApiFancyMenuAddon implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		registerPlaceholders();
		registerActions();
		registerRequirements();
	}

	private void registerPlaceholders() {
		PlaceholderRegistry.register(new NBTValuePlaceholder());
	}

	private void registerActions() {
		ActionRegistry.register(new SetNBTAction());
	}

	private void registerRequirements() {
		LoadingRequirementRegistry.register(new NBTValueRequirement());
	}
}