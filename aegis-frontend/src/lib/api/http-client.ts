import type {
  BackendAppError,
  BackendServiceErrorResponse,
  InternalAppError,
  InternalServerErrorResponse,
  RateLimitAppError,
  RateLimitErrorResponse,
  ValidationAppError,
  ValidationErrorResponse,
} from "@/contracts/errors";

function getBaseUrl(): string {
  const url = process.env.NEXT_PUBLIC_BFF_BASE_URL;
  if (!url) {
    throw new Error("NEXT_PUBLIC_BFF_BASE_URL is not defined");
  }
  return url;
}

function buildUrl(path: string): string {
  const baseUrl = getBaseUrl();
  return new URL(path, baseUrl).toString();
}

async function parseJson(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

function asValidationError(
  status: number,
  data: unknown
): ValidationAppError {
  const payload = data as Partial<ValidationErrorResponse> | null;
  if (payload && payload.error === "Validation Error" && payload.details) {
    return {
      kind: "validation",
      status: 400,
      response: {
        error: "Validation Error",
        details: payload.details,
      },
    };
  }
  return {
    kind: "validation",
    status: 400,
    response: {
      error: "Validation Error",
      details: [],
    },
  };
}

function asRateLimitError(status: number, data: unknown): RateLimitAppError {
  const payload = data as Partial<RateLimitErrorResponse> | null;
  const message =
    (payload && payload.error) ||
    "Too many requests, please try again later.";
  return {
    kind: "rateLimit",
    status: status as 429,
    response: {
      error: message,
    },
  };
}

function asInternalError(
  status: number,
  data: unknown
): InternalAppError {
  const payload = data as Partial<InternalServerErrorResponse> | null;
  const message =
    (payload && payload.message) || "An unexpected error occurred.";
  return {
    kind: "internal",
    status: 500,
    response: {
      error: "Internal Server Error",
      message,
    },
  };
}

function asBackendError(
  status: number,
  data: unknown
): BackendAppError {
  const payload = data as Partial<BackendServiceErrorResponse> | null;
  const message =
    (payload && payload.message) || "Request failed with backend service error.";
  const backendData =
    payload && "backendData" in payload ? payload.backendData : undefined;
  return {
    kind: "backend",
    status,
    response: {
      error: "Backend Service Error",
      message,
      backendData,
    },
  };
}

function asNetworkBackendError(error: unknown): BackendAppError {
  const message =
    error instanceof Error ? error.message : "Network error while calling backend.";
  return {
    kind: "backend",
    status: 502,
    response: {
      error: "Backend Service Error",
      message,
    },
  };
}

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const url = buildUrl(path);
  const headers: HeadersInit = {
    Accept: "application/json",
    ...options.headers,
  };

  if (
    options.body !== undefined &&
    !("Content-Type" in headers) &&
    !(headers as Record<string, string>)["content-type"]
  ) {
    (headers as Record<string, string>)["Content-Type"] =
      "application/json";
  }

  let response: Response;
  try {
    response = await fetch(url, {
      ...options,
      headers,
    });
  } catch (error) {
    throw asNetworkBackendError(error);
  }

  const data = await parseJson(response);

  if (response.ok) {
    return data as T;
  }

  const status = response.status;

  if (status === 400) {
    throw asValidationError(status, data);
  }

  if (status === 429) {
    throw asRateLimitError(status, data);
  }

  if (status === 500) {
    throw asInternalError(status, data);
  }

  throw asBackendError(status, data);
}
