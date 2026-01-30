package tools.redstone.redstonetools.malilib.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import net.fabricmc.loader.api.FabricLoader;
import tools.redstone.redstonetools.macros.actions.CommandAction;
import tools.redstone.redstonetools.malilib.widget.macro.MacroBase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MacroManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static boolean shouldMute;
	private static final Path MACROS_FILE_PATH = FabricLoader.getInstance().getConfigDir()
		.resolve("redstonetools")
		.resolve("macros.json");
	private static List<MacroBase> macros = new ArrayList<>();

	public static List<MacroBase> getAllMacros() {
		return macros;
	}

	public static void saveChanges() {
		JsonElement jsonElement = MacroBase.CODEC.listOf().encodeStart(JsonOps.INSTANCE, macros).getOrThrow();
		try {
			if (MACROS_FILE_PATH.getParent() != null) Files.createDirectories(MACROS_FILE_PATH.getParent());

			Path tmp = MACROS_FILE_PATH.resolveSibling(MACROS_FILE_PATH.getFileName().toString() + ".tmp");
			try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
				GSON.toJson(jsonElement, writer);
			}
			Files.move(tmp, MACROS_FILE_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException ignored) {
		}
	}

	public static void loadMacros() {
		if (!Files.exists(MACROS_FILE_PATH)) {
			macros = getDefaultMacros();
			return;
		}
		try (BufferedReader reader = Files.newBufferedReader(MACROS_FILE_PATH, StandardCharsets.UTF_8)) {
			List<MacroBase> macrosFromFile = MacroBase.CODEC.listOf().parse(JsonOps.INSTANCE, GSON.fromJson(reader, JsonElement.class)).result().orElse(null);
			if (macrosFromFile == null) {
				macros = getDefaultMacros();
				return;
			}
			macros.addAll(macrosFromFile);
		} catch (IOException ignored) {
			macros = getDefaultMacros();
		}
	}

	public static List<MacroBase> getMacros() {
		return macros;
	}

	public static MacroBase getMacro(String name) {
		for (MacroBase macro : macros) {
			if (macro.getName().equals(name)) {
				return macro;
			}
		}

		return null;
	}

	public static boolean nameExists(String name, MacroBase exclude) {
		for (MacroBase macro : macros) {
			if (macro == exclude) continue;
			if (macro.getName().equals(name)) return true;
		}
		return false;
	}

	public static List<MacroBase> getDefaultMacros() {
		return new ArrayList<>(List.of(
			createCommandMacro("redstoner", new String[]{
				"/gamerule doTileDrops false",
				"/gamerule doTraderSpawning false",
				"/gamerule doWeatherCycle false",
				"/gamerule doDaylightCycle false",
				"/gamerule doMobSpawning false",
				"/gamerule doContainerDrops false",
				"/time set noon",
				"/weather clear"
			}),
			createCommandMacro("copystate", new String[]{"/copystate" }),
			createCommandMacro("itembind", new String[]{"/itembind" }),
			createCommandMacro("minsel", new String[]{"//minsel" }),
			createCommandMacro("quicktp", new String[]{"/quicktp" }),
			createCommandMacro("binaryblockread", new String[]{"//binaryblockread" }),
			createCommandMacro("rstack", new String[]{"//rstack" }),
			createCommandMacro("update", new String[]{"//update" })
		));
	}

	public static MacroBase createCommandMacro(String name, String[] commands) {
		var actions = new CommandAction[commands.length];
		for (int i = 0; i < commands.length; i++) {
			actions[i] = new CommandAction(commands[i]);
		}

		return new MacroBase(name, "", KeybindSettings.PRESS_ALLOWEXTRA, List.of(actions));
	}

	public static void removeMacro(MacroBase macro) {
		macros.remove(macro);
	}

	public static void addMacroToTop(MacroBase macroBase) {
		if (MacroManager.nameExists(macroBase.getName(), null)) {
			macroBase.setName(macroBase.getName() + " " + UUID.randomUUID());
		}
		macros.addFirst(macroBase);
	}

}
