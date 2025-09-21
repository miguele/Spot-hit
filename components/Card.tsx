

import React from 'react';
import { View } from 'react-native';
// FIX: Import styled from nativewind to handle className prop.
import { styled } from 'nativewind';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  isProcessing?: boolean; // Note: animation needs a nativewind setup or reanimated
}

// FIX: Create a styled version of View to accept the className prop.
const StyledView = styled(View);

const Card: React.FC<CardProps> = ({ children, className = '', isProcessing = false }) => {
  // The pulse animation from web would need to be configured in tailwind.config.js for nativewind
  // For now, we just apply base styles.
  const processingClass = isProcessing ? 'border-green-500' : 'border-gray-700';

  return (
    // FIX: Use StyledView to apply className.
    <StyledView
      className={`bg-gray-800/80 border rounded-2xl p-6 ${processingClass} ${className}`}
    >
      {children}
    </StyledView>
  );
};

export default Card;