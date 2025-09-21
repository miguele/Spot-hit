

import React from 'react';
import { TouchableOpacity, Text, View } from 'react-native';
// FIX: Import styled from nativewind to handle className prop.
import { styled } from 'nativewind';

interface ButtonProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary';
  className?: string;
  onPress?: () => void;
  disabled?: boolean;
}

// FIX: Create styled versions of components to accept the className prop.
const StyledTouchableOpacity = styled(TouchableOpacity);
const StyledText = styled(Text);

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
    // FIX: Use StyledTouchableOpacity to apply className.
    <StyledTouchableOpacity
      onPress={onPress}
      disabled={disabled}
      className={finalClassName}
      activeOpacity={0.7}
    >
      {/* FIX: Use StyledText to apply className. */}
      <StyledText className={textClassName}>{children}</StyledText>
    </StyledTouchableOpacity>
  );
};

export default Button;