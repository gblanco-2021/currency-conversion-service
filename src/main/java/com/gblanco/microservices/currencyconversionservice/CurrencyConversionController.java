package com.gblanco.microservices.currencyconversionservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Configuration(proxyBeanMethods = false)
class RestTemplateConfiguration {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}

@Slf4j
@RestController
public class CurrencyConversionController {

    @Autowired
    private CurrencyExchangeProxy proxy;
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversion(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
    ) {
        log.info("calling data defautl por 8000");
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to", to);


        ResponseEntity<CurrencyConversion> responseEntity = restTemplate.getForEntity(
                "http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                CurrencyConversion.class, uriVariables);

        if (responseEntity.getBody() == null) throw new NullPointerException("no hay resultado");

        CurrencyConversion currencyConversion = new CurrencyConversion();
        currencyConversion.setId(responseEntity.getBody().getId());
        currencyConversion.setFrom(responseEntity.getBody().getFrom());
        currencyConversion.setTo(responseEntity.getBody().getTo());
        currencyConversion.setQuantity(quantity);
        currencyConversion.setConversionMultiple(responseEntity.getBody().getConversionMultiple());
        currencyConversion.setTotalCalculatedAmount(quantity.multiply(responseEntity.getBody().getConversionMultiple()));
        currencyConversion.setEnvironment(responseEntity.getBody().getEnvironment() + " - restTemplate");
        return currencyConversion;
    }

    @GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionFeign(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
    ) {

        CurrencyConversion response = proxy.retrieveExchangeValue(from, to);

        CurrencyConversion currencyConversion = new CurrencyConversion();
        currencyConversion.setId(response.getId());
        currencyConversion.setFrom(response.getFrom());
        currencyConversion.setTo(response.getTo());
        currencyConversion.setQuantity(quantity);
        currencyConversion.setConversionMultiple(response.getConversionMultiple());
        currencyConversion.setTotalCalculatedAmount(quantity.multiply(response.getConversionMultiple()));
        currencyConversion.setEnvironment(response.getEnvironment() + " - feign");
        return currencyConversion;
    }
}
