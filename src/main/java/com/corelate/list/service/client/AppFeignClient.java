package com.corelate.list.service.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("orchestrator")
interface AppFeignClient {
    //Fetch for BPNM microservice Data
}
