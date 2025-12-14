package tools.redstone.redstonetools.mixin.features;

import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor {
	@Accessor(value = "text")
	void setTextDirectly(String s);

	//? if >=1.21.10 {
	@Accessor
	java.util.List<TextFieldWidget.Formatter> getFormatters();
	//?}
}
