package org.openmrs.module.simplelabentry.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.simplelabentry.SimpleLabEntryService;

public class SimpleLabEntryUtil { 

	private static Log log = LogFactory.getLog(SimpleLabEntryUtil.class);
	public static String REPLACE_TREATMENT_GROUP_NAMES = "PEDIATRIC=PEDI,FOLLOWING=FOL,GROUP =,PATIENT TRANSFERRED OUT=XFER,PATIENT DIED=DIED";
	
	public static SimpleLabEntryService getSimpleLabEntryService() { 
		return (SimpleLabEntryService) Context.getService(SimpleLabEntryService.class);
	}
	
	/**
	 * Gets the lab order type associated with the underlying lab order type global property.
	 * 
	 * @return
	 */
	public static OrderType getLabOrderType() { 		
		return (OrderType) getGlobalPropertyValue("simplelabentry.labOrderType");				
	}	
	
	public static PatientIdentifierType getPatientIdentifierType() { 
		return (PatientIdentifierType) getGlobalPropertyValue("simplelabentry.patientIdentifierType");
	}

	public static Program getProgram() { 
		return (Program) getGlobalPropertyValue("simplelabentry.programToDisplay");
	}

	public static ProgramWorkflow getWorkflow() { 
		return (ProgramWorkflow) getGlobalPropertyValue("simplelabentry.workflowToDisplay");
	}
	
	public static List<Program> getLabReportPrograms() { 
		List<Program> programs = new LinkedList<Program>();		
		programs.add(Context.getProgramWorkflowService().getProgramByName("HIV PROGRAM"));
		//programs.add(Context.getProgramWorkflowService().getProgramByName("PMTCT PROGRAM"));
		programs.add(Context.getProgramWorkflowService().getProgramByName("PEDIATRIC HIV PROGRAM"));
		//programs.add(Context.getProgramWorkflowService().getProgramByName("TUBERCULOSIS PROGRAM"));	
		return programs;
	}	
	
	/**
	 * Gets the lab order type associated with the underlying lab order type global property.
	 * 
	 * FIXME Obviously this is a hack, but it's better than having the code to get these properties
	 * copied in different locations.
	 * 
	 * @return
	 */
	public static Object getGlobalPropertyValue(String property) { 
		
		// Retrieve proper OrderType for Lab Orders
		Object object = null;
		String identifier = 
			Context.getAdministrationService().getGlobalProperty(property);

		try { 
			if ("simplelabentry.labOrderType".equals(property)) { 
				object = (OrderType)
					Context.getOrderService().getOrderType(Integer.valueOf(identifier));
			}
			else if ("simplelabentry.programToDisplay".equals(property)) { 
				object = (Program)
					Context.getProgramWorkflowService().getProgramByName(identifier);
			}
			else if ("simplelabentry.labTestEncounterType".equals(property)) { 
				object = (EncounterType)
					Context.getEncounterService().getEncounterType(Integer.valueOf(identifier));
			}
			else if ("simplelabentry.patientHealthCenterAttributeType".equals(property)) { 
				object = (PersonAttributeType)
					Context.getPersonService().getPersonAttributeType(Integer.valueOf(identifier));
			}
			else if ("simplelabentry.patientIdentifierType".equals(property)) { 
				object = (PatientIdentifierType)
					Context.getPatientService().getPatientIdentifierType(Integer.valueOf(identifier));
			}
			else if ("simplelabentry.workflowToDisplay".equals(property)) { 
				object = (ProgramWorkflow) SimpleLabEntryUtil.getProgram().getWorkflowByName(identifier);
			}
						
		}
		catch (Exception e) {
			log.error("error: ", e);
			
		}
			
		if (object == null) {
			throw new RuntimeException("Unable to retrieve object with identifier <" + identifier + ">.  Please specify an appropriate value for global property '" + property + "'");
		}
		
		return object;
	}	
	
	
	public static Cohort getCohort(List<Encounter> encounters) { 		
		// Get cohort of patients from encounters
		Cohort patients = new Cohort();
		for (Encounter encounter : encounters) { 
			patients.addMember(encounter.getPatientId());
		}			
		return patients;
	}
	
	
	/**
	 * This is required because columns need to be in a specific order.
	 * TODO Move this to a global property at some point
	 * @return
	 */
	public static List<Concept> getLabReportConcepts() { 
		List<Concept> concepts = new LinkedList<Concept>();
		concepts.add(Context.getConceptService().getConcept(5497)); // CD4 (5497)
		concepts.add(Context.getConceptService().getConcept(730)); // CD4% (730)
		concepts.add(Context.getConceptService().getConcept(653)); // SGOT (653)
		concepts.add(Context.getConceptService().getConcept(654)); // SGPT (654)
		concepts.add(Context.getConceptService().getConcept(790)); // Cr (790)
		concepts.add(Context.getConceptService().getConcept(1017)); // MCHC (1017)
		concepts.add(Context.getConceptService().getConcept(678)); // WBC (678)
		concepts.add(Context.getConceptService().getConcept(3059)); // Gr (3059)
		concepts.add(Context.getConceptService().getConcept(3060)); // Gr% (3060)
		concepts.add(Context.getConceptService().getConcept(952)); // ALC (952)
		concepts.add(Context.getConceptService().getConcept(1021)); // Ly% (1021)
		concepts.add(Context.getConceptService().getConcept(729)); // PLTS (729)
		concepts.add(Context.getConceptService().getConcept(856)); // Viral Load (856)		
		//concepts.add(Context.getConceptService().getConcept(3055)); // Ur (3055)
		//concepts.add(Context.getConceptService().getConcept(1015)); // HCT (1015)
		//concepts.add(Context.getConceptService().getConcept(21)); // HB (21))
		return concepts;
	}
	
