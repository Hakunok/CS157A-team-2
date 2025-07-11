import { Progress } from "@/components/ui/progress"
import {useState} from "react";
import StepOne from "./steps/StepOne_Account";

const TOTAL_STEPS = 3

export default function MultiStepSignup({ onComplete }) {
  const [step, setStep] = useState(1)
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    firstName: "",
    lastName: "",
    password: "",
    confirmPassword: "",
    wantsAuthor: false,
    adminCode: "",
    topics: [],
  })

  const updateField = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const next = () => setStep((prev) => prev + 1)
  const back = () => setStep((prev) => prev - 1)

  const progressValue = ((step - 1) / TOTAL_STEPS) * 100

  return (
      <div className="max-w-xl mx-auto mt-8 p-6 border rounded-lg shadow-sm space-y-6">
        <div>
          <Progress value={progressValue} />
          <p className="text-sm text-muted-foreground mt-1">
            Step {step} of {TOTAL_STEPS}
          </p>
        </div>

        {step === 1 && (
            <StepOne formData={formData} updateField={updateField} next={next} />
        )}
      </div>
  )
}
