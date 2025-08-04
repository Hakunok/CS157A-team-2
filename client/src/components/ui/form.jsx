import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import {
  Controller,
  FormProvider,
  useFormContext,
  useFormState,
} from "react-hook-form"
import { cn } from "@/lib/utils"
import { Label } from "@/components/ui/label"

const Form = FormProvider

const FormFieldContext = React.createContext({})

const FormField = (props) => {
  return (
      <FormFieldContext.Provider value={{ name: props.name }}>
        <Controller {...props} />
      </FormFieldContext.Provider>
  )
}

const FormItemContext = React.createContext({})

function FormItem({ className, ...props }) {
  const id = React.useId()

  return (
      <FormItemContext.Provider value={{ id }}>
        <div
            data-slot="form-item"
            className={cn("grid gap-2 font-ui", className)}
            {...props}
        />
      </FormItemContext.Provider>
  )
}

function useFormField() {
  const fieldContext = React.useContext(FormFieldContext)
  const itemContext = React.useContext(FormItemContext)
  const { getFieldState } = useFormContext()
  const formState = useFormState({ name: fieldContext.name })
  const fieldState = getFieldState(fieldContext.name, formState)

  if (!fieldContext) {
    throw new Error("useFormField should be used within <FormField>")
  }

  const { id } = itemContext

  return {
    id,
    name: fieldContext.name,
    formItemId: `${id}-form-item`,
    formDescriptionId: `${id}-form-item-description`,
    formMessageId: `${id}-form-item-message`,
    ...fieldState,
  }
}

function FormLabel({ className, ...props }) {
  const { error, formItemId } = useFormField()

  return (
      <Label
          data-slot="form-label"
          htmlFor={formItemId}
          data-error={!!error}
          className={cn(
              "text-sm font-ui font-normal transition-colors",
              "data-[error=true]:text-[var(--color-destructive)]",
              className
          )}
          {...props}
      />
  )
}

function FormControl({ ...props }) {
  const { error, formItemId, formDescriptionId, formMessageId } = useFormField()

  return (
      <Slot
          data-slot="form-control"
          id={formItemId}
          aria-describedby={
            !error ? formDescriptionId : `${formDescriptionId} ${formMessageId}`
          }
          aria-invalid={!!error}
          className={cn(
              // Theme tokens applied here
              "focus-visible:ring-2 focus-visible:ring-[var(--color-ring)] focus-visible:ring-offset-2 ring-offset-background",
              "aria-invalid:ring-[var(--color-destructive)]/30 aria-invalid:border-[var(--color-destructive)]",
              props.className
          )}
          {...props}
      />
  )
}

function FormDescription({ className, ...props }) {
  const { formDescriptionId } = useFormField()

  return (
      <p
          data-slot="form-description"
          id={formDescriptionId}
          className={cn(
              "text-[var(--color-muted-foreground)] text-sm font-content leading-normal",
              className
          )}
          {...props}
      />
  )
}

function FormMessage({ className, ...props }) {
  const { error, formMessageId } = useFormField()
  const body = error ? String(error?.message ?? "") : props.children

  if (!body) return null

  return (
      <p
          data-slot="form-message"
          id={formMessageId}
          className={cn(
              "text-[var(--color-destructive)] text-sm font-ui font-normal leading-normal",
              className
          )}
          {...props}
      >
        {body}
      </p>
  )
}

export {
  useFormField,
  Form,
  FormItem,
  FormLabel,
  FormControl,
  FormDescription,
  FormMessage,
  FormField,
}
