package in.projecteka.utils.data;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import in.projecteka.utils.DocRequest;
import in.projecteka.utils.data.model.Doctor;
import in.projecteka.utils.data.model.Obs;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
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

public class WellnessRecordGenerator implements DocumentGenerator {
    private Properties doctors;
    private Properties patients;

    @Override
    public void init() throws Exception {
        doctors = Utils.loadFromFile("/practitioners.properties");
        patients = Utils.loadFromFile("/patients.properties");
    }

    @Override
    public void execute(DocRequest request) throws Exception {
        FhirContext fhirContext = FhirContext.forR4();
        LocalDateTime dateTime = request.getFromDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        for (int i = 0; i < request.getNumber(); i++) {
            Date date = Utils.getNextDate(dateTime, i);
            Bundle bundle = createWellnessRecordBundle(date, request.getPatientName(), request.getHipPrefix(), request.getPatientId(), fhirContext.newJsonParser()
            );
            String encodedString = fhirContext.newJsonParser().encodeResourceToString(bundle);
            List<Bundle.BundleEntryComponent> patientEntries =
                    bundle.getEntry().stream()
                            .filter(e -> e.getResource().getResourceType().equals(ResourceType.Patient))
                            .collect(Collectors.toList());
            Bundle.BundleEntryComponent patientEntry = patientEntries.get(0);
            String fileName = String.format("%s%sWellnessRecordDoc%s.json",
                    request.getHipPrefix().toUpperCase(),
                    patientEntry.getResource().getId(),
                    Utils.formatDate(date, "yyyyMMdd"));
            Path path = Paths.get(request.getOutPath().toString(), fileName);
            System.out.println("Saving WellnessRecord to file:" + path.toString());
            Utils.saveToFile(path, encodedString);
        }
    }

    private Bundle createWellnessRecordBundle(Date date, String patientName, String hipPrefix, String patientId, IParser jsonParser) throws Exception {
        Bundle bundle = FHIRUtils.createBundle(date, hipPrefix);
        Patient patientResource = FHIRUtils.getPatientResource(patientName, patientId, patients);
        Reference patientRef = createPatientReference(patientResource);

        Composition wellnessRecordDoc = new Composition();
        wellnessRecordDoc.setId(UUID.randomUUID().toString());
        wellnessRecordDoc.setDate(bundle.getTimestamp());
        wellnessRecordDoc.setIdentifier(FHIRUtils.getIdentifier(wellnessRecordDoc.getId(), hipPrefix, "document"));
        wellnessRecordDoc.setStatus(Composition.CompositionStatus.FINAL);
        CodeableConcept wellnessRecordType = FHIRUtils.getWellnessRecordType();
        wellnessRecordDoc.setType(wellnessRecordType);
        wellnessRecordDoc.setTitle("Wellness Record");
        FHIRUtils.addToBundleEntry(bundle, wellnessRecordDoc, false);

        Practitioner author = FHIRUtils.createAuthor(hipPrefix, doctors);
        FHIRUtils.addToBundleEntry(bundle, author, false);
        wellnessRecordDoc.addAuthor().setResource(author);
        if (Utils.randomBool()) {
            wellnessRecordDoc.getAuthor().get(0).setDisplay(Doctor.getDisplay(author));
        }

        FHIRUtils.addToBundleEntry(bundle, patientResource, false);
        wellnessRecordDoc.setSubject(FHIRUtils.getReferenceToPatient(patientResource));

        Encounter encounter = FHIRUtils.createEncounter("Outpatient visit", "AMB", wellnessRecordDoc.getDate());
        encounter.setSubject(patientRef);
        FHIRUtils.addToBundleEntry(bundle, encounter, false);
        wellnessRecordDoc.setEncounter(FHIRUtils.getReferenceToResource(encounter));

        generateSections(jsonParser, bundle, wellnessRecordDoc);

        Composition.SectionComponent section = wellnessRecordDoc.addSection();
        section.setTitle("Document Reference");
        DocumentReference docReference = FHIRUtils.getReportAsDocReference(author, "Surgical Pathology Report");
        FHIRUtils.addToBundleEntry(bundle, docReference, false);
        section.getEntry().add(FHIRUtils.getReferenceToResource(docReference));
        return bundle;
    }

