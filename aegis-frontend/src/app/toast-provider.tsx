"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import type { ShowToast } from "@/lib/query/query-error-handling";

type ToastState = {
  id: number;
  title: string;
  description?: string;
} | null;

const ToastContext = createContext<ShowToast | null>(null);

type ToastProviderProps = {
  children: ReactNode;
};

export function ToastProvider({ children }: ToastProviderProps) {
  const [toast, setToast] = useState<ToastState>(null);

  const showToast: ShowToast = useCallback(
    ({ title, description }) => {
      setToast({
        id: Date.now(),
        title,
        description,
      });
    },
    [setToast]
  );

  useEffect(() => {
    if (!toast) {
      return;
    }
    const timeoutId = setTimeout(() => {
      setToast(null);
    }, 5000);
    return () => {
      clearTimeout(timeoutId);
    };
  }, [toast]);

  return (
    <ToastContext.Provider value={showToast}>
      {children}
      {toast && (
        <div className="pointer-events-none fixed inset-x-0 bottom-4 flex justify-center px-4 sm:justify-end sm:px-6">
          <div className="pointer-events-auto max-w-sm rounded-md border border-neutral-200 bg-white px-4 py-3 shadow-lg shadow-black/10">
            <div className="flex items-start gap-3">
              <div className="mt-0.5 h-2 w-2 shrink-0 rounded-full bg-red-500" />
              <div className="flex-1">
                <p className="text-sm font-medium text-neutral-900">
                  {toast.title}
                </p>
                {toast.description && (
                  <p className="mt-1 text-xs text-neutral-600">
                    {toast.description}
                  </p>
                )}
              </div>
              <button
                type="button"
                className="ml-2 text-xs font-medium text-neutral-500 hover:text-neutral-800"
                onClick={() => setToast(null)}
              >
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}
    </ToastContext.Provider>
  );
}

export function useToast(): ShowToast {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    throw new Error("useToast must be used within ToastProvider");
  }
  return ctx;
}

