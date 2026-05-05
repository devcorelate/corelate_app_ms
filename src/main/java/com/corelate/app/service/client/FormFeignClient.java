package com.corelate.app.service.client;

import com.corelate.app.dto.FormElementLabelRequestDto;
import com.corelate.app.dto.FormElementLabelResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("forms")
public interface FormFeignClient {

    @GetMapping("/builder/fetch/label/{elementId}")
    String fetchLabelByElementId(@PathVariable("elementId") String elementId);

    @PostMapping("/builder/fetch/labels")
    List<FormElementLabelResponseDto> fetchLabelsByElementIds(@RequestBody FormElementLabelRequestDto request);
}
