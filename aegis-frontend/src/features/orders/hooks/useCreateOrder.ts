import { useMemo } from "react";
import {
  useMutation,
  UseMutationResult,
  useQueryClient,
} from "@tanstack/react-query";
import type { CreateOrderInput, OrderDTO } from "@/contracts/orders";
import type { AppError } from "@/contracts/errors";
import { postCreateOrder } from "@/lib/api/orders";
import {
  mapAppErrorToMessage,
  type MappedError,
} from "@/lib/errors/mapAppErrorToMessage";
import { UiError } from "./useListOrders";

export type FieldErrors = Record<string, string>;

export type CreateOrderSuccessInfo = {
  id: string;
  displayStatus: string;
};

export type UseCreateOrderResult = UseMutationResult<
  OrderDTO,
  AppError,
  CreateOrderInput
> & {
  uiError: UiError | null;
  fieldErrors: FieldErrors | null;
  successInfo: CreateOrderSuccessInfo | null;
};

export function useCreateOrder(): UseCreateOrderResult {
  const queryClient = useQueryClient();

  const mutation = useMutation<OrderDTO, AppError, CreateOrderInput>({
    mutationFn: (input) => postCreateOrder(input),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["orders"],
        exact: false,
      });
    },
  });

  const mappedError: MappedError | null = useMemo(
    () =>
      mutation.error ? mapAppErrorToMessage(mutation.error) : null,
    [mutation.error]
  );

  const uiError = useMemo<UiError | null>(() => {
    if (!mutation.error || !mappedError) {
      return null;
    }

    return {
      kind: mappedError.type as UiError["kind"],
      status: mutation.error.status,
      message: mappedError.message,
      fieldErrors: mappedError.fieldErrors,
    };
  }, [mappedError, mutation.error]);

  const fieldErrors = useMemo<FieldErrors | null>(() => {
    if (!mappedError || !mappedError.fieldErrors) {
      return null;
    }
    return mappedError.fieldErrors;
  }, [mappedError]);

  const successInfo = useMemo(() => {
    if (!mutation.data) {
      return null;
    }
    return {
      id: mutation.data.id,
      displayStatus: mutation.data.displayStatus,
    };
  }, [mutation.data]);

  return {
    ...mutation,
    uiError,
    fieldErrors,
    successInfo,
  };
}
