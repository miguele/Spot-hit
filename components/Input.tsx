

import React from 'react';
import { TextInput } from 'react-native';

interface InputProps extends React.ComponentProps<typeof TextInput> {
  className?: string;
}

const Input: React.FC<InputProps> = ({ className = '', ...props }) => {
  return (
    <TextInput
      className={`bg-gray-700 border-2 border-gray-600 text-white text-center text-lg rounded-full w-full py-3 px-6 ${className}`}
      placeholderTextColor="#9CA3AF"
      {...props}
    />
  );
};

export default Input;