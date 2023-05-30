/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.extractionemr.api.dao;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.extractionemr.Item;
import org.openmrs.module.extractionemr.model.BiometricVerify;
import org.openmrs.module.extractionemr.model.DatimMap;
import org.openmrs.module.extractionemr.model.NDRExport;
import org.openmrs.module.extractionemr.model.NDRExportBatch;
//import org.openmrs.module.extractionemr.util.LoggerUtils;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository("extractionemr.ExtractionemrDao")
public class ExtractionemrDao {
	
	DbSessionFactory sessionFactory;
	
	public Item saveItem(Item item) {
		getSession().saveOrUpdate(item);
		return item;
	}
	
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public NDRExport getNDRExportById(int id) {
		return (NDRExport) getSession().createCriteria(NDRExport.class).add(Restrictions.eq("id", id)).uniqueResult();
	}
	
	public NDRExportBatch getNDRExportBatchById(int id) {
		return (NDRExportBatch) getSession().createCriteria(NDRExportBatch.class).add(Restrictions.eq("id", id))
		        .uniqueResult();
	}
	
	public NDRExport saveNdrExport(NDRExport ndrExport) {
		getSession().evict(ndrExport);
		getSession().saveOrUpdate(ndrExport);
		return ndrExport;
	}
	
	@SuppressWarnings("unchecked")
	public List<NDRExport> getExports(boolean includeVoided) throws DAOException {
		Criteria criteria = getSession().createCriteria(NDRExport.class);
		criteria.add(Restrictions.eq("voided", includeVoided));
		criteria.addOrder(Order.desc("dateStarted"));
		return criteria.list();
	}
	
	public List<NDRExport> getExports(Map<String, Object> conditions, Integer size, boolean includeVoided)
	        throws DAOException {
		Criteria criteria = getSession().createCriteria(NDRExport.class);
		criteria.add(Restrictions.eq("voided", includeVoided));
		for (String key : conditions.keySet()) {
			criteria.add(Restrictions.eq(key, conditions.get(key)));
		}
		criteria.addOrder(Order.desc("dateStarted"));
		//		criteria.setFetchSize(size);
		if (size != null && size > 0)
			criteria.setMaxResults(size);
		return criteria.list();
	}
	
	public List<BiometricVerify> getBiometricInfoByPatientId(Integer patientId) throws DAOException {
		Criteria criteria = getSession().createCriteria(BiometricVerify.class);
		criteria.add(Restrictions.eq("patientId", patientId));
		return criteria.list();
	}
	
	public NDRExportBatch save(NDRExportBatch ndrExportBatch) throws APIException {
		getSession().saveOrUpdate(ndrExportBatch);
		return ndrExportBatch;
	}
	
	public List<NDRExportBatch> getExportBatchByStatus(String status, boolean includeVoided) throws APIException {
		Criteria criteria = getSession().createCriteria(NDRExportBatch.class);
		if (status != null) {
			criteria.add(Restrictions.eq("status", status));
		}
		if (!includeVoided)
			criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.desc("dateCreated"));
		return criteria.list();
	}
	
	public NDRExportBatch getExportBatch(int id) throws APIException {
		Criteria criteria = getSession().createCriteria(NDRExportBatch.class);
		criteria.add(Restrictions.eq("id", id));
		criteria.addOrder(Order.desc("dateCreated"));
		return (NDRExportBatch) criteria.uniqueResult();
	}
	
	public DatimMap getDatatimMapByDataimId(String datimId) {
		Criteria criteria = getSession().createCriteria(DatimMap.class);
		criteria.add(Restrictions.eq("datimCode", datimId));
		criteria.setFetchSize(1);
		List<DatimMap> datimMapList = (List<DatimMap>) criteria.list();
		if (datimMapList.size() > 0)
			return datimMapList.get(0);
		return null;
	}
	
	public void updateStatus(int exportId, int batchId, String status, boolean done) {
		StringBuilder sb = new StringBuilder("update finger_verify_export set status = :status, date_updated = :dateUpdated");
		
		if (done)
			sb.append(", date_ended = :dateEnded");
		
		sb.append(" where batch_id = :batchId");
		if (exportId > 0)
			sb.append(" and finger_verify_export_id = :exportId");
		
		SQLQuery sql = getSession().createSQLQuery(sb.toString());
		
		sql.setString("status", status);
		if (exportId > 0)
			sql.setInteger("exportId", exportId);
		sql.setInteger("batchId", batchId);
		sql.setTimestamp("dateUpdated", new Date());
		if (done)
			sql.setTimestamp("dateEnded", new Date());
		sql.executeUpdate();
	}
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public List<NDRExport> getDelayedProcessingExports(Map<String, Object> conditions) {

		StringBuilder query = new StringBuilder("SELECT distinct(N.finger_verify_export_id) FROM finger_verify_export N "
				+ " WHERE TIMESTAMPDIFF(MINUTE,N.date_updated,NOW()) > 15 ");
		for (String key : conditions.keySet()) {
			query.append(" AND N.").append(key).append(" = ").append(conditions.get(key));
		}

		SQLQuery sql = getSession().createSQLQuery(query.toString());

		List<Integer> exportId = sql.list();

		if(exportId.size()>0) {
			Criteria criteria = getSession().createCriteria(NDRExport.class);
			criteria.add(Restrictions.in("id", exportId));

			return criteria.list();
		}
		return new ArrayList<>();
	}
	
	public void deleteExports(int batchId) {
		SQLQuery sql = getSession().createSQLQuery("delete from finger_verify_export where batch_id = :batchId ");
		sql.setInteger("batchId", batchId);
		sql.executeUpdate();
	}
	
	public Integer getFinishedExportCount(Integer batchId, boolean includeVoided) {
		SQLQuery sql = getSession().createSQLQuery(
		    "select count(*) from finger_verify_export where batch_id = :batchId and status in ('Failed', 'Done') ");
		sql.setInteger("batchId", batchId);
		return ((BigInteger) sql.uniqueResult()).intValue();
	}
	
	public void updateAllStatus(String status) {
		SQLQuery sql = getSession().createSQLQuery(
		    "update finger_verify_batch_export set status = :status where status = 'Processing'");
		sql.setString("status", status);
		sql.executeUpdate();
	}
	
	public List<NDRExport> getNDRExportByBatchIdByStatus(int batchId, String status) {
		Criteria criteria = getSession().createCriteria(NDRExport.class);
		criteria.add(Restrictions.eq("batchId", batchId));
		criteria.add(Restrictions.eq("status", status));
		List<NDRExport> ndrExports = criteria.list();
		if(ndrExports != null && ndrExports.size() > 0) return ndrExports;
		return new ArrayList<>();
	}
	
	public List<NDRExportBatch> getExportBatchByStartMode(boolean startMode, boolean includeVoided) {
		Criteria criteria = getSession().createCriteria(NDRExportBatch.class);
		criteria.add(Restrictions.eq("automatic", startMode));
		if (!includeVoided)
			criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.desc("dateCreated"));
		return criteria.list();
	}
	
	public Item getItemByUuid(String uuid) {
		return (Item) getSession().createCriteria(Item.class).add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
}
