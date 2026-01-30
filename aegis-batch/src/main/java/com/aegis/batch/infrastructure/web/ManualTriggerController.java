package com.aegis.batch.infrastructure.web;

import com.aegis.batch.job.DailyCloseJob;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manual/jobs")
@RequiredArgsConstructor
// @Profile("!prod") // Idealmente, proteja isso em prod. Para agora, deixamos aberto local.
public class ManualTriggerController {

    private final DailyCloseJob dailyCloseJob;

    @PostMapping("/daily-close")
    public ResponseEntity<String> triggerDailyClose() {
        // Roda em thread separada para não bloquear HTTP, mas logs aparecerão no console
        new Thread(dailyCloseJob::executeDailyClose).start();
        return ResponseEntity.ok("Job disparado manualmente! Verifique os logs.");
    }
}
