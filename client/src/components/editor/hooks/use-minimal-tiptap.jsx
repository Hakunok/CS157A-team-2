import * as React from "react";
import { useEditor, useEditorState, EditorContent } from "@tiptap/react";
import { StarterKit } from "@tiptap/starter-kit";
// Removed Placeholder - not needed
import { Typography } from "@tiptap/extension-typography";
import Underline from "@tiptap/extension-underline";
import Link from "@tiptap/extension-link";

import {
  CodeBlockLowlight,
  HorizontalRule,
  UnsetAllMarks,
  ResetMarksOnEnter,
} from "../extensions";

import { cn } from "@/lib/utils";
import { getOutput } from "../utils";
import { useThrottle } from "./use-throttle";

const createExtensions = () => [
  StarterKit.configure({
    blockquote: {
      HTMLAttributes: {},
    },
    codeBlock: false,
    heading: {
      HTMLAttributes: {},
    },
    bulletList: {
      HTMLAttributes: {},
    },
    orderedList: {
      HTMLAttributes: {},
    },
    code: {
      HTMLAttributes: {
        spellcheck: "false",
        class: "inline"
      },
    },
    dropcursor: {
      width: 2,
      class: "ProseMirror-dropcursor border",
    },
    paragraph: {
      HTMLAttributes: {},
    },
  }),

  Underline,

  Link.configure({
    openOnClick: false,
    autolink: true,
    HTMLAttributes: {},
  }),

  Typography,
  UnsetAllMarks,
  HorizontalRule,
  ResetMarksOnEnter,

  CodeBlockLowlight.configure({
    HTMLAttributes: {
      class: "hljs",
    },
  }),

];

export const useMinimalTiptapEditor = ({
  value,
  output = "html",
  editorClassName,
  throttleDelay = 0,
  onUpdate,
  onBlur,
  ...props
}) => {
  const throttledSetValue = useThrottle(
      (content) => onUpdate?.(content),
      throttleDelay
  );

  const handleUpdate = React.useCallback(
      (editor) => throttledSetValue(getOutput(editor, output)),
      [output, throttledSetValue]
  );

  const handleCreate = React.useCallback(
      (editor) => {
        if (value && editor.isEmpty) {
          editor.commands.setContent(value);
        }
      },
      [value]
  );

  const handleBlur = React.useCallback(
      (editor) => onBlur?.(getOutput(editor, output)),
      [output, onBlur]
  );

  const editor = useEditor({
    immediatelyRender: false,
    extensions: createExtensions(),
    editorProps: {
      attributes: {
        autocomplete: "off",
        autocorrect: "off",
        autocapitalize: "on",
        class: cn("ProseMirror focus:outline-hidden", editorClassName),
      },
    },
    onUpdate: ({ editor }) => handleUpdate(editor),
    onCreate: ({ editor }) => handleCreate(editor),
    onBlur: ({ editor }) => handleBlur(editor),
    ...props,
  });

  const { editor: mainEditor } = useEditorState({
    editor,
    selector(context) {
      if (!context.editor) {
        return {
          editor: null,
          editorState: undefined,
          canCommand: undefined,
        };
      }

      return {
        editor: context.editor,
        editorState: context.editor.state,
        canCommand: context.editor.can,
      };
    },
  });

  return mainEditor;
};

export default useMinimalTiptapEditor;