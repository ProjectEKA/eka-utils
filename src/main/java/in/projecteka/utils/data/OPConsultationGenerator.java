package in.projecteka.utils.data;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import in.projecteka.utils.DocRequest;
import in.projecteka.utils.data.model.Doctor;
import in.projecteka.utils.data.model.Medicine;
import in.projecteka.utils.data.model.Obs;
import in.projecteka.utils.data.model.SimpleAllergy;
import in.projecteka.utils.data.model.SimpleCondition;
import in.projecteka.utils.data.model.SimpleDiagnosticTest;
import in.projecteka.utils.data.model.TargetDisease;
import in.projecteka.utils.data.model.Vaccine;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Type;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import static in.projecteka.utils.data.FHIRUtils.getDiagnosticTestCode;
import static in.projecteka.utils.data.FHIRUtils.getMedication;
import static in.projecteka.utils.data.FHIRUtils.getReportAsDocReference;
import static in.projecteka.utils.data.FHIRUtils.getSurgicalReportAsAttachment;
import static in.projecteka.utils.data.Utils.randomBool;

public class OPConsultationGenerator implements  DocumentGenerator {
    private Properties doctors;
    private Properties patients;
    private Properties medicationProps;
    private Properties immunizationVaccineProps;
    private Properties immunizationDiseaseProps;

    @Override
    public void init() throws Exception {
        doctors = Utils.loadFromFile("/practitioners.properties");
        patients = Utils.loadFromFile("/patients.properties");
        medicationProps = Utils.loadFromFile("/medications.properties");
        immunizationDiseaseProps = Utils.loadFromFile("/immunization-diseases.properties");
        immunizationVaccineProps = Utils.loadFromFile("/immunization-vaccines.properties");
    }

    @Override
    public void execute(DocRequest request) throws Exception {
        FhirContext fhirContext = FhirContext.forR4();
        LocalDateTime dateTime = request.getFromDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        for (int i = 0; i < request.getNumber(); i++) {
            Date docDate = getCompositionDate(dateTime, i);
            Bundle bundle = createOPConsultationBundle(docDate, request.getPatientName(), request.getHipPrefix(), fhirContext.newJsonParser(), request.getPatientId());
            String encodedString = fhirContext.newJsonParser().encodeResourceToString(bundle);
            List<Bundle.BundleEntryComponent> patientEntries =
                    bundle.getEntry().stream()
                            .filter(e -> e.getResource().getResourceType().equals(ResourceType.Patient))
                            .collect(Collectors.toList());
            Bundle.BundleEntryComponent patientEntry = patientEntries.get(0);
            Path path = getFileSavePath(request, docDate, patientEntry);
            System.out.println("Saving " + request.getType() +  " Document to file:" + path.toString());
            Utils.saveToFile(path, encodedString);
            //System.out.println(encodedString);
        }
    }

    protected Date getCompositionDate(LocalDateTime dateTime, int docIndex) {
        return Utils.getFutureDate(dateTime, docIndex*2);
    }

    private Path getFileSavePath(DocRequest request, Date date, Bundle.BundleEntryComponent patientEntry) {
        String fileName = String.format("%s%s" + getDocBasicName() + "%s.json",
                request.getHipPrefix().toUpperCase(),
                patientEntry.getResource().getId(),
                Utils.formatDate(date, "yyyyMMdd"));
        return Paths.get(request.getOutPath().toString(), fileName);
    }

    protected String getDocBasicName() {
        return "OPConsultationDoc";
    }

