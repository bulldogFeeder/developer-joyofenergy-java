package uk.tw.energy.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccountService {

    /**
     * 账户服务就是电表id及对应电力公司id（PricePlanId）
     * 该map中key为电表id，value为PricePlanId
     */
    private final Map<String, String> smartMeterToPricePlanAccounts;

    public AccountService(Map<String, String> smartMeterToPricePlanAccounts) {
        this.smartMeterToPricePlanAccounts = smartMeterToPricePlanAccounts;
    }

    public String getPricePlanIdForSmartMeterId(String smartMeterId) {
        return smartMeterToPricePlanAccounts.get(smartMeterId);
    }
}