	/**
	 * 
	 * @param encounters
	 * @return
	 */
	public static Map<Integer, String> getTreatmentGroupCache(Cohort patients) { 
		Map<Integer, String> treatmentGroupCache = new HashMap<Integer, String>();
		
		if (!patients.isEmpty()) { 		
			// Loop over every program - does not do PMTCT PROGRAM because  
			for (Program program : SimpleLabEntryUtil.getLabReportPrograms()) { 
				// Get patient programs / treatment groups for all patients
				Map<Integer, PatientProgram> patientPrograms = 
					Context.getPatientSetService().getPatientPrograms(patients, program);
				
				for(PatientProgram patientProgram : patientPrograms.values()) { 
					
					// We only need to lookup the treatment group in the case that a 
					// patient does not already have a treatment group placed in the cache
					String treatmentGroup = 
						treatmentGroupCache.get(patientProgram.getPatient().getPatientId());
					
					if (treatmentGroup == null) { 
						// FIXME Hack to get the treatment group for either HIV PROGRAM, PEDIATRIC PROGRAM, or TUBERCULOSIS PROGRAM
						ProgramWorkflow workflow = program.getWorkflowByName("TREATMENT GROUP");
						if (workflow == null) workflow = program.getWorkflowByName("TUBERCULOSIS TREATMENT GROUP");
						if (workflow == null) continue;	// if we can't find a workflow at this point we just move to the next patient
						
						// Get the patient's current state based 						
						PatientState patientState = patientProgram.getCurrentState(workflow);	
						if (patientState != null) { 					
							// TODO This needs to be more generalized since not everyone will use the Rwanda
							// convention for naming groups
							// Show only the group number
							String value = patientState.getState().getConcept().getDisplayString();
							
							if (value != null) {
								value = SimpleLabEntryUtil.replace(value, REPLACE_TREATMENT_GROUP_NAMES);				
							}
							treatmentGroupCache.put(patientProgram.getPatient().getPatientId(), value);
						}			
					}
				}		
			}
		}
		return treatmentGroupCache;
	}

	
	/**
	 * Remove all unwanted words from the given string.
	 * 
	 * @param str
	 * @param unwanteds
	 * @return
	 */
	public static String remove(String str, String removeWords) { 		
		if (removeWords != null) { 
			for (String remove : removeWords.split(",")) {
				if (remove != null) { 
					str = str.replace(remove, "");
				}
			}
		}
		return str.trim();
		
	}
	
	/**
	 * Remove all unwanted words from the given string.
	 * 
	 * TODO Should be handled by a regular expression
	 * 
	 * @param str
	 * @param replaceWords
	 * @return
	 */
	public static String replace(String str, String replaceWords) { 		
		if (replaceWords != null) { 
			// replaceMapping: oldWord=newWord
			for (String replaceMapping : replaceWords.split(",")) {				
				String [] replaceArray = replaceMapping.split("=");
				if (replaceArray[0] != null) { 					
					if (replaceArray.length >= 2) { 
						str = str.replace(replaceArray[0], replaceArray[1]);
					}
					else { 
						str = str.replace(replaceArray[0], "");
					}
				}
			}
		}
		return str.trim();
		
	}
		
	
}