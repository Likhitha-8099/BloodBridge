import React, { forwardRef } from 'react';

/**
 * Reusable Input component supporting labels, error bounds, and ref forwarding.
 */
const Input = forwardRef(({ 
  label, 
  type = 'text', 
  error, 
  className = '', 
  ...props 
}, ref) => {
  return (
    <div className="w-full flex flex-col gap-1.5">
      {label && <label className="text-xs font-semibold text-gray-600 tracking-wide">{label}</label>}
      <input
        ref={ref}
        type={type}
        className={`w-full px-4 py-2.5 rounded-xl border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-offset-0 ${
          error 
            ? 'border-red-400 focus:border-red-500 focus:ring-red-100 bg-red-50/10' 
            : 'border-gray-200 focus:border-primary focus:ring-red-100'
        } bg-white text-gray-900 placeholder-gray-400 ${className}`}
        {...props}
      />
      {error && <span className="text-[11px] text-red-500 font-medium pl-1">{error}</span>}
    </div>
  );
});

Input.displayName = 'Input';

export default Input;
