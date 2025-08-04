import * as React from "react";
import { cn } from "@/lib/utils";
import { getShortcutKey } from "../utils";

const ShortcutKey = ({ className, keys = [], ...props }) => {
  const modifiedKeys = keys.map((key) => getShortcutKey(key));
  const ariaLabel = modifiedKeys.map((k) => k.readable).join(" + ");

  return (
      <span
          aria-label={ariaLabel}
          className={cn("inline-flex items-center gap-0.5", className)}
          {...props}
      >
      {modifiedKeys.map((shortcut) => (
          <kbd
              key={shortcut.symbol}
              className={cn(
                  "inline-block min-w-2.5 text-center align-baseline font-sans text-xs font-medium text-[rgb(156,157,160)] capitalize"
              )}
          >
            {shortcut.symbol}
          </kbd>
      ))}
    </span>
  );
};

ShortcutKey.displayName = "ShortcutKey";

export { ShortcutKey };
export default ShortcutKey;