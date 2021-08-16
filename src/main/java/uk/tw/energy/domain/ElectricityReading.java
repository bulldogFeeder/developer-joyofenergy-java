package uk.tw.energy.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 描述每一次读数，包含时间戳与当前读数
 */

public class ElectricityReading {

    private Instant time;
    private BigDecimal reading; // kW

    public ElectricityReading() { }

    public ElectricityReading(Instant time, BigDecimal reading) {
        this.time = time;
        this.reading = reading;
    }

    public BigDecimal getReading() {
        return reading;
    }

    public Instant getTime() {
        return time;
    }
}
