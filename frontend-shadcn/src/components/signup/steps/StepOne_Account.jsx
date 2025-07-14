"use client"

import { useForm } from "react-hook-form"
import { useState, useRef } from "react"
import debounce from "lodash.debounce"
import {
  Form,
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Eye, EyeOff } from "lucide-react"

const FIELDS = [
  { name: "firstName", label: "First Name" },
  { name: "lastName", label: "Last Name" },
  { name: "username", label: "Username" },
  { name: "email", label: "Email", type: "email" },
  { name: "password", label: "Password", type: "password" },
  { name: "confirmPassword", label: "Confirm Password", type: "password" },
]

export default function StepOne({ formData, updateField, next }) {
  const [visibilities, setVisibilities] = useState({
    password: false,
    confirmPassword: false,
  })

  const [validFields, setValidFields] = useState({})
  const form = useForm({
    defaultValues: formData,
    mode: "onChange",
  })

  const debouncers = useRef({})
  const REQUIRED_FIELDS = FIELDS.map((f) => f.name)
  const isFormValid = REQUIRED_FIELDS.every((field) => validFields[field])

  const validateWithBackend = async (field, value, values) => {
    const payload = { field, value }
    if (field === "confirmPassword") {
      payload.extra = { password: values.password }
    }

    try {
      const res = await fetch(
          "http://localhost:8080/airchive_war_exploded/api/users/validate",
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
            credentials: "include",
          }
      )

      if (!res.ok) {
        const data = await res.json()
        return data.message || "Invalid input"
      }

      return true
    } catch {
      return "Could not connect to server"
    }
  }

  const getDebouncedValidator = (field) => {
    if (!debouncers.current[field]) {
      debouncers.current[field] = debounce(async (value, values) => {
        if (!value.trim()) {
          setValidFields((prev) => ({ ...prev, [field]: false }))
          form.setError(field, {
            type: "manual",
            message: "Please fill out this field.",
          })
          return
        }

        const result = await validateWithBackend(field, value, values)

        if (result === true) {
          form.clearErrors(field)
          setValidFields((prev) => ({ ...prev, [field]: true }))
        } else {
          form.setError(field, {
            type: "manual",
            message: result,
          })
          setValidFields((prev) => ({ ...prev, [field]: false }))
        }
      }, 400)
    }

    return debouncers.current[field]
  }

  const onSubmit = async (values) => {
    let allValid = true

    for (const field of REQUIRED_FIELDS) {
      const value = values[field]
      if (!value.trim()) {
        allValid = false
        form.setError(field, {
          type: "manual",
          message: "Please fill out this field.",
        })
        setValidFields((prev) => ({ ...prev, [field]: false }))
        continue
      }

      const result = await validateWithBackend(field, value, values)
      if (result !== true) {
        allValid = false
        form.setError(field, {
          type: "manual",
          message: result,
        })
        setValidFields((prev) => ({ ...prev, [field]: false }))
      } else {
        setValidFields((prev) => ({ ...prev, [field]: true }))
      }
    }

    if (allValid) {
      Object.entries(values).forEach(([k, v]) => updateField(k, v))
      next()
    }
  }

  return (
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          {FIELDS.map(({ name, label, type }) => (
              <FormField
                  key={name}
                  control={form.control}
                  name={name}
                  render={({ field, fieldState }) => {
                    const isErrored = !!fieldState.error
                    const isValid = validFields[name]

                    return (
                        <FormItem>
                          <FormLabel className={isErrored ? "text-destructive" : ""}>
                            {label}
                          </FormLabel>
                          <FormControl>
                            <div className="relative">
                              <Input
                                  {...field}
                                  type={
                                    type === "password"
                                        ? visibilities[name]
                                            ? "text"
                                            : "password"
                                        : type || "text"
                                  }
                                  onChange={(e) => {
                                    const value = e.target.value
                                    field.onChange(e)

                                    if (
                                        name === "password" &&
                                        form.getValues("confirmPassword")
                                    ) {
                                      getDebouncedValidator("confirmPassword")(
                                          form.getValues("confirmPassword"),
                                          {
                                            ...form.getValues(),
                                            password: value,
                                          }
                                      )
                                    }

                                    getDebouncedValidator(name)(value, {
                                      ...form.getValues(),
                                      [name]: value,
                                    })
                                  }}
                                  className={
                                    isErrored
                                        ? "border-destructive focus-visible:ring-destructive"
                                        : isValid
                                            ? "border-[var(--chart-2)] focus-visible:ring-[var(--chart-2)]"
                                            : "focus-visible:ring-0"
                                  }
                              />
                              {type === "password" && (
                                  <button
                                      type="button"
                                      className="absolute right-2 top-2 text-muted-foreground"
                                      onClick={() =>
                                          setVisibilities((prev) => ({
                                            ...prev,
                                            [name]: !prev[name],
                                          }))
                                      }
                                  >
                                    {visibilities[name] ? (
                                        <EyeOff className="h-4 w-4" />
                                    ) : (
                                        <Eye className="h-4 w-4" />
                                    )}
                                  </button>
                              )}
                            </div>
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                    )
                  }}
              />
          ))}

          <Button
              type="submit"
              className={`w-full ${!isFormValid ? "opacity-50 cursor-not-allowed" : ""}`}
              disabled={!isFormValid}
          >
            Next
          </Button>
        </form>
      </Form>
  )
}
