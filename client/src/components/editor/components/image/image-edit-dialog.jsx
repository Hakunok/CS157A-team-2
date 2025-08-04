import { useState } from "react";
import { ImageIcon } from "@radix-ui/react-icons";
import { ToolbarButton } from "../toolbar-button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogDescription,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import ImageEditBlock from "./image-edit-block";

const ImageEditDialog = ({ editor, size, variant }) => {
  const [open, setOpen] = useState(false);

  return (
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          <ToolbarButton
              isActive={editor.isActive("image")}
              tooltip="Image"
              aria-label="Image"
              size={size}
              variant={variant}
          >
            <ImageIcon className="size-5" />
          </ToolbarButton>
        </DialogTrigger>
        <DialogContent className="sm:max-w-lg">
          <DialogHeader>
            <DialogTitle>Insert Image</DialogTitle>
            <DialogDescription>
              Paste a direct image link (e.g. https://example.com/image.jpg)
            </DialogDescription>
          </DialogHeader>
          <ImageEditBlock editor={editor} close={() => setOpen(false)} />
        </DialogContent>
      </Dialog>
  );
};

export { ImageEditDialog };
