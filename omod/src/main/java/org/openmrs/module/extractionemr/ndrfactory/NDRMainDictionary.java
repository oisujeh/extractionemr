package org.openmrs.module.extractionemr.ndrfactory;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.extractionemr.api.service.ExtractionEncounterService;
import org.openmrs.module.extractionemr.model.ndr.*;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//on master
public class NDRMainDictionary {

    private String appDirectory = "";

    private static Map<Integer, String> map = new HashMap<>();
    private CommonQuestions commonQuestionDictionary;
    private ExtractionEncounterService extractionEncounterService;

    public NDRMainDictionary() {
        commonQuestionDictionary = new CommonQuestions();
        extractionEncounterService = Context.getService(ExtractionEncounterService.class);

    }

    public PatientDemographicsType createPatientDemographicType2(Patient patient, FacilityType facility, Map<Object, List<Obs>> groupedObsByEncounterTypes) throws DatatypeConfigurationException {
        return commonQuestionDictionary.createPatientDemographicsType(patient, facility, groupedObsByEncounterTypes);
    }

}