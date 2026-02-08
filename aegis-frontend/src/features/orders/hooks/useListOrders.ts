import { useMemo } from "react";
import { useQuery, UseQueryResult } from "@tanstack/react-query";
import type { ListOrdersResponse } from "@/contracts/orders";
import type { AppError, ValidationAppError } from "@/contracts/errors";
import { getOrdersByDate } from "@/lib/api/orders";

export type UiError = {
  kind: AppError["kind"];
  status: number | undefined;
  message: string;
  fieldErrors?: Record<string, string>;
};

function mapValidationFieldErrors(
  error: ValidationAppError
): Record<string, string> {
  const result: Record<string, string> = {};
  for (const detail of error.response.details) {
    const key = detail.path.join(".");
    if (!result[key]) {
      result[key] = detail.message;
    }
  }
  return result;
}

export function mapAppErrorToUiError(error: AppError): UiError {
  if (error.kind === "validation") {
    return {
      kind: "validation",
      status: error.status,
      message: error.response.error,
      fieldErrors: mapValidationFieldErrors(error),
    };
  }

  if (error.kind === "rateLimit") {
    return {
      kind: "rateLimit",
      status: error.status,
      message: error.response.error,
    };
  }

  if (error.kind === "internal") {
    return {
      kind: "internal",
      status: error.status,
      message: error.response.message,
    };
  }

  return {
    kind: "backend",
    status: error.status,
    message: error.response.message,
  };
}

type UseListOrdersResult = UseQueryResult<ListOrdersResponse, AppError> & {
  uiError: UiError | null;
};

export function useListOrders(date: string): UseListOrdersResult {
  const query = useQuery<ListOrdersResponse, AppError>({
    queryKey: ["orders", date],
    queryFn: () => getOrdersByDate(date),
    enabled: Boolean(date),
  });

  const uiError = useMemo(
    () => (query.error ? mapAppErrorToUiError(query.error) : null),
    [query.error]
  );

  return {
    ...query,
    uiError,
  };
}
