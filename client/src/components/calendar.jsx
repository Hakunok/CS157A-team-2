import * as React from "react"
import {
  ChevronDownIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
} from "lucide-react"
import { DayPicker, getDefaultClassNames } from "react-day-picker"

import { cn } from "@/lib/utils.js"
import { Button, buttonVariants } from "@/components/button.jsx"

function Calendar({
  className,
  classNames,
  showOutsideDays = true,
  captionLayout = "label",
  buttonVariant = "ghost",
  formatters,
  components,
  ...props
}) {
  const defaultClassNames = getDefaultClassNames()

  return (
      <DayPicker
          showOutsideDays={showOutsideDays}
          captionLayout={captionLayout}
          className={cn(
              "bg-[var(--color-background)] group/calendar p-3",
              "[[data-slot=card-content]_&]:bg-transparent",
              "[[data-slot=popover-content]_&]:bg-transparent",
              "rtl:[.rdp-button_next>svg]:rotate-180 rtl:[.rdp-button_previous>svg]:rotate-180",
              className
          )}
          formatters={{
            formatMonthDropdown: (date) =>
                date.toLocaleString("default", { month: "short" }),
            ...formatters,
          }}
          classNames={{
            root: cn("w-fit", defaultClassNames.root),
            months: cn("flex gap-4 flex-col md:flex-row relative", defaultClassNames.months),
            month: cn("flex flex-col w-full gap-4", defaultClassNames.month),
            nav: cn("flex items-center gap-1 w-full absolute top-0 inset-x-0 justify-between", defaultClassNames.nav),
            button_previous: cn(
                buttonVariants({ variant: buttonVariant }),
                "size-9 aria-disabled:opacity-50 p-0 select-none",
                defaultClassNames.button_previous
            ),
            button_next: cn(
                buttonVariants({ variant: buttonVariant }),
                "size-9 aria-disabled:opacity-50 p-0 select-none",
                defaultClassNames.button_next
            ),
            month_caption: cn("flex items-center justify-center h-9 w-full px-4", defaultClassNames.month_caption),
            dropdowns: cn("w-full flex items-center text-sm font-medium justify-center h-9 gap-1.5", defaultClassNames.dropdowns),
            dropdown_root: cn(
                "relative has-focus:border-[var(--color-ring)] border border-[var(--color-input)] shadow-xs "
                + "has-focus:ring-[var(--color-ring)]/50 has-focus:ring-[3px] rounded-[var(--radius)]",
                defaultClassNames.dropdown_root
            ),
            dropdown: cn("absolute inset-0 bg-[var(--color-popover)] opacity-0", defaultClassNames.dropdown),
            caption_label: cn(
                "select-none font-medium",
                captionLayout === "label"
                    ? "text-sm"
                    : "rounded-[var(--radius)] pl-2 pr-1 flex items-center gap-1 text-sm h-8 [&>svg]:text-[var(--color-muted-foreground)] [&>svg]:size-3.5",
                defaultClassNames.caption_label
            ),
            table: "w-full border-collapse",
            weekdays: cn("flex", defaultClassNames.weekdays),
            weekday: cn(
                "text-[var(--color-muted-foreground)] rounded-[var(--radius-sm)] flex-1 font-normal text-xs select-none",
                defaultClassNames.weekday
            ),
            week: cn("flex w-full mt-2", defaultClassNames.week),
            week_number_header: cn("select-none w-9", defaultClassNames.week_number_header),
            week_number: cn(
                "text-xs select-none text-[var(--color-muted-foreground)]",
                defaultClassNames.week_number
            ),
            day: cn(
                "relative w-full h-full p-0 text-center group/day aspect-square select-none",
                "[&:first-child[data-selected=true]_button]:rounded-l-[var(--radius)]",
                "[&:last-child[data-selected=true]_button]:rounded-r-[var(--radius)]",
                defaultClassNames.day
            ),
            range_start: cn("rounded-l-[var(--radius)] bg-[var(--color-accent)] text-[var(--color-accent-foreground)]", defaultClassNames.range_start),
            range_middle: cn("rounded-none", defaultClassNames.range_middle),
            range_end: cn("rounded-r-[var(--radius)] bg-[var(--color-accent)] text-[var(--color-accent-foreground)]", defaultClassNames.range_end),
            today: cn("bg-[var(--color-accent)] text-[var(--color-accent-foreground)] rounded-[var(--radius)] data-[selected=true]:rounded-none", defaultClassNames.today),
            outside: cn("text-[var(--color-muted-foreground)] aria-selected:text-[var(--color-muted-foreground)]", defaultClassNames.outside),
            disabled: cn("text-[var(--color-muted-foreground)] opacity-50", defaultClassNames.disabled),
            hidden: cn("invisible", defaultClassNames.hidden),
            ...classNames,
          }}
          components={{
            Root: ({ className, rootRef, ...props }) => (
                <div data-slot="calendar" ref={rootRef} className={cn(className)} {...props} />
            ),
            Chevron: ({ className, orientation, ...props }) => {
              if (orientation === "left") return <ChevronLeftIcon className={cn("size-4", className)} {...props} />
              if (orientation === "right") return <ChevronRightIcon className={cn("size-4", className)} {...props} />
              return <ChevronDownIcon className={cn("size-4", className)} {...props} />
            },
            DayButton: CalendarDayButton,
            WeekNumber: ({ children, ...props }) => (
                <td {...props}>
                  <div className="flex size-9 items-center justify-center text-center">
                    {children}
                  </div>
                </td>
            ),
            ...components,
          }}
          {...props}
      />
  )
}

function CalendarDayButton({ className, day, modifiers, ...props }) {
  const defaultClassNames = getDefaultClassNames()
  const ref = React.useRef(null)

  React.useEffect(() => {
    if (modifiers.focused) ref.current?.focus()
  }, [modifiers.focused])

  return (
      <Button
          ref={ref}
          variant="ghost"
          size="icon"
          data-day={day.date.toLocaleDateString()}
          data-selected-single={
              modifiers.selected &&
              !modifiers.range_start &&
              !modifiers.range_end &&
              !modifiers.range_middle
          }
          data-range-start={modifiers.range_start}
          data-range-end={modifiers.range_end}
          data-range-middle={modifiers.range_middle}
          className={cn(
              "data-[selected-single=true]:bg-[var(--color-primary)] data-[selected-single=true]:text-[var(--color-primary-foreground)]",
              "data-[range-middle=true]:bg-[var(--color-accent)] data-[range-middle=true]:text-[var(--color-accent-foreground)]",
              "data-[range-start=true]:bg-[var(--color-primary)] data-[range-start=true]:text-[var(--color-primary-foreground)]",
              "data-[range-end=true]:bg-[var(--color-primary)] data-[range-end=true]:text-[var(--color-primary-foreground)]",
              "group-data-[focused=true]/day:ring-[3px] group-data-[focused=true]/day:ring-[var(--color-ring)]/50 "
              + "group-data-[focused=true]/day:border-[var(--color-ring)]",
              "text-sm font-normal w-full min-w-9 aspect-square flex flex-col items-center justify-center",
              "data-[range-end=true]:rounded-[var(--radius)] data-[range-end=true]:rounded-r-[var(--radius)]",
              "data-[range-start=true]:rounded-[var(--radius)] data-[range-start=true]:rounded-l-[var(--radius)]",
              "[&>span]:text-xs [&>span]:opacity-70",
              defaultClassNames.day,
              className
          )}
          {...props}
      />
  )
}

export { Calendar, CalendarDayButton }