import * as React from "react";
import { cn } from "@/lib/utils";
import {
  CaretDownIcon,
  LetterCaseCapitalizeIcon,
} from "@radix-ui/react-icons";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { ToolbarButton } from "../toolbar-button";
import { ShortcutKey } from "../shortcut-key";

const formatActions = [
  {
    label: "Normal",
    element: "span",
    level: null,
    className: "grow text-sm font-content font-medium",
    shortcuts: ["mod", "alt", "0"],
  },
  {
    label: "H1",
    element: "h1",
    level: 1,
    className: "grow text-3xl font-content font-extrabold",
    shortcuts: ["mod", "alt", "1"],
  },
  {
    label: "H2",
    element: "h2",
    level: 2,
    className: "grow text-xl font-content font-bold",
    shortcuts: ["mod", "alt", "2"],
  },
  {
    label: "H3",
    element: "h3",
    level: 3,
    className: "grow text-lg font-content font-semibold",
    shortcuts: ["mod", "alt", "3"],
  },
  {
    label: "H4",
    element: "h4",
    level: 4,
    className: "grow text-base font-content font-semibold",
    shortcuts: ["mod", "alt", "4"],
  },
  {
    label: "H5",
    element: "h5",
    level: 5,
    className: "grow text-sm font-content font-normal",
    shortcuts: ["mod", "alt", "5"],
  },
  {
    label: "H6",
    element: "h6",
    level: 6,
    className: "grow text-sm font-content font-light",
    shortcuts: ["mod", "alt", "6"],
  },
];

const SectionOne = ({ editor, activeLevels = [1, 2, 3, 4, 5, 6], size, variant }) => {
  const filteredActions = React.useMemo(
      () =>
          formatActions.filter(
              (action) => action.level === null || activeLevels.includes(action.level)
          ),
      [activeLevels]
  );

  const handleStyleChange = (level) => {
    if (level != null) {
      editor.chain().focus().toggleHeading({ level }).run();
    } else {
      editor.chain().focus().setParagraph().run();
    }
  };

  const renderMenuItem = ({ label, element: Element, level, className, shortcuts }) => (
      <DropdownMenuItem
          key={label}
          onClick={() => handleStyleChange(level)}
          className={cn(
              "flex flex-row items-center justify-between gap-4 px-2 py-1.5 font-ui",
              {
                "bg-[var(--color-accent)] text-[var(--color-accent-foreground)]":
                    level != null
                        ? editor.isActive("heading", { level })
                        : editor.isActive("paragraph"),
              }
          )}
          aria-label={label}
      >
        <Element className={className}>{label}</Element>
        <ShortcutKey keys={shortcuts} />
      </DropdownMenuItem>
  );

  return (
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <ToolbarButton
              isActive={editor.isActive("heading")}
              tooltip="Text styles"
              aria-label="Text styles"
              disabled={editor.isActive("codeBlock")}
              size={size}
              variant={variant}
              className="gap-0"
          >
            <LetterCaseCapitalizeIcon className="size-5" />
            <CaretDownIcon className="size-5" />
          </ToolbarButton>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="start" className="w-48">
          {filteredActions.map(renderMenuItem)}
        </DropdownMenuContent>
      </DropdownMenu>
  );
};

SectionOne.displayName = "SectionOne";

export { SectionOne };