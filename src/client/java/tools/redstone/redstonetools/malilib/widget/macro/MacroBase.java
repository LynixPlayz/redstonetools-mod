package tools.redstone.redstonetools.malilib.widget.macro;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import tools.redstone.redstonetools.macros.actions.Action;
import tools.redstone.redstonetools.macros.actions.CommandAction;
import tools.redstone.redstonetools.malilib.KeybindHandler;
import tools.redstone.redstonetools.malilib.config.MacroManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MacroBase {
	private static final Codec<KeybindSettings> KEYBIND_SETTINGS_CODEC = new Codec<>() {
		@Override
		public <T> DataResult<T> encode(KeybindSettings keybindSettings, DynamicOps<T> dynamicOps, T t) {
			return DataResult.success(JsonOps.INSTANCE.convertTo(dynamicOps, keybindSettings.toJson()));
		}

		@Override
		public <T> DataResult<Pair<KeybindSettings, T>> decode(DynamicOps<T> dynamicOps, T t) {
			JsonElement jsonElement = dynamicOps.convertTo(JsonOps.INSTANCE, t);
			KeybindSettings keybindSettings1 = !jsonElement.isJsonObject() ? KeybindSettings.PRESS_ALLOWEXTRA : KeybindSettings.fromJson(jsonElement.getAsJsonObject());
			return DataResult.success(Pair.of(keybindSettings1, dynamicOps.empty()));
		}
	};
	public static final Codec<MacroBase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("name", "").forGetter(macro -> macro.name),
		Codec.STRING.optionalFieldOf("keybind", "").forGetter(macro -> macro.hotkey.getStringValue()),
		KEYBIND_SETTINGS_CODEC.optionalFieldOf("keybindSettings", KeybindSettings.PRESS_ALLOWEXTRA).forGetter(macro -> macro.hotkey.getKeybind().getSettings()),
		CommandAction.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(macro -> macro.actions),
		Codec.BOOL.optionalFieldOf("enabled", true).forGetter(macro -> macro.enabled),
		Codec.BOOL.optionalFieldOf("muted", false).forGetter(macro -> macro.muted)
	).apply(instance, MacroBase::new));

	public ConfigHotkey hotkey;
	public boolean muted;
	protected String name;
	protected boolean enabled;
	public KeybindHandler handler;
	public List<CommandAction> actions;

	public MacroBase(String name, String keybind, KeybindSettings keybindSettings, List<CommandAction> actions) {
		this(name, keybind, keybindSettings, actions, true, false);
	}

	public MacroBase(String name, String keybind, KeybindSettings keybindSettings, List<CommandAction> actions, boolean enabled, boolean muted) {
		this.actions = new ArrayList<>(actions);
		this.hotkey = new ConfigHotkey("Hotkey", keybind, keybindSettings, "Pressing this hotkey will activate the macro");
		this.hotkey.getKeybind().setCallback((t, g) -> {
			this.run();
			return true;
		});
		this.name = name;
		this.enabled = enabled;
		this.muted = muted;
		this.handler = new KeybindHandler(this);
		InputEventHandler.getKeybindManager().registerKeybindProvider(this.handler);
		InputEventHandler.getInputManager().registerKeyboardInputHandler(this.handler);
		InputEventHandler.getInputManager().registerMouseInputHandler(this.handler);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	private final AtomicInteger layers = new AtomicInteger(0);

	public void run() {
		if (muted) MacroManager.shouldMute = true;
		if (!enabled) return;
		if (layers.getAndSet(layers.get() + 1) > 100) {
			MinecraftClient.getInstance().player.sendMessage(Text.of("Please don't cause a stackoverflow :("), false);
			return;
		}
		try {
			for (Action action : actions) {
				action.run();
			}
		} catch (StackOverflowError ignored) {
			try {
				MinecraftClient.getInstance().player.sendMessage(Text.of("Please don't cause a stackoverflow :("), false);
			} catch (NoClassDefFoundError e) {
				// yeah we are absolutely cooked, there is no way to recover from this. I'm not even sure this can happen
				// actually there's probably a better throwable to be thrown here. whatever.
				throw new InternalError("Something has gone terribly wrong. Shouldn't have run a macro that runs itself.", e);
			}
		} finally {
			layers.set(0);
		}
		if (muted) MacroManager.shouldMute = false;
	}
}