    @SneakyThrows
    private Bundle createOPConsultationBundle(Date date, String patientName, String hipPrefix, IParser jsonParser, String patientId) {
        Bundle bundle = FHIRUtils.createBundle(date, hipPrefix);

        Composition opDoc = new Composition();
        opDoc.setId(UUID.randomUUID().toString());
        opDoc.setDate(bundle.getTimestamp());
        opDoc.setIdentifier(FHIRUtils.getIdentifier(opDoc.getId(), hipPrefix, "document"));
        opDoc.setStatus(Composition.CompositionStatus.FINAL);
        opDoc.setType(getDocumentType());
        opDoc.setTitle(getCompositionDocumentTitle());
        FHIRUtils.addToBundleEntry(bundle, opDoc, false);
        Practitioner author = FHIRUtils.createAuthor(hipPrefix, doctors);
        FHIRUtils.addToBundleEntry(bundle, author, false);
        opDoc.addAuthor().setResource(author);
        if (randomBool()) {
            opDoc.getAuthor().get(0).setDisplay(Doctor.getDisplay(author));
        }

        if (randomBool()) {
            Organization organization = jsonParser.parseResource(Organization.class, FHIRUtils.loadOrganization(hipPrefix));
            FHIRUtils.addToBundleEntry(bundle, organization, true);
            opDoc.setCustodian(FHIRUtils.getReferenceToResource(organization));
        }

        Patient patientResource = FHIRUtils.getPatientResource(patientName, patientId, patients);
        FHIRUtils.addToBundleEntry(bundle, patientResource, false);
        opDoc.setSubject(FHIRUtils.getReferenceToPatient(patientResource));

        Reference patientRef = FHIRUtils.getReferenceToPatient(patientResource);
        Encounter encounter = getCompositionEncounter(opDoc, bundle, patientRef);
        FHIRUtils.addToBundleEntry(bundle, encounter, false);
        Reference referenceToResource = FHIRUtils.getReferenceToResource(encounter);
        opDoc.setEncounter(referenceToResource);

        generateSections(hipPrefix, jsonParser, bundle, opDoc, patientResource, date);
        return bundle;
    }

    protected String getCompositionDocumentTitle() {
        return "OP Consultation Document";
    }

    protected void generateSections(String hipPrefix, IParser jsonParser, Bundle bundle, Composition opDoc, Patient patientResource, Date date) {
        createChiefComplaintsSection(bundle, opDoc, patientResource);
        createAllergiesSection(bundle, opDoc, patientResource, jsonParser);
        createMedicalHistorySection(bundle, opDoc, patientResource); //TODO
        createSymptomSection(bundle, opDoc, patientResource); //TODO
        createObservationSection(bundle, opDoc, patientResource, jsonParser);
        createInvestigationSection(bundle, opDoc, patientResource); //TODO
        createPrescriptionSection(bundle, opDoc, patientResource);
        createImmunizationRecordSection(bundle, opDoc, patientResource, date);
        createDocumentsSection(bundle, opDoc, patientResource, hipPrefix);
        createProcedureSection(bundle, opDoc, patientResource, hipPrefix);
        createDiagnosticReportSection(bundle, opDoc, patientResource, jsonParser, hipPrefix);
        createPlanSection(bundle, opDoc, patientResource);
        createFollowupSection(bundle, opDoc, patientResource, hipPrefix);
    }

    protected CodeableConcept getDocumentType() {
        return FHIRUtils.getOPConsultationType();
    }

    protected Encounter getCompositionEncounter(Composition opDoc, Bundle bundle, Reference patientRef) {
        Encounter encounter = FHIRUtils.createEncounter("Outpatient visit", "AMB", opDoc.getDate());
        encounter.setSubject(patientRef);
        return encounter;
    }

