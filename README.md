# Data tracking by auditing data #

Proof of concept of how to track data usage by aggregating audit-logs of different components in a single instance.

This proof of concept is based on the use of the Eclipse Dataspace Components Connector and the use of AWS S3 buckets for data storage. Log information is stored within the the EDC Connector as well as from the S3 bucket in an Elasticsearch instance. From the logs of the EDC Connector and the AWS S3 bucket, we can see how the data is being used by the consumer, even though the EDC Connector no longer has access to this component (AWS S3 Bucket).

## Getting Started ## 

This extension is based on the Milestone 7 version of the Eclipse Dataspace Components Connector.

### Preparation/Configuration ###

#### Prepare AWS S3 Bucket ####

In order to obtain logs from the AWS S3 buckets, preparations must first be made. For the consumer bucket an additional bucket must be created on the same AWS S3 server where its logs can be stored.
The next step is to configure the consumer bucket which bucket to write its logs to.

To do this, open the target bucket under AWS and enable the Server Access Logging option under Properties. The bucket created for this purpose is selected as the target bucket for the logs.

#### Configure Logstash ####

The awspipeline.conf file must be adapted to your own use before it can be used.

```
    "aws_credentials_file" => "[PATH to the file with the AWS credentials. Attention: File must be mounted in the Docker container]"
    "bucket" => "[Name of the AWS bucket managing the logs]"
    "region" => "[Server Region from AWS S3 Bucket]"
    "interval" => "120"
    "additional_settings" => {
        "force_path_style" => true
        "follow_redirects" => false
           }
        }
```


The following format is expected in the credential file for AWS:

```
:access_key_id: "[Your accessKey Id]"
:secret_access_key: "[Your secret access key]"
```

[Documentation](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-s3.html) about the logstash S3Plugin.

#### Mounting the AWS Secret File in Docker Compose ####

Mount the aws-secret file in the logstash container at the position specified in the configuration file. (The docker-container-location)

```
logstash:
    image: docker.elastic.co/logstash/logstash:8.4.3
    container_name: logstash
    restart: always
    environment:
      Test: test
    networks:
      - edc_network
    volumes:
      - ./logstash_config/pipeline/:/usr/share/logstash/pipeline/
      - {hostmachine-location}:{docker-container-location}
```



#### Prepare Anaylse Tool ####

Used **Python3.8** during development.

The Python script uses various libraries that must first be installed. It is also recommended to first create a [virtual environment](https://docs.python.org/3.8/tutorial/venv.html) and install the libraries in it.

```
//First step go into the analyse-tool directory
cd analyse-tool

//Create virtual enviroment mit Name audit-logging
python3 -m venv analyse-tool

//Aktivate virtual enviroment
source analyse-tool/bin/activate

//Install libaries
python3 -m pip install elasticsearch
python3 -m pip install python-dotenv
```

### Starting and using the Enviroment ###

Start the project with

```
./gradlew build
docker compose up --build
```

Use the AWS S3 Dataplane of the Eclipse Dataspace Connector to transfer the data from the data provider to the data consumer.

#### Using the analysis script: ####

The analysis script has the task to show a possible example how the data from the ElasticSearch Server can be analyzed. Potential for improvement on the side of performance as well as quality of the analysis can certainly be used much further.


```
// Output of all registered data streams and their history
python3.8 analysetool.py

// Output of all registered data streams
python3.8 analysetool.py -r

// Output of the history of a specific data stream based on its ID in the EDC Connector
python3.8 analysetool.py -e [EDCID]

// Output the ID of the data stream in the EDC Connector based on the key in the AWS S3 bucket.
python3.8 analysetool.py -a [AWSID]

//Attention: Combination of using this parameters as an combination does not work yet

```

## Known issues ##

Known problems/bugs of the PoC are listed here and extended, should further problems occur

### Duplication AWS S3 Logs ###

If the Logstash Docker container restarts, the logs from the AWS S3 buckets are re-pulled and pushed to the Elastic Search instance. This results in duplication of the AWS S3 log data. Especially with the use of persistence for the ElasticSearch instance, this leads to duplication of the AWS S3 logs every time the environment is restarted.

No solution to this problem has been found yet.

### The delay of creating AWS S3 logs is long ###

The time it takes after an action in the AWS S3 and creating an Log-Object in the connected Log Bucket takes a long time.

### Limit results query from Elasticsearch server to 10000 objects ###

With Elasticsearch's Python API, only currently 10000 objects can be obtained at once with a query query. However, this limit can be extended with better query logic.

## Possibility of extension

This extension follows the same philosophy as the EDC Connector. So it is possible to replace the provided ElasticAuditLoggingManager by other AuditLoggingManagers, as it is also possible in the EDC-Core project for different extensions.
