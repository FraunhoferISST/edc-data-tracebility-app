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

from elasticsearch import Elasticsearch
import json
import util

class ELKManager:
    def __init__(self, elasticUrl, elasticAWSIndex,elasticEDCIndex):
        self.elasticServer = Elasticsearch(elasticUrl)
        self.elasticAWSIndex = elasticAWSIndex
        self.elasticEDCIndex = elasticEDCIndex
        self.responseCount = 10000

        self.checkIfIndexesExist()

    # Returns a List of all registered Dataflows with {"assetID" : "ID in EDC", "awsID" : "ID in the AWS Bucket"}
    def list_registered_IDs(self):

        listIDs = []
        self.elasticServer.indices.refresh(index=self.elasticEDCIndex)        
        resp = self.elasticServer.search(index=self.elasticEDCIndex, query={"match_all": {}},size=self.responseCount)

        for hit in resp['hits']['hits']:        
            msg = msg = json.loads(hit["_source"]["message"])

            if msg["type"] == "AWSPut":
                entry = {"assetId" : msg["assetID"], "awsID": msg["awsID"]}
                listIDs.append(entry)
        return listIDs

    #Returns a list of all Logs in relation to the given awsID
    def list_logs_on_AWS_Object(self,awsID):
        resp = self.elasticServer.search(index=self.elasticAWSIndex, query={"match_all":{}},size=self.responseCount)

        listOfLogs = []
        for hit in resp['hits']['hits']:
            msg = hit["_source"]["message"]
        
            if awsID in msg and "REST.GET.OBJECT" in msg:
                listOfLogs.append({"time" : util.get_time(msg), "message" : msg, "action" : "ACCESS ON FILE: "})
            if awsID in msg and ("POST" in msg or "Post" in msg or "post" in msg):
                listOfLogs.append({"time" : util.get_time(msg), "message" : msg, "action" : "PUSH OF FILE: "})   
                
        return listOfLogs
    
    def checkIfIndexesExist(self):
        try:
            if not self.elasticServer.indices.exists(index=self.elasticAWSIndex):
                print(self.elasticAWSIndex + " Index existiert nicht") 
                exit()
            if not self.elasticServer.indices.exists(index=self.elasticEDCIndex):
                print(self.elasticEDCIndex + " Index existiert nicht")
                exit()
        except:
            print("The connection to the ElasticServer could not be accomplished")
            exit()
