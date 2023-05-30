package org.openmrs.module.extractionemr.page.controller;

import org.openmrs.module.extractionemr.ndrUtils.Utils;
import org.openmrs.ui.framework.page.PageModel;

public class CustomNdrPageController {
	
	public Object controller(PageModel model) {
		String lastNDRRunDate = Utils.getLastNDRDateString();
		if (lastNDRRunDate != null && !lastNDRRunDate.isEmpty()) {
			String[] dateOnly = lastNDRRunDate.trim().split(" ");
			if (dateOnly.length > 0) {
				model.addAttribute("lastNDRRunDate", dateOnly[0]);
			}
		}
		return null;
	}
	
}
