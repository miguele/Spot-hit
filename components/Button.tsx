

import React from 'react';
import { TouchableOpacity, Text, View } from 'react-native';

interface ButtonProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary';
  className?: string;
  onPress?: () => void;
  disabled?: boolean;
}

const Button: React.FC<ButtonProps> = ({ children, variant = 'primary', className = '', onPress, disabled = false }) => {
  const baseClasses = 'py-3 px-6 rounded-full items-center justify-center';

  const variantClasses = {
    primary: 'bg-[#1DB954]',
    secondary: 'bg-gray-700',
  };

  const textVariantClasses = {
    primary: 'text-black',
    secondary: 'text-white',
  }

  const finalClassName = `${baseClasses} ${variantClasses[variant]} ${disabled ? 'opacity-50' : ''} ${className}`;
  const textClassName = `font-bold text-lg ${textVariantClasses[variant]}`;

  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={disabled}
      className={finalClassName}
      activeOpacity={0.7}
    >
      <Text className={textClassName}>{children}</Text>
    </TouchableOpacity>
  );
};

export default Button;