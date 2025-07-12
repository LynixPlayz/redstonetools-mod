package tools.redstone.redstonetools.macros.gui.malilib;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;

import java.io.File;

public class Configs implements IConfigHandler {
	private static final String CONFIG_FILE_NAME = "MacroConfig.json";

	public static class Generic {
		public static final ConfigHotkey        MACRO_EDITOR                        = new ConfigHotkey("macros", "", "Macros");

		public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(MACRO_EDITOR);
	}


	public static void loadFromFile()
	{
		File configFile = new File(FileUtils.getConfigDirectory(), CONFIG_FILE_NAME);

		if (configFile.exists() && configFile.isFile() && configFile.canRead())
		{
			JsonElement element = JsonUtils.parseJsonFile(configFile);

			if (element != null && element.isJsonObject())
			{
				JsonObject root = element.getAsJsonObject();
				ConfigUtils.readConfigBase(root, "Generic", Configs.Generic.OPTIONS);
			}
		}
	}

	public static void saveToFile()
	{
		File dir = FileUtils.getConfigDirectory();

		if ((dir.exists() && dir.isDirectory()) || dir.mkdirs())
		{
			JsonObject root = new JsonObject();
			ConfigUtils.writeConfigBase(root, "Generic", Configs.Generic.OPTIONS);
			JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
		}
	}

	@Override
	public void load()
	{
		loadFromFile();
	}

	@Override
	public void save()
	{
		saveToFile();
	}
}
