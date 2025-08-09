import * as React from "react";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/tooltip.jsx";
import { Button } from "@/components/button.jsx";
import { cn } from "@/lib/utils";

const ToolbarButton = ({
  isActive,
  children,
  tooltip,
  className,
  tooltipOptions,
  variant = "ghost",
  size = "sm",
  ...props
}) => {
  const toolbarButton = (
      <Button
          variant={isActive ? "secondary" : variant}
          size={size}
          className={cn(
              "h-8 px-2 min-w-8",
              isActive && "bg-[var(--color-accent)] text-[var(--color-accent-foreground)]",
              className
          )}
          {...props}
      >
        {children}
      </Button>
  );

  if (!tooltip) return toolbarButton;

  return (
      <Tooltip>
        <TooltipTrigger asChild>{toolbarButton}</TooltipTrigger>
        <TooltipContent {...tooltipOptions}>
          <div className="flex flex-col items-center text-center font-ui">
            {tooltip}
          </div>
        </TooltipContent>
      </Tooltip>
  );
};

ToolbarButton.displayName = "ToolbarButton";

export { ToolbarButton };