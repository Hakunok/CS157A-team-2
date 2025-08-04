import * as React from "react";
import { EditorContent } from "@tiptap/react";
import { Separator } from "@/components/ui/separator";
import { cn } from "@/lib/utils";
import { SectionOne } from "./components/section/one";
import { SectionTwo } from "./components/section/two";
import { SectionFour } from "./components/section/four";
import { LinkBubbleMenu } from "./components/bubble-menu/link-bubble-menu";
import { useMinimalTiptapEditor } from "./hooks/use-minimal-tiptap";
import { MeasuredContainer } from "./components/measured-container";

const Toolbar = ({ editor }) => (
    <div className="flex h-12 shrink-0 overflow-x-auto border-b border-[var(--color-border)] bg-[var(--color-muted)] px-2 py-1">
      <div className="flex w-max items-center gap-px">
        {/* Text formatting (H1, H2, H3, etc.) */}
        <SectionOne editor={editor} activeLevels={[1, 2, 3, 4, 5, 6]} />
        <Separator orientation="vertical" className="mx-2" />

        {/* Text styling (bold, italic, underline, etc.) */}
        <SectionTwo editor={editor} />
        <Separator orientation="vertical" className="mx-2" />

        {/* Block elements (lists, quotes, code blocks, dividers) */}
        <SectionFour editor={editor} />
      </div>
    </div>
);

const MinimalTiptapEditor = ({
  value,
  onChange,
  className,
  editorContentClassName,
  ...props
}) => {
  const editor = useMinimalTiptapEditor({
    value,
    onUpdate: onChange,
    ...props,
  });

  if (!editor) return null;

  return (
      <MeasuredContainer
          as="div"
          name="editor"
          className={cn(
              "flex flex-col w-full rounded-[var(--radius-md)] overflow-hidden",
              "border border-[var(--color-border)] bg-[var(--color-muted)]",
              "focus-within:ring-[1px] focus-within:ring-[var(--color-ring)] focus-within:border-[var(--color-ring)]",
              className
          )}
      >
        <Toolbar editor={editor} />
        <EditorContent
            editor={editor}
            className={cn(
                "minimal-tiptap-editor px-4 py-3 max-w-none",
                editorContentClassName
            )}
        />
        <LinkBubbleMenu editor={editor} />
      </MeasuredContainer>
  );
};

export default MinimalTiptapEditor;