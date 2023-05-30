/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.extractionemr.ndrfactory;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.extractionemr.api.service.ExtractionPatientService;
import org.openmrs.module.extractionemr.api.service.ExtractionemrService;
import org.openmrs.module.extractionemr.fragment.controller.NdrFragmentController;
import org.openmrs.module.extractionemr.model.BiometricVerify;
import org.openmrs.module.extractionemr.model.ndr.*;
import org.openmrs.module.extractionemr.ndrUtils.LoggerUtils;
import org.openmrs.module.extractionemr.ndrUtils.LoggerUtils.LogFormat;
import org.openmrs.module.extractionemr.ndrUtils.Utils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonQuestions {

    private static Map<Integer, String> map = new HashMap<>();
    ExtractionemrService extractionemrService = Context.getService(ExtractionemrService.class);
    ExtractionPatientService extractionPatientService = Context.getService(ExtractionPatientService.class);

    Utils utils = new Utils();

    private Map<Integer, String> hivQuestionDictionary = new HashMap<>();

    private void loadDictionary() {
    }

    public PatientDemographicsType createPatientDemographicsType(Patient pts, FacilityType facility, Map<Object, List<Obs>> groupedObsByEncounterTypes) throws DatatypeConfigurationException {

        PatientDemographicsType demo = new PatientDemographicsType();
        try {

            //Identifier 4 is Pepfar ID
            PatientIdentifier pidHospital, pidOthers, htsId, ancId, exposedInfantId, pepId, recencyId, pepfarid, openmrsId, tbId;
            pepfarid = new PatientIdentifier();

            pidOthers = Utils.getPatientIdentifier(pts.getIdentifiers(), Utils.OTHER_IDENTIFIER_INDEX);
            pidHospital = Utils.getPatientIdentifier(pts.getIdentifiers(), Utils.HOSPITAL_IDENTIFIER_INDEX);
            htsId = Utils.getPatientIdentifier(pts.getIdentifiers(), Utils.HTS_IDENTIFIER_INDEX);
            ancId = Utils.getPatientIdentifier(pts.getIdentifiers(), Utils.PMTCT_IDENTIFIER_INDEX);
            exposedInfantId = Utils.getPatientIdentifier(pts.getIdentifiers(), Utils.EXPOSE_INFANT_IDENTIFIER_INDEX);
            pepId = Utils.getPatientIdentifier(pts.getIdentifiers(), Utils.PEP_IDENTIFIER_INDEX);
            // pepfarid = pts.getPatientIdentifier(Utils.PEPFAR_IDENTIFIER_INDEX);
            recencyId = Utils.getPatientIdentifier(pts.getIdentifiers(), Utils.RECENCY_INDENTIFIER_INDEX);
            tbId = Utils.getPatientIdentifier(pts.getIdentifiers(), Utils.TB_IDENTIFIER_INDEX );
            openmrsId = Utils.getPatientIdentifier(pts.getIdentifiers(), Utils.OPENMRS_IDENTIFIER_INDEX);
            pepfarid = Utils.getPatientIdentifier(pts.getIdentifiers(), Utils.PEPFAR_IDENTIFIER_INDEX);

            IdentifierType idt;
            IdentifiersType identifiersType = new IdentifiersType();
            // Use PepfarID as preferred ID if it exist, else use other IDs
            if (pepfarid != null) {
                idt = new IdentifierType();
                idt.setIDNumber(pepfarid.getIdentifier());
                demo.setPatientIdentifier(pepfarid.getIdentifier());
            }else{
                String pepfaridForRedactedPatient = extractionPatientService.getPatientIdentifierByPatientsId(pts.getPatientId(), Utils.PEPFAR_IDENTIFIER_INDEX);
                if(pepfaridForRedactedPatient != null) {
                    demo.setPatientIdentifier(pepfaridForRedactedPatient);
                }
            }

            if (pidHospital != null) {
                idt = new IdentifierType();
                idt.setIDNumber(pidHospital.getIdentifier());
                idt.setIDTypeCode("HN");
                identifiersType.getIdentifier().add(idt);
            }
            if (pidOthers != null) {
                idt = new IdentifierType();
                idt.setIDNumber(pidOthers.getIdentifier());
                idt.setIDTypeCode("EID");
                identifiersType.getIdentifier().add(idt);
            }
            if (htsId != null) {
                idt = new IdentifierType();
                idt.setIDNumber(htsId.getIdentifier());
                idt.setIDTypeCode("HTS");
                identifiersType.getIdentifier().add(idt);
            }
            if (exposedInfantId != null) {
                idt = new IdentifierType();
                idt.setIDNumber(exposedInfantId.getIdentifier());
                idt.setIDTypeCode("HEI");
                identifiersType.getIdentifier().add(idt);
            }
            if (pepId != null) {
                idt = new IdentifierType();
                idt.setIDNumber(pepId.getIdentifier());
                idt.setIDTypeCode("PEP");
                identifiersType.getIdentifier().add(idt);
            }
            if (recencyId != null) {
                idt = new IdentifierType();
                idt.setIDNumber(recencyId.getIdentifier());
                idt.setIDTypeCode("RECENT");
                identifiersType.getIdentifier().add(idt);
            }
            if (tbId != null) {
                idt = new IdentifierType();
                idt.setIDNumber(tbId.getIdentifier());
                idt.setIDTypeCode("TB");
                identifiersType.getIdentifier().add(idt);
            }

            if(pts.isVoided() && pepfarid != null){
                ExtractionPatientService extractionPatientService = Context.getService(ExtractionPatientService.class);
                List<Integer> patientIds = extractionPatientService.getPatientIdsByIdentifiersByType(pepfarid.getIdentifier(),4);
                if(patientIds.size() > 0) return null;
            }

            //check Finger Print if available
            demo.setFingerPrints(getPatientsFingerPrint(pts.getPatientId()));


            String ndrCodedValue;
            Integer[] formEncounterTypeTargets = {Utils.ADULT_INITIAL_ENCOUNTER_TYPE, Utils.PED_INITIAL_ENCOUNTER_TYPE, Utils.INITIAL_ENCOUNTER_TYPE, Utils.HIV_Enrollment_Encounter_Type_Id, Utils.Client_Tracking_And_Termination_Encounter_Type_Id};

            List<Obs> obsListForEncounterTypesValues = Utils.extractObsList(groupedObsByEncounterTypes, Arrays.asList(formEncounterTypeTargets));


            List<Integer> obsCodeList = Arrays.asList(Utils.REASON_FOR_TERMINATION_CONCEPT,
                    Utils.DATE_OF_TERMINATION_CONCEPT,Utils.EDUCATIONAL_LEVEL_CONCEPT,
                    Utils.OCCUPATIONAL_STATUS_CONCEPT,Utils.MARITAL_STATUS_CONCEPT
            );
            Map<Object, List<Obs>> obsListForEncounterTypes = Utils.groupedByConceptIdsOnly(obsListForEncounterTypesValues);

            return demo;
        } catch (Exception ex) {
            LoggerUtils.write(NDRMainDictionary.class.getName(), ex.getMessage(), LoggerUtils.LogFormat.FATAL, LoggerUtils.LogLevel.live);
            //throw new DatatypeConfigurationException(Arrays.toString(ex.getStackTrace()));
        }

        return demo;

    }



    public FingerPrintType getPatientsFingerPrint(int id) {
        try {
            List<BiometricVerify> biometricInfos = extractionemrService.getBiometricInfoByPatientId(id);
            FingerPrintType fingerPrintsType = new FingerPrintType();
            if (biometricInfos.size() > 0) {
                RightHandType rightFingerType = new RightHandType();
                LeftHandType leftFingerType = new LeftHandType();
                XMLGregorianCalendar dataCaptured = null;
                for (BiometricVerify biometricInfo: biometricInfos) {
                    String fingerPosition = biometricInfo.getFingerPosition();
                    dataCaptured = utils.getXmlDateTime(biometricInfo.getDateCreated());
                    switch (fingerPosition) {
                        case "RightThumb":
                            rightFingerType.setRightThumb(biometricInfo.getTemplate());
                            rightFingerType.setHashedRightThumb(biometricInfo.getHashed());
                            rightFingerType.setRightThumbQuality(biometricInfo.getImageQuality());
                            break;
                        case "RightIndex":
                            rightFingerType.setRightIndex(biometricInfo.getTemplate());
                            rightFingerType.setHashedRightIndex(biometricInfo.getHashed());
                            rightFingerType.setRightIndexQuality(biometricInfo.getImageQuality());
                            break;
                        case "RightMiddle":
                            rightFingerType.setRightMiddle(biometricInfo.getTemplate());
                            rightFingerType.setHashedRightMiddle(biometricInfo.getHashed());
                            rightFingerType.setRightMiddleQuality(biometricInfo.getImageQuality());
                            break;
                        case "RightWedding":
                            rightFingerType.setRightWedding(biometricInfo.getTemplate());
                            rightFingerType.setHashedRightWedding(biometricInfo.getHashed());
                            rightFingerType.setRightWeddingQuality(biometricInfo.getImageQuality());
                            break;
                        case "RightSmall":
                            rightFingerType.setRightSmall(biometricInfo.getTemplate());
                            rightFingerType.setHashedRightSmall(biometricInfo.getHashed());
                            rightFingerType.setRightSmallQuality(biometricInfo.getImageQuality());
                            break;
                        case "LeftThumb":
                            leftFingerType.setLeftThumb(biometricInfo.getTemplate());
                            leftFingerType.setHashedLeftThumb(biometricInfo.getHashed());
                            leftFingerType.setLeftThumbQuality(biometricInfo.getImageQuality());
                            break;
                        case "LeftIndex":
                            leftFingerType.setLeftIndex(biometricInfo.getTemplate());
                            leftFingerType.setHashedLeftIndex(biometricInfo.getHashed());
                            leftFingerType.setLeftIndexQuality(biometricInfo.getImageQuality());
                            break;
                        case "LeftMiddle":
                            leftFingerType.setLeftMiddle(biometricInfo.getTemplate());
                            leftFingerType.setHashedLeftMiddle(biometricInfo.getHashed());
                            leftFingerType.setLeftMiddleQuality(biometricInfo.getImageQuality());
                            break;
                        case "LeftWedding":
                            leftFingerType.setLeftWedding(biometricInfo.getTemplate());
                            leftFingerType.setHashedLeftWedding(biometricInfo.getHashed());
                            leftFingerType.setLeftWeddingQuality(biometricInfo.getImageQuality());
                            break;
                        case "LeftSmall":
                            leftFingerType.setLeftSmall(biometricInfo.getTemplate());
                            leftFingerType.setHashedLeftSmall(biometricInfo.getHashed());
                            leftFingerType.setLeftSmallQuality(biometricInfo.getImageQuality());
                            break;
                    }
                }

                fingerPrintsType.setDateCaptured(dataCaptured);
                //fingerPrintsType.setCaptureCount(biometricInfo.);
                fingerPrintsType.setRightHand(rightFingerType);
                fingerPrintsType.setLeftHand(leftFingerType);
                return fingerPrintsType;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerUtils.write(NDRMainDictionary.class.getName(), e.getMessage(), LogFormat.FATAL, LoggerUtils.LogLevel.live.live);
        }
        return null;
    }

}
