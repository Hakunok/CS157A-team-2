import * as React from "react";
import { CaretDownIcon } from "@radix-ui/react-icons";
import { cn } from "@/lib/utils";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

import { ToolbarButton } from "./toolbar-button";
import { ShortcutKey } from "./shortcut-key";
import { getShortcutKey } from "../utils";

const ToolbarSection = ({
                          editor,
                          actions,
                          activeActions = actions.map((a) => a.value),
                          mainActionCount = 0,
                          dropdownIcon,
                          dropdownTooltip = "More options",
                          dropdownClassName = "w-12",
                          size,
                          variant,
                        }) => {
  const { mainActions, dropdownActions } = React.useMemo(() => {
    const sortedActions = actions
    .filter((a) => activeActions.includes(a.value))
    .sort(
        (a, b) =>
            activeActions.indexOf(a.value) - activeActions.indexOf(b.value)
    );

    return {
      mainActions: sortedActions.slice(0, mainActionCount),
      dropdownActions: sortedActions.slice(mainActionCount),
    };
  }, [actions, activeActions, mainActionCount]);

  const renderToolbarButton = (action) => (
      <ToolbarButton
          key={action.label}
          onClick={() => action.action(editor)}
          disabled={!action.canExecute(editor)}
          isActive={action.isActive(editor)}
          tooltip={`${action.label} ${action.shortcuts
          .map((s) => getShortcutKey(s).symbol)
          .join(" ")}`}
          aria-label={action.label}
          size={size}
          variant={variant}
      >
        {action.icon}
      </ToolbarButton>
  );

  const renderDropdownMenuItem = (action) => (
      <DropdownMenuItem
          key={action.label}
          onClick={() => action.action(editor)}
          disabled={!action.canExecute(editor)}
          className={cn("flex flex-row items-center justify-between gap-4", {
            "bg-accent": action.isActive(editor),
          })}
          aria-label={action.label}
      >
        <span className="grow">{action.label}</span>
        <ShortcutKey keys={action.shortcuts} />
      </DropdownMenuItem>
  );

  const isDropdownActive = dropdownActions.some((a) =>
      a.isActive(editor)
  );

  return (
      <>
        {mainActions.map(renderToolbarButton)}
        {dropdownActions.length > 0 && (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <ToolbarButton
                    isActive={isDropdownActive}
                    tooltip={dropdownTooltip}
                    aria-label={dropdownTooltip}
                    className={cn("gap-0", dropdownClassName)}
                    size={size}
                    variant={variant}
                >
                  {dropdownIcon || <CaretDownIcon className="size-5" />}
                </ToolbarButton>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="start" className="w-full">
                {dropdownActions.map(renderDropdownMenuItem)}
              </DropdownMenuContent>
            </DropdownMenu>
        )}
      </>
  );
};

export { ToolbarSection };
export default ToolbarSection;
