package com.example.bandmaster.util;

import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Component
public class DateUtils {

    public LocalDate getFifthBusinessDayOfNextMonth(LocalDate referenceDate) {
        if (referenceDate == null) {
            return null;
        }

        // Move para o primeiro dia do próximo mês
        LocalDate date = referenceDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());

        int businessDaysCount = 0;

        // Loop até encontrar o 5º dia útil
        while (businessDaysCount < 5) {
            // Verifica se é dia útil (Segunda a Sexta)
            // Nota: O script Python original tinha um parametro 'considerar_sabado',
            // mas o padrão era False, então mantive apenas Seg-Sex aqui.
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                businessDaysCount++;
            }

            if (businessDaysCount < 5) {
                date = date.plusDays(1);
            }
        }
        return date;
    }
}