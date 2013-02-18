/*******************************************************************************
 * Copyright (c) 2012 Arvind Nanjundappa.
 *  
 *  All rights reserved. This program and the accompanying materials was created as 
 *  part of fulfilment of project requirement for OS II taught by Dr. Donggang Liu .
 ******************************************************************************/
package edu.uta.os.client.helper;

/**
 * Message is a simple POJO which represents a message, it has components like
 * the source of message ,eventual recipient, message type
 * Request/Notification/Decision. A unique ID representing the message and
 * of-course the actual message for convenience have formatted the message to
 * have the format as shown below : Message Format: <Requestor
 * Name>,<Subject>,<Decision>
 * 
 * @author Arvind
 * 
 */
public class Message {
	// Represents Source of the Message
	private String source;
	// Represents the intended Destination of the message
	private String destination;
	// Represents the type of the message Request/Notifier/Decision
	private String type;
	// A unique Id identifying the message
	private long id;
	// The message to be transmitted always in CSForm
	// <Requestor>,<Subject>,<Decision>
	private String msg;

	/**
	 * Message A parametrised constructor used to build the POJO.
	 * 
	 * @param String
	 *            source
	 * @param String
	 *            destination
	 * @param String
	 *            type
	 * @param Long
	 *            id
	 * @param CS
	 *            String msg
	 */
	public Message(final String source, final String destination, final String type,final long id,
			final String msg) {
		super();
		this.source = source;
		this.destination = destination;
		this.type = type;
		this.id = id;
		this.msg = msg;
	}

	/**
	 * setSource () A setter method used for setting the source.
	 * 
	 * @param source
	 *            Address as string
	 */
	public void setSource(final String source) {
		this.source = source;
	}

	/**
	 * setDestination () A setter method used for setting the destination.
	 * 
	 * @param destination
	 *            Address as string
	 */
	public void setDestination(final String destination) {
		this.destination = destination;
	}

	/**
	 * setType () A setter method used for setting the type of message.
	 * 
	 * @param type
	 *            type of message as string
	 */
	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * setId () A setter method used for setting the Id of message.
	 * 
	 * @param Long
	 *            Id Unique identifier of the message
	 */
	public void setId(final long id) {
		this.id = id;
	}

	/**
	 * setMsg () A setter method used for setting the content of message.
	 * 
	 * @param String
	 *            message Comma separated string
	 */
	public void setMsg(final String msg) {
		this.msg = msg;
	}

	/**
	 * getSource () A getter message for the POJO used for retrieving the
	 * source.
	 * 
	 * @return String The Source of message
	 */
	public String getSource() {
		return source;
	}
	/**
	 * getDestination () A getter message for the POJO used for retrieving the
	 * destination.
	 * 
	 * @return String The Destination of message
	 */
	public String getDestination() {
		return destination;
	}
	/**
	 * getType () A getter message for the POJO used for retrieving the
	 * Type of message.
	 * 
	 * @return String The Type of message
	 */
	public String getType() {
		return type;
	}
	/**
	 * getId () A getter message for the POJO used for retrieving the
	 * unique identifier of the message.
	 * 
	 * @return Long The Id of the message
	 */
	public long getId() {
		return id;
	}
	/**
	 * getMsg () A getter message for the POJO used for retrieving the
	 * message.
	 * 
	 * @return CS String The content of message
	 */
	public String getMsg() {
		return msg;
	}

}
