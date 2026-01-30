package com.aegis.backend.infrastructure.web.internal_api.controller;

import com.aegis.backend.application.order.BatchDailyOrderReadService;
import com.aegis.backend.domain.order.DailyOrderSlice;
import com.aegis.backend.domain.order.DailyOrderSummary;
import com.aegis.backend.shared.exception.DomainException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final BatchDailyOrderReadService batchDailyOrderReadService;
    private final ObjectMapper objectMapper;

    @Value("${aegis.security.service-token}")
    private String expectedToken;

    @GetMapping("/daily")
    public void streamDailyOrders(
            @RequestHeader(value = "X-SERVICE-TOKEN", required = false) String token,
            @RequestParam("date") String dateParam,
            HttpServletResponse response) throws IOException {

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Service Token");
        }
        if (!expectedToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Service Token");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateParam);
        } catch (DateTimeParseException ex) {
            throw new DomainException("Invalid date");
        }

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");

        JsonGenerator g = objectMapper.getFactory().createGenerator(response.getOutputStream());
        g.writeStartArray();

        int page = 0;
        int size = 1000;

        while (true) {
            DailyOrderSlice slice = batchDailyOrderReadService.read(date, page, size);
            for (DailyOrderSummary s : slice.items()) {
                g.writeObject(s);
            }
            if (!slice.hasNext()) break;
            page++;
        }

        g.writeEndArray();
        g.flush();
    }
}
