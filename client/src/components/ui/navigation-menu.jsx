import * as React from "react"
import * as NavigationMenuPrimitive from "@radix-ui/react-navigation-menu"
import { ChevronDownIcon } from "lucide-react"
import { cva } from "class-variance-authority"
import { cn } from "@/lib/utils"

function NavigationMenu({ className, children, viewport = true, ...props }) {
  return (
      <NavigationMenuPrimitive.Root
          data-slot="navigation-menu"
          data-viewport={viewport}
          className={cn(
              "group/navigation-menu relative flex max-w-max flex-1 items-center justify-center",
              className
          )}
          {...props}
      >
        {children}
        {viewport && <NavigationMenuViewport />}
      </NavigationMenuPrimitive.Root>
  )
}

function NavigationMenuList({ className, ...props }) {
  return (
      <NavigationMenuPrimitive.List
          data-slot="navigation-menu-list"
          className={cn("group flex flex-1 list-none items-center justify-center gap-1", className)}
          {...props}
      />
  )
}

function NavigationMenuItem({ className, ...props }) {
  return (
      <NavigationMenuPrimitive.Item
          data-slot="navigation-menu-item"
          className={cn("relative", className)}
          {...props}
      />
  )
}

const navigationMenuTriggerStyle = cva(
    "group inline-flex h-9 w-max items-center justify-center rounded-[var(--radius)] px-3 py-2 text-sm font-medium font-ui transition-all duration-200 ease-in-out",
    {
      variants: {
        intent: {
          default: [
            "text-[var(--color-foreground)]",
            "hover:bg-[var(--color-muted)]/60 hover:text-[var(--color-foreground)]",
            "data-[state=open]:bg-[var(--color-muted)]/80 data-[state=open]:text-[var(--color-foreground)]",
            "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] ring-offset-background focus-visible:ring-offset-2"
          ].join(" "),
          active: "bg-[var(--color-muted)]/80 text-[var(--color-foreground)]"
        },
      },
      defaultVariants: {
        intent: "default",
      },
    }
)

function NavigationMenuTrigger({ className, children, ...props }) {
  return (
      <NavigationMenuPrimitive.Trigger
          data-slot="navigation-menu-trigger"
          className={cn(navigationMenuTriggerStyle(), className)}
          {...props}
      >
        {children}
        <ChevronDownIcon
            className="ml-1.5 size-3 transition-transform duration-200 group-data-[state=open]:rotate-180"
            aria-hidden="true"
        />
      </NavigationMenuPrimitive.Trigger>
  )
}

function NavigationMenuContent({ className, ...props }) {
  return (
      <NavigationMenuPrimitive.Content
          data-slot="navigation-menu-content"
          className={cn(
              "z-50 w-full p-2 md:absolute md:w-auto overflow-hidden",
              "bg-[var(--color-popover)]/95 text-[var(--color-popover-foreground)] backdrop-blur-sm",
              "border border-[var(--color-border)]/30 rounded-[var(--radius)] shadow-lg",
              "data-[motion^=from-]:animate-in data-[motion^=to-]:animate-out",
              "data-[motion=from-end]:slide-in-from-right-52 data-[motion=from-start]:slide-in-from-left-52",
              "data-[motion=to-end]:slide-out-to-right-52 data-[motion=to-start]:slide-out-to-left-52",
              className
          )}
          {...props}
      />
  )
}

function NavigationMenuViewport({ className, ...props }) {
  return (
      <div className="absolute top-full left-0 isolate z-50 flex justify-center">
        <NavigationMenuPrimitive.Viewport
            data-slot="navigation-menu-viewport"
            className={cn(
                "origin-top-center relative mt-1.5 h-[var(--radix-navigation-menu-viewport-height)] w-full md:w-[var(--radix-navigation-menu-viewport-width)] overflow-hidden",
                "rounded-[var(--radius)] border border-[var(--color-border)]/30 bg-[var(--color-popover)]/95 text-[var(--color-popover-foreground)] backdrop-blur-sm shadow-lg",
                "data-[state=open]:animate-in data-[state=closed]:animate-out",
                "data-[state=open]:zoom-in-95 data-[state=closed]:zoom-out-95",
                className
            )}
            {...props}
        />
      </div>
  )
}

function NavigationMenuLink({ className, ...props }) {
  return (
      <NavigationMenuPrimitive.Link
          data-slot="navigation-menu-link"
          className={cn(
              "flex items-center rounded-[var(--radius)] p-2 text-sm font-ui font-medium",
              "text-[var(--color-popover-foreground)] transition-all duration-200 outline-none",
              "hover:bg-[var(--color-muted)]/50 hover:text-[var(--color-foreground)]",
              "focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] ring-offset-background focus-visible:ring-offset-2",
              className
          )}
          {...props}
      />
  )
}

function NavigationMenuIndicator({ className, ...props }) {
  return (
      <NavigationMenuPrimitive.Indicator
          data-slot="navigation-menu-indicator"
          className={cn(
              "z-[1] top-full flex h-1.5 items-end justify-center data-[state=visible]:fade-in data-[state=hidden]:fade-out",
              className
          )}
          {...props}
      >
        <div className="relative top-[60%] h-2 w-2 rotate-45 rounded-tl-sm bg-[var(--color-border)]/50 shadow-md" />
      </NavigationMenuPrimitive.Indicator>
  )
}

export {
  NavigationMenu,
  NavigationMenuList,
  NavigationMenuItem,
  NavigationMenuContent,
  NavigationMenuTrigger,
  NavigationMenuLink,
  NavigationMenuIndicator,
  NavigationMenuViewport,
  navigationMenuTriggerStyle,
}
