import * as React from "react";
import { Button } from "@/components/button.jsx";
import { Label } from "@/components/label.jsx";
import { Switch } from "@/components/switch.jsx";
import { Input } from "@/components/input.jsx";
import { cn } from "@/lib/utils";

const LinkEditBlock = ({
                         defaultUrl = "",
                         defaultText = "",
                         defaultIsNewTab = false,
                         onSave,
                         className,
                       }) => {
  const formRef = React.useRef(null);
  const [url, setUrl] = React.useState(defaultUrl);
  const [text, setText] = React.useState(defaultText);
  const [isNewTab, setIsNewTab] = React.useState(defaultIsNewTab);

  const handleSave = (e) => {
    e.preventDefault();
    if (formRef.current) {
      const inputs = formRef.current.querySelectorAll("input");
      const isValid = Array.from(inputs).every((input) => input.checkValidity());

      if (isValid) {
        onSave(url, text, isNewTab);
      } else {
        inputs.forEach((input) => {
          if (!input.checkValidity()) input.reportValidity();
        });
      }
    }
  };

  return (
      <div ref={formRef}>
        <div className={cn("space-y-4", className)}>
          <div className="space-y-1">
            <Label>URL</Label>
            <Input
                type="url"
                required
                placeholder="Enter URL"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
            />
          </div>

          <div className="space-y-1">
            <Label>Display Text (optional)</Label>
            <Input
                type="text"
                placeholder="Enter display text"
                value={text}
                onChange={(e) => setText(e.target.value)}
            />
          </div>

          <div className="flex items-center space-x-2">
            <Label>Open in New Tab</Label>
            <Switch checked={isNewTab} onCheckedChange={setIsNewTab} />
          </div>

          <div className="flex justify-end space-x-2">
            <Button type="button" onClick={handleSave}>
              Save
            </Button>
          </div>
        </div>
      </div>
  );
};

LinkEditBlock.displayName = "LinkEditBlock";

export { LinkEditBlock };