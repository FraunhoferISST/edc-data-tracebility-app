/*
 *  Copyright (c) 2022 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V. - initial API and implementation
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
    api("${edcGroupId}:control-plane-spi:${edcVersion}")

    implementation("${edcGroupId}:control-plane-core:${edcVersion}")
}
