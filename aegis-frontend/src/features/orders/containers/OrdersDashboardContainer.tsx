import { useState } from "react";
import { useListOrders } from "@/features/orders/hooks/useListOrders";

function formatCurrency(value: number): string {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

function formatDateTime(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function getTodayIsoDate(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function getStatusBadgeClasses(status: string): string {
  if (status.toLowerCase() === "pago") {
    return "bg-green-100 text-green-800 border-green-200";
  }
  if (status.toLowerCase() === "aguardando pagamento") {
    return "bg-yellow-100 text-yellow-800 border-yellow-200";
  }
  if (status.toLowerCase() === "cancelado") {
    return "bg-red-100 text-red-800 border-red-200";
  }
  return "bg-neutral-100 text-neutral-800 border-neutral-200";
}

export function OrdersDashboardContainer() {
  const [selectedDate, setSelectedDate] = useState<string>(() =>
    getTodayIsoDate()
  );

  const { data, isLoading, isError, uiError, refetch } =
    useListOrders(selectedDate);

  const handleDateChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setSelectedDate(event.target.value);
  };

  const handleRefreshClick = () => {
    refetch();
  };

  const hasError = isError || !!uiError;

  const orders = data?.data ?? [];
  const meta = data?.meta;

  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">
          Dashboard de Pedidos do Dia
        </h1>
        <p className="text-sm text-neutral-500">
          Visualize os pedidos para uma data específica e atualize os
          dados sob demanda.
        </p>
      </div>

      <section
        aria-label="Filtro de data"
        className="flex flex-wrap items-end gap-3 rounded-md border border-neutral-200 bg-neutral-50 p-4"
      >
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium" htmlFor="orders-date">
            Data
          </label>
          <input
            id="orders-date"
            type="date"
            className="h-9 rounded-md border border-neutral-300 px-3 text-sm outline-none focus-visible:border-neutral-900 focus-visible:ring-2 focus-visible:ring-neutral-900/20"
            value={selectedDate}
            onChange={handleDateChange}
          />
        </div>

        <button
          type="button"
          className="inline-flex h-9 items-center rounded-md bg-neutral-900 px-4 text-sm font-medium text-white hover:bg-neutral-800 disabled:opacity-60"
          onClick={handleRefreshClick}
          disabled={isLoading || !selectedDate}
        >
          {isLoading ? "Carregando..." : "Atualizar"}
        </button>
      </section>

      {hasError && (
        <div className="rounded-md border border-red-300 bg-red-50 p-4 text-sm text-red-800">
          <p className="font-medium">
            Não foi possível carregar os pedidos. Tente novamente.
          </p>
          {uiError && (
            <p className="mt-1 text-xs">
              Detalhes técnicos: {uiError.message}
            </p>
          )}
        </div>
      )}

      <section aria-label="Tabela de pedidos do dia" className="rounded-md border border-neutral-200 bg-white">
        <div className="overflow-x-auto">
          <table className="min-w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-neutral-200 bg-neutral-50">
                <th className="px-3 py-2 text-left font-medium">
                  ID do Pedido
                </th>
                <th className="px-3 py-2 text-left font-medium">Status</th>
                <th className="px-3 py-2 text-left font-medium">Total</th>
                <th className="px-3 py-2 text-left font-medium">
                  Data/Hora
                </th>
              </tr>
            </thead>
            <tbody>
              {isLoading && (
                <tr>
                  <td
                    colSpan={4}
                    className="px-3 py-6 text-center text-sm text-neutral-500"
                  >
                    Carregando pedidos...
                  </td>
                </tr>
              )}

              {!isLoading && orders.length === 0 && !hasError && (
                <tr>
                  <td
                    colSpan={4}
                    className="px-3 py-6 text-center text-sm text-neutral-500"
                  >
                    Nenhum pedido para esta data.
                  </td>
                </tr>
              )}

              {!isLoading &&
                orders.map((order) => (
                  <tr
                    key={order.id}
                    className="border-b border-neutral-200 last:border-b-0"
                  >
                    <td className="px-3 py-2 align-top font-mono text-xs">
                      {order.id}
                    </td>
                    <td className="px-3 py-2 align-top">
                      <span
                        className={`inline-flex items-center rounded-full border px-2 py-0.5 text-xs font-medium ${getStatusBadgeClasses(
                          order.status
                        )}`}
                      >
                        {order.status}
                      </span>
                    </td>
                    <td className="px-3 py-2 align-top">
                      {formatCurrency(order.total)}
                    </td>
                    <td className="px-3 py-2 align-top">
                      {formatDateTime(order.date)}
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>

        <div className="flex flex-wrap items-center justify-between gap-2 border-t border-neutral-200 px-3 py-2 text-xs text-neutral-600">
          <div>
            {typeof meta?.count === "number" ? (
              <span>
                {meta.count} pedido{meta.count === 1 ? "" : "s"}
              </span>
            ) : (
              <span>Número de pedidos indisponível</span>
            )}
          </div>
          <div>
            {meta?.source ? (
              <span>Fonte: {meta.source}</span>
            ) : (
              <span>Fonte: desconhecida</span>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}

