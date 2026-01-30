package tools.redstone.redstonetools.mixin.features;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tools.redstone.redstonetools.utils.StringUtils;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public class SuggestionWindowMixin {
	@Shadow
	@Final
	ChatInputSuggestor field_21615;

	@WrapMethod(method = "complete")
	private void expandVariablesToo(Operation<Void> original) {
		TextFieldWidget textField = ((ChatInputSuggestorAccessor) this.field_21615).getTextField();
		String beforeComplete = textField.getText();
		original.call();
		if (StringUtils.expand(beforeComplete, textField.getText()).equals(textField.getText())) return;
		textField.setText(StringUtils.expand(beforeComplete, textField.getText()));
	}
}
