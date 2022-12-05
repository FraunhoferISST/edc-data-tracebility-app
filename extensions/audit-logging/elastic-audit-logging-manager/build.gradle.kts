/*
 *  Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */


val edcVersion: String by project
val edcGroupId: String by project

plugins {
    `java-library`
}

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
    api("${edcGroupId}:core-spi:${edcVersion}")
    api("${edcGroupId}:transfer-spi:${edcVersion}")

    api(project(":extensions:audit-logging:audit-logging-spi"))

    implementation("co.elastic.clients:elasticsearch-java:8.4.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")

    implementation("javax.annotation:javax.annotation-api:1.2-b01")
}
