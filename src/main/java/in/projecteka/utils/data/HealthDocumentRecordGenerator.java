package in.projecteka.utils.data;

import ca.uhn.fhir.context.FhirContext;
import in.projecteka.utils.DocRequest;
import in.projecteka.utils.data.model.Doctor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
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

public class HealthDocumentRecordGenerator implements DocumentGenerator {
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
            Bundle bundle = createHealthDocumentRecordBundle(date, request.getPatientName(), request.getHipPrefix(), request.getPatientId());
            String encodedString = fhirContext.newJsonParser().encodeResourceToString(bundle);
            List<Bundle.BundleEntryComponent> patientEntries =
                    bundle.getEntry().stream()
                            .filter(e -> e.getResource().getResourceType().equals(ResourceType.Patient))
                            .collect(Collectors.toList());
            Bundle.BundleEntryComponent patientEntry = patientEntries.get(0);
            String fileName = String.format("%s%sHealthDocumentRecordDoc%s.json",
                    request.getHipPrefix().toUpperCase(),
                    patientEntry.getResource().getId(),
                    Utils.formatDate(date, "yyyyMMdd"));
            Path path = Paths.get(request.getOutPath().toString(), fileName);
            System.out.println("Saving HealthDocumentRecord to file:" + path.toString());
            Utils.saveToFile(path, encodedString);
        }
    }

    private Bundle createHealthDocumentRecordBundle(Date date, String patientName, String hipPrefix, String patientId) throws Exception {
        Bundle bundle = FHIRUtils.createBundle(date, hipPrefix);
        Patient patientResource = FHIRUtils.getPatientResource(patientName, patientId, patients);
        Reference patientRef = createPatientReference(patientResource);

        Composition healthDocumentRecordDoc = new Composition();
        healthDocumentRecordDoc.setId(UUID.randomUUID().toString());
        healthDocumentRecordDoc.setDate(bundle.getTimestamp());
        healthDocumentRecordDoc.setIdentifier(FHIRUtils.getIdentifier(healthDocumentRecordDoc.getId(), hipPrefix, "document"));
        healthDocumentRecordDoc.setStatus(Composition.CompositionStatus.FINAL);
        CodeableConcept healthDocumentRecordType = FHIRUtils.getHealthDocumentRecordType();
        healthDocumentRecordDoc.setType(healthDocumentRecordType);
        healthDocumentRecordDoc.setTitle("Health Document Record");
        FHIRUtils.addToBundleEntry(bundle, healthDocumentRecordDoc, false);

        Practitioner author = FHIRUtils.createAuthor(hipPrefix, doctors);
        FHIRUtils.addToBundleEntry(bundle, author, false);
        healthDocumentRecordDoc.addAuthor().setResource(author);
        if (Utils.randomBool()) {
            healthDocumentRecordDoc.getAuthor().get(0).setDisplay(Doctor.getDisplay(author));
        }
        FHIRUtils.addToBundleEntry(bundle, patientResource, false);
        healthDocumentRecordDoc.setSubject(FHIRUtils.getReferenceToPatient(patientResource));

        Encounter encounter = FHIRUtils.createEncounter("Outpatient visit", "AMB", healthDocumentRecordDoc.getDate());
        encounter.setSubject(patientRef);
        FHIRUtils.addToBundleEntry(bundle, encounter, false);
        healthDocumentRecordDoc.setEncounter(FHIRUtils.getReferenceToResource(encounter));

        Composition.SectionComponent section = healthDocumentRecordDoc.addSection();
        section.setTitle("Health Document Record");
        section.setCode(healthDocumentRecordType);
        DocumentReference docReference = FHIRUtils.getReportAsDocReference(author, "Health Document Record");
        FHIRUtils.addToBundleEntry(bundle, docReference, false);
        section.getEntry().add(FHIRUtils.getReferenceToResource(docReference));

        return bundle;
    }

    private Reference createPatientReference(Patient patientResource) {
        Reference patientRef = new Reference();
        patientRef.setResource(patientResource);
        return patientRef;
    }

}