    protected void createProcedureSection(Bundle bundle, Composition composition, Patient patient, String hipPrefix) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Procedures");
        section.setCode(FHIRUtils.getProcedureSectionCode());
        Procedure procedure = new Procedure();
        procedure.setId(UUID.randomUUID().toString());
        procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
        if (randomBool()) {
            procedure.setCode(FHIRUtils.getCodeableConcept(
                    "90105005",
                    "Biopsy of soft tissue of forearm (Procedure)",
                    "Biopsy of suspected melanoma L) arm"));
        } else {
            procedure.setCode(FHIRUtils.getCodeableConcept(
                    "232717009",
                    "Coronary artery bypass grafting",
                    ""));
        }
        procedure.setSubject(FHIRUtils.getReferenceToResource(patient));
        procedure.setPerformed(getProcedureDate(composition));
        procedure.setAsserter(composition.getAuthorFirstRep());
        Procedure.ProcedurePerformerComponent performer = procedure.addPerformer();
        //performer.setActor()

        if (randomBool()) {
            Practitioner surgeon = FHIRUtils.createAuthor(hipPrefix, doctors);
            Resource existingResourceInBundle = FHIRUtils.findResourceInBundleById(bundle, ResourceType.Practitioner, surgeon.getId());
            if (existingResourceInBundle != null) {
                performer.setActor(FHIRUtils.getReferenceToResource(existingResourceInBundle));
            } else {
                FHIRUtils.addToBundleEntry(bundle, surgeon, true);
                performer.setActor(FHIRUtils.getReferenceToResource(surgeon));
            }
        }
        procedure.setComplication(Collections.singletonList(FHIRUtils.getCodeableConcept("131148009", "Bleeding", null)));
        FHIRUtils.addToBundleEntry(bundle, procedure, true);
        section.getEntry().add(FHIRUtils.getReferenceToResource(procedure));
    }

    protected DateTimeType getProcedureDate(Composition composition) {
        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValue(Utils.getFutureTime(composition.getDate(), 60));
        return dateTimeType;
    }

    @SneakyThrows
    protected void createFollowupSection(Bundle bundle, Composition composition, Patient patient, String hipPrefix) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Follow up");
        section.setCode(FHIRUtils.getFollowupSectionCode());
        Reference docRef = composition.getAuthor().get(0);
        Appointment app = FHIRUtils.createAppointment(docRef, getAppointmentDate(composition));
        if (randomBool()) {
            Practitioner anotherDoc = FHIRUtils.createAuthor(hipPrefix, doctors);
            Resource doctor = FHIRUtils.findResourceInBundleById(bundle, ResourceType.Practitioner, anotherDoc.getId());
            if (doctor == null) {
                FHIRUtils.addToBundleEntry(bundle, anotherDoc, true);
                doctor = anotherDoc;
            }
            Appointment.AppointmentParticipantComponent participant = app.addParticipant();
            participant.setActor(FHIRUtils.getReferenceToResource(doctor));
            if (app.getStatus().equals(Appointment.AppointmentStatus.BOOKED)) {
                participant.setStatus(Appointment.ParticipationStatus.ACCEPTED);
            } else {
                participant.setStatus(Appointment.ParticipationStatus.TENTATIVE);
            }
        }

        FHIRUtils.addToBundleEntry(bundle, app, true);
        section.getEntry().add(FHIRUtils.getReferenceToResource(app));
    }

    protected Date getAppointmentDate(Composition composition) {
        return Utils.getFutureDate(composition.getDate(), 7);
    }

    @SneakyThrows
    protected void createPlanSection(Bundle bundle, Composition composition, Patient patient) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Care Plan");
        section.setCode(FHIRUtils.getCarePlanSectionType());
        CarePlan plan = new CarePlan();
        plan.setId(UUID.randomUUID().toString());
        if (randomBool()) {
            plan.setStatus(CarePlan.CarePlanStatus.ACTIVE);
            plan.setIntent(CarePlan.CarePlanIntent.PLAN);
            plan.setTitle("Active Plan for next 2 months");
        } else {
            plan.setStatus(CarePlan.CarePlanStatus.DRAFT);
            plan.setIntent(CarePlan.CarePlanIntent.PROPOSAL);
            plan.setTitle("Tentative Plan for next 2 months");
        }
        plan.setSubject(composition.getSubject());
        plan.setAuthor(composition.getAuthorFirstRep());
        plan.setDescription("Actively monitor progress. Review every week to start with. Medications to be revised after 2 weeks.");

        Period period = new Period();
        period.setStart(composition.getDate());
        period.setEnd(Utils.getFutureDate(composition.getDate(), 60));
        plan.setPeriod(period);

        Annotation annotation1 = new Annotation();
        annotation1.setText("Actively monitor progress.");
        Annotation annotation2 = new Annotation();
        annotation2.setText("Review every week to start with. Medications to be revised after 2 weeks.");
        plan.setNote(Arrays.asList(annotation1, annotation2));

        FHIRUtils.addToBundleEntry(bundle, plan, false);
        section.getEntry().add(FHIRUtils.getReferenceToResource(plan));
    }

    @SneakyThrows
    protected void createDiagnosticReportSection(Bundle bundle, Composition composition, Patient patient, IParser jsonParser, String hipPrefix) {
        if (randomBool()) return; //dont need diagnosticReport always

        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Diagnostic Reports");
        section.setCode(FHIRUtils.getDiagnosticReportType());

        DiagnosticReport report = new DiagnosticReport();
        report.setId(UUID.randomUUID().toString());
        report.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
        section.getEntry().add(FHIRUtils.getReferenceToResource(report));
        FHIRUtils.addToBundleEntry(bundle, report, false);
        report.setSubject(FHIRUtils.getReferenceToPatient(patient));

        Reference encounterRef = composition.getEncounter();
        if (randomBool()) {
            Encounter encounter = FHIRUtils.createEncounter("Outpatient visit", "AMB", composition.getDate());
            encounter.setSubject(FHIRUtils.getReferenceToPatient(patient));
            FHIRUtils.addToBundleEntry(bundle, encounter, false);
            encounterRef = FHIRUtils.getReferenceToResource(encounter);
        }

        report.setIssued(composition.getDate());
        if (randomBool()) {
            report.setEffective(FHIRUtils.getDateTimeType(composition.getDate()));
        }

        Reference organizationRef = composition.getCustodian();
        if (organizationRef == null) {
            //TODO load a different organization
            Organization organization = jsonParser.parseResource(Organization.class, FHIRUtils.loadOrganization(hipPrefix));
            FHIRUtils.addToBundleEntry(bundle, organization, true);
            organizationRef = FHIRUtils.getReferenceToResource(organization);
        }
        report.setPerformer(Collections.singletonList(organizationRef));

        Practitioner interpreter = FHIRUtils.createAuthor(hipPrefix, doctors);
        Resource doctor = FHIRUtils.findResourceInBundleById(bundle, ResourceType.Practitioner, interpreter.getId());
        if (doctor == null) {
            FHIRUtils.addToBundleEntry(bundle, interpreter, false);
            doctor = interpreter;
        } else {
            doctor = ((Practitioner) composition.getAuthor().get(0).getResource());
        }
        report.setResultsInterpreter(Collections.singletonList(FHIRUtils.getReferenceToResource(doctor)));
        report.setCode(getDiagnosticTestCode(SimpleDiagnosticTest.getRandomTest()));

        if (randomBool()) {
            report.setConclusion("Refer to Doctor. To be correlated with further study.");
        }

        if (randomBool()) {
            //presented form
            report.getPresentedForm().add(getSurgicalReportAsAttachment("Surgical Pathology Report"));
            if (randomBool()) {
                addObservvationsToBundle(jsonParser, bundle, report);
            }
        } else {
            addObservvationsToBundle(jsonParser, bundle, report);
        }

        if (randomBool()) {
            DocumentReference docReference = getReportAsDocReference(interpreter, "Surgical Pathology Report");
            FHIRUtils.addToBundleEntry(bundle, docReference, false);
            section.getEntry().add(FHIRUtils.getReferenceToResource(docReference));
        }
    }

    @SneakyThrows
    protected void createDocumentsSection(Bundle bundle, Composition composition, Patient patient, String hipPrefix) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Clinical consultation");
        section.setCode(FHIRUtils.getDocumentReferenceSectionType());

        Practitioner docAuthor = (Practitioner) composition.getAuthorFirstRep().getResource();
        if (randomBool()) {
            Practitioner anotherDoc = FHIRUtils.createAuthor(hipPrefix, doctors);
            Practitioner doctor = (Practitioner) FHIRUtils.findResourceInBundleById(bundle, ResourceType.Practitioner, anotherDoc.getId());
            if (doctor == null) {
                FHIRUtils.addToBundleEntry(bundle, anotherDoc, false);
                docAuthor = anotherDoc;
            }
        }
        DocumentReference docReference = FHIRUtils.getReportAsDocReference(docAuthor, "Surgical Pathology Report");
        FHIRUtils.addToBundleEntry(bundle, docReference, false);
        section.getEntry().add(FHIRUtils.getReferenceToResource(docReference));
    }

    @SneakyThrows
    protected void createPrescriptionSection(Bundle bundle, Composition composition, Patient patient) {
        int numberOfMeds = Utils.randomInt(1, 3);
        Composition.SectionComponent section = composition.addSection();
        section.setTitle(getMedicationSectionsTitle());
        section.setCode(FHIRUtils.getPrescriptionSectionType());

        Reference referenceToPatient = FHIRUtils.getReferenceToPatient(patient);
        for (int i = 0; i < numberOfMeds; i++) {
            int medIndex = Utils.randomInt(1, 10);
            Medicine med = Medicine.parse((String) medicationProps.get(String.valueOf(medIndex)));
            Condition condition = FHIRUtils.getCondition(med.getCondition());
            Medication medication = getMedication(med);
            if (condition != null) {
                condition.setSubject(referenceToPatient);
                FHIRUtils.addToBundleEntry(bundle, condition, false);
            }
            var useMedicationCodeableConcept = Utils.randomBool();
            if (!useMedicationCodeableConcept) {
                FHIRUtils.addToBundleEntry(bundle, medication, false);
            }
            MedicationRequest medReq = FHIRUtils.createMedicationRequest(
                    getPractitioner(composition),
                    getMedicationDate(composition),
                    med,
                    medication,
                    condition,
                    useMedicationCodeableConcept);
            medReq.setSubject(referenceToPatient);
            FHIRUtils.addToBundleEntry(bundle, medReq, false);
            section.getEntry().add(FHIRUtils.getReferenceToResource(medReq));
        }
    }

    protected Date getMedicationDate(Composition composition) {
        return composition.getDate();
    }

    protected String getMedicationSectionsTitle() {
        return "Prescription";
    }

    private Practitioner getPractitioner(Composition composition) {
        IBaseResource resource = composition.getAuthor().get(0).getResource();
        return (Practitioner) resource;
    }

    @SneakyThrows
    protected void createInvestigationSection(Bundle bundle, Composition composition, Patient patient) {
        return;
    }

    @SneakyThrows
    protected void createObservationSection(Bundle bundle, Composition composition, Patient patient, IParser parser) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Physical Examination");
        section.setCode(FHIRUtils.getPhysicalExaminationSectionCode());
        int numOfObs = Utils.randomInt(1,3);
        for (int i = 0; i < numOfObs; i++) {
            Observation observation = parser.parseResource(Observation.class, Obs.getPhysicalObsResString());
            observation.setEffective(getEffectiveObservationDate(composition, i));
            observation.setId(UUID.randomUUID().toString());
            FHIRUtils.addToBundleEntry(bundle, observation, true);
            section.getEntry().add(FHIRUtils.getReferenceToResource(observation));
        }
    }

    protected Type getEffectiveObservationDate(Composition composition, int index) {
        DateTimeType dateTimeType = new DateTimeType();
        dateTimeType.setValue(composition.getDate());
        return dateTimeType;
    }

    @SneakyThrows
    protected void createSymptomSection(Bundle bundle, Composition composition, Patient patient) {
        return;
    }

    @SneakyThrows
    protected void createAllergiesSection(Bundle bundle, Composition composition, Patient patient, IParser parser) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Allergy Section");
        section.setCode(FHIRUtils.getAllergySectionType());
        AllergyIntolerance foodAllergy = SimpleAllergy.getFoodAllergy(parser, composition.getSubject(), composition.getAuthorFirstRep());
        AllergyIntolerance medicationAllergy = SimpleAllergy.getMedicationAllergy(parser, composition.getSubject(), composition.getAuthorFirstRep());
        FHIRUtils.addToBundleEntry(bundle, foodAllergy, true);
        FHIRUtils.addToBundleEntry(bundle, medicationAllergy, true);
        section.getEntry().add(FHIRUtils.getReferenceToResource(foodAllergy));
        section.getEntry().add(FHIRUtils.getReferenceToResource(medicationAllergy));
    }

    @SneakyThrows
    protected void createImmunizationRecordSection(Bundle bundle, Composition composition, Patient patient, Date date) {
        Composition.SectionComponent section = composition.addSection();
        section.setTitle("Immunization Recommendation");
        section.setCode(FHIRUtils.getImmunizationRecommendationSectionType());
        var vaccineIndex = RandomUtils.nextInt(1, 11);
        var diseaseIndex = RandomUtils.nextInt(1, 10);
        Vaccine vaccine = Vaccine.parse((String) immunizationVaccineProps.get(String.valueOf(vaccineIndex)));
        TargetDisease targetDisease = TargetDisease.parse((String) immunizationDiseaseProps.get(String.valueOf(diseaseIndex)));
        ImmunizationRecommendation immunizationRecommendation = FHIRUtils.getImmunizationRecommendation(vaccine, targetDisease, date);
        immunizationRecommendation.setPatient(FHIRUtils.getReferenceToPatient(patient));
        FHIRUtils.addToBundleEntry(bundle, immunizationRecommendation, true);
        section.getEntry().add(FHIRUtils.getReferenceToResource(immunizationRecommendation));
    }

    @SneakyThrows
    protected void createMedicalHistorySection(Bundle bundle, Composition composition, Patient patient) {
        return;
    }

    @SneakyThrows
    protected void createChiefComplaintsSection(Bundle bundle, Composition composition, Patient patient) {
        int numberOfComplaints = Utils.randomInt(1, 3);
        Composition.SectionComponent section = composition.addSection();
        section.setTitle(getComplaintsSectionTitle());
        section.setCode(FHIRUtils.getChiefComplaintSectionType());

        Reference referenceToPatient = FHIRUtils.getReferenceToPatient(patient);
        for (int i = 0; i < numberOfComplaints; i++) {
            Condition condition = FHIRUtils.createCondition(SimpleCondition.getRandomComplaint(), getComplaintDate(composition));
            condition.setSubject(referenceToPatient);
            FHIRUtils.addToBundleEntry(bundle, condition, false);
            section.getEntry().add(FHIRUtils.getReferenceToResource(condition));
        }
    }

    private Date getComplaintDate(Composition composition) {
        Encounter encounter = (Encounter) composition.getEncounter().getResource();
        return encounter.getPeriod().getStart();
    }

    protected String getComplaintsSectionTitle() {
        return "Chief Complaints";
    }

    private void addObservvationsToBundle(IParser parser, Bundle bundle, DiagnosticReport report) {
        Observation observation = parser.parseResource(Observation.class, Obs.getObservationResString());
        observation.setId(UUID.randomUUID().toString());
        FHIRUtils.addToBundleEntry(bundle, observation, true);
        report.addResult(FHIRUtils.getReferenceToResource(observation));
    }
}
