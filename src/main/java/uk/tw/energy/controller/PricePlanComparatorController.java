package uk.tw.energy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.PricePlanService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/price-plans")
public class PricePlanComparatorController {

    public final static String PRICE_PLAN_ID_KEY = "pricePlanId";
    public final static String PRICE_PLAN_COMPARISONS_KEY = "pricePlanComparisons";
    private final PricePlanService pricePlanService;
    private final AccountService accountService;

    public PricePlanComparatorController(PricePlanService pricePlanService, AccountService accountService) {
        this.pricePlanService = pricePlanService;
        this.accountService = accountService;
    }

    /**
     *
     * @param smartMeterId 传入电表id
     * @return 返回电表id在各种pricePlan下的实际收费情况以及该电表id实际适用的pricePlan
     */
    @GetMapping("/compare-all/{smartMeterId}")
    public ResponseEntity<Map<String, Object>> calculatedCostForEachPricePlan(@PathVariable String smartMeterId) {
        //根据电表id从已有map中取得实际对应的priceplanid
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);

        //根据电表id获取在不同planName情况下的收费价格
        Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
                pricePlanService.getConsumptionCostOfElectricityReadingsForEachPricePlan(smartMeterId);

        if (!consumptionsForPricePlans.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> pricePlanComparisons = new HashMap<>();
        pricePlanComparisons.put(PRICE_PLAN_ID_KEY, pricePlanId);
        pricePlanComparisons.put(PRICE_PLAN_COMPARISONS_KEY, consumptionsForPricePlans.get());

        return consumptionsForPricePlans.isPresent()
                ? ResponseEntity.ok(pricePlanComparisons)
                : ResponseEntity.notFound().build();
    }

    /**
     *
     * @param smartMeterId 传入电表id
     * @param limit 查询前limit项
     * @return 返回前limit项的<pricePlanId,实际收费>
     */
    @GetMapping("/recommend/{smartMeterId}")
    public ResponseEntity<List<Map.Entry<String, BigDecimal>>> recommendCheapestPricePlans(@PathVariable String smartMeterId,
                                                                                           @RequestParam(value = "limit", required = false) Integer limit) {
        //根据电表id获取在不同planName情况下的收费价格
        Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
                pricePlanService.getConsumptionCostOfElectricityReadingsForEachPricePlan(smartMeterId);

        if (!consumptionsForPricePlans.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        //将该电表id对应的各个pricePlan下实际收费价格转为list
        List<Map.Entry<String, BigDecimal>> recommendations = new ArrayList<>(consumptionsForPricePlans.get().entrySet());
        //将实际收费价格进行排序
        recommendations.sort(Comparator.comparing(Map.Entry::getValue));

        if (limit != null && limit < recommendations.size()) {
            //获取排序列表前limit项
            recommendations = recommendations.subList(0, limit);
        }

        return ResponseEntity.ok(recommendations);
    }
}
