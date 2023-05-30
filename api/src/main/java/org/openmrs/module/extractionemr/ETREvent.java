package org.openmrs.module.extractionemr;

import org.openmrs.module.extractionemr.model.NDRExport;
import org.springframework.jms.core.JmsTemplate;

public class ETREvent {
	
	JmsTemplate jmsTemplate;
	
	public ETREvent(final JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
	
	public void send(final NDRExport ndrExport) {
		jmsTemplate.convertAndSend(ndrExport);
	}
}
