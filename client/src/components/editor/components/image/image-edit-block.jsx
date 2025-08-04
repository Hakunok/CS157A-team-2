import * as React from "react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";

const ImageEditBlock = ({ editor, close }) => {
  const [link, setLink] = React.useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    e.stopPropagation();

    if (link) {
      editor.commands.setImages([{ src: link }]);
      close();
    }
  };

  return (
      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="space-y-1">
          <Label htmlFor="image-link">Attach an image link</Label>
          <div className="flex">
            <Input
                id="image-link"
                type="url"
                required
                placeholder="https://example.com/image.jpg"
                value={link}
                className="grow"
                onChange={(e) => setLink(e.target.value)}
            />
            <Button type="submit" className="ml-2">
              Submit
            </Button>
          </div>
        </div>
      </form>
  );
};

export default ImageEditBlock;
