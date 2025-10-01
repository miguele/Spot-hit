

import React, { useEffect } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { Svg, Path } from 'react-native-svg';
import type { NotificationType } from '../types';

interface NotificationProps {
  message: string;
  type: NotificationType;
  onDismiss: () => void;
}

// FIX: Replaced JSX.Element with React.ReactNode to resolve namespace error.
const icons: Record<NotificationType, React.ReactNode> = {
  success: (
    <Svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"><Path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" /></Svg>
  ),
  error: (
    <Svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"><Path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" /></Svg>
  ),
  info: (
    <Svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"><Path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></Svg>
  ),
};

const backgroundColors: Record<NotificationType, string> = {
    success: 'bg-green-500/90',
    error: 'bg-red-500/90',
    info: 'bg-blue-500/90',
};


const Notification: React.FC<NotificationProps> = ({ message, type, onDismiss }) => {
    useEffect(() => {
        const timer = setTimeout(() => {
            onDismiss();
        }, 5000); // Auto-dismiss after 5 seconds

        return () => clearTimeout(timer);
    }, [onDismiss]);

    return (
        <TouchableOpacity 
            onPress={onDismiss} 
            className={`flex-row items-center p-4 rounded-lg shadow-lg m-2 ${backgroundColors[type]}`}
            activeOpacity={0.8}
        >
            <View className="flex-shrink-0">{icons[type]}</View>
            <Text className="ml-3 text-white font-semibold flex-1">{message}</Text>
        </TouchableOpacity>
    );
};

export default Notification;