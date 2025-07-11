import React, { useState, useCallback, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useApp } from '../../AppContext';
import { Form, Button, Container, Card, Alert, Row, Col, Spinner } from 'react-bootstrap';
import { Formik } from 'formik';
import * as yup from 'yup';
import debounce from 'lodash.debounce';

const API_BASE_URL = 'http://localhost:8080/airchive_war_exploded/api';

const validateWithBackend = async (field, value, allValues) => {
  try {
    const payload = { field, value, extra: { password: allValues.password } };

    const response = await fetch(`${API_BASE_URL}/users/validate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(payload)
    });

    if (response.ok) return true;

    const errorData = await response.json();
    return errorData.message || 'An unknown error occurred.';
  } catch (error) {
    console.error("Validation fetch error:", error);
    return 'Cannot connect to server for validation.';
  }
};

const SignUpPage = () => {
  const { register } = useApp();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState('');
  const [validatingFields, setValidatingFields] = useState({});
  const [fieldErrors, setFieldErrors] = useState({});

  const debouncers = useRef({}).current;

  const getDebouncerForField = (fieldName) => {
    if (!debouncers[fieldName]) {
      debouncers[fieldName] = debounce(async (value, allValues) => {
        if (!value || !value.trim()) {
          setValidatingFields(prev => ({ ...prev, [fieldName]: false }));
          setFieldErrors(prev => ({ ...prev, [fieldName]: undefined }));
          return;
        }

        setValidatingFields(prev => ({ ...prev, [fieldName]: true }));
        const result = await validateWithBackend(fieldName, value, allValues);
        setValidatingFields(prev => ({ ...prev, [fieldName]: false }));

        if (result === true) {
          setFieldErrors(prev => ({ ...prev, [fieldName]: 'valid' }));
        } else {
          setFieldErrors(prev => ({ ...prev, [fieldName]: result }));
        }
      }, 400);
    }
    return debouncers[fieldName];
  };

  // Simple client-side validation schema without backend validation
  const schema = yup.object().shape({
    firstName: yup.string().required('First name is required'),
    lastName: yup.string().required('Last name is required'),
    username: yup.string().required('Username is required'),
    email: yup.string().email('Invalid email format').required('Email is required'),
    password: yup.string().required('Password is required'),
    confirmPassword: yup.string().required('Please confirm your password'),
  });

  const handleFormSubmit = async (values, { setSubmitting }) => {
    setServerError('');

    // Final validation before submitting
    const finalValidationPromises = [];
    const requiredFields = ['firstName', 'lastName', 'username', 'email', 'password', 'confirmPassword'];

    // Clear all previous validation states
    setFieldErrors({});
    setValidatingFields({});

    // Validate all fields one final time
    for (const field of requiredFields) {
      if (values[field] && values[field].trim()) {
        const promise = validateWithBackend(field, values[field], values);
        finalValidationPromises.push({ field, promise });
      }
    }

    try {
      // Wait for all validations to complete
      const results = await Promise.all(finalValidationPromises.map(({ promise }) => promise));

      // Check if all validations passed
      const allValid = results.every(result => result === true);

      if (!allValid) {
        // Set errors for failed validations
        const newErrors = {};
        results.forEach((result, index) => {
          if (result !== true) {
            newErrors[finalValidationPromises[index].field] = result;
          }
        });
        setFieldErrors(newErrors);
        setServerError('Please fix the validation errors before submitting.');
        return;
      }

      // All validations passed, proceed with registration
      await register(values);
      navigate('/dashboard');
    } catch (err) {
      setServerError(err.message || 'Failed to create account.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleFieldChange = (fieldName, value, allValues, formikHandleChange) => {
    formikHandleChange({ target: { name: fieldName, value } });

    // Create updated values object for validation
    const updatedValues = { ...allValues, [fieldName]: value };

    // If field is cleared, immediately remove the 'valid' state
    if (!value || !value.trim()) {
      setFieldErrors(prev => ({ ...prev, [fieldName]: undefined }));
      setValidatingFields(prev => ({ ...prev, [fieldName]: false }));
    }

    // Special handling for password field changes
    if (fieldName === 'password') {
      // If password changes and confirmPassword has a value, re-validate confirmPassword
      if (updatedValues.confirmPassword && updatedValues.confirmPassword.trim()) {
        setFieldErrors(prev => ({ ...prev, confirmPassword: undefined }));
        const confirmPasswordDebouncer = getDebouncerForField('confirmPassword');
        confirmPasswordDebouncer(updatedValues.confirmPassword, updatedValues);
      }
    }

    // All fields now validate with backend
    if (['firstName', 'lastName', 'username', 'email', 'password', 'confirmPassword'].includes(fieldName)) {
      const fieldDebouncer = getDebouncerForField(fieldName);
      fieldDebouncer(value, updatedValues);
    }
  };

  // Helper function to determine field validation state
  const getFieldValidationState = (fieldName, values, errors, touched) => {
    // If field is empty and touched, show required error
    if (touched[fieldName] && (!values[fieldName] || !values[fieldName].trim())) {
      return 'error';
    }

    // If field has content and is touched
    if (touched[fieldName] && values[fieldName] && values[fieldName].trim()) {
      if (validatingFields[fieldName]) return 'validating';
      if (fieldErrors[fieldName] === 'valid') return 'success';
      if (fieldErrors[fieldName] && fieldErrors[fieldName] !== 'valid') return 'error';
    }

    return null;
  };

  // Helper function to check if form is valid for submit button
  const isFormValid = (values, errors) => {
    const requiredFields = ['firstName', 'lastName', 'username', 'email', 'password', 'confirmPassword'];
    const allFieldsFilled = requiredFields.every(field => values[field] && values[field].trim());
    const noFormikErrors = Object.keys(errors).length === 0;
    const noBackendErrors = Object.values(fieldErrors).every(error => error === 'valid' || error === undefined);
    const notValidating = Object.values(validatingFields).every(isValidating => !isValidating);
    return allFieldsFilled && noFormikErrors && noBackendErrors && notValidating;
  };

  return (
      <Container className="d-flex align-items-center justify-content-center" style={{ minHeight: '100vh', padding: '2rem 0' }}>
        <Card className="border-0" style={{ width: '500px' }}>
          <Card.Body>
            <h2 className="text-center mb-4">Create an Account</h2>
            {serverError && <Alert variant="danger">{serverError}</Alert>}

            <Formik
                validationSchema={schema}
                onSubmit={handleFormSubmit}
                initialValues={{ firstName: '', lastName: '', username: '', email: '', password: '', confirmPassword: '' }}
                validateOnChange={true}
                validateOnBlur={true}
            >
              {({ handleSubmit, handleChange, values, errors, touched, isSubmitting, setFieldTouched }) => {
                const getFieldFeedback = (fieldName, label) => {
                  const state = getFieldValidationState(fieldName, values, errors, touched);
                  if (state === 'validating') return 'Validating...';
                  if (state === 'error') {
                    // If field is empty and touched, show required message
                    if (touched[fieldName] && (!values[fieldName] || !values[fieldName].trim())) {
                      return `${label} is required`;
                    }
                    // Otherwise show backend error
                    return fieldErrors[fieldName] || errors[fieldName];
                  }
                  if (state === 'success') {
                    switch (fieldName) {
                      case 'firstName': return 'First name is valid!';
                      case 'lastName': return 'Last name is valid!';
                      case 'username': return 'Username is available!';
                      case 'email': return 'Email is available!';
                      case 'password': return 'Password meets requirements!';
                      case 'confirmPassword': return 'Passwords match!';
                      default: return 'Looks good!';
                    }
                  }
                  return '';
                };

                const renderField = (fieldName, type = 'text', label) => {
                  const state = getFieldValidationState(fieldName, values, errors, touched);

                  // Define autocomplete attributes based on field type
                  let autoCompleteProps = {};

                  // Set specific autocomplete for each field to enable browser popups
                  if (fieldName === 'username') {
                    autoCompleteProps.autoComplete = "username";
                  } else if (fieldName === 'email') {
                    autoCompleteProps.autoComplete = "email";
                  } else if (fieldName === 'password') {
                    autoCompleteProps.autoComplete = "new-password";
                  } else if (fieldName === 'confirmPassword') {
                    autoCompleteProps.autoComplete = "new-password";
                  } else if (fieldName === 'firstName') {
                    autoCompleteProps.autoComplete = "given-name";
                  } else if (fieldName === 'lastName') {
                    autoCompleteProps.autoComplete = "family-name";
                  }

                  return (
                      <Form.Group className="mb-3" controlId={fieldName}>
                        <Form.Label>{label}</Form.Label>
                        <Form.Control
                            type={type}
                            name={fieldName}
                            value={values[fieldName]}
                            onChange={(e) => {
                              const value = e.target.value;
                              setFieldTouched(fieldName, true);
                              handleFieldChange(fieldName, value, values, handleChange);
                            }}
                            isValid={state === 'success'}
                            isInvalid={state === 'error'}
                            {...autoCompleteProps}
                        />
                        {state === 'validating' && (
                            <div className="text-muted small mt-1">
                              <Spinner as="span" animation="border" size="sm" className="me-1" />
                              Validating...
                            </div>
                        )}
                        {state === 'error' && (
                            <Form.Control.Feedback type="invalid">
                              {getFieldFeedback(fieldName, label)}
                            </Form.Control.Feedback>
                        )}
                        {state === 'success' && (
                            <Form.Control.Feedback type="valid">
                              {getFieldFeedback(fieldName)}
                            </Form.Control.Feedback>
                        )}
                      </Form.Group>
                  );
                };

                return (
                    <Form noValidate onSubmit={handleSubmit}>
                      <Row>
                        <Col>
                          {renderField('firstName', 'text', 'First name')}
                        </Col>
                        <Col>
                          {renderField('lastName', 'text', 'Last name')}
                        </Col>
                      </Row>
                      {renderField('username', 'text', 'Username')}
                      {renderField('email', 'email', 'Email')}
                      {renderField('password', 'password', 'Password')}
                      {renderField('confirmPassword', 'password', 'Confirm Password')}
                      <Button
                          disabled={isSubmitting || !isFormValid(values, errors)}
                          className="w-100 mt-3"
                          type="submit"
                      >
                        {isSubmitting ? <><Spinner as="span" animation="border" size="sm" /> Signing Up...</> : 'Sign Up'}
                      </Button>
                    </Form>
                );
              }}
            </Formik>
            <div className="w-100 text-center mt-3">
              Already have an account? <Link to="/signin">Sign In</Link>
            </div>
          </Card.Body>
        </Card>
      </Container>
  );
};

export default SignUpPage;