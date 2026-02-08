import { useSystemHealth } from "@/features/system-status/hooks/useSystemHealth";

const MOCKS_ON =
  typeof process !== "undefined" &&
  process.env.NEXT_PUBLIC_USE_MOCKS_UI === "true";

function getStatusColors(status: "READY" | "NOT_READY") {
  if (status === "READY") {
    return {
      container: "border-green-200 bg-green-50",
      badge: "bg-green-600 text-white",
      label: "text-green-800",
    };
  }
  return {
    container: "border-red-200 bg-red-50",
    badge: "bg-red-600 text-white",
    label: "text-red-800",
  };
}

function getCheckBadgeClasses(value: string | undefined): string {
  if (!value) {
    return "bg-neutral-100 text-neutral-700 border-neutral-200";
  }
  const normalized = value.toUpperCase();
  if (normalized === "UP") {
    return "bg-green-100 text-green-800 border-green-200";
  }
  if (normalized === "DOWN") {
    return "bg-red-100 text-red-800 border-red-200";
  }
  if (normalized === "SKIPPED") {
    return "bg-yellow-100 text-yellow-800 border-yellow-200";
  }
  return "bg-neutral-100 text-neutral-700 border-neutral-200";
}

export function SystemStatusContainer() {
  const { data, isLoading, isError, refetch } = useSystemHealth();

  const effectiveStatus: "READY" | "NOT_READY" =
    !isError && data?.status === "READY" ? "READY" : "NOT_READY";
  const colors = getStatusColors(effectiveStatus);

  const redisStatus = data?.checks.redis ?? "SKIPPED";
  const databaseStatus = data?.checks.database ?? "SKIPPED";

  return (
    <div className="mx-auto flex max-w-4xl flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Status do Sistema</h1>
        <p className="text-sm text-neutral-500">
          Acompanhe a saúde do BFF e serviços dependentes em tempo quase
          real.
        </p>
      </div>

      <section
        aria-label="Status geral do sistema"
        className={`rounded-md border p-4 ${colors.container}`}
      >
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <span
              className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold ${colors.badge}`}
            >
              {effectiveStatus === "READY" ? "READY" : "NOT_READY"}
            </span>
            <div>
              <p className={`text-sm font-medium ${colors.label}`}>
                {effectiveStatus === "READY"
                  ? "BFF está pronto para receber requisições."
                  : "BFF não está pronto. Verifique os serviços dependentes."}
              </p>
              {isLoading && (
                <p className="text-xs text-neutral-600">
                  Verificando status do sistema...
                </p>
              )}
              {isError && (
                <p className="text-xs text-red-700">
                  Não foi possível obter o status do sistema. Tente
                  novamente.
                </p>
              )}
            </div>
          </div>

          <div className="flex flex-col items-end gap-2 text-xs">
            <div className="flex items-center gap-2">
              <span className="font-medium">Mocks:</span>
              <span
                className={`inline-flex items-center rounded-full border px-2 py-0.5 font-medium ${
                  MOCKS_ON
                    ? "bg-blue-100 text-blue-800 border-blue-200"
                    : "bg-neutral-100 text-neutral-800 border-neutral-200"
                }`}
              >
                {MOCKS_ON ? "ON" : "OFF"}
              </span>
            </div>
            <button
              type="button"
              className="inline-flex h-8 items-center rounded-md border border-neutral-300 px-3 font-medium text-neutral-700 hover:bg-neutral-100 disabled:opacity-60"
              onClick={() => refetch()}
              disabled={isLoading}
            >
              {isLoading ? "Verificando..." : "Tentar novamente"}
            </button>
          </div>
        </div>
      </section>

      <section
        aria-label="Checks detalhados"
        className="grid gap-4 md:grid-cols-2"
      >
        <div className="rounded-md border border-neutral-200 bg-white p-4">
          <h2 className="text-sm font-medium">Redis</h2>
          <p className="mt-1 text-xs text-neutral-500">
            Cache utilizado pelo BFF. Status baseado no health check.
          </p>
          <div className="mt-3">
            <span
              className={`inline-flex items-center rounded-full border px-2 py-0.5 text-xs font-medium ${getCheckBadgeClasses(
                redisStatus
              )}`}
            >
              {redisStatus}
            </span>
          </div>
        </div>

        <div className="rounded-md border border-neutral-200 bg-white p-4">
          <h2 className="text-sm font-medium">Database</h2>
          <p className="mt-1 text-xs text-neutral-500">
            Status conforme informado pelo BFF. Em alguns ambientes pode
            estar marcado como SKIPPED.
          </p>
          <div className="mt-3">
            <span
              className={`inline-flex items-center rounded-full border px-2 py-0.5 text-xs font-medium ${getCheckBadgeClasses(
                databaseStatus
              )}`}
            >
              {databaseStatus}
            </span>
          </div>
        </div>
      </section>

      <section
        aria-label="Links de observabilidade"
        className="rounded-md border border-neutral-200 bg-neutral-50 p-4 text-sm text-neutral-700"
      >
        <p className="font-medium">Observabilidade do BFF</p>
        <div className="mt-2 flex flex-wrap gap-3">
          <a
            href="http://localhost:3000/docs"
            target="_blank"
            rel="noreferrer"
            className="inline-flex h-8 items-center rounded-md bg-neutral-900 px-3 text-xs font-medium text-white hover:bg-neutral-800"
          >
            Abrir Swagger (/docs)
          </a>
          <a
            href="http://localhost:3000/metrics"
            target="_blank"
            rel="noreferrer"
            className="inline-flex h-8 items-center rounded-md border border-neutral-300 px-3 text-xs font-medium text-neutral-800 hover:bg-neutral-100"
          >
            Métricas Prometheus (/metrics)
          </a>
        </div>
        <p className="mt-2 text-xs text-neutral-500">
          O indicador de mocks (ON/OFF) é configurado manualmente via
          variável de ambiente do frontend e poderá ser substituído por um
          endpoint dedicado no futuro.
        </p>
      </section>
    </div>
  );
}

