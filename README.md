# Trading - OMS
## Overview
Order Management System (OMS) is part of Trading project. More description .....

## To Run
At current point of time, we have three big main components from docker compose perspective:
1. Order Management System (OMS)
     * docker compose in this project consists of:
       * OMS (with embedded Hazelcast)
         * Dockerfile located in {PROJECT_DIR}\docker\oms
       * Algo Engine (with embedded Hazelcast)
         * Due to the fact that algo engine is a separate project, we need to copy jar built to {PROJECT_DIR}\target
         * Dockerfile located in {PROJECT_DIR}\docker\algoengine
       * Kafka
2. Adapters 
    * docker compose will reside in adapters project, consists of:
      * 1 instance of Adapter
      * Postgres DB
      * PG admin
3. Dashboard BE
   * TBD

## Doker compose file
* compose.yaml for local use
* compose-dev.yaml for development environment
* compose-test.yaml for integration test