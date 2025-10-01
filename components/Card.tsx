

import React from 'react';
import { View } from 'react-native';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  isProcessing?: boolean; // Note: animation needs a nativewind setup or reanimated
}

const Card: React.FC<CardProps> = ({ children, className = '', isProcessing = false }) => {
  // The pulse animation from web would need to be configured in tailwind.config.js for nativewind
  // For now, we just apply base styles.
  const processingClass = isProcessing ? 'border-green-500' : 'border-gray-700';

  return (
    <View
      className={`bg-gray-800/80 border rounded-2xl p-6 ${processingClass} ${className}`}
    >
      {children}
    </View>
  );
};

export default Card;