/*******************************************************************************
 * Copyright (c) 2012 Arvind Nanjundappa.
 * 
 *  All rights reserved. This program and the accompanying materials was created as 
 *  part of fulfilment of project requirement for OS II taught by Dr. Donggang Liu .
 ******************************************************************************/
package arvind.mq;

/**
 * Constants Static Class , place holder for constants used in the process.
 * 
 * @author Arvind
 * 
 */
public class Constants {
	/**
	 * Represent's the type of Message : Student Advisor via MQS
	 */
	public final static String REQUEST = "REQUEST";
	/**
	 * Represent's the type of Message : Student Advisor to Notifier via MQS
	 */
	public final static String NOTIFICATION = "NOTIFICATION";
	/**
	 * Represent's the type of Message : Notifier to Student via MQS
	 */
	public final static String DECISSION = "DECISSION";
	/**
	 * Place holder for representing source or Destination process : Student Process
	 */
	public final static String STUDENT = "STUDENT";
	/**
	 * Place holder for representing source or Destination process : Advisor process
	 */
	public final static String ADVISOR = "ADVISOR";
	/**
	 * Place holder for representing source or Destination process : Notifier process
	 */
	public final static String NOTIFIER = "NOTIFIER";
	/**
	 * The URL of the back-up file
	 */
	public static final String FILE_URL = "C:/subjects/OS-2/Project 2/msg_bckup.txt";

}
