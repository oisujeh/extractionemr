package org.openmrs.module.extractionemr.ndrfactory;


import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.extractionemr.api.service.ExtractionEncounterService;
import org.openmrs.module.extractionemr.api.service.ExtractionObsService;
import org.openmrs.module.extractionemr.api.service.ExtractionemrService;
import org.openmrs.module.extractionemr.model.ndr.*;
import org.openmrs.module.extractionemr.ndrUtils.ConstantsUtil;
import org.openmrs.module.extractionemr.ndrUtils.LoggerUtils;
import org.openmrs.module.extractionemr.ndrUtils.LoggerUtils.LogLevel;
import org.openmrs.module.extractionemr.ndrUtils.Utils;
import org.openmrs.module.extractionemr.omodmodels.DBConnection;
import org.openmrs.module.extractionemr.omodmodels.Version;

import javax.xml.datatype.DatatypeConfigurationException;
import java.sql.*;
import java.util.Date;
import java.util.*;

public class NDRConverter {

    Utils utils = new Utils();

    private Patient patient;

    private Map<Integer, List<Encounter>> groupedEncounters = new HashMap<>();

    private Map<String, List<Encounter>> groupedEncountersByUUID = new HashMap<>();

    private Map<Object, List<Obs>> groupedObsByConceptIds = new HashMap<>();

    private Map<Object, List<Obs>> groupedObsByEncounterTypes = new HashMap<>();

    private Map<Object, List<Obs>> groupedObsByVisitDate = new HashMap<>();

    private Encounter lastEncounter;

    private final DBConnection openmrsConn;

    private Map<Object, List<Obs>> groupedpatientBaselineObsByConcept = new HashMap<>();

    private Map<Object, List<Obs>> groupedpatientBaselineObsByEncounterType = new HashMap<>();

    private final ExtractionEncounterService extractionEncounterService;

    private final ExtractionObsService extractionObsService;

    private final Date fromDate;

    private final Date toDate;

    NDRMainDictionary mainDictionary;

    public NDRConverter(DBConnection _openmrsConn, Date fromDate, Date toDate) {
        this.openmrsConn = _openmrsConn;
        this.extractionEncounterService = Context.getService(ExtractionEncounterService.class);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.extractionObsService = Context.getService(ExtractionObsService.class);
        mainDictionary = new NDRMainDictionary();
    }

    public Container createContainer(Patient pts) throws Exception {
        String facilityName = Utils.getFacilityName();
        String DATIMID = Utils.getFacilityDATIMId();
        String FacilityType = "FAC";
        boolean hasUpdate = false;

        try {
            patient = pts;
            if(!pts.isVoided()) {
               List<Integer> encounterIds = extractionObsService.getPatientEncounterIdsByDate(pts.getId(), this.fromDate, this.toDate);
                if(encounterIds != null && encounterIds.size() > 0) {
                    List<Encounter> filteredEncounters = extractionEncounterService.getEncountersByEncounterIds(encounterIds);
                    if (filteredEncounters == null || filteredEncounters.isEmpty()) {
                        return null;
                    }
                    List<Encounter> encounters = new ArrayList<>(filteredEncounters);
                    this.lastEncounter = filteredEncounters.get(filteredEncounters.size() - 1);
                    this.groupedEncounters = Utils.extractEncountersByEncounterTypesId(encounters);
                    this.groupedEncountersByUUID = Utils.extractEncountersByEncounterTypesUUID(encounters);

                    List<Obs> allobs = Utils.extractObsfromEncounter(filteredEncounters);
                    Map<String, Map<Object, List<Obs>>> grouped = Utils.groupObs(allobs);
                    this.groupedObsByConceptIds = grouped.get("groupedByConceptIds");
                    this.groupedObsByEncounterTypes = grouped.get("groupedByEncounterTypes");
                    this.groupedObsByVisitDate = grouped.get("groupedObsByVisitDate");
                    List<Obs> patientBaselineObs = Context.getObsService().getObservationsByPerson(patient);
                    Map<String, Map<Object, List<Obs>>> groupedPatientBaseLine = Utils.groupObs(patientBaselineObs);
                    this.groupedpatientBaselineObsByConcept = groupedPatientBaseLine.get("groupedByConceptIds");
                    this.groupedpatientBaselineObsByEncounterType = groupedPatientBaseLine.get("groupedByEncounterTypes");

                    if (filteredEncounters.size() > 0)
                        for(Encounter enc: filteredEncounters){
                            Date newToDate = this.toDate;
                            if(newToDate == null) newToDate = new Date();
                            int dateCreatedComp = enc.getDateCreated().compareTo(newToDate);
                            int dateModifiedComp = -1;
                            if (enc.getDateChanged() != null) {
                                dateModifiedComp = enc.getDateChanged().compareTo(newToDate);
                            }
                            if(dateCreatedComp <= -1 && dateModifiedComp <= -1){
                                hasUpdate = true;
                                break;
                            }
                        }
                }
            }

            MessageHeaderType header = createMessageHeaderType(pts, hasUpdate);
            org.openmrs.module.extractionemr.model.ndr.FacilityType sendingOrganization = Utils.createFacilityType(facilityName, DATIMID, FacilityType);
            header.setMessageSendingOrganisation(sendingOrganization);

            Container container = new Container();
            PatientDemographicsType patientDemographicsType  = createContainer();
            container.setEmrType("NMRS");
            container.setPatientDemographics(patientDemographicsType);

            this.lastEncounter = null;
            this.groupedEncounters = null;
            this.groupedObsByConceptIds = null;
            this.groupedObsByEncounterTypes = null;
            this.groupedObsByVisitDate = null;
            this.groupedpatientBaselineObsByConcept =null;
            this.groupedpatientBaselineObsByEncounterType = null;

            return container;
        } catch (Exception ex) {
            LoggerUtils.write(NDRConverter.class.getName(), ex.getMessage(), LoggerUtils.LogFormat.FATAL, LogLevel.live);
            throw ex;
        }
    }


    private MessageHeaderType createMessageHeaderType(Patient pts, boolean hasUpdate) throws DatatypeConfigurationException {
        MessageHeaderType header = new MessageHeaderType();
        Calendar cal = Calendar.getInstance();
        header.setMessageCreationDateTime((utils.getXmlDateTime(cal.getTime())));
        Boolean isDeleted = pts.getPerson().getVoided();
        header.setMessageUniqueID(UUID.randomUUID().toString());
        header.setMessageVersion(1.0F);
        header.setXmlType("fingerprintsvalidation");
        return header;
    }

    private PatientDemographicsType createContainer() {
        String facilityName = Utils.getFacilityName();
        String DATIMID = Utils.getFacilityDATIMId();
        FacilityType facility = Utils.createFacilityType(facilityName, DATIMID, "FAC");
        PatientDemographicsType patientDemography = null;
        try {
            patientDemography = new NDRMainDictionary().createPatientDemographicType2(patient, facility, this.groupedObsByEncounterTypes);
            if (patientDemography == null) {
                return null;
            }
        } catch (Exception ex) {
            LoggerUtils.write(NDRConverter.class.getName(), ex.getMessage(), LoggerUtils.LogFormat.FATAL, LoggerUtils.LogLevel.live);
        }

        return patientDemography;
    }


}
