/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.extractionemr.api.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.extractionemr.ExtractionemrConfig;
import org.openmrs.module.extractionemr.Item;
import org.openmrs.module.extractionemr.model.BiometricVerify;
import org.openmrs.module.extractionemr.model.DatimMap;
import org.openmrs.module.extractionemr.model.NDRExport;
import org.openmrs.module.extractionemr.model.NDRExportBatch;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface ExtractionemrService extends OpenmrsService {
	
	/**
	 * Returns an item by uuid. It can be called by any authenticated user. It is fetched in read
	 * only transaction.
	 * 
	 * @param uuid
	 * @return
	 * @throws APIException
	 */
	@Authorized()
	@Transactional(readOnly = true)
	Item getItemByUuid(String uuid) throws APIException;
	
	/**
	 * Saves an item. Sets the owner to superuser, if it is not set. It can be called by users with
	 * this module's privilege. It is executed in a transaction.
	 * 
	 * @param item
	 * @return
	 * @throws APIException
	 */
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	Item saveItem(Item item) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional(readOnly = true)
	NDRExport getNDRExportById(int id) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional(readOnly = true)
	NDRExportBatch getNDRExportBatchById(int id) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<NDRExport> getNDRExportByBatchIdByStatus(int batchId, String status) throws APIException;
	
	/**
	 * Saves an item. Sets the owner to superuser, if it is not set. It can be called by users with
	 * this module's privilege. It is executed in a transaction.
	 * 
	 * @param ndrExport2
	 * @return
	 * @throws APIException
	 */
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	NDRExport saveNdrExportItem(NDRExport ndrExport2) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	NDRExportBatch saveNdrExportBatchItem(NDRExportBatch ndrExportBatch2, boolean override) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	void updateNdrExportItemProcessedCount(int id, int count) throws APIException;
	
	@Transactional
	void updateStatus(int exportId, int batchId, String status, boolean done) throws APIException;
	
	@Transactional
	void updateAllStatus(String status) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	List<NDRExport> getExports(boolean includeVoided) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	void voidExportEntry(int id) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	void voidExportBatchEntry(int id) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	List<NDRExport> getExports(Map<String, Object> conditions, Integer size, boolean includeVoided) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	Integer getFinishedExportCount(int batchId) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	List<NDRExport> getDelayedProcessingExports(Map<String, Object> conditions) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	NDRExportBatch createExportBatch(Date lastExportDate, int totalPatients) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	List<NDRExportBatch> getExportBatchByStatus(String status, boolean includeVoided) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	List<NDRExportBatch> getExportBatchByStartMode(boolean startMode, boolean includeVoided) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	NDRExportBatch updateExportBatch(int id, String status, boolean end) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	void resetExportBatch(int id) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	public List<BiometricVerify> getBiometricInfoByPatientId(Integer patientId) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	DatimMap getDatatimMapByDataimId(String datimId) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	@Transactional
	void deleteExports(int idInt) throws APIException;
	
	@Authorized(ExtractionemrConfig.MODULE_PRIVILEGE)
	List<Integer> getPatientIdsByIdentifiers(List<String> identifiers, Date fromDate, Date toDate) throws APIException;
	
}
