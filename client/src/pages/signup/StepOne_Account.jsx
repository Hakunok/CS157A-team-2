"use client"

import { useForm } from "react-hook-form"
import { useState, useRef, useCallback } from "react"
import debounce from "lodash.debounce"
import {
  Form,
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form.jsx"
import { Input } from "@/components/ui/input.jsx"
import { Button } from "@/components/ui/button.jsx"
import { Eye, EyeOff, CheckCircle } from "lucide-react"

const FIELDS = [
  { name: "firstName", label: "First Name" },
  { name: "lastName", label: "Last Name" },
  { name: "username", label: "Username" },
  { name: "email", label: "Email", type: "email" },
  { name: "password", label: "Password", type: "password" },
  { name: "confirmPassword", label: "Confirm Password", type: "password" },
]

const API_BASE_URL = "http://localhost:8080/server_war_exploded/api"

export default function StepOne({ formData, updateField, next }) {
  const [visibilities, setVisibilities] = useState({
    password: false,
    confirmPassword: false,
  })

  const [fieldStates, setFieldStates] = useState({})
  const [isValidating, setIsValidating] = useState({})

  const form = useForm({
    defaultValues: formData,
    mode: "onChange",
  })

  const debouncers = useRef({})
  const abortControllers = useRef({})
  const REQUIRED_FIELDS = FIELDS.map((f) => f.name)

  const isFormValid = REQUIRED_FIELDS.every((field) =>
      fieldStates[field]?.isValid === true
  )

  const validateWithBackend = useCallback(async (field, value, allValues) => {
    if (abortControllers.current[field]) {
      abortControllers.current[field].abort()
    }

    const controller = new AbortController()
    abortControllers.current[field] = controller

    const payload = { field, value }

    if (field === "confirmPassword") {
      payload.extra = { password: allValues.password }
    }

    try {
      const response = await fetch(`${API_BASE_URL}/users/validate`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
        credentials: "include",
        signal: controller.signal,
      })

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const result = await response.json()

      if (result.isValid) {
        return { isValid: true, message: null }
      } else {
        return { isValid: false, message: result.message || "Invalid input" }
      }
    } catch (error) {
      if (error.name === 'AbortError') {
        return null
      }
      console.error(`Validation error for ${field}:`, error)
      return {
        isValid: false,
        message: "Could not connect to server. Please try again."
      }
    } finally {
      delete abortControllers.current[field]
    }
  }, [])

  const getDebouncedValidator = useCallback((field) => {
    if (!debouncers.current[field]) {
      debouncers.current[field] = debounce(async (value, allValues) => {
        setFieldStates(prev => ({
          ...prev,
          [field]: { isValid: false, message: null }
        }))

        if (!value || !value.trim()) {
          const errorMessage = "This field is required."
          form.setError(field, {
            type: "manual",
            message: errorMessage,
          })
          setFieldStates(prev => ({
            ...prev,
            [field]: { isValid: false, message: errorMessage }
          }))
          setIsValidating(prev => ({ ...prev, [field]: false }))
          return
        }

        setIsValidating(prev => ({ ...prev, [field]: true }))
        const result = await validateWithBackend(field, value, allValues)
        if (result === null) return

        setIsValidating(prev => ({ ...prev, [field]: false }))

        if (result.isValid) {
          form.clearErrors(field)
          setFieldStates(prev => ({
            ...prev,
            [field]: { isValid: true, message: null }
          }))
        } else {
          form.setError(field, {
            type: "manual",
            message: result.message,
          })
          setFieldStates(prev => ({
            ...prev,
            [field]: { isValid: false, message: result.message }
          }))
        }
      }, 400)
    }
    return debouncers.current[field]
  }, [form, validateWithBackend])

  const [isSubmitting, setIsSubmitting] = useState(false)

  const onSubmit = async (values) => {
    setIsSubmitting(true)
    let allValid = true
    const validationPromises = REQUIRED_FIELDS.map(async (field) => {
      const value = values[field]
      if (!value || !value.trim()) {
        const msg = "This field is required."
        form.setError(field, { type: "manual", message: msg })
        setFieldStates(prev => ({...prev, [field]: { isValid: false, message: msg }}))
        return false
      }
      const result = await validateWithBackend(field, value, values)
      if (result && !result.isValid) {
        form.setError(field, { type: "manual", message: result.message })
        setFieldStates(prev => ({...prev, [field]: { isValid: false, message: result.message }}))
        return false
      } else if (result) {
        setFieldStates(prev => ({...prev, [field]: { isValid: true, message: null }}))
        return true
      }
      return false
    })

    const results = await Promise.all(validationPromises)
    allValid = results.every(res => res)

    if (allValid) {
      try {
        const registerData = {
          username: values.username,
          firstName: values.firstName,
          lastName: values.lastName,
          email: values.email,
          password: values.password
        }

        const response = await fetch(`${API_BASE_URL}/auth/register`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(registerData),
          credentials: "include",
        })

        if (!response.ok) {
          const errorData = await response.json()
          throw new Error(errorData.error || "Registration failed")
        }

        Object.entries(values).forEach(([key, value]) => updateField(key, value))

        if (next) next()

      } catch (error) {
        console.error("Registration error:", error)
        form.setError("root", {
          type: "manual",
          message: error.message || "Registration failed. Please try again."
        })
      }
    }
    setIsSubmitting(false)
  }

  const getFieldIcon = (fieldName) => {
    if (isValidating[fieldName]) {
      return <div className="animate-spin h-4 w-4 border-2 border-blue-500 border-t-transparent rounded-full" />
    }
    if (fieldStates[fieldName]?.isValid) {
      return <CheckCircle className="h-4 w-4 text-green-500" />
    }
    return null
  }

  const getFieldClassName = (fieldState, isErrored) => {
    if (isErrored) return "border-destructive focus-visible:ring-destructive"
    if (fieldState?.isValid) return "border-green-500 focus-visible:ring-green-500"
    return "focus-visible:ring-0"
  }

  return (
      <Form {...form}>
        <div className="text-center mb-6">
          <h2 className="text-2xl font-bold">Create Your Account</h2>
          <p className="text-muted-foreground">First, let's get your basic information.</p>
        </div>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          {FIELDS.map(({ name, label, type }) => (
              <FormField
                  key={name}
                  control={form.control}
                  name={name}
                  render={({ field, fieldState }) => (
                      <FormItem>
                        <FormLabel className={fieldState.error ? "text-destructive" : ""}>
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
                                  if (name === "password" && form.getValues("confirmPassword")) {
                                    getDebouncedValidator("confirmPassword")(
                                        form.getValues("confirmPassword"),
                                        { ...form.getValues(), password: value }
                                    )
                                  }
                                  getDebouncedValidator(name)(value, form.getValues())
                                }}
                                className={getFieldClassName(fieldStates[name], !!fieldState.error)}
                            />
                            <div className="absolute right-3 top-1/2 -translate-y-1/2 flex items-center gap-2">
                              {getFieldIcon(name)}
                              {type === "password" && (
                                  <button
                                      type="button"
                                      className="text-muted-foreground hover:text-foreground"
                                      onClick={() =>
                                          setVisibilities(prev => ({...prev,[name]: !prev[name]}))
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
                          </div>
                        </FormControl>
                        <FormMessage />
                        {name === "confirmPassword" && form.formState.errors.root && (
                            <p className="text-sm text-destructive mt-1">
                              {form.formState.errors.root.message}
                            </p>
                        )}
                      </FormItem>
                  )}
              />
          ))}
          <Button
              type="submit"
              className="w-full !mt-6"
              disabled={!isFormValid || isSubmitting}
          >
            {isSubmitting ? "Creating Account..." : "Create Account & Continue"}
          </Button>
        </form>
      </Form>
  )
}