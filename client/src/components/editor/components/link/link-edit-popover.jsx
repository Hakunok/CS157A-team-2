import * as React from "react";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Link2Icon } from "@radix-ui/react-icons";
import { ToolbarButton } from "../toolbar-button";
import LinkEditBlock from "./link-edit-block";

const LinkEditPopover = ({ editor, size, variant }) => {
  const [open, setOpen] = React.useState(false);

  const { from, to } = editor.state.selection;
  const text = editor.state.doc.textBetween(from, to, " ");

  const onSetLink = React.useCallback(
      (url, displayText, openInNewTab) => {
        editor
        .chain()
        .focus()
        .extendMarkRange("link")
        .insertContent({
          type: "text",
          text: displayText || url,
          marks: [
            {
              type: "link",
              attrs: {
                href: url,
                target: openInNewTab ? "_blank" : "",
              },
            },
          ],
        })
        .setLink({ href: url })
        .run();

        editor.commands.enter();
      },
      [editor]
  );

  return (
      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <ToolbarButton
              isActive={editor.isActive("link")}
              tooltip="Link"
              aria-label="Insert link"
              disabled={editor.isActive("codeBlock")}
              size={size}
              variant={variant}
          >
            <Link2Icon className="size-5" />
          </ToolbarButton>
        </PopoverTrigger>
        <PopoverContent align="end" side="bottom">
          <LinkEditBlock onSave={onSetLink} defaultText={text} />
        </PopoverContent>
      </Popover>
  );
};

export { LinkEditPopover };
