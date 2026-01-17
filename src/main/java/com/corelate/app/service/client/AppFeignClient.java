package com.corelate.app.service.client;

import com.corelate.app.dto.CustomerDto;
import com.corelate.app.dto.PublishDtoEmail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("accounts")
public interface AppFeignClient {
	@GetMapping("/api/fetch/account")
	CustomerDto fetchAccountsById(@RequestParam("accountId") Long accountId);
}
