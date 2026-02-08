export type ValidationErrorDetail = {
  path: (string | number)[];
  message: string;
};

export type ValidationErrorResponse = {
  error: "Validation Error";
  details: ValidationErrorDetail[];
};

export type BackendServiceErrorResponse = {
  error: "Backend Service Error";
  message: string;
  backendData?: unknown;
};

export type InternalServerErrorResponse = {
  error: "Internal Server Error";
  message: string;
};

export type RateLimitErrorResponse = {
  error: string;
};

export type ValidationAppError = {
  kind: "validation";
  status: 400;
  response: ValidationErrorResponse;
};

export type BackendAppError = {
  kind: "backend";
  status: number;
  response: BackendServiceErrorResponse;
};

export type InternalAppError = {
  kind: "internal";
  status: 500;
  response: InternalServerErrorResponse;
};

export type RateLimitAppError = {
  kind: "rateLimit";
  status: 429;
  response: RateLimitErrorResponse;
};

export type AppError =
  | ValidationAppError
  | BackendAppError
  | InternalAppError
  | RateLimitAppError;

