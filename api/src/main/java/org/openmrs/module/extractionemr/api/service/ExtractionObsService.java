package org.openmrs.module.extractionemr.api.service;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.ObsService;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.extractionemr.ExtractionemrConfig;

import java.util.Date;
import java.util.List;

public interface ExtractionObsService extends ObsService {
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Obs> getObsByConceptId(Integer personId, Integer conceptId, List<Integer> encounterIds, boolean includeVoided)
	        throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Obs> getObsByEncounters(List<Integer> encounterIds, boolean includeVoided) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Obs> getObsByEncounterTypes(List<Integer> ObsIds, List<Integer> encounterTypeIds, boolean includeVoided)
	        throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	Obs getLastObsByConceptId(Person person, Concept concept, Date from, Date to, boolean includeVoided) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	Obs getFirstObsByConceptId(Person person, Concept concept, Date from, Date to, boolean includeVoided)
	        throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	Obs getHighestObsByConcept(Person person, Concept concept, Date from, Date to, boolean includeVoided)
	        throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	Obs getObsbyValueCoded(Concept concept, Concept valueCoded, List<Integer> obsList) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	Obs getLastObsByConcept(Concept concept, List<Integer> obsList) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Obs> getObsByConcept(Concept concept, List<Integer> obsList) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Obs> getObsByConcept(List<Concept> concept, List<Integer> obsList) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Obs> getObsByVisitDate(Date visitDate, List<Integer> obsIds, boolean includeVoided) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Concept> getConcepts(List<Integer> conceptIds, boolean includeVoided) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Integer> getPatientsByObsDate(Date from, Date to, List<String> patientIds, boolean includeVoided)
	        throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Integer> getPatientEncounterIdsByDate(Integer id, Date fromDate, Date toDate) throws APIException;
}
