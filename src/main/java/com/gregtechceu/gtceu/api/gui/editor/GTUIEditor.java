package com.gregtechceu.gtceu.api.gui.editor;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.ui.UIEditor;

@LDLRegister(name = "editor.gtceu", group = "editor")
public class GTUIEditor extends UIEditor {

    public GTUIEditor() {
        super(LDLib.location);
    }
}
