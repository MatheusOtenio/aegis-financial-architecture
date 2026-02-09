"use client";

import { useState, type ReactNode } from "react";
import {
  QueryClient,
  QueryClientProvider,
  QueryCache,
  MutationCache,
} from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import {
  useQueryErrorToast,
  type ShowToast,
} from "@/lib/query/query-error-handling";
import { useToast } from "./toast-provider";

type ReactQueryProviderProps = {
  children: ReactNode;
};

export function ReactQueryProvider({
  children,
}: ReactQueryProviderProps) {
  const showToast: ShowToast = useToast();
  const queryErrorToast = useQueryErrorToast(showToast);

  const [queryClient] = useState(
    () =>
      new QueryClient({
        queryCache: new QueryCache({
          onError: (error) => {
            queryErrorToast(error);
          },
        }),
        mutationCache: new MutationCache({
          onError: (error) => {
            queryErrorToast(error);
          },
        }),
        defaultOptions: {
          queries: {
            retry: 1,
          },
        },
      })
  );

  return (
    <QueryClientProvider client={queryClient}>
      {children}
      {process.env.NODE_ENV === "development" && (
        <ReactQueryDevtools initialIsOpen={false} />
      )}
    </QueryClientProvider>
  );
}
