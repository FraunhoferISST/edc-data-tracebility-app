/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Fraunhofer Institute for Software and Systems Engineering - added dependencies
 *       ZF Friedrichshafen AG - add dependency
 *
 */

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val edcVersion: String by project
val edcGroupId: String by project

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public/")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation("${edcGroupId}:control-plane-core:${edcVersion}")
    implementation("${edcGroupId}:api-observability:${edcVersion}")
    implementation("${edcGroupId}:configuration-filesystem:${edcVersion}")
    implementation("${edcGroupId}:iam-mock:${edcVersion}")
    implementation("${edcGroupId}:auth-tokenbased:${edcVersion}")
    implementation("${edcGroupId}:data-management-api:${edcVersion}")

    implementation(project(":extensions:audit-logging:elastic-audit-logging-manager"))

    implementation("${edcGroupId}:ids:${edcVersion}")

    //Postgres SQL stores
    implementation("${edcGroupId}:contract-negotiation-store-sql:${edcVersion}")
    implementation("${edcGroupId}:transfer-process-store-sql:${edcVersion}")
    implementation("${edcGroupId}:contract-definition-store-sql:${edcVersion}")
    implementation("${edcGroupId}:asset-index-sql:${edcVersion}")
    implementation("${edcGroupId}:policy-definition-store-sql:${edcVersion}")

    implementation("${edcGroupId}:data-plane-aws-s3:${edcVersion}")

    //Postgres SQL needed helper modules
    implementation("${edcGroupId}:transaction-local:${edcVersion}")
    implementation("${edcGroupId}:transaction-spi:${edcVersion}")
    implementation("${edcGroupId}:transaction-datasource-spi:${edcVersion}")
    implementation("${edcGroupId}:sql-core:${edcVersion}")
    implementation("${edcGroupId}:sql-lease:${edcVersion}")
    implementation("${edcGroupId}:sql-pool-apache-commons:${edcVersion}")

    implementation("${edcGroupId}:data-plane-transfer-client:${edcVersion}")
    implementation("${edcGroupId}:data-plane-selector-client:${edcVersion}")
    implementation("${edcGroupId}:data-plane-selector-core:${edcVersion}")
    implementation("${edcGroupId}:data-plane-framework:${edcVersion}")
    implementation("${edcGroupId}:data-plane-http:${edcVersion}")

    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    implementation("org.postgresql:postgresql:42.3.3")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("audit-logging.jar")
}