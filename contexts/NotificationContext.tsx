

import React, { createContext, useState, useContext, ReactNode, useCallback } from 'react';
import { View } from 'react-native';
// FIX: Import styled from nativewind to handle className prop.
import { styled } from 'nativewind';
import type { NotificationType } from '../types';
import Notification from '../components/Notification';

interface NotificationMessage {
  id: number;
  message: string;
  type: NotificationType;
}

interface NotificationContextType {
  addNotification: (message: string, type: NotificationType) => void;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

// FIX: Create a styled version of View to accept the className prop.
const StyledView = styled(View);

export const NotificationProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [notifications, setNotifications] = useState<NotificationMessage[]>([]);

  const addNotification = useCallback((message: string, type: NotificationType) => {
    const id = new Date().getTime();
    setNotifications(prev => [...prev, { id, message, type }]);
  }, []);

  const removeNotification = useCallback((id: number) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  }, []);

  return (
    <NotificationContext.Provider value={{ addNotification }}>
      {children}
      {/* FIX: Use StyledView to apply className. */}
      <StyledView className="absolute top-12 right-0 left-0 z-50">
        {notifications.map(notification => (
          <Notification
            key={notification.id}
            message={notification.message}
            type={notification.type}
            onDismiss={() => removeNotification(notification.id)}
          />
        ))}
      </StyledView>
    </NotificationContext.Provider>
  );
};

export const useNotification = (): NotificationContextType => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
};