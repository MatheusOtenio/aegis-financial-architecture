package com.aegis.backend.domain.order;

import java.util.List;

public record DailyOrderSlice(
        List<DailyOrderSummary> items,
        boolean hasNext
) {}
