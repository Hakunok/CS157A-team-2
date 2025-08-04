import * as React from "react";
import {
  BoldIcon,
  ItalicIcon,
  UnderlineIcon,
  StrikethroughIcon,
  CodeIcon,
  EraserIcon,
  LinkIcon,
} from "lucide-react";
import { ToolbarSection } from "../toolbar-section";

const actions = [
  {
    label: "Bold",
    value: "bold",
    icon: <BoldIcon className="size-4" />,
    action: (editor) => editor.chain().focus().toggleBold().run(),
    canExecute: (editor) => editor.can().toggleBold(),
    isActive: (editor) => editor.isActive("bold"),
    shortcuts: ["Mod-b"],
  },
  {
    label: "Italic",
    value: "italic",
    icon: <ItalicIcon className="size-4" />,
    action: (editor) => editor.chain().focus().toggleItalic().run(),
    canExecute: (editor) => editor.can().toggleItalic(),
    isActive: (editor) => editor.isActive("italic"),
    shortcuts: ["Mod-i"],
  },
  {
    label: "Underline",
    value: "underline",
    icon: <UnderlineIcon className="size-4" />,
    action: (editor) => editor.chain().focus().toggleUnderline?.().run(),
    canExecute: (editor) => !!editor.can().toggleUnderline?.(),
    isActive: (editor) => editor.isActive("underline"),
    shortcuts: ["Mod-u"],
  },
  {
    label: "Strikethrough",
    value: "strikethrough",
    icon: <StrikethroughIcon className="size-4" />,
    action: (editor) => editor.chain().focus().toggleStrike().run(),
    canExecute: (editor) => editor.can().toggleStrike(),
    isActive: (editor) => editor.isActive("strike"),
    shortcuts: [],
  },
  {
    label: "Inline Code",
    value: "code",
    icon: <CodeIcon className="size-4" />,
    action: (editor) => editor.chain().focus().toggleCode().run(),
    canExecute: (editor) => editor.can().toggleCode(),
    isActive: (editor) => editor.isActive("code"),
    shortcuts: [],
  },
  {
    label: "Link",
    value: "link",
    icon: <LinkIcon className="size-4" />,
    action: (editor) => {
      const url = window.prompt("Enter a URL");
      if (url) {
        const normalizedUrl = url.startsWith("http://") || url.startsWith("https://")
            ? url
            : `https://${url}`;

        editor.chain().focus().setLink({ href: normalizedUrl }).run();
      }
    },
    canExecute: () => true,
    isActive: (editor) => editor.isActive("link"),
    shortcuts: ["Mod-k"],
  },
  {
    label: "Clear Formatting",
    value: "clearFormatting",
    icon: <EraserIcon className="size-4" />,
    action: (editor) => editor.chain().focus().unsetAllMarks().clearNodes().run(),
    canExecute: () => true,
    isActive: () => false,
    shortcuts: [],
  },
];

export const SectionTwo = ({ editor, size, variant }) => {
  return (
      <ToolbarSection
          editor={editor}
          actions={actions}
          size={size}
          variant={variant}
          mainActionCount={actions.length}
      />
  );
};