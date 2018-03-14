import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';  
import { BrowserModule } from '@angular/platform-browser';
import { UtilService } from '../app-services/util.service';
import { HealthcheckService } from '../app-services/healthcheck.service';
import { HealthCheck } from '../healthcheck';
import { ProjectService } from '../app-services/project.service';
import { Project } from '../project';
import { NgModule }  from '@angular/core';
import { Headers, Http } from '@angular/http';
import { Observable } from 'rxjs/Rx';


@Component({
  selector: 'app-healthcheck-dashboard',
  templateUrl: './healthcheck-dashboard.component.html',
  styleUrls: ['./healthcheck-dashboard.component.css']
})

@NgModule({
  imports: [CommonModule],
})

export class HealthcheckDashboardComponent implements OnInit {
  healthChecks: HealthCheck[];
  //title = 'HealthCheck DashBoard';
  envTypes: Array<string>;
  uniqueProjectNames: string[] = [];
  healthCheckById: HealthCheck = new HealthCheck();
  public timer;
  public subscription;

  constructor(
  	private healthCheckService: HealthcheckService,
    private utilService: UtilService,
    private http: Http) { 
  }

  ngOnInit() {
    this.envTypes = this.utilService.envTypes;
  }

  getHealthChecksByEnv(selectedEnv: string): void {
    if(this.subscription){
      this.subscription.unsubscribe();
    }
    
    this.timer = Observable.timer(5000,3500);
    this.subscription = this.timer.subscribe(() => {
      this.healthCheckService.getHealthChecksForAnEnv(selectedEnv)
      .then(healthChecks => this.healthChecks = healthChecks ) 
      .then(() => {
        for(let healthCheck of this.healthChecks){
          this.uniqueProjectNames.push(healthCheck.projectName);
        }
        this.uniqueProjectNames = Array.from(new Set(this.uniqueProjectNames));
      });
      console.log("inside function");
    });
  }

  getHealthCheckByHealthCheckId(healthCheckId: string): void {
    this.healthCheckService.getHealthCheckById(healthCheckId)
    .then(healthCheckById => this.healthCheckById = healthCheckById);
  }
}
