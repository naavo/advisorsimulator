/*******************************************************************************
 * Copyright (c) 2012 Arvind Nanjundappa.
 *  
 *  All rights reserved. This program and the accompanying materials was created as 
 *  part of fulfilment of project requirement for OS II taught by Dr. Donggang Liu .
 ******************************************************************************/
package edu.uta.os.client;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import edu.uta.os.client.helper.Message;

/**
 * Process is a parent class containing the common functionalities of my clients
 * i.e StudentProcess,AdvisorProcess and NotifierProcess.I.e basically contains
 * logic used to fetch messages from MQ, Delete message to MQ, Insert messages
 * to MQ. Corresponding exposed methods on Web Service (my MQ) are
 * getMessage(),deleteMessage() and pushMessage().
 * 
 * @author Arvind Nanjundappa
 * 
 */
public abstract class Process {

	private Service service;
	private Call call;
	private URL serviceUrl;
	protected static Properties prop = new Properties();
	protected static String WEBSERVICE;

	/**
	 * initializeLookAndFeel() is a abstract method that needs to be implemented
	 * by subclass. The look and feel of different process will be different as
	 * they are destined for different purpose.
	 */
	public abstract void initializeLookAndFeel();

	/**
	 * processMSG() is abstract class, which would need to be implemented by
	 * subclass. Function should be responsible to process any received message
	 * and update various swing components based on the message.
	 * 
	 * @param Messages
	 *            [] POJO Array representing an incoming message's / request's
	 */
	public abstract void processMSG(final Object[] Messages);

	/**
	 * initializeProperties method is used to load the properties from external
	 * file.
	 * 
	 * @throws Exception
	 *             when RPC could not be initialised
	 */
	public void initializePropertiesAndRPC() throws Exception {
		// Initialising Properties from external properties file
		try {
			InputStreamReader propStream = new InputStreamReader(this
					.getClass().getResourceAsStream("config.properties"));
			prop.load(propStream);
			WEBSERVICE = prop.getProperty("WEBSERVICE");
			if (service == null) {
				service = new Service();
			}
			if (call == null) {
				call = (Call) service.createCall();
			}
			if (serviceUrl == null) {
				serviceUrl = new URL(WEBSERVICE);
			}
			call.setTargetEndpointAddress(serviceUrl);
		} catch (MalformedURLException e) {
			throw new Exception("Error creating service URL at " + WEBSERVICE
					+ e.getMessage());
		} catch (ServiceException e) {
			throw new Exception("Error creating service call: "
					+ e.getMessage());
		}
	}

	/**
	 * pushMessage() - method responsible for posting the message to MQS
	 * intended for the destination process
	 * 
	 * @param Message
	 *            Representing decision to be post'd to MQS
	 * @return boolean if message was post'd successfully or not
	 * @throws Exception
	 *             is thrown if the Remote procedure call could not be made.
	 */

	protected boolean pushMessage(final Message temp) throws Exception {
		boolean status = false;
		// Select operation to call
		call.setOperationName(new QName("http://soapinterop.org/",
				"pushMessage"));
		final Calendar cal = Calendar.getInstance();
		final String[] postMsg = new String[] { temp.getSource(),
				temp.getDestination(), temp.getType(),
				String.valueOf(cal.getTimeInMillis()), temp.getMsg() };

		try {
			call.invoke(new Object[] { postMsg });
			status = true;
		} catch (RemoteException e) {
			throw new Exception("Error invoking " + WEBSERVICE
					+ " while inserting message in " + temp.getSource()
					+ " process " + e.getMessage());

		}
		return status;
	}

	/**
	 * getMessage() : Method responsible to fetch any message's from the MQS
	 * intended for destination.Makes the call to Remote procedure via the
	 * webservice.
	 * 
	 * @param messageType
	 * @return Array Of Objects i.e the messages intended for the destination
	 *         process.
	 * @throws Exception
	 *             exception when rpc could not be performed
	 */
	protected Object[] getMessage(final String messageType) throws Exception {
		// Select operation to call i.e getMessage
		call.setOperationName(new QName("http://soapinterop.org/", "getMessage"));
		// Invoke remote call and get result
		Object[] results = null;
		try {
			results = (Object[]) call.invoke(new Object[] { messageType });
		} catch (RemoteException e) {
			throw new Exception("Error invoking " + WEBSERVICE
					+ " while fetching messages of type " + messageType
					+ e.getMessage());
		}
		return results;
	}

	/**
	 * deleteMessage() - Used by clients to delete message from MQS once the
	 * receiver has received and process'd a message
	 * 
	 * @param id
	 *            the Message ID which needs to be deleted from MQS a long
	 * @throws Exception
	 *             when rpc could not be performed with custom message
	 */
	protected void deleteMessage(final long id) throws Exception {
		// Select Remote procedure to be called i.e deleteMessage
		call.setOperationName(new QName("http://soapinterop.org/",
				"deleteMessage"));
		try {
			call.invoke(new Object[] { id });
		} catch (RemoteException e) {
			throw new Exception("Error invoking " + WEBSERVICE
					+ " while deleting messages" + e.getMessage());
		}
	}
}
