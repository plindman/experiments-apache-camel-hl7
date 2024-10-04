package com.example;

import ca.uhn.hl7v2.model.v24.message.ORU_R01;
import ca.uhn.hl7v2.model.v24.segment.OBX;
import ca.uhn.hl7v2.model.v24.datatype.CE;
import ca.uhn.hl7v2.model.v24.datatype.TS;
import ca.uhn.hl7v2.model.v24.datatype.NM;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.parser.Parser;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HL7Converter {

    public static String convertToHL7(String bodyTemperature) throws Exception {
        HapiContext context = new DefaultHapiContext();
        ORU_R01 message = new ORU_R01();
        message.initQuickstart("ORU", "R01", "P");

        // Set up the OBX segment
        OBX obx = message.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(0).getOBX();
        obx.getSetIDOBX().setValue("1");
        obx.getValueType().setValue("NM");
        obx.getObservationIdentifier().getIdentifier().setValue("8310-5");
        obx.getObservationIdentifier().getText().setValue("Body temperature");
        obx.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
        
        // Set the observation value correctly
        NM numericValue = new NM(message);
        numericValue.setValue(bodyTemperature);
        obx.getObservationValue(0).setData(numericValue);
        
        obx.getUnits().getIdentifier().setValue("Cel");
        obx.getUnits().getText().setValue("Celsius");
        
        // Set the observation date/time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        obx.getDateTimeOfTheObservation().parse(sdf.format(new Date()));

        // Generate the HL7 message
        Parser parser = context.getPipeParser();
        return parser.encode(message);
    }
}