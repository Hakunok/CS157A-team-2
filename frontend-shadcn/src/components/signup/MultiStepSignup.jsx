import { Progress } from "@/components/ui/progress"
import { useState } from "react"
import { CheckCircle } from "lucide-react"
import StepOne from "./steps/StepOne_Account"
import StepTwo from "./steps/StepTwo_Role"
import StepThree from "./steps/StepThree_Topics"

const TOTAL_STEPS = 3

export default function MultiStepSignup({ onComplete }) {
  const [step, setStep] = useState(1)
  const [isCompleting, setIsCompleting] = useState(false)
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    requestAuthor: false,
    isAdmin: false,
  });


  const next = () => setStep((prev) => Math.min(prev + 1, TOTAL_STEPS + 1))
  const skip = () => setStep((prev) => Math.min(prev + 1, TOTAL_STEPS + 1))

  const updateField = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }


  const progressValue = isCompleting ? 100 : ((step - 1) / TOTAL_STEPS) * 100

  const handleComplete = () => {
    setIsCompleting(true)
    setTimeout(() => {
      if (onComplete) onComplete()
    }, 1500)
  }

  const renderStep = () => {
    switch (step) {
      case 1:
        return <StepOne formData={formData} updateField={updateField} next={next} />
      case 2:
        return <StepTwo formData={formData} updateField={updateField} next={next} skip={skip} />
      case 3:
        return <StepThree formData={formData} onComplete={handleComplete} />
      default:
        return null
    }
  }


  return (
      <div className="w-full max-w-2xl mx-auto border rounded-xl shadow-lg">
        <div className="p-8 space-y-8">
          <div>
            <Progress value={progressValue} className="transition-all duration-1000 ease-out" />
            <div className="flex items-center justify-between mt-2">
              <p className="text-sm text-muted-foreground">
                {isCompleting ? "Completing signup..." : `Step ${step} of ${TOTAL_STEPS}`}
              </p>
              {isCompleting && (
                  <div className="flex items-center gap-2 text-sm text-green-600">
                    <CheckCircle className="h-4 w-4" />
                    <span>Complete!</span>
                  </div>
              )}
            </div>
          </div>

          {!isCompleting && renderStep()}

          {isCompleting && (
              <div className="text-center py-12">
                <div className="inline-flex items-center justify-center w-20 h-20 bg-green-100 rounded-full mb-4">
                  <CheckCircle className="h-10 w-10 text-green-600" />
                </div>
                <h3 className="text-2xl font-semibold text-green-700 mb-2">
                  Welcome to aiRchive!
                </h3>
                <p className="text-muted-foreground">
                  Redirecting you to the homepage...
                </p>
              </div>
          )}
        </div>
      </div>
  )
}
