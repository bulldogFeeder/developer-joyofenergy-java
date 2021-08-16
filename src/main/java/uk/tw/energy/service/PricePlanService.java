package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PricePlanService {

    //pricePlans是三种电力计划
    private final List<PricePlan> pricePlans;
    private final MeterReadingService meterReadingService;

    public PricePlanService(List<PricePlan> pricePlans, MeterReadingService meterReadingService) {
        this.pricePlans = pricePlans;
        this.meterReadingService = meterReadingService;
    }

    /**
     *
     * @param smartMeterId 传入电表id
     * @return 返回该电表id在三种不同的方案下的收费价格，key为planName，value是该电表在该planName下的对应价格
     */
    public Optional<Map<String, BigDecimal>> getConsumptionCostOfElectricityReadingsForEachPricePlan(String smartMeterId) {
        //获取电表id对应的全量读数list
        Optional<List<ElectricityReading>> electricityReadings = meterReadingService.getReadings(smartMeterId);

        if (!electricityReadings.isPresent()) {
            return Optional.empty();
        }
        //返回每个电力公司<planName，电力公司实际价格>的map
        return Optional.of(pricePlans.stream().collect(Collectors.toMap(PricePlan::getPlanName, t -> calculateCost(electricityReadings.get(), t))));
    }

    //计算每家电力公司的实际收费价格
    private BigDecimal calculateCost(List<ElectricityReading> electricityReadings, PricePlan pricePlan) {
        //average是传入读数list中读数的的平均值
        BigDecimal average = calculateAverageReading(electricityReadings);
        //timeElapsed是传入读数list中的总时间间隔（按小时计算）
        BigDecimal timeElapsed = calculateTimeElapsed(electricityReadings);
        //averageCost表示平均读数除以时间间隔
        BigDecimal averagedCost = average.divide(timeElapsed, RoundingMode.HALF_UP);
        //返回平均花费与对应电力公司的收费比率，相当于对应电力公司的实际价格
        return averagedCost.multiply(pricePlan.getUnitRate());
    }

    private BigDecimal calculateAverageReading(List<ElectricityReading> electricityReadings) {
        BigDecimal summedReadings = electricityReadings.stream()
                .map(ElectricityReading::getReading)
                .reduce(BigDecimal.ZERO, (reading, accumulator) -> reading.add(accumulator));

        return summedReadings.divide(BigDecimal.valueOf(electricityReadings.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTimeElapsed(List<ElectricityReading> electricityReadings) {
        ElectricityReading first = electricityReadings.stream()
                .min(Comparator.comparing(ElectricityReading::getTime))
                .get();
        ElectricityReading last = electricityReadings.stream()
                .max(Comparator.comparing(ElectricityReading::getTime))
                .get();

        return BigDecimal.valueOf(Duration.between(first.getTime(), last.getTime()).getSeconds() / 3600.0);
    }

}
