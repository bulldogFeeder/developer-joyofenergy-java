package uk.tw.energy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.service.MeterReadingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/readings")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    public MeterReadingController(MeterReadingService meterReadingService) {
        this.meterReadingService = meterReadingService;
    }

    /**
     * 存储读数
     * @param meterReadings 电表读数
     * @return
     */
    @PostMapping("/store")
    public ResponseEntity storeReadings(@RequestBody MeterReadings meterReadings) {
        //参数非空校验：判断电表id是否为空，判断该电表历史读数是否为空
        if (!isMeterReadingsValid(meterReadings)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        //向历史读书表中追加本次读数
        meterReadingService.storeReadings(meterReadings.getSmartMeterId(), meterReadings.getElectricityReadings());
        return ResponseEntity.ok().build();
    }

    /**
     * 校验电表及电表读数list是否为空
     * @param meterReadings
     * @return
     */
    private boolean isMeterReadingsValid(MeterReadings meterReadings) {
        String smartMeterId = meterReadings.getSmartMeterId();
        List<ElectricityReading> electricityReadings = meterReadings.getElectricityReadings();
        return smartMeterId != null && !smartMeterId.isEmpty()
                && electricityReadings != null && !electricityReadings.isEmpty();
    }

    /**
     * 查询电表id对应的读数列表（全量读数）
     * @param smartMeterId
     * @return
     */
    @GetMapping("/read/{smartMeterId}")
    public ResponseEntity readReadings(@PathVariable String smartMeterId) {
        //适用Optional优雅地处理空指针
        Optional<List<ElectricityReading>> readings = meterReadingService.getReadings(smartMeterId);
        //读数列表不为空就返回ok
        return readings.isPresent()
                ? ResponseEntity.ok(readings.get())
                : ResponseEntity.notFound().build();
    }
}