    private void generateSections(IParser jsonParser, Bundle bundle, Composition wellnessRecDoc) {
        createObservationVitalSignsSection(bundle, wellnessRecDoc, jsonParser);
        createObservationBodyMeasurementSection(bundle, wellnessRecDoc, jsonParser);
        createObservationGeneralAssessmentSection(bundle, wellnessRecDoc, jsonParser);
        createObservationPhysicalActivitySection(bundle, wellnessRecDoc, jsonParser);
        createObservationWomenHealthSection(bundle, wellnessRecDoc, jsonParser);
        createObservationLifestyleSection(bundle, wellnessRecDoc, jsonParser);
        createDocumentReferenceSection(bundle, wellnessRecDoc, jsonParser);
    }

    private void createDocumentReferenceSection(Bundle bundle, Composition wellnessRecDoc, IParser jsonParser) {

    }

    private void createObservationVitalSignsSection(Bundle bundle, Composition composition, IParser parser) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Vital Signs");
        int numOfObs = Utils.randomInt(1, 3);
        for (int i = 0; i < numOfObs; i++) {
            Observation observation = parser.parseResource(Observation.class, Obs.getVitalSignsObsResString());
            observation.setId(UUID.randomUUID().toString());
            FHIRUtils.addToBundleEntry(bundle, observation, true);
            section.getEntry().add(FHIRUtils.getReferenceToResource(observation));
        }
    }

    private void createObservationBodyMeasurementSection(Bundle bundle, Composition composition, IParser parser) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Body Measurement");
        int numOfObs = Utils.randomInt(1, 3);
        for (int i = 0; i < numOfObs; i++) {
            Observation observation = parser.parseResource(Observation.class, Obs.getBodyMeasurementObsResString());
            observation.setId(UUID.randomUUID().toString());
            FHIRUtils.addToBundleEntry(bundle, observation, true);
            section.getEntry().add(FHIRUtils.getReferenceToResource(observation));
        }
    }

    private void createObservationPhysicalActivitySection(Bundle bundle, Composition composition, IParser parser) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Physical Activity");
        int numOfObs = Utils.randomInt(1, 3);
        for (int i = 0; i < numOfObs; i++) {
            Observation observation = parser.parseResource(Observation.class, Obs.getPhysicalActivityObsResString());
            observation.setId(UUID.randomUUID().toString());
            FHIRUtils.addToBundleEntry(bundle, observation, true);
            section.getEntry().add(FHIRUtils.getReferenceToResource(observation));
        }
    }

    private void createObservationGeneralAssessmentSection(Bundle bundle, Composition composition, IParser parser) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("General Assessment");
        int numOfObs = Utils.randomInt(1, 3);
        for (int i = 0; i < numOfObs; i++) {
            Observation observation = parser.parseResource(Observation.class, Obs.getGeneralAssessmentResString());
            observation.setId(UUID.randomUUID().toString());
            FHIRUtils.addToBundleEntry(bundle, observation, true);
            section.getEntry().add(FHIRUtils.getReferenceToResource(observation));
        }
    }

    private void createObservationWomenHealthSection(Bundle bundle, Composition composition, IParser parser) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Women Health");
        int numOfObs = Utils.randomInt(1, 3);
        for (int i = 0; i < numOfObs; i++) {
            Observation observation = parser.parseResource(Observation.class, Obs.getWomenHealthObsResString());
            observation.setId(UUID.randomUUID().toString());
            FHIRUtils.addToBundleEntry(bundle, observation, true);
            section.getEntry().add(FHIRUtils.getReferenceToResource(observation));
        }
    }

    private void createObservationLifestyleSection(Bundle bundle, Composition composition, IParser parser) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Lifestyle");
        int numOfObs = Utils.randomInt(1, 3);
        for (int i = 0; i < numOfObs; i++) {
            Observation observation = parser.parseResource(Observation.class, Obs.getLifestyleObsResString());
            observation.setId(UUID.randomUUID().toString());
            FHIRUtils.addToBundleEntry(bundle, observation, true);
            section.getEntry().add(FHIRUtils.getReferenceToResource(observation));
        }
    }

    private Reference createPatientReference(Patient patientResource) {
        Reference patientRef = new Reference();
        patientRef.setResource(patientResource);
        return patientRef;
    }
}
