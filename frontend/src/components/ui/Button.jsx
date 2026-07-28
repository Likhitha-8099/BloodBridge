import React from 'react';

/**
 * Reusable Button component with different visual styles and loading feedback.
 */
export default function Button({ 
  children, 
  type = 'button', 
  variant = 'primary', 
  className = '', 
  isLoading = false, 
  disabled = false, 
  ...props 
}) {
  const baseStyle = "px-4 py-2.5 rounded-xl font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 flex items-center justify-center disabled:opacity-60 disabled:cursor-not-allowed active:scale-[0.98] transform shadow-sm text-sm";
  
  const variants = {
    primary: "bg-primary text-white hover:bg-primary-dark focus:ring-primary",
    secondary: "bg-secondary text-white hover:bg-red-950 focus:ring-secondary",
    outline: "border border-gray-200 text-gray-700 bg-white hover:bg-gray-50 focus:ring-gray-400",
    danger: "bg-red-500 text-white hover:bg-red-600 focus:ring-red-500",
  };

  return (
    <button
      type={type}
      disabled={disabled || isLoading}
      className={`${baseStyle} ${variants[variant]} ${className}`}
      {...props}
    >
      {isLoading ? (
        <>
          <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-current" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Please wait...
        </>
      ) : children}
    </button>
  );
}
