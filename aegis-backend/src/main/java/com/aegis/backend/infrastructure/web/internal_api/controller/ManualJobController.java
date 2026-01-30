package com.aegis.backend.infrastructure.web.internal_api.controller;

import com.aegis.backend.application.job.DailyCloseJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/manual/jobs")
@RequiredArgsConstructor
@Slf4j
public class ManualJobController {

    private final DailyCloseJob dailyCloseJob;

    @Value("${aegis.security.service-token}")
    private String expectedToken;

    @PostMapping("/daily-close")
    public ResponseEntity<Map<String, Object>> trigger(
            @RequestHeader(value = "X-SERVICE-TOKEN", required = false) String token,
            @RequestParam(value = "date", required = false) String dateParam) {

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Service Token");
        }
        if (!expectedToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Service Token");
        }

        LocalDate date = LocalDate.now();
        if (dateParam != null && !dateParam.isBlank()) {
            try {
                date = LocalDate.parse(dateParam);
            } catch (DateTimeParseException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date");
            }
        }

        log.info("manual-daily-close triggered date={}", date);

        LocalDate targetDate = date;
        CompletableFuture.runAsync(() -> dailyCloseJob.run(targetDate));

        return ResponseEntity.accepted().body(Map.of(
                "status", "accepted",
                "message", "Daily close triggered",
                "date", date.toString()
        ));
    }
}
