import { useQuery, UseQueryResult } from "@tanstack/react-query";
import type { AppError } from "@/contracts/errors";
import {
  getHealthReady,
  type HealthReadyResponse,
} from "@/lib/api/health";

export type UseSystemHealthResult = UseQueryResult<
  HealthReadyResponse,
  AppError
>;

export function useSystemHealth(): UseSystemHealthResult {
  return useQuery<HealthReadyResponse, AppError>({
    queryKey: ["system-health"],
    queryFn: () => getHealthReady(),
    refetchInterval: 30_000,
    refetchOnWindowFocus: true,
  });
}

