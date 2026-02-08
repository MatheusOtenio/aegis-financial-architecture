import {
  useForm,
  useFieldArray,
  useWatch,
  type FieldPath,
} from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { CreateOrderInputSchema, CreateOrderInput } from "@/contracts/orders";
import { useCreateOrder } from "@/features/orders/hooks/useCreateOrder";

type FormValues = CreateOrderInput;

function formatCurrency(value: number): string {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

export function CreateOrderContainer() {
  const {
    register,
    control,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(CreateOrderInputSchema),
    defaultValues: {
      customerId: "",
      items: [
        {
          productId: "",
          quantity: 1,
          unitPrice: 0,
        },
      ],
      date: undefined,
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: "items",
  });

  const { mutateAsync, isPending, uiError, fieldErrors, successInfo } =
    useCreateOrder();

  const items =
    useWatch({
      control,
      name: "items",
    }) ?? [];
  const totalItems = items.reduce(
    (acc, item) => acc + (item.quantity || 0),
    0
  );
  const totalAmount = items.reduce(
    (acc, item) =>
      acc + (item.quantity || 0) * (item.unitPrice || 0),
    0
  );
  const summary = {
    totalItems,
    totalAmount,
    totalAmountFormatted: formatCurrency(totalAmount),
  };

  const onSubmit = handleSubmit(async (values) => {
    try {
      const result = await mutateAsync(values);
      reset({
        customerId: "",
        items: [
          {
            productId: "",
            quantity: 1,
            unitPrice: 0,
          },
        ],
        date: undefined,
      });
      if (successInfo) {
        alert(
          `Pedido criado com sucesso. ID: ${successInfo.id}, Status: ${successInfo.displayStatus}`
        );
      } else if (result) {
        alert(
          `Pedido criado com sucesso. ID: ${result.id}, Status: ${result.displayStatus}`
        );
      }
    } catch {
      if (fieldErrors) {
        Object.entries(fieldErrors).forEach(([path, message]) => {
          setError(path as FieldPath<FormValues>, {
            type: "server",
            message,
          });
        });
      }
    }
  });

  const hasNonValidationError =
    uiError && uiError.kind !== "validation" ? uiError : null;

  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6">
      <div>
        <h1 className="text-2xl font-semibold">Criar Pedido</h1>
        <p className="text-sm text-neutral-500">
          Preencha os dados do cliente e dos itens para criar um novo pedido.
        </p>
      </div>

      {hasNonValidationError && (
        <div className="rounded-md border border-red-300 bg-red-50 p-4 text-sm text-red-800">
          <p>Ocorreu um erro ao criar o pedido: {hasNonValidationError.message}</p>
        </div>
      )}

      <form className="flex flex-col gap-6" onSubmit={onSubmit} noValidate>
        <div className="flex flex-col gap-2">
          <label className="text-sm font-medium" htmlFor="customerId">
            Cliente (UUID)
          </label>
          <input
            id="customerId"
            type="text"
            className="h-9 rounded-md border border-neutral-300 px-3 text-sm outline-none focus-visible:border-neutral-900 focus-visible:ring-2 focus-visible:ring-neutral-900/20"
            placeholder="00000000-0000-0000-0000-000000000000"
            {...register("customerId")}
          />
          {errors.customerId && (
            <p className="text-xs text-red-700">
              {errors.customerId.message as string}
            </p>
          )}
        </div>

        <div className="flex flex-col gap-2">
          <label className="text-sm font-medium" htmlFor="date">
            Data do pedido (opcional)
          </label>
          <input
            id="date"
            type="text"
            className="h-9 rounded-md border border-neutral-300 px-3 text-sm outline-none focus-visible:border-neutral-900 focus-visible:ring-2 focus-visible:ring-neutral-900/20"
            placeholder="ISO 8601, ex: 2026-02-08T12:34:56.789Z"
            {...register("date")}
          />
          {errors.date && (
            <p className="text-xs text-red-700">{errors.date.message as string}</p>
          )}
        </div>

        <div className="flex flex-col gap-3">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-medium">Itens do pedido</h2>
            <button
              type="button"
              className="inline-flex h-8 items-center rounded-md bg-neutral-900 px-3 text-sm font-medium text-white hover:bg-neutral-800 disabled:opacity-60"
              onClick={() =>
                append({
                  productId: "",
                  quantity: 1,
                  unitPrice: 0,
                })
              }
              disabled={isPending}
            >
              Adicionar item
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full border-collapse text-sm">
              <thead>
                <tr className="border-b border-neutral-200 bg-neutral-50">
                  <th className="px-3 py-2 text-left font-medium">Produto (UUID)</th>
                  <th className="px-3 py-2 text-left font-medium">Quantidade</th>
                  <th className="px-3 py-2 text-left font-medium">Preço unitário</th>
                  <th className="px-3 py-2 text-left font-medium">Ações</th>
                </tr>
              </thead>
              <tbody>
                {fields.map((field, index) => (
                  <tr
                    key={field.id}
                    className="border-b border-neutral-200 last:border-b-0"
                  >
                    <td className="px-3 py-2 align-top">
                      <label className="sr-only" htmlFor={`items.${index}.productId`}>
                        Produto (UUID)
                      </label>
                      <input
                        id={`items.${index}.productId`}
                        type="text"
                        className="h-8 w-full rounded-md border border-neutral-300 px-2 text-xs outline-none focus-visible:border-neutral-900 focus-visible:ring-2 focus-visible:ring-neutral-900/20"
                        placeholder="00000000-0000-0000-0000-000000000000"
                        {...register(`items.${index}.productId` as const)}
                      />
                      {errors.items?.[index]?.productId && (
                        <p className="mt-1 text-xs text-red-700">
                          {errors.items[index]?.productId?.message as string}
                        </p>
                      )}
                    </td>
                    <td className="px-3 py-2 align-top">
                      <label className="sr-only" htmlFor={`items.${index}.quantity`}>
                        Quantidade
                      </label>
                      <input
                        id={`items.${index}.quantity`}
                        type="number"
                        min={1}
                        className="h-8 w-24 rounded-md border border-neutral-300 px-2 text-xs outline-none focus-visible:border-neutral-900 focus-visible:ring-2 focus-visible:ring-neutral-900/20"
                        {...register(`items.${index}.quantity` as const, {
                          valueAsNumber: true,
                        })}
                      />
                      {errors.items?.[index]?.quantity && (
                        <p className="mt-1 text-xs text-red-700">
                          {errors.items[index]?.quantity?.message as string}
                        </p>
                      )}
                    </td>
                    <td className="px-3 py-2 align-top">
                      <label className="sr-only" htmlFor={`items.${index}.unitPrice`}>
                        Preço unitário
                      </label>
                      <input
                        id={`items.${index}.unitPrice`}
                        type="number"
                        step="0.01"
                        min={0}
                        className="h-8 w-32 rounded-md border border-neutral-300 px-2 text-xs outline-none focus-visible:border-neutral-900 focus-visible:ring-2 focus-visible:ring-neutral-900/20"
                        {...register(`items.${index}.unitPrice` as const, {
                          valueAsNumber: true,
                        })}
                      />
                      {errors.items?.[index]?.unitPrice && (
                        <p className="mt-1 text-xs text-red-700">
                          {errors.items[index]?.unitPrice?.message as string}
                        </p>
                      )}
                    </td>
                    <td className="px-3 py-2 align-top">
                      <button
                        type="button"
                        className="inline-flex h-8 items-center rounded-md border border-neutral-300 px-2 text-xs font-medium text-neutral-700 hover:bg-neutral-100 disabled:opacity-60"
                        onClick={() => remove(index)}
                        disabled={isPending || fields.length === 1}
                      >
                        Remover
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {errors.items && typeof errors.items === "object" && !Array.isArray(errors.items) && (
            <p className="text-xs text-red-700">
              {errors.items?.root?.message as string}
            </p>
          )}
        </div>

        <div className="flex flex-col gap-1 rounded-md bg-neutral-50 p-3 text-sm">
          <div className="flex justify-between">
            <span className="font-medium">Número de itens</span>
            <span>{summary.totalItems}</span>
          </div>
          <div className="flex justify-between">
            <span className="font-medium">Total estimado</span>
            <span>{summary.totalAmountFormatted}</span>
          </div>
        </div>

        <div className="flex justify-end gap-3">
          <button
            type="submit"
            className="inline-flex h-9 items-center rounded-md bg-neutral-900 px-4 text-sm font-medium text-white hover:bg-neutral-800 disabled:opacity-60"
            disabled={isPending}
          >
            {isPending ? "Criando..." : "Criar pedido"}
          </button>
        </div>
      </form>
    </div>
  );
}
