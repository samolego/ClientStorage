package org.samo_lego.clientstorage.casts;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public interface IRemoteCrafting {
    default EditBox getSearchBox(Font font, int x, int y) {
        var box = new EditBox(font, x + 73, y - 35, 84, font.lineHeight, Component.translatable("itemGroup.search"));
        box.setFocus(true);
        return box;
    }
}
