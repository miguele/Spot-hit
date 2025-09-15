import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary';
  className?: string;
  href?: string;
}

const Button: React.FC<ButtonProps> = ({ children, variant = 'primary', className = '', href, disabled, ...props }) => {
  const baseClasses = 'font-bold py-3 px-6 rounded-full transition-all duration-300 ease-in-out focus:outline-none focus:ring-4 disabled:opacity-50 disabled:cursor-not-allowed text-lg inline-flex items-center justify-center';

  const variantClasses = {
    primary: 'bg-[#1DB954] text-black hover:bg-[#1ED760] focus:ring-[#1ED760]/50 transform hover:scale-105',
    secondary: 'bg-gray-700 text-white hover:bg-gray-600 focus:ring-gray-500/50',
  };

  const finalClassName = `${baseClasses} ${variantClasses[variant]} ${className}`;

  if (href) {
    if (disabled) {
      // A disabled link should not be navigable. We render an `a` tag without an href
      // and prevent default click behavior to ensure it's non-interactive.
      return (
        <a
          className={`${finalClassName} opacity-50 cursor-not-allowed`}
          onClick={(e) => e.preventDefault()}
          aria-disabled={true}
          role="button"
        >
          {children}
        </a>
      );
    }
    
    // An enabled link is a standard anchor tag. Open in a new tab to avoid sandbox restrictions.
    return (
      <a
        href={href}
        target="_blank"
        rel="noopener noreferrer"
        className={finalClassName}
        role="button"
      >
        {children}
      </a>
    );
  }

  // For buttons, the `disabled` attribute is supported and works with Tailwind's `disabled:` variants.
  return (
    <button
      className={finalClassName}
      disabled={disabled}
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;