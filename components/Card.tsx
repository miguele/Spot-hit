
import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  onClick?: () => void;
  isProcessing?: boolean;
}

const Card: React.FC<CardProps> = ({ children, className = '', onClick, isProcessing = false }) => {
  const processingClass = isProcessing ? 'animate-pulse-processing' : '';

  return (
    <div
      onClick={onClick}
      className={`bg-gray-800/50 backdrop-blur-sm border border-gray-700 rounded-2xl shadow-lg p-6 ${processingClass} ${className}`}
    >
      {children}
    </div>
  );
};

export default Card;