import { apiFetch } from "./http-client";

export type HealthStatus = "READY" | "NOT_READY";

export type HealthCheckStatus = "UP" | "DOWN" | "SKIPPED";

export type HealthReadyResponse = {
  status: HealthStatus;
  checks: {
    redis?: HealthCheckStatus;
    database?: HealthCheckStatus;
    [key: string]: HealthCheckStatus | undefined;
  };
};

export function getHealthReady(): Promise<HealthReadyResponse> {
  return apiFetch<HealthReadyResponse>("/health/ready");
}

