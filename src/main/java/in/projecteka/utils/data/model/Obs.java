package in.projecteka.utils.data.model;

import in.projecteka.utils.data.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class Obs {
    private static final String cholesterol = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"id\": \"cholesterol\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "    \"text\": \"Cholesterol\"\n" +
            "  },\n" +
            "  \"valueString\": \"6.3mmol/L\"\n" +
            "}";
    private static final String triglyceride = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"id\": \"triglyceride\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "    \"text\": \"Triglyceride\"\n" +
            "  },\n" +
            "  \"valueString\": \"1.3 mmol/L\"\n" +
            "}";
    private static final String temperature = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"id\": \"temperature\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "    \"text\": \"Temperature\"\n" +
            "  },\n" +
            "  \"valueQuantity\": {\n" +
            "    \"value\": 99.5,\n" +
            "    \"unit\": \"C\"\n" +
            "  }\n" +
            "}";
    private static final String pulse = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"id\": \"pulse\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "    \"text\": \"pulse\"\n" +
            "  },\n" +
            "  \"valueString\": \"72 bpm\"" +
            "}";
    private static final String rr = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"id\": \"rr\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "    \"text\": \"respiratory rate\"\n" +
            "  },\n" +
            "  \"valueString\": \"14\"" +
            "}";
    private static final String bloodPressure = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"id\": \"bloodPressure\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "    \"text\": \"Blood Pressure\"\n" +
            "  },\n" +
            "  \"valueString\": \"110/77\"" +
            "}";
    private static final String bodySurfaceTemperature = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"61008-9\",\n" +
            "    \"display\": \"Body surface temperature\"\n" +
            "  }]\n" +
            "  },\n" +
            "  \"valueQuantity\": {\n" +
            "    \"value\": 99.5,\n" +
            "    \"unit\": \"C\"\n" +
            "  }\n" +
            "}";
    private static final String respiratoryRate = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": {\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"9279-1\",\n" +
            "    \"display\": \"Respiratory rate\"\n" +
            "  }\n" +
            "  },\n" +
            "   \"valueQuantity\" : {\n" +
            "    \"value\" : 26,\n" +
            "    \"unit\" : \"breaths/minute\",\n" +
            "    \"system\" : \"http://unitsofmeasure.org\",\n" +
            "    \"code\" : \"/min\"\n" +
            "  }" +
            "}";
    private static final String heartRate = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": {\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"8867-4\",\n" +
            "    \"display\": \"Heart rate\"\n" +
            "  }\n" +
            "  },\n" +
            "   \"valueQuantity\" : {\n" +
            "    \"value\" : 44,\n" +
            "    \"unit\" : \"beats/minute\",\n" +
            "    \"system\" : \"http://unitsofmeasure.org\",\n" +
            "    \"code\" : \"/min\"\n" +
            "  }" +
            "}";
    private static final String oxygenSaturation = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"2708-6\",\n" +
            "    \"display\": \"Oxygen saturation in Arterial blood\"\n" +
            "  }]\n" +
            "  },\n" +
            "   \"valueQuantity\" : {\n" +
            "    \"value\" : 95,\n" +
            "    \"unit\" : \"%\",\n" +
            "    \"system\" : \"http://unitsofmeasure.org\",\n" +
            "    \"code\" : \"%\"\n" +
            "  }" +
            "}";
    private static final String bloodPressureVitalSigns = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            " \"code\" : {\n" +
            "    \"coding\" : [\n" +
            "      {\n" +
            "        \"system\" : \"http://loinc.org\",\n" +
            "        \"code\" : \"85354-9\",\n" +
            "        \"display\" : \"Blood pressure panel with all children optional\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"text\" : \"Blood pressure panel with all children optional\"\n" +
            "  }," +
            "  \"component\": [{"+
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"8480-6\",\n" +
            "    \"display\": \"Systolic blood pressure\"\n" +
            "  }]\n" +
            "  },\n" +
            "  \"valueQuantity\": {\n" +
            "    \"value\": 107,\n" +
            "    \"unit\": \"mmHg\",\n" +
            "    \"code\": \"mm[Hg]\"\n" +
            "  }\n" +
            "},\n{"+
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"8462-4\",\n" +
            "    \"display\": \"Diastolic blood pressure\"\n" +
            "  }]\n" +
            "  },\n" +
            "  \"valueQuantity\": {\n" +
            "    \"value\": 60,\n" +
            "    \"unit\": \"mmHg\",\n" +
            "    \"code\": \"mm[Hg]\"\n" +
            "   }\n" +
            "  }]\n" +
            "}";
    private static final String bmi = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"39156-5\",\n" +
            "    \"display\": \"Body mass index (BMI) [Ratio]\"\n" +
            "  }]\n" +
            "  },\n" +
            "\"valueQuantity\" : {\n" +
            "    \"value\" : 16.2,\n" +
            "    \"unit\" : \"kg/m2\",\n" +
            "    \"system\" : \"http://unitsofmeasure.org\",\n" +
            "    \"code\" : \"kg/m2\"\n" +
            "  }"+
            "}";
    private static final String bodyWeight = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"29463-7\",\n" +
            "    \"display\": \"Body weight\"\n" +
            "  }]\n" +
            "  },\n" +
            "  \"valueQuantity\": {\n" +
            "    \"value\": 185,\n" +
            "    \"unit\": \"lbs\"\n" +
            "  }\n" +
            "}";
    private static final String bodyHeight = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"8302-2\",\n" +
            "    \"display\": \"Body height\"\n" +
            "  }]\n" +
            "  },\n" +
            "  \"valueQuantity\": {\n" +
            "    \"value\": 66.899999,\n" +
            "    \"unit\": \"in\"\n" +
            "  }\n" +
            "}";

    private static final String sleepDuration = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"93832-4\",\n" +
            "    \"display\": \"Sleep duration\"\n" +
            "  }]\n" +
            "  },\n" +
            "  \"valueQuantity\": {\n" +
            "    \"value\": 8,\n" +
            "    \"unit\": \"h\"\n" +
            "  }\n" +
            "}";
    private static final String caloriesBurned = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"41981-2\",\n" +
            "    \"display\": \"Calories burned\"\n" +
            "  }]\n" +
            "  },\n" +
            "  \"valueQuantity\": {\n" +
            "    \"value\": 800,\n" +
            "    \"unit\": \"kcal\"\n" +
            "  }\n" +
            "}";
    private static final String fluidIntake = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"8999-5\",\n" +
            "    \"display\": \"Fluid intake oral Estimated\"\n" +
            "  }]\n" +
            "  },\n" +
            "  \"valueQuantity\": {\n" +
            "    \"value\": 99.5,\n" +
            "    \"unit\": \"C\"\n" +
            "  },\n" +
            "   \"valueQuantity\" : {\n" +
            "    \"value\" : 3,\n" +
            "    \"unit\" : \"Litres\",\n" +
            "    \"system\" : \"http://unitsofmeasure.org\",\n" +
            "    \"code\" : \"{mL or Litres}\"\n" +
            "  }" +
            "}";
    private static final String glucose = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"2339-0\",\n" +
            "    \"display\": \"Glucose [Mass/volume] in Blood\"\n" +
            "  }]\n" +
            "  },\n" +
            "   \"valueQuantity\" : {\n" +
            "    \"value\" : 142,\n" +
            "    \"unit\" : \"mg/dL\",\n" +
            "    \"system\" : \"http://unitsofmeasure.org\",\n" +
            "    \"code\" : \"mg/dL\"\n" +
            "  }" +
            "}";
    private static final String mentalStatus = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"8693-4\",\n" +
            "    \"display\": \"Mental status\"\n" +
            "  }]\n" +
            "  },\n" +
            "  \"valueCodeableConcept\" : {\n" +
            "    \"coding\" : [\n" +
            "      {\n" +
            "        \"system\" : \"http://snomed.info/sct\",\n" +
            "        \"code\" : \"247805009\",\n" +
            "        \"display\" : \"Anxiety and fear\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"text\" : \"Anxiety and fear\"\n" +
            "  }" +
            "}";
    private static final String ovulationDate = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"11976-8\",\n" +
            "    \"display\": \"Ovulation date\"\n" +
            "  }]\n" +
            "  },\n" +
            "  \"valueQuantity\" : {\n" +
            "    \"value\" : 41021,\n" +
            "    \"unit\" : \"MMDDYY\",\n" +
            "    \"system\" : \"http://unitsofmeasure.org\",\n" +
            "    \"code\" : \"{MMDDYY}\"\n" +
            "  }" +
            "}";
    private static final String menarcheAge = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"8693-4\",\n" +
            "    \"display\": \"Mental status\"\n" +
            "  }]\n" +
            "  },\n" +
            " \"valueQuantity\" : {\n" +
            "    \"value\" : 14,\n" +
            "    \"unit\" : \"age\",\n" +
            "    \"system\" : \"http://unitsofmeasure.org\",\n" +
            "    \"code\" : \"{age}\"\n" +
            "  }" +
            "}";
    private static final String menopauseAge = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": [{\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"42802-9\",\n" +
            "    \"display\": \"Age at menopause\"\n" +
            "  }]\n" +
            "  },\n" +
            " \"valueQuantity\" : {\n" +
            "    \"value\" : 52,\n" +
            "    \"unit\" : \"age\",\n" +
            "    \"system\" : \"http://unitsofmeasure.org\",\n" +
            "    \"code\" : \"{age}\"\n" +
            "  }" +
            "}";
    private static final String alcoholBehaviour = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": {\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"228273003\",\n" +
            "    \"display\": \"Finding relating to alcohol drinking behavior\"\n" +
            "  }\n" +
            "  },\n" +
            "  \"valueCodeableConcept\" : {\n" +
            "    \"coding\" : [\n" +
            "      {\n" +
            "        \"system\" : \"http://snomed.info/sct\",\n" +
            "        \"code\" : \"228365005\",\n" +
            "        \"display\" : \"Craving for alcohol\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"text\" : \"Craving for alcohol\"\n" +
            "  }\n" +
            "}";
    private static final String tobaccoSmoking = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": {\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"365981007\",\n" +
            "    \"display\": \"Finding of tobacco smoking behavior\"\n" +
            "  }\n" +
            "  },\n" +
            " \"valueCodeableConcept\" : {\n" +
            "    \"coding\" : [\n" +
            "      {\n" +
            "        \"system\" : \"http://snomed.info/sct\",\n" +
            "        \"code\" : \"266919005\",\n" +
            "        \"display\" : \"Never smoked tobacco\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"text\" : \"Never smoked tobacco\"\n" +
            "  }\n" +
            "}";
    private static final String tobaccoChewing = "{\n" +
            "  \"resourceType\": \"Observation\",\n" +
            "  \"status\": \"final\",\n" +
            "  \"code\": {\n" +
            "  \"coding\": {\n" +
            "    \"system\": \"http://loinc.org\",\n" +
            "    \"code\": \"228509002\",\n" +
            "    \"display\": \"Finding relating to tobacco chewing\"\n" +
            "  }\n" +
            "  },\n" +
            " \"valueCodeableConcept\" : {\n" +
            "    \"coding\" : [\n" +
            "      {\n" +
            "        \"system\" : \"http://snomed.info/sct\",\n" +
            "        \"code\" : \"228512004\",\n" +
            "        \"display\" : \"Never chewed tobacco\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"text\" : \"Never chewed tobacco\"\n" +
            "  }\n" +
            "}";

    private static Map<String, String> obsList = new HashMap<>() {{
       put("1", cholesterol);
       put("2", triglyceride);
       put("3", temperature);
       put("4", pulse);
    }};

    private static Map<String, String> commonObs = new HashMap<>() {{
        put("1", temperature);
        put("2", pulse);
        put("3", rr);
        put("4", bloodPressure);
    }};

    private static Map<String, String> obsVitalSigns = new HashMap<>() {{
        put("1", bodySurfaceTemperature);
        put("2", respiratoryRate);
        put("3", heartRate);
        put("4", oxygenSaturation);
        put("5", bloodPressureVitalSigns);
    }};

    private static Map<String, String> obsBodyMeasurement = new HashMap<>() {{
        put("1", bmi);
        put("2", bodyWeight);
        put("3", bodyHeight);
    }};

    private static Map<String, String> obsPhysicalActivity = new HashMap<>() {{
        put("1", sleepDuration);
        put("2", caloriesBurned);
    }};

    private static Map<String, String> obsGeneralAssessment = new HashMap<>() {{
        put("1", glucose);
        put("2", fluidIntake);
        put("3", mentalStatus);
    }};

    private static Map<String, String> obsWomenHealth = new HashMap<>() {{
        put("1", ovulationDate);
        put("2", menarcheAge);
        put("3", menopauseAge);
    }};

    private static Map<String, String> obsLifestyle = new HashMap<>() {{
        put("1", alcoholBehaviour);
        put("2", tobaccoSmoking);
        put("3", tobaccoChewing);
    }};

    public static String getObservationResString() {
        int index = Utils.randomInt(1, obsList.size());
        return obsList.get(String.valueOf(index));
    }

    public static String getPhysicalObsResString() {
        int index = Utils.randomInt(1, commonObs.size());
        return commonObs.get(String.valueOf(index));
    }

    public static String getVitalSignsObsResString() {
        int index = Utils.randomInt(1, obsVitalSigns.size());
        return obsVitalSigns.get(String.valueOf(index));
    }

    public static String getBodyMeasurementObsResString() {
        int index = Utils.randomInt(1, obsBodyMeasurement.size());
        return obsBodyMeasurement.get(String.valueOf(index));
    }

    public static String getGeneralAssessmentResString() {
        int index = Utils.randomInt(1, obsGeneralAssessment.size());
        return obsGeneralAssessment.get(String.valueOf(index));
    }

    public static String getWomenHealthObsResString() {
        int index = Utils.randomInt(1, obsWomenHealth.size());
        return obsWomenHealth.get(String.valueOf(index));
    }

    public static String getPhysicalActivityObsResString() {
        int index = Utils.randomInt(1, obsPhysicalActivity.size());
        return obsPhysicalActivity.get(String.valueOf(index));
    }

    public static String getLifestyleObsResString() {
        int index = Utils.randomInt(1, obsLifestyle.size());
        return obsLifestyle.get(String.valueOf(index));
    }

}
