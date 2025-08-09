import React, { useCallback } from 'react';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Placeholder from '@tiptap/extension-placeholder';
import Link from '@tiptap/extension-link';
import Underline from '@tiptap/extension-underline';
import CodeBlockLowlight from '@tiptap/extension-code-block-lowlight';
import { createLowlight } from 'lowlight';
import css from 'highlight.js/lib/languages/css';
import js from 'highlight.js/lib/languages/javascript';
import ts from 'highlight.js/lib/languages/typescript';
import html from 'highlight.js/lib/languages/xml';

import {
  Bold, Italic, Underline as UnderlineIcon, Strikethrough, List, ListOrdered,
  Quote, Heading1, Heading2, Heading3, Code, Link as LinkIcon, Undo, Redo
} from 'lucide-react';

const lowlight = createLowlight();

lowlight.register('html', html);
lowlight.register('css', css);
lowlight.register('js', js);
lowlight.register('ts', ts);

const ToolbarButton = ({ onClick, children, title, isActive = false, disabled = false }) => (
    <button
        type="button"
        onClick={onClick}
        disabled={disabled}
        title={title}
        className={`
      h-8 w-8 p-0 rounded border transition-colors
      flex items-center justify-center
      ${isActive
            ? 'bg-[var(--color-primary)] text-[var(--color-primary-foreground)] border-[var(--color-primary)]'
            : 'bg-[var(--color-card)] text-[var(--color-foreground)] border-[var(--color-border)] hover:bg-[var(--color-accent)]'
        }
      ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
    `}
    >
      {children}
    </button>
);

const EditorToolbar = ({ editor }) => {
  const setLink = useCallback(() => {
    const previousUrl = editor.getAttributes('link').href;
    const url = window.prompt('URL', previousUrl);

    if (url === null) {
      return;
    }

    if (url === '') {
      editor.chain().focus().extendMarkRange('link').unsetLink().run();
      return;
    }

    editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
  }, [editor]);

  if (!editor) {
    return null;
  }

  return (
      <div className="flex items-center gap-1 p-2 border-b border-[var(--color-border)] bg-[var(--color-card)] flex-wrap">
        <ToolbarButton onClick={() => editor.chain().focus().toggleBold().run()} disabled={!editor.can().chain().focus().toggleBold().run()}
                       isActive={editor.isActive('bold')} title="Bold"><Bold className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={() => editor.chain().focus().toggleItalic().run()} disabled={!editor.can().chain().focus().toggleItalic().run()}
                       isActive={editor.isActive('italic')} title="Italic"><Italic className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={() => editor.chain().focus().toggleUnderline().run()} disabled={!editor.can().chain().focus().toggleUnderline().run()}
                       isActive={editor.isActive('underline')} title="Underline"><UnderlineIcon className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={() => editor.chain().focus().toggleStrike().run()} disabled={!editor.can().chain().focus().toggleStrike().run()}
                       isActive={editor.isActive('strike')} title="Strikethrough"><Strikethrough className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={setLink} isActive={editor.isActive('link')} title="Link"><LinkIcon className="h-4 w-4" />
        </ToolbarButton>

        <div className="w-px h-6 bg-[var(--color-border)] mx-1" />

        <ToolbarButton onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
                       isActive={editor.isActive('heading', { level: 1 })} title="Heading 1"><Heading1 className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
                       isActive={editor.isActive('heading', { level: 2 })} title="Heading 2"><Heading2 className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}
                       isActive={editor.isActive('heading', { level: 3 })} title="Heading 3"><Heading3 className="h-4 w-4" />
        </ToolbarButton>

        <div className="w-px h-6 bg-[var(--color-border)] mx-1" />

        <ToolbarButton onClick={() => editor.chain().focus().toggleBulletList().run()}
                       isActive={editor.isActive('bulletList')} title="Bullet List"><List className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={() => editor.chain().focus().toggleOrderedList().run()}
                       isActive={editor.isActive('orderedList')} title="Numbered List"><ListOrdered className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={() => editor.chain().focus().toggleBlockquote().run()}
                       isActive={editor.isActive('blockquote')} title="Quote"><Quote className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={() => editor.chain().focus().toggleCodeBlock().run()}
                       isActive={editor.isActive('codeBlock')} title="Code Block"><Code className="h-4 w-4" />
        </ToolbarButton>

        <div className="w-px h-6 bg-[var(--color-border)] mx-1" />

        <ToolbarButton onClick={() => editor.chain().focus().undo().run()}
                       disabled={!editor.can().undo()} title="Undo"><Undo className="h-4 w-4" />
        </ToolbarButton>
        <ToolbarButton onClick={() => editor.chain().focus().redo().run()}
                       disabled={!editor.can().redo()} title="Redo"><Redo className="h-4 w-4" />
        </ToolbarButton>
      </div>
  );
};


const TiptapEditor = ({
  initialContent = '',
  onChange,
  placeholder = "Start writing your masterpiece...",
}) => {
  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        heading: { levels: [1, 2, 3] },
        codeBlock: false,
      }),
      Placeholder.configure({ placeholder }),
      Underline,
      Link.configure({
        openOnClick: false,
        autolink: true,
      }),
      CodeBlockLowlight.configure({
        lowlight,
      }),
    ],
    content: initialContent,
    onUpdate: ({ editor }) => {
      if (onChange) {
        onChange({
          html: editor.getHTML(),
          json: editor.getJSON(),
        });
      }
    },
    editorProps: {
      attributes: {
        class: 'prose prose-wide focus:outline-none',
      },
    },
  });

  return (
      <div className="border border-[var(--color-border)] bg-[var(--color-muted)] rounded-lg overflow-hidden">
        <EditorToolbar editor={editor} />
        <div className="p-6 min-h-[500px] bg-[var(--color-surface)]">
          <EditorContent editor={editor} />
        </div>
      </div>
  );
};

export default TiptapEditor;
