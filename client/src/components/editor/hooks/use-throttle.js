import { useRef, useCallback } from "react";

export function useThrottle(callback, delay) {
  const lastRan = useRef(Date.now());
  const timeoutRef = useRef(null);

  return useCallback(
      (...args) => {
        const now = Date.now();

        const handler = () => {
          if (now - lastRan.current >= delay) {
            callback(...args);
            lastRan.current = Date.now();
          } else {
            if (timeoutRef.current) {
              clearTimeout(timeoutRef.current);
            }
            timeoutRef.current = setTimeout(() => {
              callback(...args);
              lastRan.current = Date.now();
            }, delay - (now - lastRan.current));
          }
        };

        handler();
      },
      [callback, delay]
  );
}
