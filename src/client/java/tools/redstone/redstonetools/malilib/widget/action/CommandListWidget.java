package tools.redstone.redstonetools.malilib.widget.action;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
/*$ click_and_inputs_imports {*/
//
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;/*$}*/
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import tools.redstone.redstonetools.macros.actions.CommandAction;
import tools.redstone.redstonetools.malilib.GuiMacroEditor;
import tools.redstone.redstonetools.malilib.widget.macro.MacroBase;
import tools.redstone.redstonetools.mixin.features.ChatInputSuggestorAccessor;
import tools.redstone.redstonetools.mixin.features.SuggestionWindowAccessor;
import tools.redstone.redstonetools.mixin.features.TextFieldWidgetAccessor;

public class CommandListWidget extends EntryListWidget<CommandListWidget.CommandEntry> {
	private final GuiMacroEditor parent;
	@Nullable
	private ChatInputSuggestor commandSuggester;
	private final MacroBase macro;

	public CommandListWidget(GuiMacroEditor parent, MinecraftClient mc, int width, int height, int y, int itemHeight, MacroBase macro) {
		super(mc, width, height, y, itemHeight);
		this.parent = parent;
		this.macro = macro;
		for (CommandAction commandAction : this.macro.actions) {
			CommandEntry entry = new CommandEntry(commandAction);
			this.addEntry(entry);
			entry.afterAdded();
			this.setSelected(entry);
		}
		this.setSelected(null);
	}

	@Override
	public void setSelected(@Nullable CommandListWidget.CommandEntry entry) {
		if (this.getSelectedOrNull() != null) {
			this.getSelectedOrNull().commandWidget.setSuggestion(null);
		}
		super.setSelected(entry);
		if (entry == null) {
			this.commandSuggester = null;
			return;
		}
		((TextFieldWidgetAccessor) entry.commandWidget).getFormatters().clear();
		this.commandSuggester = new ChatInputSuggestor(
			client,
			this.parent,
			entry.commandWidget,
			client.textRenderer,
			false,
			false,
			0,
			10,
			false,
			0xD0000000
		) {
			@Override
			public void refresh() {
				if (client == null) return;
				if (client.getNetworkHandler() == null) return;
				super.refresh();
			}

			@Override
			public void renderMessages(DrawContext context) {
				context.getMatrices().pushMatrix();
				context.getMatrices().translate(0, entry.commandWidget.getY() + 20 - 72);
				super.renderMessages(context);
				context.getMatrices().popMatrix();
			}
		};
		this.commandSuggester.setWindowActive(true);
		this.commandSuggester.refresh();
	}

	@Override
	public void setScrollY(double scrollY) {
		super.setScrollY(scrollY);
		if (this.commandSuggester != null && this.getSelectedOrNull() != null) {
			ChatInputSuggestor.SuggestionWindow window = ((ChatInputSuggestorAccessor) this.commandSuggester).getWindow();
			if (window == null) return;
			Rect2i area = ((SuggestionWindowAccessor) window).getArea();
			area.setY(this.getSelectedOrNull().getY() + 20);
		}
	}

	@Override
	public boolean mouseClicked(/*$ mouse_clicked_params {*/Click click, boolean doubleClick/*$}*/) {
		if (this.commandSuggester != null && this.commandSuggester.mouseClicked(/*? if <1.21.10 {*//*mouseX, mouseY, button*//*?} else {*/click/*?}*/)) return true;
		return super.mouseClicked(/*$ mouse_clicked_args {*/click, doubleClick/*$}*/);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.commandSuggester != null && this.commandSuggester.mouseScrolled(verticalAmount)) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(/*$ keyinput_params {*/KeyInput input/*$}*/) {
		if (this.commandSuggester != null && this.commandSuggester.keyPressed(/*$ keyinput_args {*/input/*$}*/)) return true;
		return super.keyPressed(/*$ keyinput_args {*/input/*$}*/);
	}

	@Override
	protected void renderList(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.renderList(context, mouseX, mouseY, deltaTicks);
		if (this.commandSuggester != null) {
			this.commandSuggester.render(context, mouseX, mouseY);
		}
	}

	@Override
	protected boolean isEntrySelectionAllowed() {
		return false;
	}

	@Override
	public int getRowWidth() {
		return this.width - 50;
	}

	public void addEntry() {
		this.macro.actions.addFirst(new CommandAction(""));
		CommandEntry entry = new CommandEntry(this.macro.actions.getFirst());
		this.addEntryToTop(entry);
		this.centerScrollOn(this.getFirst());
		entry.afterAdded();
		if (this.commandSuggester != null) {
			this.commandSuggester.refresh();
		}
	}

