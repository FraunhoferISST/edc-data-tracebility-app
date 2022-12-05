#    Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
# 
#    This program and the accompanying materials are made available under the
#    terms of the Apache License, Version 2.0 which is available at
#    https://www.apache.org/licenses/LICENSE-2.0
# 
#    SPDX-License-Identifier: Apache-2.0
# 
#    Contributors:
#         Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
# 

# -buster is required to have apt available
FROM openjdk:18-slim-buster

# by default curl is not available, so install it
RUN apt update && apt install curl -y

WORKDIR /app
COPY ./build/libs/audit-logging.jar /app
COPY ./config.properties /app

EXPOSE 8181
EXPOSE 8182
EXPOSE 8183
EXPOSE 8282


ENV POSTGRES_HOST="localhost"
ENV POSTGRES_PORT="5432"
ENV POSTGRES_DATABASE="bootx"
ENV POSTGRES_USER="root"
ENV POSTGRES_PASSWORD="changeme"
ENV IDS_WEBHOOK_ADRESS="localhost"
ENV EDC_API_KEY="password"
ENV EDC_IDS_ID="urn:connector:provider"
ENV EDC_ID="FraunhoferISST"


ENTRYPOINT java \
    -Dedc.fs.config=config.properties \
    -Dids.webhook.address=http://$IDS_WEBHOOK_ADRESS:8282 \
    -Dedc.api.auth.key=$EDC_API_KEY \
    -Dedc.datasource.contractnegotiation.url=jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/$POSTGRES_DATABASE \
    -Dedc.datasource.contractnegotiation.user=$POSTGRES_USER \
    -Dedc.datasource.contractnegotiation.password=$POSTGRES_PASSWORD \
    -Dedc.datasource.asset.url=jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/$POSTGRES_DATABASE \
    -Dedc.datasource.asset.user=$POSTGRES_USER \
    -Dedc.datasource.asset.password=$POSTGRES_PASSWORD \
    -Dedc.datasource.contractdefinition.url=jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/$POSTGRES_DATABASE \
    -Dedc.datasource.contractdefinition.user=$POSTGRES_USER \
    -Dedc.datasource.contractdefinition.password=$POSTGRES_PASSWORD \
    -Dedc.datasource.policy.url=jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/$POSTGRES_DATABASE \
    -Dedc.datasource.policy.user=$POSTGRES_USER \
    -Dedc.datasource.policy.password=$POSTGRES_PASSWORD \
    -Dedc.datasource.transferprocess.url=jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/$POSTGRES_DATABASE \
    -Dedc.datasource.transferprocess.user=$POSTGRES_USER \
    -Dedc.datasource.transferprocess.password=$POSTGRES_PASSWORD \
    -Dedc.ids.id=$EDC_IDS_ID \
    -Dedc.hostname=$EDC_ID \
    -Djava.security.edg=file:/dev/.urandom -jar audit-logging.jar