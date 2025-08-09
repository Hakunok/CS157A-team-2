"use client"

import * as React from "react"
import { ChevronDownIcon, CalendarIcon } from "lucide-react"
import { Button } from "@/components/button.jsx"
import { Calendar } from "@/components/calendar.jsx"
import { Label } from "@/components/label.jsx"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/popover.jsx"
import { cn } from "@/lib/utils.js"

export function DatePicker({
  value,
  onValueChange,
  placeholder = "Select date",
  disabled = false,
  className,
  label,
  id,
  required = false,
  formatDate = (date) => date.toLocaleDateString(),
  icon = "chevron",
  width = "w-48",
  align = "start",
  defaultToToday = false,
  ...props
}) {
  const [open, setOpen] = React.useState(false)

  const effectiveValue = React.useMemo(() => {
    if (value !== undefined) return value
    if (defaultToToday) return new Date()
    return undefined
  }, [value, defaultToToday])

  React.useEffect(() => {
    if (defaultToToday && value === undefined && onValueChange) {
      onValueChange(new Date())
    }
  }, [defaultToToday, value, onValueChange])

  const handleSelect = (selectedDate) => {
    onValueChange?.(selectedDate)
    setOpen(false)
  }

  const getApiDateString = (date) => {
    if (!date) return null
    return date.toISOString()
  }

  const getDateForApi = () => {
    return getApiDateString(effectiveValue)
  }

  const IconComponent = icon === "calendar" ? CalendarIcon : ChevronDownIcon

  return (
      <div className={cn("flex flex-col gap-3", className)}>
        {label && (
            <Label htmlFor={id} className="px-1">
              {label}
              {required && <span className="text-destructive ml-1">*</span>}
            </Label>
        )}
        <Popover open={open} onOpenChange={setOpen}>
          <PopoverTrigger asChild>
            <Button
                variant="outline"
                id={id}
                className={cn(
                    width,
                    "justify-between font-normal",
                    !effectiveValue && "text-muted-foreground"
                )}
                disabled={disabled}
                {...props}
            >
              {effectiveValue ? formatDate(effectiveValue) : placeholder}
              <IconComponent className="h-4 w-4" />
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-auto overflow-hidden p-0" align={align}>
            <Calendar
                mode="single"
                selected={effectiveValue}
                captionLayout="dropdown"
                onSelect={handleSelect}
                disabled={disabled}
            />
          </PopoverContent>
        </Popover>
      </div>
  )
}

export function DatePickerField({ label, id, required, ...props }) {
  return (
      <DatePicker
          label={label}
          id={id}
          required={required}
          {...props}
      />
  )
}

export function DatePickerInline({ className, ...props }) {
  const [open, setOpen] = React.useState(false)

  const handleSelect = (selectedDate) => {
    props.onValueChange?.(selectedDate)
    setOpen(false)
  }

  const IconComponent = props.icon === "calendar" ? CalendarIcon : ChevronDownIcon

  return (
      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button
              variant="outline"
              className={cn(
                  props.width || "w-48",
                  "justify-between font-normal",
                  !props.value && "text-muted-foreground",
                  className
              )}
              disabled={props.disabled}
          >
            {props.value
                ? (props.formatDate?.(props.value) || props.value.toLocaleDateString())
                : (props.placeholder || "Select date")
            }
            <IconComponent className="h-4 w-4" />
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto overflow-hidden p-0" align={props.align || "start"}>
          <Calendar
              mode="single"
              selected={props.value}
              captionLayout="dropdown"
              onSelect={handleSelect}
              disabled={props.disabled}
          />
        </PopoverContent>
      </Popover>
  )
}

export default DatePicker


export const DatePickerUtils = {
  toApiString: (date) => {
    if (!date) return null
    return date.toISOString()
  },

  fromApiString: (isoString) => {
    if (!isoString) return null
    return new Date(isoString)
  },

  toMySQLDateTime: (date) => {
    if (!date) return null
    return date.toISOString().slice(0, 19).replace('T', ' ')
  },

  toMySQLDate: (date) => {
    if (!date) return null
    return date.toISOString().split('T')[0]
  }
}