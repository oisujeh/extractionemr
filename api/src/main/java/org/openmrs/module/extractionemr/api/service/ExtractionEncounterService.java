package org.openmrs.module.extractionemr.api.service;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.module.extractionemr.ExtractionemrConfig;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public interface ExtractionEncounterService extends EncounterService {
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Encounter> getEncountersByPatient(Patient patient, Date from, Date to) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	Encounter getLastEncounterByPatient(Patient patient, Date from, Date to) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Encounter> getEncountersByEncounterTypeIds(Patient patient, Date fromDate, Date toDate,
	        List<Integer> encounterTypeIds) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	Encounter getLastEncounterByEncounterTypeIds(Patient patient, Date fromDate, Date toDate, List<Integer> encounterTypeIds)
	        throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Encounter> getEncountersByEncounterIds(List<Integer> encounterIds) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	Encounter getEncounterByEncounterType(Patient patient, int encounterTypeId) throws APIException;
}
