import * as React from "react";
import * as AlertDialogPrimitive from "@radix-ui/react-alert-dialog";
import { cn } from "@/lib/utils.js";
import { buttonVariants } from "@/components/button.jsx";

function AlertDialog(props) {
  return <AlertDialogPrimitive.Root {...props} />;
}

function AlertDialogTrigger(props) {
  return <AlertDialogPrimitive.Trigger {...props} />;
}

function AlertDialogPortal(props) {
  return <AlertDialogPrimitive.Portal {...props} />;
}

function AlertDialogOverlay({ className, ...props }) {
  return (
      <AlertDialogPrimitive.Overlay
          className={cn(
              "fixed inset-0 z-50 bg-black/60 backdrop-blur-sm",
              "data-[state=open]:animate-in data-[state=closed]:animate-out",
              "data-[state=open]:fade-in-0 data-[state=closed]:fade-out-0",
              className
          )}
          {...props}
      />
  );
}

function AlertDialogContent({ className, ...props }) {
  return (
      <AlertDialogPortal>
        <AlertDialogOverlay />
        <AlertDialogPrimitive.Content
            className={cn(
                "fixed left-1/2 top-1/2 z-50 w-full max-w-lg",
                "translate-x-[-50%] translate-y-[-50%]",
                "rounded-xl border border-border bg-surface shadow-lg",
                "p-6 grid gap-4",
                "text-foreground font-ui",
                "data-[state=open]:animate-in data-[state=closed]:animate-out",
                "data-[state=open]:zoom-in-95 data-[state=closed]:zoom-out-95",
                "data-[state=open]:fade-in-0 data-[state=closed]:fade-out-0",
                className
            )}
            {...props}
        />
      </AlertDialogPortal>
  );
}

function AlertDialogHeader({ className, ...props }) {
  return (
      <div
          className={cn("flex flex-col gap-2 text-center sm:text-left", className)}
          {...props}
      />
  );
}

function AlertDialogFooter({ className, ...props }) {
  return (
      <div
          className={cn("flex flex-col-reverse gap-2 sm:flex-row sm:justify-end", className)}
          {...props}
      />
  );
}

function AlertDialogTitle({ className, ...props }) {
  return (
      <AlertDialogPrimitive.Title
          className={cn("text-xl font-semibold font-ui", className)}
          {...props}
      />
  );
}

function AlertDialogDescription({ className, ...props }) {
  return (
      <AlertDialogPrimitive.Description
          className={cn("text-sm text-muted-foreground font-content", className)}
          {...props}
      />
  );
}

function AlertDialogAction({ className, ...props }) {
  return (
      <AlertDialogPrimitive.Action
          className={cn(buttonVariants({ variant: "default" }), className)}
          {...props}
      />
  );
}

function AlertDialogCancel({ className, ...props }) {
  return (
      <AlertDialogPrimitive.Cancel
          className={cn(buttonVariants({ variant: "outline" }), className)}
          {...props}
      />
  );
}

export {
  AlertDialog,
  AlertDialogTrigger,
  AlertDialogPortal,
  AlertDialogOverlay,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogFooter,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogAction,
  AlertDialogCancel,
};