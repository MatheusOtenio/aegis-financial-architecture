"use client";

import { CreateOrderContainer } from "@/features/orders/containers/CreateOrderContainer";
import { OrdersDashboardContainer } from "@/features/orders/containers/OrdersDashboardContainer";
import { SystemStatusContainer } from "@/features/system-status/containers/SystemStatusContainer";

export default function Home() {
  return (
    <div className="min-h-screen bg-neutral-50 px-4 py-8 text-neutral-900">
      <main className="mx-auto flex w-full max-w-6xl flex-col gap-8">
        <header className="flex flex-col gap-1">
          <h1 className="text-3xl font-semibold">
            Aegis Console Operacional
          </h1>
          <p className="text-sm text-neutral-500">
            Acompanhe o status do sistema, crie pedidos e visualize o
            fluxo diário em um único lugar.
          </p>
        </header>

        <section>
          <SystemStatusContainer />
        </section>

        <section className="grid gap-6 md:grid-cols-[minmax(0,1.1fr)_minmax(0,1.3fr)]">
          <div>
            <CreateOrderContainer />
          </div>
          <div>
            <OrdersDashboardContainer />
          </div>
        </section>
      </main>
    </div>
  );
}
