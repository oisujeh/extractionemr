package org.openmrs.module.extractionemr;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.extractionemr.model.NDRExport;
import org.openmrs.module.extractionemr.ndrfactory.NDRExtractor;
import org.openmrs.module.extractionemr.service.NdrExtractionService;
import org.openmrs.module.extractionemr.util.LoggerUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.bind.JAXBContext;

public class Consumer implements MessageListener {
	
	JAXBContext jaxbContext;
	
	NDRExtractor ndrExtractor;
	
	public Consumer() {
		try {
			jaxbContext = JAXBContext.newInstance("org.openmrs.module.extractionemr.model.ndr");
		}
		catch (Exception e) {
			LoggerUtils.write(Consumer.class.getName(), e.getMessage(), LoggerUtils.LogFormat.FATAL,
			    LoggerUtils.LogLevel.live);
		}
	}
	
	public static void initialize(UserContext userContext) {
		Context.openSession();
		if (userContext != null) {
			Context.setUserContext(userContext);
		} else {
			Context.setUserContext(Context.getUserContext());
		}
		Context.openSessionWithCurrentUser();
		Context.addProxyPrivilege(ExtractionemrConfig.MODULE_PRIVILEGE);
		Context.addProxyPrivilege(ExtractionemrConfig.MODULE_PRIVILEGE);
		Context.addProxyPrivilege(ExtractionemrConfig.MODULE_PRIVILEGE);
		Context.addProxyPrivilege("Get Patients");
		Context.addProxyPrivilege("Get Observations");
		Context.addProxyPrivilege("Get Encounters");
		Context.addProxyPrivilege("Get Concepts");
		Context.addProxyPrivilege("Get Users");
		Context.addProxyPrivilege("Get Identifier Types");
		Context.addProxyPrivilege("Manage Global Properties");
	}
	
	public void checkIfExportIsComplete() {
		try {
			NdrExtractionService ndrExtractionService = new NdrExtractionService(jaxbContext);
			initialize(null);
			ndrExtractionService.checkIfExportIsComplete();
			Context.clearSession();
			Context.closeSession();
		}
		catch (Exception e) {
			LoggerUtils.write(Consumer.class.getName(), e.getMessage(), LoggerUtils.LogFormat.FATAL,
			    LoggerUtils.LogLevel.live);
		}
	}
	
	@Override
	public void onMessage(Message message) {
		try {
			message.acknowledge();
			NdrExtractionService ndrExtractionService = new NdrExtractionService(jaxbContext);
			ActiveMQObjectMessage msg = (ActiveMQObjectMessage) message;
			NDRExport ndrExport = (NDRExport) msg.getObject();
			initialize(null);
			LoggerUtils.write(Consumer.class.getName(),
			    "processing " + ndrExport.getId() + "with batchID " + ndrExport.getPatientsList(),
			    LoggerUtils.LogFormat.INFO, LoggerUtils.LogLevel.live);
			System.out.println("executing export" + ndrExport.getId());
			ndrExtractionService.export(ndrExport);
			Context.clearSession();
			Context.closeSession();
		}
		catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void stopAllExports() {
		try {
			NdrExtractionService ndrExtractionService = new NdrExtractionService(jaxbContext);
			initialize(null);
			ndrExtractionService.pauseFile(null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
