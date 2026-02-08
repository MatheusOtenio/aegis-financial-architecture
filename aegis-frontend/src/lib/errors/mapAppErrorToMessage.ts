import type {
  AppError,
  ValidationAppError,
} from "@/contracts/errors";

export type MappedErrorType =
  | "validation"
  | "backend"
  | "internal"
  | "rateLimit"
  | "unknown";

export type MappedError = {
  type: MappedErrorType;
  message: string;
  fieldErrors?: Record<string, string>;
};

function isValidationError(error: AppError): error is ValidationAppError {
  return error.kind === "validation";
}

export function mapAppErrorToMessage(error: unknown): MappedError {
  if (!error || typeof error !== "object") {
    return {
      type: "unknown",
      message: "Não foi possível processar a operação. Tente novamente.",
    };
  }

  const appError = error as AppError;

  if (isValidationError(appError)) {
    const fieldErrors: Record<string, string> = {};
    for (const detail of appError.response.details) {
      const key = detail.path.join(".");
      if (!fieldErrors[key]) {
        fieldErrors[key] = detail.message;
      }
    }

    return {
      type: "validation",
      message: "Alguns campos precisam de atenção.",
      fieldErrors:
        Object.keys(fieldErrors).length > 0 ? fieldErrors : undefined,
    };
  }

  if (appError.kind === "rateLimit") {
    const message =
      appError.response.error ||
      "Muitas requisições em sequência. Tente novamente em instantes.";

    return {
      type: "rateLimit",
      message,
    };
  }

  if (appError.kind === "internal") {
    return {
      type: "internal",
      message: "Ocorreu um erro interno. Tente novamente mais tarde.",
    };
  }

  if (appError.kind === "backend") {
    const message =
      appError.response.message ||
      "Ocorreu um erro ao comunicar com o serviço de backend.";

    return {
      type: "backend",
      message,
    };
  }

  return {
    type: "unknown",
    message: "Não foi possível processar a operação. Tente novamente.",
  };
}

