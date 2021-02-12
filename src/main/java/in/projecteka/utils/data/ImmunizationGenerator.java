package in.projecteka.utils.data;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import in.projecteka.utils.DocRequest;
import in.projecteka.utils.data.model.Doctor;
import in.projecteka.utils.data.model.Obs;
import in.projecteka.utils.data.model.SimpleCondition;
import in.projecteka.utils.data.model.Vaccine;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImmunizationGenerator implements DocumentGenerator {
    private Properties immunizationProps;
    private Properties doctors;
    private Properties patients;

    public void init() throws Exception {
        immunizationProps = Utils.loadFromFile("/immunization-vaccines.properties");
        doctors = Utils.loadFromFile("/practitioners.properties");
        patients = Utils.loadFromFile("/patients.properties");
    }

    public void execute(DocRequest request) throws Exception {
        FhirContext fhirContext = FhirContext.forR4();
        LocalDateTime dateTime = request.getFromDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        for (int i = 0; i < request.getNumber(); i++) {
            Date date = Utils.getNextDate(dateTime, i);
            Bundle bundle = createImmunizationBundle(date, request.getPatientName(), request.getHipPrefix(), request.getPatientId(), fhirContext.newJsonParser());
            String encodedString = fhirContext.newJsonParser().encodeResourceToString(bundle);
            List<Bundle.BundleEntryComponent> patientEntries =
                    bundle.getEntry().stream()
                            .filter(e -> e.getResource().getResourceType().equals(ResourceType.Patient))
                            .collect(Collectors.toList());
            Bundle.BundleEntryComponent patientEntry = patientEntries.get(0);
            String fileName = String.format("%s%sImmunizationDoc%s.json",
                    request.getHipPrefix().toUpperCase(),
                    patientEntry.getResource().getId(),
                    Utils.formatDate(date, "yyyyMMdd"));
            Path path = Paths.get(request.getOutPath().toString(), fileName);
            System.out.println("Saving Immunization to file:" + path.toString());
            Utils.saveToFile(path, encodedString);
            //System.out.println(encodedString);
        }
    }

    private Bundle createImmunizationBundle(Date date, String patientName, String hipPrefix, String patientId, IParser parser) throws Exception {
        Bundle bundle = FHIRUtils.createBundle(date, hipPrefix);
        Patient patientResource = FHIRUtils.getPatientResource(patientName, patientId, patients);
        Reference patientRef = createPatientReference(patientResource);

        Composition immunizationDoc = new Composition();
        immunizationDoc.setId(UUID.randomUUID().toString());
        immunizationDoc.setDate(bundle.getTimestamp());
        immunizationDoc.setIdentifier(FHIRUtils.getIdentifier(immunizationDoc.getId(), hipPrefix, "document"));
        immunizationDoc.setStatus(Composition.CompositionStatus.FINAL);
        CodeableConcept immunizationType = FHIRUtils.getImmunizationType();
        immunizationDoc.setType(immunizationType);
        immunizationDoc.setTitle("Immunization");
        FHIRUtils.addToBundleEntry(bundle, immunizationDoc, false);

        Practitioner author = FHIRUtils.createAuthor(hipPrefix, doctors);
        FHIRUtils.addToBundleEntry(bundle, author, false);
        immunizationDoc.addAuthor().setResource(author);
        if (Utils.randomBool()) {
            immunizationDoc.getAuthor().get(0).setDisplay(Doctor.getDisplay(author));
        }
        FHIRUtils.addToBundleEntry(bundle, patientResource, false);
        immunizationDoc.setSubject(FHIRUtils.getReferenceToPatient(patientResource));

        if (Utils.randomBool()) {
            //add encounter
            Encounter encounter = FHIRUtils.createEncounter("Outpatient visit", "AMB", immunizationDoc.getDate());
            encounter.setSubject(patientRef);
            FHIRUtils.addToBundleEntry(bundle, encounter, false);
            immunizationDoc.setEncounter(FHIRUtils.getReferenceToResource(encounter));
        }

        Composition.SectionComponent section = immunizationDoc.addSection();
        section.setTitle("OPD Immunization");
        section.setCode(immunizationType);

        int numberOfVaccines = Utils.randomInt(1, 3);
        for (int i = 0; i < numberOfVaccines; i++) {
            int medIndex = Utils.randomInt(1, 10);
            Vaccine vaccine = Vaccine.parse((String) immunizationProps.get(String.valueOf(medIndex)));
            Immunization immunization = FHIRUtils.getImmunization(vaccine, bundle.getTimestamp(), hipPrefix);

            Organization organization = FhirContext.forR4().newJsonParser().parseResource(Organization.class, FHIRUtils.loadOrganization(hipPrefix));
            FHIRUtils.addToBundleEntry(bundle, organization, true);
            immunization.setManufacturer(FHIRUtils.getReferenceToResource(organization));

            //Reason reference
            if(Utils.randomBool()) {
                int randomReasonRefNumber = Utils.randomInt(1,2);
                Resource reasonResource = FHIRUtils.createCondition(SimpleCondition.getRandomCondition(), date);

                if(randomReasonRefNumber == 1){
                    reasonResource = getObservation(parser);
                }

                if(randomReasonRefNumber == 2){
                    var diagnosticReport = FHIRUtils.getDiagnosticReport(date);
                    diagnosticReport.addResultsInterpreter(FHIRUtils.getReferenceToResource(author));
                    reasonResource = diagnosticReport;
                }

                FHIRUtils.addToBundleEntry(bundle, reasonResource, false);
                immunization.addReasonReference(FHIRUtils.getReferenceToResource(reasonResource));
            }

            if(Utils.randomBool()){
                Observation observation = getObservation(parser);
                FHIRUtils.addToBundleEntry(bundle, observation, false);
                immunization.addReaction().setDetail(FHIRUtils.getReferenceToResource(observation));
            }

            immunization.setPatient(patientRef);
            FHIRUtils.addToBundleEntry(bundle, immunization, false);
            section.getEntry().add(FHIRUtils.getReferenceToResource(immunization));
        }

        if (Utils.randomInt(1,10) % 3 == 0) {
            DocumentReference docReference = FHIRUtils.getReportAsDocReference(author, "Immunization Report");
            FHIRUtils.addToBundleEntry(bundle, docReference, false);
            section.getEntry().add(FHIRUtils.getReferenceToResource(docReference));
        }
        return bundle;
    }

    private Reference createPatientReference(Patient patientResource) {
        Reference patientRef = new Reference();
        patientRef.setResource(patientResource);
        return patientRef;
    }

    private Observation getObservation(IParser parser) {
        Observation observation = parser.parseResource(Observation.class, Obs.getObservationResString());
        observation.setId(UUID.randomUUID().toString());
        return observation;
    }

}
