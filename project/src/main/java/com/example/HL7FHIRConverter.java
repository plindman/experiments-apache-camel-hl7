package com.example;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Quantity;

public class HL7FHIRConverter {

    public static String convertToFHIR(String bodyTemperature) throws Exception {

        // Create a FHIR Observation
        FhirContext ctx = FhirContext.forR4(); // Use FHIR version of your choice
        Observation observation = new Observation();
        observation.setId("observation-example");
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.getCode().addCoding()
                .setSystem("http://loinc.org")
                .setCode("8310-5") // LOINC code for body temperature
                .setDisplay("Body Temperature");
        observation.setSubject(new Reference("Patient/123456")); // Reference to the patient
        observation.setEffective(new DateTimeType("2020-10-05T10:30:00Z")); // Effective date/time
        observation.setValue(new Quantity()
                .setValue(Double.parseDouble(bodyTemperature))
                .setUnit("C")
                .setSystem("http://unitsofmeasure.org")
                .setCode("Cel"));

        // Convert FHIR resource to JSON
        String fhirObservationJson = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation);
        return fhirObservationJson;
    }
}