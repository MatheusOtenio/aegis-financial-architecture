import { useCallback } from "react";
import {
  mapAppErrorToMessage,
  type MappedError,
} from "@/lib/errors/mapAppErrorToMessage";

export type ShowToast = (options: {
  title: string;
  description?: string;
}) => void;

export function mapUnknownQueryError(error: unknown): MappedError {
  return mapAppErrorToMessage(error);
}

export function useQueryErrorToast(showToast: ShowToast) {
  return useCallback(
    (error: unknown) => {
      const mapped = mapAppErrorToMessage(error);

      if (mapped.type === "validation") {
        return;
      }

      showToast({
        title: "Erro ao processar solicitação",
        description: mapped.message,
      });
    },
    [showToast]
  );
}

