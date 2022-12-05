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

from datetime import datetime
from elasticsearch import Elasticsearch
from dotenv import load_dotenv
from ELKManager import ELKManager
import os
import json
import util
import argparse

load_dotenv()  # take environment variables from .env.
elasticServer=os.getenv("elasticServer")
elasticIndexAudit=os.getenv("elasticIndexAudit")
elasticIndexMapping=os.getenv("elasticIndexMapping")


elk = ELKManager(elasticServer,elasticIndexAudit,elasticIndexMapping)
args = util.parseArguments()

idsDic = elk.list_registered_IDs()

if args.edcid is not None :
    for el in idsDic:
        if el["assetId"] == args.edcid:
            listLogs = elk.list_logs_on_AWS_Object(el["awsID"])
            sortedList = sorted(listLogs, key=lambda d: d['time']) 
            
            util.printListe(args.edcid,sortedList)

elif args.awsid is not None:    
    for el in idsDic:
        if el["awsID"] == args.awsid:
            print(el["assetId"])

elif args.reg:
    util.printEDCListe(idsDic)

else:
    util.printEDCListe(idsDic)
    for entry in idsDic:
        logList = elk.list_logs_on_AWS_Object(entry["awsID"])
    
        sortedList = sorted(logList, key=lambda d: d['time'])
        util.printListe(entry["awsID"], sortedList)
          