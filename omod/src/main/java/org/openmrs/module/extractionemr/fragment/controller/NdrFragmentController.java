package org.openmrs.module.extractionemr.fragment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.extractionemr.Consumer;
import org.openmrs.module.extractionemr.api.service.ExtractionObsService;
import org.openmrs.module.extractionemr.api.service.ExtractionPatientService;
import org.openmrs.module.extractionemr.api.service.ExtractionemrService;
import org.openmrs.module.extractionemr.model.NDRExport;
import org.openmrs.module.extractionemr.ndrUtils.LoggerUtils;
import org.openmrs.module.extractionemr.ndrUtils.Utils;
import org.openmrs.module.extractionemr.omodmodels.DBConnection;
import org.openmrs.module.extractionemr.omodmodels.LocationModel;
import org.openmrs.module.extractionemr.service.NdrExtractionService;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NdrFragmentController {
	
	ExtractionPatientService extractionPatientService = Context.getService(ExtractionPatientService.class);
	
	ExtractionObsService extractionObsService = Context.getService(ExtractionObsService.class);
	
	ExtractionemrService extractionemrService = Context.getService(ExtractionemrService.class);
	
	DBConnection openmrsConn;
	
	ObjectMapper mapper = new ObjectMapper();
	
	NdrExtractionService ndrExtractionService;
	
	JAXBContext jaxbContext;
	
	public NdrFragmentController() throws Exception {
		jaxbContext = JAXBContext.newInstance("org.openmrs.module.extractionemr.model.ndr");
		ndrExtractionService = new NdrExtractionService(jaxbContext, false);
		openmrsConn = Utils.getNmrsConnectionDetails();
	}
	
	public void controller() {
	}
	
	public String generateCustomNDRFile(HttpServletRequest request,
										@RequestParam(value = "identifiers", required = false) String identifiers,
										@RequestParam(value = "to", required = false) String to,
										@RequestParam(value = "from", required = false) String from) throws Exception {
		try {
			// get date that's bounds to the date the export is kicked off
			Date lastDate = null;
			if (from != null && !from.isEmpty()) {
				lastDate = new SimpleDateFormat("yyyy-MM-dd").parse(from);
			}
			Date currentDate = null;
			if (to != null && !to.isEmpty()) {
				currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(to);
			}
			currentDate = Utils.getLastNDRDate();

			List<Integer> patients = new ArrayList<>();
			if (identifiers != null && !identifiers.isEmpty()) {
				String[] ary = identifiers.split(",");
				if (ary.length > 0) {
					List<String> identifierList = new ArrayList<>(Arrays.asList(ary));
					List<Integer> patientIdsFromIdentifiers = extractionPatientService.getPatientIdsByIdentifiers(identifierList, null, null);
					identifierList.addAll(patientIdsFromIdentifiers.stream().map(String::valueOf).collect(Collectors.toList()));
					List<Integer> patientIds = ndrExtractionService.getPatientIds(lastDate,currentDate,identifierList,true);
					Set<Integer> set = new HashSet<>(patientIds);
					patients = new ArrayList<>(set);
				}
			} else {
				List<Integer> patientIds = ndrExtractionService.getPatientIds(lastDate,currentDate,null,true);
				if(patientIds != null && patientIds.size()>0) patients.addAll(patientIds);
			}

			DBConnection openmrsConn = Utils.getNmrsConnectionDetails();

			//check if global variable for logging exists
			LoggerUtils.checkLoggerGlobalProperty(openmrsConn);
			LoggerUtils.clearLogFile();
			LoggerUtils.checkPatientLimitGlobalProperty(openmrsConn);

			String DATIMID = Utils.getFacilityDATIMId();
			return startGenerateFile(request, patients, DATIMID, lastDate, currentDate, false);
		}catch (Exception ex){
			return ex.getMessage();
		}
	}
	
	public String generateNDRFile(HttpServletRequest request) throws Exception {
		// get date that's bounds to the date the export is kicked off
		Date currentDate = new Date();
		DBConnection openmrsConn = Utils.getNmrsConnectionDetails();
		//check if global variable for logging exists
		LoggerUtils.checkLoggerGlobalProperty(openmrsConn);
		LoggerUtils.clearLogFile();
		LoggerUtils.checkPatientLimitGlobalProperty(openmrsConn);
		Date lastDate = Utils.getLastNDRDate();
		List<Integer> patients = ndrExtractionService.getPatientIds(lastDate, currentDate, null, true);
		String DATIMID = Utils.getFacilityDATIMId();
		return startGenerateFile(request, patients, DATIMID, lastDate, currentDate, true);
	}
	
	private String startGenerateFile(HttpServletRequest request, List<Integer> filteredPatients,
									 String DATIMID,Date lastDate, Date currentDate, boolean updateNdrLastRun) throws Exception {

		// Check that no export is in progress
		Map<String, Object> condition = new HashMap<>();
		condition.put("status","Processing");
		List<NDRExport> exports = extractionemrService.getExports(condition,1, false);
		if(exports.size() > 0 ) return "You already have an export in process, Kindly wait for it to finish";
		if(filteredPatients == null || filteredPatients.size() <= 0) return "no new patient record found";
		String contextPath = request.getContextPath();
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		UserContext userContext =  Context.getUserContext();
		Thread thread = new Thread(() -> {
			try {
				Consumer.initialize(userContext);
				ndrExtractionService.saveExport(fullContextPath,contextPath,filteredPatients,DATIMID,lastDate,currentDate, updateNdrLastRun);
			} catch (Exception e) {
				LoggerUtils.write(NdrFragmentController.class.getName(), e.getMessage(), LoggerUtils.LogFormat.FATAL,
						LoggerUtils.LogLevel.live);
			}
		});
		thread.start();
		if (updateNdrLastRun) Utils.updateLast_NDR_Run_Date(new Date());
		return "Export is being processed";
	}
	
	public String getFileList() throws IOException {
		return ndrExtractionService.getFileList(true);
	}
	
	public String getManualFileList() throws IOException {
		return ndrExtractionService.getFileList(false);
	}
	
	public boolean deleteFile(HttpServletRequest request, @RequestParam(value = "id") String id) {
		String contextPath = request.getContextPath();
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		return ndrExtractionService.deleteFile(fullContextPath, id);
	}
	
	public boolean restartFile(HttpServletRequest request, @RequestParam(value = "id") String id,
	        @RequestParam(value = "action") String action) {
		String contextPath = request.getContextPath();
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		String finalAction = action == null || action.isEmpty() ? "restart" : action;
		return ndrExtractionService.restartFile(fullContextPath, id, finalAction);
	}
	
	public boolean resumeFile(HttpServletRequest request, @RequestParam(value = "id") String id) {
		String contextPath = request.getContextPath();
		String fullContextPath = request.getSession().getServletContext().getRealPath(contextPath);
		return ndrExtractionService.restartFile(fullContextPath, id, "resume");
	}
	
	public void pauseFile(HttpServletRequest request, @RequestParam(value = "id") String id) {
		ndrExtractionService.pauseFile(id);
	}
	
	public String getAllLocation() {
		List<LocationModel> locationModels = new ArrayList<>();
		String locationString = "";

		try {

			Context.getLocationService().getAllLocations().stream().forEach(a -> {
				locationModels.add(new LocationModel(a.getName(), a.getLocationId()));
			});

			locationString = mapper.writeValueAsString(locationModels);
		} catch (JsonProcessingException ex) {
			Logger.getLogger(NdrFragmentController.class.getName()).log(Level.SEVERE, null, ex);
		}

		return locationString;
	}
}
