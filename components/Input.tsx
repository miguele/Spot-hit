

import React from 'react';
import { TextInput } from 'react-native';
// FIX: Import styled from nativewind to handle className prop.
import { styled } from 'nativewind';

interface InputProps extends React.ComponentProps<typeof TextInput> {
  className?: string;
}

// FIX: Create a styled version of TextInput to accept the className prop.
const StyledTextInput = styled(TextInput);

const Input: React.FC<InputProps> = ({ className = '', ...props }) => {
  return (
    // FIX: Use StyledTextInput to apply className.
    <StyledTextInput
      className={`bg-gray-700 border-2 border-gray-600 text-white text-center text-lg rounded-full w-full py-3 px-6 ${className}`}
      placeholderTextColor="#9CA3AF"
      {...props}
    />
  );
};

export default Input;