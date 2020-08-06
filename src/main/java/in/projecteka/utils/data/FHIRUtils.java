package in.projecteka.utils.data;

import in.projecteka.utils.data.model.Doctor;
import in.projecteka.utils.data.model.Medicine;
import in.projecteka.utils.data.model.SimpleDiagnosticTest;
import lombok.SneakyThrows;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import static in.projecteka.utils.data.Constants.EKA_LOINC_SYSTEM;
import static in.projecteka.utils.data.Utils.randomBool;

public class FHIRUtils {
    static Enumerations.AdministrativeGender getGender(String gender) {
        return Enumerations.AdministrativeGender.fromCode(gender);
    }

    static HumanName getHumanName(String name, String prefix, String suffix) {
        HumanName humanName = new HumanName();
        humanName.setText(name);
        if (!Utils.isBlank(prefix)) {
            humanName.setPrefix(Collections.singletonList(new StringType(prefix)));
        }
        if (!Utils.isBlank(suffix)) {
            humanName.setSuffix(Collections.singletonList(new StringType(suffix)));
        }
        return humanName;
    }

    static Identifier getIdentifier(String id, String domain, String resType) {
        Identifier identifier = new Identifier();
        identifier.setSystem(getHospitalSystemForType(domain, resType));
        identifier.setValue(id);
        return identifier;
    }

    private static String getHospitalSystemForType(String hospitalDomain, String type) {
        return String.format(Constants.HOSPITAL_SYSTEM, hospitalDomain, type);
    }

    static Bundle createBundle(Date forDate, String hipDomain) {
        Bundle bundle = new Bundle();
        bundle.setId(UUID.randomUUID().toString());
        bundle.setTimestamp(forDate);
        bundle.setIdentifier(getIdentifier(bundle.getId(), hipDomain, "bundle"));
        Meta bundleMeta = Utils.getMeta(forDate);
        bundle.setMeta(bundleMeta);
        bundle.setType(Bundle.BundleType.DOCUMENT);
        return bundle;
    }

    static Practitioner createAuthor(String hipPrefix, Properties doctors) {
        String details = (String) doctors.get(String.valueOf(Utils.randomInt(1, doctors.size())));
        Doctor doc = Doctor.parse(details);
        Practitioner practitioner = new Practitioner();
        practitioner.setId(hipPrefix.toUpperCase() + doc.getDocId());
        practitioner.setIdentifier(Arrays.asList(getIdentifier(practitioner.getId(), "mciindia", "doctor")));
        practitioner.getName().add(getHumanName(doc.getName(), doc.getPrefix(), doc.getSuffix()));
        return practitioner;
    }

    static Patient getPatientResource(String name, String patientId, Properties patients) throws Exception {
        Object patientDetail = patients.get(name);
        if (patientDetail == null) {
            throw new Exception("Can not identify patient with name: " + name);
        }
        in.projecteka.utils.data.model.Patient patient = in.projecteka.utils.data.model.Patient.parse((String) patientDetail);
        Patient patientResource = new Patient();
        if (Utils.isBlank(patientId)) {
            patientResource.setId(patient.getHid());
        } else {
            patientResource.setId(patientId);
        }
        patientResource.setName(Collections.singletonList(getHumanName(patient.getName(), null, null)));
        patientResource.setGender(getGender(patient.getGender()));
        return patientResource;
    }

    static Encounter createEncounter(String display, String encClass, Date date) {
        Encounter encounter = new Encounter();
        Period period = new Period();
        period.setStart(date);
        encounter.setPeriod(period);
        encounter.setStatus(Encounter.EncounterStatus.FINISHED);
        Coding coding = new Coding();
        coding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode");
        coding.setCode(encClass);
        coding.setDisplay(display);
        encounter.setClass_(coding);
        encounter.setId(UUID.randomUUID().toString());
        return encounter;
    }

