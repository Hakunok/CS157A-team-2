import { CodeBlockLowlight as TiptapCodeBlockLowlight } from "@tiptap/extension-code-block-lowlight";
import { common, createLowlight } from "lowlight";

const lowlight = createLowlight(common);

export const CodeBlockLowlight = TiptapCodeBlockLowlight.extend({
  addOptions() {
    return {
      ...this.parent?.(),
      lowlight,
      defaultLanguage: null,
      HTMLAttributes: {
        class: "code-block prose-pre:bg-[var(--color-muted)] prose-pre:text-[var(--color-foreground)] prose-pre:rounded-md prose-pre:p-4 prose-pre:overflow-x-auto font-mono text-sm",
      },
    };
  },
});