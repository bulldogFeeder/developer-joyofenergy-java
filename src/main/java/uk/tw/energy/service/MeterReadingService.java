package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MeterReadingService {

    private final Map<String, List<ElectricityReading>> meterAssociatedReadings;

    public MeterReadingService(Map<String, List<ElectricityReading>> meterAssociatedReadings) {
        this.meterAssociatedReadings = meterAssociatedReadings;
    }

    //获取电表id对应的全量读数list
    public Optional<List<ElectricityReading>> getReadings(String smartMeterId) {
        return Optional.ofNullable(meterAssociatedReadings.get(smartMeterId));
    }

    public void storeReadings(String smartMeterId, List<ElectricityReading> electricityReadings) {
        //在电表id为空时新建对应读数list
        if (!meterAssociatedReadings.containsKey(smartMeterId)) {
            meterAssociatedReadings.put(smartMeterId, new ArrayList<>());
        }
        //电表id不为空，获取电表id对应的读数list，并将此次读数追加到历史读数列表
        meterAssociatedReadings.get(smartMeterId).addAll(electricityReadings);
    }
}
