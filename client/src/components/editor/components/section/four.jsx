import * as React from "react";
import {
  ListIcon,
  ListOrderedIcon,
  QuoteIcon,
  MinusIcon,
  Code2Icon,
} from "lucide-react";
import { ToolbarSection } from "../toolbar-section";

const actions = [
  {
    label: "Bullet List",
    value: "bulletList",
    icon: <ListIcon className="size-4" />,
    action: (editor) => editor.chain().focus().toggleBulletList().run(),
    canExecute: (editor) => editor.can().toggleBulletList(),
    isActive: (editor) => editor.isActive("bulletList"),
    shortcuts: [],
  },
  {
    label: "Numbered List",
    value: "orderedList",
    icon: <ListOrderedIcon className="size-4" />,
    action: (editor) => editor.chain().focus().toggleOrderedList().run(),
    canExecute: (editor) => editor.can().toggleOrderedList(),
    isActive: (editor) => editor.isActive("orderedList"),
    shortcuts: [],
  },
  {
    label: "Blockquote",
    value: "blockquote",
    icon: <QuoteIcon className="size-4" />,
    action: (editor) => editor.chain().focus().toggleBlockquote().run(),
    canExecute: (editor) => editor.can().toggleBlockquote(),
    isActive: (editor) => editor.isActive("blockquote"),
    shortcuts: [],
  },
  {
    label: "Code Block",
    value: "codeBlock",
    icon: <Code2Icon className="size-4" />,
    action: (editor) => editor.chain().focus().toggleCodeBlock().run(),
    canExecute: (editor) => editor.can().toggleCodeBlock(),
    isActive: (editor) => editor.isActive("codeBlock"),
    shortcuts: [],
  },
  {
    label: "Divider",
    value: "horizontalRule",
    icon: <MinusIcon className="size-4 rotate-90" />,
    action: (editor) => editor.chain().focus().setHorizontalRule().run(),
    canExecute: () => true,
    isActive: () => false,
    shortcuts: [],
  },
];

export const SectionFour = ({ editor, size, variant }) => {
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