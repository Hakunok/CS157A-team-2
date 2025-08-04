import { Extension } from "@tiptap/react";

export const UnsetAllMarks = Extension.create({
  name: "unsetAllMarks",

  addKeyboardShortcuts() {
    return {
      "Mod-\\": () => this.editor.commands.unsetAllMarks(),
    };
  },
});

export default UnsetAllMarks;