	//? if >=1.21.10 {
	private CommandEntry getFirst() {
		return this.children().getFirst();
	}
	//?}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {

	}

	public class CommandEntry extends Entry<CommandEntry> {
		public final CommandAction command;
		private TextFieldWidget commandWidget;
		private ButtonBase removeButton;

		public CommandEntry(CommandAction command) {
			this.command = command;
		}

		private void onCommandChanged(String text) {
			command.command = text;
			if (CommandListWidget.this.commandSuggester != null) {
				CommandListWidget.this.commandSuggester.refresh();
			}
		}

		public void afterAdded() {
			this.commandWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.getX() + 4, this.getY() + 3, this.getWidth() - 100, 26, Text.of(""));
			commandWidget.setMaxLength(256);
			commandWidget.setText(command.command);
			commandWidget.setChangedListener(this::onCommandChanged);

			this.removeButton = new ButtonGeneric(0, this.getY() + 6, -1, 20, "Remove");
			this.removeButton.setX(this.getX() + this.getWidth() - this.removeButton.getWidth() - 10);
			this.removeButton.setActionListener((button, mouseButton) -> {
				CommandListWidget.this.macro.actions.remove(CommandListWidget.this.children().indexOf(this));
				CommandListWidget.this.removeEntry(this);
			});
		}

		@Override
		public void render(DrawContext context, /*? if <1.21.10 {*/ /*int index, int y, int x, int entryWidth, int entryHeight, *//*?}*/ int mouseX, int mouseY, boolean hovered, float tickProgress) {
			this.commandWidget.setX(this.getX() + 4);
			this.commandWidget.setY(this.getY() + 3);
			commandWidget.render(context, mouseX, mouseY, tickProgress);

			this.removeButton.setY(this.getY() + 6);
			this.removeButton.setX(this.getX() + this.getWidth() - this.removeButton.getWidth() - 10);
			//? if <=1.21.5 {
			/*removeButton.render(mouseX, mouseY, removeButton.isMouseOver(), context);
			*///?} else {
			removeButton.render(context, mouseX, mouseY, removeButton.isMouseOver());
			//?}
		}

		@Override
		public void setFocused(boolean focused) {
			commandWidget.setFocused(focused);
		}

		@Override
		public void mouseMoved(double mouseX, double mouseY) {
			super.mouseMoved(mouseX, mouseY);
			commandWidget.mouseMoved(mouseX, mouseY);
		}

		@Override
		public boolean mouseClicked(/*$ mouse_clicked_params {*/Click click, boolean doubleClick/*$}*/) {
			if (commandWidget.mouseClicked(/*$ mouse_clicked_args {*/click, doubleClick/*$}*/)) return true;
			if (removeButton.onMouseClicked(/*$ on_mouse_clicked_args {*/click, doubleClick/*$}*/)) return true;
			return false;
		}

		@Override
		public boolean mouseReleased(/*$ dragged_released_params {*/Click click/*$}*/) {
			removeButton.onMouseReleased(/*$ on_released_args {*/click/*$}*/);
			return commandWidget.mouseReleased(/*$ dragged_released_args {*/click/*$}*/);
		}

		@Override
		public boolean mouseDragged(/*$ dragged_released_params {*/Click click/*$}*/, double deltaX, double deltaY) {
			return commandWidget.mouseDragged(/*$ dragged_released_args {*/click/*$}*/, deltaX, deltaY);
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
			if (commandWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
			if (removeButton.onMouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
			return false;
		}

		@Override
		public boolean keyPressed(/*$ keyinput_params {*/KeyInput input/*$}*/) {
			if (commandWidget.keyPressed(/*$ keyinput_args {*/input/*$}*/)) return true;
			if (removeButton.onKeyTyped(/*$ keyinput_args {*/input/*$}*/)) return true;
			return false;
		}

		@Override
		public boolean keyReleased(/*$ keyinput_params {*/KeyInput input/*$}*/) {
			return commandWidget.keyReleased(/*$ keyinput_args {*/input/*$}*/);
		}

		@Override
		public boolean charTyped(/*$ charinput_params {*/CharInput input/*$}*/) {
			if (commandWidget.charTyped(/*$ charinput_args {*/input/*$}*/)) return true;
			if (removeButton.onCharTyped(/*$ charinput_args {*/input/*$}*/)) return true;
			return false;
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			if (commandWidget.isMouseOver(mouseX, mouseY)) return true;
			if (removeButton.isMouseOver((int) mouseX, (int) mouseY)) return true;
			return false;
		}
	}
}
