/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.simplelabentry.web.dwr;

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;

public class LabResultListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer orderId;
	private Integer obsId;
	private Integer conceptId;
	private String result;
	
	public LabResultListItem() { }
	
	public LabResultListItem(Obs obs) {
		orderId = obs.getOrder().getOrderId();
		obsId = obs.getObsId();
		conceptId = obs.getConcept().getConceptId();
		result = getValueStringFromObs(obs);
	}
	
	public String toString() {
		return "result: " + orderId + "," + obsId + "," + conceptId + "," + result;
	}
	
	public static String getValueStringFromObs(Obs obs) {
		String result = null;
		ConceptDatatype dt = obs.getConcept().getDatatype();
		if (dt.isBoolean() || dt.isNumeric()) {
			result = obs.getValueNumeric() == null ? null : obs.getValueNumeric().toString();
		}
		else if (dt.isCoded()) {
			result = obs.getValueCoded() == null ? null : obs.getValueCoded().getConceptId().toString();
		}
		else if (dt.isDate()) {
			result = obs.getValueDatetime() == null ? null : Context.getDateFormat().format(obs.getValueDatetime());
		}
		else if (dt.isText()) {
			result = obs.getValueText();
		}
		else {
			result = obs.getValueAsString(Context.getLocale());
		}
		return result;
	}
	
	public static void setObsFromValueString(Obs obs, String value) {
		ConceptDatatype dt = obs.getConcept().getDatatype();
		if (dt.isBoolean() || dt.isNumeric()) {
			obs.setValueNumeric(value == null ? null : Double.valueOf(value));
		}
		else if (dt.isCoded()) {
			obs.setValueCoded(value == null ? null : Context.getConceptService().getConcept(Integer.parseInt(value)));
		}
		else if (dt.isDate()) {
			try {
				obs.setValueDatetime(value == null ? null : Context.getDateFormat().parse(value));
			}
			catch (ParseException e) {
				throw new RuntimeException("Unable to set date for value = " + value + " and obs = " + obs);
			}
		}
		else if (dt.isText()) {
			obs.setValueText(value);
		}
		else {
			throw new RuntimeException("Unable to set value of " + value + " for obs: " + obs);
		}
	}

	public boolean equals(Object obj) {
		if (obj instanceof LabResultListItem) {
			LabResultListItem ot = (LabResultListItem)obj;
			if (ot.getObsId() == null || getObsId() == null)
				return false;
			return ot.getObsId().equals(getObsId());
		}
		return false;
	}
	
	public int hashCode() {
		if (getObsId() != null)
			return 31 * getObsId().hashCode();
		else
			return super.hashCode();
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getObsId() {
		return obsId;
	}

	public void setObsId(Integer obsId) {
		this.obsId = obsId;
	}

	public Integer getConceptId() {
		return conceptId;
	}

	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
