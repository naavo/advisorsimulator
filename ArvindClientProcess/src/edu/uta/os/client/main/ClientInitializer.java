/*******************************************************************************
 * Copyright (c) 2012 Arvind Nanjundappa.
 *  
 *  All rights reserved. This program and the accompanying materials was created as 
 *  part of fulfilment of project requirement for OS II taught by Dr. Donggang Liu .
 ******************************************************************************/
package edu.uta.os.client.main;

import edu.uta.os.client.AdvisorProcess;
import edu.uta.os.client.NotifierProcess;
import edu.uta.os.client.StudentProcess;
import edu.uta.os.client.helper.Constants;

public class ClientInitializer {

	/**
	 * Launch the Notifier Application application.
	 * 
	 */
	public static void main(String[] args) {
		switch (args[0]) {
		case Constants.STUDENT:
			StudentProcess studentProcess = new StudentProcess();
			break;
		case Constants.ADVISOR:
			AdvisorProcess advisorProcess = new AdvisorProcess();
			break;
		case Constants.NOTIFIER:
			NotifierProcess notifierProcess = new NotifierProcess();
			break;
		}
	}
}
