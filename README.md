# Apache-Camel HL7 V2 message experiment

Simple example of how to use Apache Camel to simulate reading data, creating an HL7v2 MSH (Message Header Segment) message, and writing it out. This example uses Camel's HL7 component and routes to handle data transformations.

Requirements:
- Apache Camel
- Camel HL7 Component (camel-hl7)
- Camel Timer Component (to simulate scheduled reads)
- Camel Log Component (for output)
- Docker (optional for deployment)

### Project Folder Structure

```bash
.
project/
├── output/
│   └── observation-[timestamp].hl7 # Output messages
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           ├── ObservationSimulator.java 
│       │           └── HL7FHIRConverter.java # Data to HL7V2 converter
│       │           └── HL7V2Converter.java # Data to FHIR converter
│       └── resources/
│           └── application.properties
├── pom.xml
└── Dockerfile
- docker-compose.yml # Use this to run the experiment
- init.sh # Run once to create all the folders and files
```

## Code

The most interesting files are [HL7V2Converter.java](src/main/java/com/example/HL7V2Converter.java) and [HL7FHIRConverter.java](src/main/java/com/example/HL7FHIRConverter.java). They have the code to convert the value to HL7 V2 / FHIR messages.

### Output sample

```json
MSH|^~\&|||||20241004083550.079+0000||ORU^R01^ORU_R01|1|P|2.4
OBR|
OBX|1|NM|8310-5^Body temperature^LN||37.2|Cel^Celsius||||||||20241004083550
```

```json
{
  "resourceType": "Observation",
  "id": "observation-example",
  "status": "final",
  "code": {
    "coding": [ {
      "system": "http://loinc.org",
      "code": "8310-5",
      "display": "Body Temperature"
    } ]
  },
  "subject": {
    "reference": "Patient/123456"
  },
  "effectiveDateTime": "2020-10-05T10:30:00Z",
  "valueQuantity": {
    "value": 37.0,
    "unit": "C",
    "system": "http://unitsofmeasure.org",
    "code": "Cel"
  }
}
```

## Run the experiment

```bash
# Just run docker-compose in the root folder. Builds and starts the application
docker-compose up

# Then you can curl messages to the endpoints

# Random value, valid valid, invalid value
curl http://localhost:8080/api/trigger/hl7v2
curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d "bodyTemp=37.5" http://localhost:8080/api/trigger/hl7v2
curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d "bodyTemp=43.0" http://localhost:8080/api/trigger/hl7v2

# Random value, valid valid, invalid value
curl http://localhost:8080/api/trigger/fhir
curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d "bodyTemp=37.5" http://localhost:8080/api/trigger/fhir
curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d "bodyTemp=43.0" http://localhost:8080/api/trigger/fhir

# Toggle scheduler
curl http://localhost:8080/api/toggle-scheduler
```

## Advanced commands

```bash
# Just build the application
docker-compose build

# Just run the application
docker-compose up --no-build
```

```bash
# Run init.sh once to make sure you have all folders and main files
chmod +x init.sh
./init.sh
```

```bash
# Container commands
cd project
docker build -t camel-hl7-simulator .
docker run -v $(pwd)/output:/app/output camel-hl7-simulator
```

