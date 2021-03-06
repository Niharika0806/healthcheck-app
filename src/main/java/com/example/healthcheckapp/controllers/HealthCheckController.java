package com.example.healthcheckapp.controllers;

import com.example.healthcheckapp.models.ServerHealthCheck;
import com.example.healthcheckapp.repositories.HealthCheckRepository;
import com.example.healthcheckapp.services.HealthCheckStatusService;
import org.apache.catalina.Server;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by 212583997 on 3/3/2018.
 */
@RestController
@EnableScheduling
@RequestMapping("/hcheck")
@CrossOrigin("*")
public class HealthCheckController {

    private HealthCheckStatusService healthCheckStatusService;

    @Autowired
    public HealthCheckController(HealthCheckStatusService healthCheckStatusService) {
        this.healthCheckStatusService = healthCheckStatusService;
    }

    @Autowired
    HealthCheckRepository healthCheckRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @GetMapping(value="/env/{env}")
    public List<ServerHealthCheck> getAllHealthchecksByEnv(@PathVariable("env") String env) {
        Query query = new Query();
        query.addCriteria(Criteria.where("envName").is(env));
        List<ServerHealthCheck> listOfServerHealthChecks = mongoTemplate.find(query, ServerHealthCheck.class);
        return listOfServerHealthChecks;
    }

    @GetMapping(value="/id/{hcheckId}")
    public ServerHealthCheck getHealthCheckForAnHealthCheckId(@PathVariable("hcheckId") String hcheckId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("healthCheckId").is(hcheckId));
        ServerHealthCheck desiredHealthCheck = mongoTemplate.findOne(query, ServerHealthCheck.class);
        return desiredHealthCheck;
    }

    @GetMapping(value="/all")
    public List<ServerHealthCheck> getAllHealthchecks()
    {
        return healthCheckRepository.findAll();
    }

    @Scheduled(fixedRate = 6000)
    public void updateTableForHealthchcekStatus()
    {
        List<ServerHealthCheck> listOfHealthChecks = healthCheckRepository.findAll();
        boolean preVerificationServerStatus = false, postVerificationServerStatus = false;
        for(ServerHealthCheck healthCheck : listOfHealthChecks) {
            preVerificationServerStatus = healthCheck.getServerStatus();
            postVerificationServerStatus = healthCheckStatusService.updateServerStatusForHealthChecks(healthCheck);
            if(preVerificationServerStatus != postVerificationServerStatus){
                healthCheck.setServerStatus(postVerificationServerStatus);
                healthCheckRepository.save(healthCheck);
            }
        }
    }

    @PostMapping(value="/add")
    public ServerHealthCheck addServerHealthCheck(@Valid @RequestBody ServerHealthCheck serverHealthCheck) {
        //  Setting the server status as false at the time of addtion as a default mechanism
        if(serverHealthCheck.getServerStatus() == null)
            serverHealthCheck.setServerStatus(false);
        return healthCheckRepository.save(serverHealthCheck);
    }

    @DeleteMapping(value="/delete/{healthCheckId}")
    public void deleteHealthcheck(@PathVariable("healthCheckId") String healthCheckId) {
        healthCheckRepository.delete(healthCheckId);
    }

    @PutMapping(value="/{hcid}")
    public ResponseEntity<ServerHealthCheck> updateHealthcheckData(@PathVariable("hcid") String hcid, @Valid @RequestBody ServerHealthCheck serverHealthCheckReq) {
        ServerHealthCheck existingHealthCheckInfo = healthCheckRepository.findOne(hcid);
        if(existingHealthCheckInfo == null){
            return new ResponseEntity<ServerHealthCheck>(HttpStatus.NOT_FOUND);
        }
        existingHealthCheckInfo.setApplicationPort(serverHealthCheckReq.getApplicationPort());
        existingHealthCheckInfo.setHealthCheckPort(serverHealthCheckReq.getHealthCheckPort());
        existingHealthCheckInfo.setServerStatus(serverHealthCheckReq.getServerStatus());
        existingHealthCheckInfo.setComponentName(serverHealthCheckReq.getComponentName());
        existingHealthCheckInfo.setCreatedBy(serverHealthCheckReq.getCreatedBy());
        existingHealthCheckInfo.setEnvName(serverHealthCheckReq.getEnvName());
        existingHealthCheckInfo.setHealthCheckUrl(serverHealthCheckReq.getHealthCheckUrl());
        existingHealthCheckInfo.setProjectName(serverHealthCheckReq.getProjectName());
        existingHealthCheckInfo.setIpAddress(serverHealthCheckReq.getIpAddress());

        ServerHealthCheck updatedHealthCheckInfo = healthCheckRepository.save(existingHealthCheckInfo);
        return new ResponseEntity<ServerHealthCheck>(updatedHealthCheckInfo, HttpStatus.OK);
    }


}
