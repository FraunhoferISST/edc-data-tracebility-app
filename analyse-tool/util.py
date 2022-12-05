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

import argparse
from datetime import datetime as dt

def parseArguments():
    # Create argument parser
    parser = argparse.ArgumentParser()

    # Optional arguments
    parser.add_argument("-e", "--edcid", help="Geben sie die EDCID für welchen Sie den Trace wollen.", type=str)
    parser.add_argument("-a", "--awsid", help="Geben sie die AWSID an und erhalten Sie die dazugehörige EDCID.", type=str)
    parser.add_argument("-r", "--reg", help="Output of all registered data streams",action='store_true')

    # Parse arguments
    args = parser.parse_args()

    return args


def printListe(id,list):
    for el in list:
        print("\n-----------------------------------------")
        print(el["action"]+ id + " at "+ str(el["time"])+"\n")
        print(el["message"])

def printEDCListe(liste):
    print("Alle registrierten IDs, welche in dem S3 Bucket abgelegt worden sind:\n")
    print("--------------------------------------------------")
    print("EDC-ID ----- AWS-ID")

    for el in liste:
        print(el["assetId"]+ " ---- " + el["awsID"])

def get_time(msg):
    dateString = msg.split()[2][1:]
    dateFormatted  = dt.strptime(dateString, "%d/%b/%Y:%H:%M:%S")
    
    return dateFormatted