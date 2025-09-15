
import React from 'react';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  className?: string;
}

const Input: React.FC<InputProps> = ({ className = '', ...props }) => {
  return (
    <input
      className={`bg-gray-700 border-2 border-gray-600 text-white placeholder-gray-400 text-center text-lg rounded-full w-full py-3 px-6 focus:outline-none focus:ring-4 focus:ring-[#1ED760]/50 focus:border-[#1ED760] transition-all duration-300 ${className}`}
      {...props}
    />
  );
};

export default Input;
