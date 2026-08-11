import React from 'react';

/**
 * Bulletproof ToggleSwitch Component for boolean flags, settings, and form states.
 * Fully compatible with React state and React Hook Form / Controller.
 */
export default function ToggleSwitch({
  checked = false,
  onChange,
  label,
  sublabel,
  disabled = false,
  name: _name,
  className = ''
}) {
  const handleToggle = () => {
    if (!disabled && onChange) {
      onChange(!checked);
    }
  };

  return (
    <div className={`flex items-start justify-between gap-4 py-2 ${className}`}>
      {(label || sublabel) && (
        <div className="flex flex-col cursor-pointer" onClick={handleToggle}>
          {label && <span className="text-xs font-bold text-gray-900 dark:text-white select-none">{label}</span>}
          {sublabel && <span className="text-[11px] text-gray-500 dark:text-slate-400 leading-relaxed select-none">{sublabel}</span>}
        </div>
      )}

      <button
        type="button"
        role="switch"
        aria-checked={checked}
        disabled={disabled}
        onClick={handleToggle}
        className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 disabled:opacity-40 disabled:cursor-not-allowed ${
          checked ? 'bg-primary' : 'bg-slate-200 dark:bg-slate-700'
        }`}
      >
        <span
          className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
            checked ? 'translate-x-5' : 'translate-x-0'
          }`}
        />
      </button>
    </div>
  );
}
