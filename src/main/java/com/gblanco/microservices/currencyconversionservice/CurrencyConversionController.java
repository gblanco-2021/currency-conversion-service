package com.gblanco.microservices.currencyconversionservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CurrencyConversionController {

    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversion(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity) {

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to", to);
        ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity("http://localhost:8001/currency-exchange/from/{from}/to/{to}",
                CurrencyConversion.class, uriVariables);

        CurrencyConversion body = responseEntity.getBody();

        if (body == null) throw new NullPointerException("no hay body");

        return new CurrencyConversion(body.getId(), from, to, quantity,
                body.getConversionMultiple(), quantity.multiply(body.getConversionMultiple()), body.getEnvironment());
    }

}