    static CodeableConcept getDiagnosticReportType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.EKA_SCT_SYSTEM);
        coding.setCode("721981007");
        coding.setDisplay("Diagnostic Report");
        return type;
    }

    static CodeableConcept getPrescriptionType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.EKA_SCT_SYSTEM);
        coding.setCode("440545006");
        coding.setDisplay("Prescription record");
        return type;
    }

    static CodeableConcept getPrescriptionSectionType() {
        CodeableConcept prescriptionType = getPrescriptionType();
        prescriptionType.getCodingFirstRep().setDisplay("Prescription");
        return prescriptionType;
    }

    static CodeableConcept getChiefComplaintSectionType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.EKA_SCT_SYSTEM);
        coding.setCode("422843007");
        coding.setDisplay("Chief Complaint Section");
        return type;
    }



    static void addToBundleEntry(Bundle bundle, Resource resource, boolean useIdPart) {
        String resourceType = resource.getResourceType().toString();
        String id = useIdPart ? resource.getIdElement().getIdPart() : resource.getId();
        bundle.addEntry()
                .setFullUrl(resourceType + "/" + id)
                .setResource(resource);
    }

    static Reference getReferenceToPatient(Patient patientResource) {
        Reference patientRef = new Reference();
        patientRef.setResource(patientResource);
        if (Utils.randomBool()) {
            patientRef.setDisplay(patientResource.getNameFirstRep().getNameAsSingleString());
        }
        return patientRef;
    }

    static Reference getReferenceToResource(Resource res) {
        Reference ref = new Reference();
        ref.setResource(res);
        return ref;
    }

    static DateTimeType getDateTimeType(Date date) {
        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValue(date);
        return dateTimeType;
    }

    @SneakyThrows
    public static String loadOrganization(String hipPrefix) {
        String fileName = "/orgs/" + hipPrefix + ".json";
        return new String(Utils.class.getResourceAsStream(fileName).readAllBytes(), StandardCharsets.UTF_8);
    }

    public static CodeableConcept getOPConsultationType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.EKA_SCT_SYSTEM);
        coding.setCode("371530004");
        coding.setDisplay("Clinical consultation report");
        return type;
    }

    static Condition getCondition(String medCondition) {
        if (Utils.randomBool()) {
            Condition condition = new Condition();
            condition.setId(UUID.randomUUID().toString());
            CodeableConcept concept = new CodeableConcept();
            concept.setText(medCondition);
            condition.setCode(concept);
            return condition;
        }
        return null;
    }

    static Medication getMedication(Medicine med) {
        Medication medication = new Medication();
        medication.setId(UUID.randomUUID().toString());
        CodeableConcept concept = new CodeableConcept();
        if (Utils.randomBool()) {
            concept.setText(med.getName());
        } else {
            Coding coding = concept.addCoding();
            coding.setSystem(Constants.EKA_ACT_SYSTEM);
            coding.setCode(med.getCode());
            coding.setDisplay(med.getName());
        }
        medication.setCode(concept);
        return medication;
    }

    static MedicationRequest createMedicationRequest(Practitioner author,
                                                     Date date,
                                                     Medicine med,
                                                     Medication medication,
                                                     Condition condition,
                                                     boolean useMedicationCodeableConcept) {
        MedicationRequest medReq = new MedicationRequest();
        medReq.setId(UUID.randomUUID().toString());
        medReq.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
        medReq.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
        Reference authorRef = new Reference();
        authorRef.setResource(author);
        medReq.setRequester(authorRef);
        medReq.setAuthoredOn(date);
        if (useMedicationCodeableConcept) {
            CodeableConcept medCodeableConcept = new CodeableConcept();
            var text = medication.getCode().hasText() ? medication.getCode().getText() : medication.getCode().getCodingFirstRep().getDisplay();
            medCodeableConcept.setText(text);
            medReq.setMedication(medCodeableConcept);
        } else {
            medReq.setMedication(getReferenceToResource(medication));
        }

        medReq.addDosageInstruction().setText(med.getInstruction());
        if (!Utils.isBlank(med.getNotes())) {
            medReq.addNote().setText(med.getNotes());
        }

        if (condition != null) {
            medReq.setReasonReference(Collections.singletonList(getReferenceToResource(condition)));
        }
        return medReq;
    }

    public static CodeableConcept simpleCodeableConcept(String text) {
        CodeableConcept concept = new CodeableConcept();
        concept.setText(text);
        return concept;
    }

    public static CodeableConcept conceptWith(String text, String code, String system) {
        CodeableConcept concept = simpleCodeableConcept(text);
        if (!Utils.isBlank(code)) {
            Coding coding = concept.addCoding();
            coding.setDisplay(text);
            coding.setSystem(system);
            coding.setCode(code);
        }
        return concept;
    }

    static Period getPeriod(Date from, Date to) {
        Period period = new Period();
        period.setStart(from);
        if (to != null) {
            period.setEnd(to);
        }
        return period;
    }

    static CodeableConcept getDiagnosticTestCode(SimpleDiagnosticTest randomTest) {
        CodeableConcept concept = new CodeableConcept();
        if (randomBool()) {
            concept.setText(randomTest.getDisplay());
        }
        Coding coding = concept.addCoding();
        coding.setCode(randomTest.getCode());
        coding.setSystem(EKA_LOINC_SYSTEM);
        coding.setDisplay(randomTest.getDisplay());
        return concept;
    }

    static Attachment getSurgicalReportAsAttachment() throws IOException {
        Attachment attachment = new Attachment();
        attachment.setTitle("Surgical Pathology Report");
        attachment.setContentType("application/pdf");
        attachment.setData(Utils.readFileContent("/sample-prescription-base64.txt"));
        return attachment;
    }

    static DocumentReference getReportAsDocReference(Practitioner author) throws IOException {
        DocumentReference documentReference = new DocumentReference();
        documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
        documentReference.setId(UUID.randomUUID().toString());
        documentReference.setType(getDiagnosticReportType());
        CodeableConcept concept = new CodeableConcept();
        Coding coding = concept.addCoding();
        coding.setSystem(EKA_LOINC_SYSTEM);
        coding.setCode("30954-2");
        coding.setDisplay("Surgical Pathology Report");
        documentReference.setType(concept);
        documentReference.setAuthor(Collections.singletonList(getReferenceToResource(author)));
        DocumentReference.DocumentReferenceContentComponent content = documentReference.addContent();
        content.setAttachment(getSurgicalReportAsAttachment());
        return  documentReference;
    }

    public static CodeableConcept getPhysicalExaminationSectionCode() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.EKA_SCT_SYSTEM);
        coding.setCode("425044008");
        coding.setDisplay("Physical exam section");
        return type;
    }
    public static CodeableConcept getFollowupSectionCode() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.EKA_SCT_SYSTEM);
        coding.setCode("736271009");
        coding.setDisplay("Follow up");
        return type;
    }

    static Appointment createAppointment(Reference participantRef, Date apptDate) {
        Appointment app = new Appointment();
        app.setId(UUID.randomUUID().toString());
        if (randomBool()) {
            app.setStatus(Appointment.AppointmentStatus.PROPOSED);
        } else {
            app.setStatus(Appointment.AppointmentStatus.BOOKED);
        }
        app.setStart(apptDate);
        Appointment.AppointmentParticipantComponent participant = app.addParticipant();
        participant.setActor(participantRef);
        if (app.getStatus().equals(Appointment.AppointmentStatus.BOOKED)) {
            participant.setStatus(Appointment.ParticipationStatus.ACCEPTED);
        } else {
            participant.setStatus(Appointment.ParticipationStatus.TENTATIVE);
        }
        app.setDescription("Review progress in 7 days");
        return app;
    }
}