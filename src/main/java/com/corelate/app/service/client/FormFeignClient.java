package com.corelate.app.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("forms")
public interface FormFeignClient {

    @GetMapping("/builder/fetch/label/{elementId}")
    String fetchLabelByElementId(@PathVariable("elementId") String elementId);
}
