# Apache-Camel HL7 V2 message experiment

Simple example of how to use Apache Camel to simulate reading data, creating an HL7v2 MSH (Message Header Segment) message, and writing it out. This example uses Camel's HL7 component and routes to handle data transformations.

Requirements:
- Apache Camel
- Camel HL7 Component (camel-hl7)
- Camel Timer Component (to simulate scheduled reads)
- Camel Log Component (for output)
- Docker (optional for deployment)

##

# Project Folder Structure

```bash
.
project/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           ├── ObservationSimulator.java
│       │           └── HL7Converter.java
│       └── resources/
│           └── application.properties
├── pom.xml
└── Dockerfile
- init.sh # Run once to create all the folders and files
```

Running init.sh to create folders and main files

```bash
chmod +x init.sh
./init.sh
```

## Running the experiment

```bash
cd project
docker build -t camel-hl7-simulator .
docker run -v $(pwd)/output:/app/output camel-hl7-simulator
```
