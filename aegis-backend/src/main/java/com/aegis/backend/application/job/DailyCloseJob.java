package com.aegis.backend.application.job;

import com.aegis.backend.application.order.BatchDailyOrderReadService;
import com.aegis.backend.application.report.RegisterDailyReportUseCase;
import com.aegis.backend.domain.order.DailyOrderSlice;
import com.aegis.backend.domain.order.DailyOrderSummary;
import com.aegis.backend.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyCloseJob {

    private final BatchDailyOrderReadService readService;
    private final RegisterDailyReportUseCase registerDailyReportUseCase;

    public void run(LocalDate date) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;
        int page = 0;
        int size = 1000;
        while (true) {
            DailyOrderSlice slice = readService.read(date, page, size);
            for (DailyOrderSummary s : slice.items()) {
                totalOrders++;
                totalRevenue = totalRevenue.add(s.totalAmount());
            }
            if (!slice.hasNext()) break;
            page++;
        }
        try {
            registerDailyReportUseCase.execute(date, totalOrders, totalRevenue);
            log.info("manual-daily-close completed date={} orders={} revenue={}", date, totalOrders, totalRevenue);
        } catch (DomainException ex) {
            log.info("manual-daily-close skipped date={} reason={}", date, ex.getMessage());
        } catch (Exception ex) {
            log.error("manual-daily-close error date={}", date, ex);
        }
    }
}
