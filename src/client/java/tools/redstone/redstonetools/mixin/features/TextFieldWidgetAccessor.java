package tools.redstone.redstonetools.mixin.features;

import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TextFieldWidget.class)
public interface TextFieldWidgetAccessor {
	@Accessor("text")
	void setTextDirectly(String s);

	//? if >=1.21.10 {
	@Accessor
	List<TextFieldWidget.Formatter> getFormatters();
	//?}
}
