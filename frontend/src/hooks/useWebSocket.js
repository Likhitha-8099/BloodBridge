import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import useAuthStore from '../store/authStore';

const getWsUrl = () => {
  if (import.meta.env.VITE_WS_URL) {
    return import.meta.env.VITE_WS_URL;
  }
  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083/api/v1';
  const origin = apiBase.replace(/\/api(\/v1)?\/?$/, '');
  return `${origin}/ws`;
};

/**
 * Enterprise Production-Grade React WebSocket STOMP Client Hook.
 * Decouples STOMP socket connection lifecycle from dynamic topic subscriptions.
 * Guaranteed never to disconnect during component re-renders.
 */
export const useWebSocket = (topics = [], onEventCallback = null) => {
  const { token } = useAuthStore();
  const [isConnected, setIsConnected] = useState(false);
  const [isFallback, setIsFallback] = useState(false);
  const [lastEvent, setLastEvent] = useState(null);

  const clientRef = useRef(null);
  const subscriptionsRef = useRef(new Map());
  const offlineTimerRef = useRef(null);
  const callbackRef = useRef(onEventCallback);

  useEffect(() => {
    callbackRef.current = onEventCallback;
  }, [onEventCallback]);

  const topicsKey = Array.isArray(topics) ? topics.filter(Boolean).sort().join(',') : '';

  // 1. Connection Management: Opens ONCE per auth token and stays alive
  useEffect(() => {
    if (!token) {
      if (clientRef.current) {
        try { clientRef.current.deactivate(); } catch {}
        clientRef.current = null;
      }
      setIsConnected(false);
      return;
    }

    const wsUrl = getWsUrl();
    console.log(`🔌 Initializing STOMP WebSocket client to ${wsUrl}`);

    const stompClient = new Client({
      webSocketFactory: () => {
        try {
          return new SockJS(wsUrl);
        } catch (err) {
          console.warn('SockJS initialization error:', err);
          return null;
        }
      },
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (msg) => {
        if (import.meta?.env?.DEV) {
          console.debug('[STOMP Debug]:', msg);
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 20000,
      heartbeatOutgoing: 20000,
    });

    stompClient.onConnect = () => {
      console.log(`✅ STOMP WebSocket Connection Established & Persistent at ${wsUrl}`);
      setIsConnected(true);
      setIsFallback(false);

      if (offlineTimerRef.current) {
        clearTimeout(offlineTimerRef.current);
        offlineTimerRef.current = null;
      }
    };

    stompClient.onStompError = (frame) => {
      console.warn('⚠️ STOMP Protocol Error:', frame?.headers?.['message'] || 'Unknown error');
      setIsConnected(false);
    };

    stompClient.onWebSocketClose = () => {
      console.warn('⚠️ STOMP WebSocket Closed. Auto-reconnecting in 5s...');
      setIsConnected(false);

      if (!offlineTimerRef.current) {
        offlineTimerRef.current = setTimeout(() => {
          console.warn('🕒 WebSocket offline > 15s. Activating REST fallback mode.');
          setIsFallback(true);
        }, 15000);
      }
    };

    stompClient.activate();
    clientRef.current = stompClient;

    const currentSubsRef = subscriptionsRef;

    return () => {
      console.log('🧹 Cleaning up STOMP WebSocket connection on auth unmount...');
      if (offlineTimerRef.current) {
        clearTimeout(offlineTimerRef.current);
      }
      const subs = currentSubsRef.current;
      subs.forEach((sub) => {
        try {
          if (sub && typeof sub.unsubscribe === 'function') {
            sub.unsubscribe();
          }
        } catch {}
      });
      subs.clear();

      try {
        stompClient.deactivate();
      } catch {}
      clientRef.current = null;
      setIsConnected(false);
    };
  }, [token]);

  // 2. Topic Subscriptions: Manages subscriptions dynamically on active connection without closing socket
  useEffect(() => {
    if (!isConnected || !clientRef.current) return;

    const currentTopics = topicsKey ? topicsKey.split(',') : [];
    const activeSubTopics = new Set(subscriptionsRef.current.keys());

    // Unsubscribe from topics no longer needed
    activeSubTopics.forEach((topic) => {
      if (!currentTopics.includes(topic)) {
        try {
          const sub = subscriptionsRef.current.get(topic);
          if (sub && typeof sub.unsubscribe === 'function') {
            sub.unsubscribe();
          }
        } catch {}
        subscriptionsRef.current.delete(topic);
        console.log(`🔇 Unsubscribed from STOMP Topic: ${topic}`);
      }
    });

    // Subscribe to new topics
    currentTopics.forEach((topic) => {
      if (topic && !subscriptionsRef.current.has(topic)) {
        try {
          const sub = clientRef.current.subscribe(topic, (message) => {
            try {
              const eventData = JSON.parse(message.body);
              console.log('[STOMP Realtime Event Received]:', eventData);
              setLastEvent(eventData);
              if (callbackRef.current) {
                callbackRef.current(eventData);
              }
            } catch (err) {
              console.error('Error parsing STOMP message:', err);
            }
          });
          subscriptionsRef.current.set(topic, sub);
          console.log(`📡 Subscribed to STOMP Topic: ${topic}`);
        } catch (subErr) {
          console.warn(`Failed to subscribe to topic ${topic}:`, subErr);
        }
      }
    });
  }, [isConnected, topicsKey]);

  const subscribe = useCallback(
    (topic, callback) => {
      if (!clientRef.current || !isConnected) return null;
      if (subscriptionsRef.current.has(topic)) return subscriptionsRef.current.get(topic);

      try {
        const sub = clientRef.current.subscribe(topic, (msg) => {
          try {
            const data = JSON.parse(msg.body);
            callback(data);
          } catch (e) {
            console.error('Error handling topic message:', e);
          }
        });
        subscriptionsRef.current.set(topic, sub);
        return sub;
      } catch (err) {
        console.warn('Error subscribing to topic:', topic, err);
        return null;
      }
    },
    [isConnected]
  );

  const publish = useCallback(
    (destination, body) => {
      if (clientRef.current && isConnected) {
        try {
          clientRef.current.publish({
            destination,
            body: JSON.stringify(body),
          });
        } catch (err) {
          console.warn('Error publishing message to:', destination, err);
        }
      }
    },
    [isConnected]
  );

  return {
    isConnected,
    isFallback,
    lastEvent,
    subscribe,
    publish,
  };
};

export default useWebSocket;
