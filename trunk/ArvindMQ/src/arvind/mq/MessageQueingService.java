/*******************************************************************************
 * Copyright (c) 2012 Arvind Nanjundappa.
 * 
 *  All rights reserved. This program and the accompanying materials was created as 
 *  part of fulfilment of project requirement for OS II taught by Dr. Donggang Liu .
 ******************************************************************************/

package arvind.mq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This Class represent the Messaging service deployed on Tomcat with help of
 * axis. It is responsible for simulating the Message-Queing-System.<br>
 * Has the methods which could be accessed by clients via RPC. The exposed
 * methods are pushMessage (responsible for adding message onto que), getMessage
 * (responsible for any interacting parties to retrieve messages),
 * deleteMessage(Responsible to remove message from the Que.Note: For simplicity
 * its the responsibility of the Client to call deleteMessage once they are done
 * receiving /processing it.)
 * 
 * @author Arvind Nanjundappa
 * 
 */

public class MessageQueingService {
	// My Que data structure a List of POJO's Message
	private List<Message> messageQue = new ArrayList<Message>();
	private Properties prop = new Properties();
	private int queSize = 0;
	private File bckUp;
	private final static Logger LOGGER = Logger
			.getLogger(MessageQueingService.class);

	/**
	 * A simple constructor of MQS performing the function of loading any Backed
	 * up messages onto the MQS for transmission.
	 */
	public MessageQueingService() {
		LOGGER.debug("Control in MessageQueingService() about to load properties from external file");
		try {
			InputStreamReader propStream = new InputStreamReader(this
					.getClass().getResourceAsStream("config.properties"));
			prop.load(propStream);
		} catch (IOException e) {
			LOGGER.error("Unable to read from properties file, to load location of back up file"
					+ e.getMessage());

		}
		File bckupFile = new File(prop.getProperty("BACKUP_FILE_LOCATION"));
		if (bckupFile.exists()) {
			LOGGER.debug("Undelivered Messages exist's on system @ "
					+ Calendar.getInstance().getTime()
					+ " loading them onto the message Que for processal");
			loadMessages(bckupFile);
			LOGGER.info("All Undelivered messages have been loaded");
			bckupFile.delete();
		}
		bckUp = new File(prop.getProperty("BACKUP_FILE_LOCATION"));
	}

	/**
	 * loadMessages() : Is a method used by message Service to load any messages
	 * that was received by the MQS but not processed to destination due to
	 * server going down or during Initial Boot of server.
	 * 
	 * @param File
	 *            bckupFile
	 */
	private void loadMessages(final File bckupFile) {
		LOGGER.debug("Control in loadMessages() : Begining to load messages");
		try {
			// Open the file that is the first
			// command line parameter
			final FileInputStream fstream = new FileInputStream(bckupFile);
			// Get the object of DataInputStream
			final DataInputStream in = new DataInputStream(fstream);
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				final String[] realMsg = strLine.split(";");
				final Message temp = new Message(realMsg[0], realMsg[1],
						realMsg[2], Long.parseLong(realMsg[3].toString()),
						realMsg[4]);
				messageQue.add(temp);
				queSize++;
			}
			in.close();
		} catch (final FileNotFoundException e) {
			LOGGER.error("Unprocessed Messages exist, but unable to open it / file not found on path"
					+ e.getMessage());
		} catch (final NumberFormatException e) {
			LOGGER.error("Unprocessed Messages exist, error reading from back up file ");
		} catch (final IOException e) {
			LOGGER.error("Unprocessed Messages exist, back up file has been corrupted ");
		}
	}

	/**
	 * getQueSize() A method used by remote client's to retrieve number of
	 * message's in the que.(Method for development purpose only.)
	 * 
	 * @return int number of elements in the que
	 */
	public int getQueSize() {
		LOGGER.debug("Control in getQueSize()");
		return queSize;
	}

	/**
	 * pushMessage() A method used for adding the message onto the Que and also
	 * backing up the message into a file.
	 * 
	 * @param A
	 *            message in form of Array of string's
	 * @return boolean whether the process was sucessfull or not.
	 */
	public boolean pushMessage(final Object[] msg) {
		LOGGER.debug("Control in pushMessage()");
		boolean status = false;
		final Message tempPushMsg = new Message(msg[0].toString(),
				msg[1].toString(), msg[2].toString(), Long.parseLong(msg[3]
						.toString()), msg[4].toString());
		LOGGER.info("pushMessage(): pushing Message" + tempPushMsg.toString());
		messageQue.add(tempPushMsg);
		if (bckUpMsg(tempPushMsg)) {
			status = true;
			LOGGER.debug("The Message has been wriiten onto the file"
					+ tempPushMsg.toString());
		}
		queSize++;
		return status;
	}

	/**
	 * getMessage() A Method used by the remote clients to retrieve the messages
	 * those are destined to them.
	 * 
	 * @param type
	 *            String represents message type
	 * @return An array of Objects representing the messages, which was
	 *         requested by remote client's
	 */
	public Object[] getMessage(final String type) {
		LOGGER.debug("Control in getMessage()");
		final List<String> msgList = new ArrayList<String>();
		for (Message msg : messageQue) {
			if (msg.getType().equalsIgnoreCase(type)) {
				LOGGER.debug("getMessage() :  Message was found. Returning to Client.");
				final StringBuffer temp = new StringBuffer();
				temp.append(msg.getSource() + ";" + msg.getDestination() + ";"
						+ msg.getType() + ";" + msg.getId() + ";"
						+ msg.getMsg());
				msgList.add(temp.toString());
			}
		}
		return (msgList.toArray());
	}

	/**
	 * deleteMessage() A method used to delete message from Que, it also is
	 * required to delete the backed up message from the file.
	 * 
	 * @param Long
	 *            Id a unique identifier of the message.
	 * @return boolean representing if the operation was successfull or not.
	 */
	public boolean deleteMessage(final Long id) {
		LOGGER.debug("Control in deleteMessage()");
		boolean status = false;
		for (Message msg : messageQue) {
			if (msg.getId() == id) {
				if (removeBckUp(id)) {
					messageQue.remove(msg);
					status = true;
					break;
				}
			}
		}
		if (status) {
			LOGGER.debug("Message Deleted from back up file and the MQS");
			queSize--;
		}
		return status;
	}

	/**
	 * removeBckUp() Method used to delete the message from the back up file.Not
	 * exposed to remote client's directly.
	 * 
	 * @return boolean representing status
	 */
	private boolean removeBckUp(final long deleteMsgId) {
		LOGGER.debug("Control in removeBckUp()");
		try {
			final File tmp = File.createTempFile("tmp", "txt");
			final BufferedReader br = new BufferedReader(new FileReader(bckUp));
			final BufferedWriter bw = new BufferedWriter(new FileWriter(tmp));

			String s;
			final String actualValue = String.valueOf(deleteMsgId);
			while ((s = br.readLine()) != null) {
				final String tokens[] = s.split(";");
				final String id = tokens[3];
				if (!actualValue.equals(id)) {
					bw.write(s + "\n");
				}
			}
			br.close();
			bw.close();
			if (bckUp.delete())
				tmp.renameTo(bckUp);
			LOGGER.debug("Control in removeBckUp(): Deleted Message and temporary rewrite complete");
		} catch (final IOException e) {
			LOGGER.error("Control in removeBckUp(): Error while deleting message from back up file"
					+ e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * bckUpMsg Method used to back up the messages onto the file.Not exposed to
	 * remote client's.
	 * 
	 * @param Message
	 *            required to be backe'd up into the file
	 * @return boolean if operation was successful
	 */
	private boolean bckUpMsg(final Message msg) {
		{
			LOGGER.debug("Control in bckUpMsg()");
			try {
				final FileWriter writer = new FileWriter(bckUp, true);
				final StringBuffer temp = new StringBuffer();
				temp.append(msg.getSource() + ";" + msg.getDestination() + ";"
						+ msg.getType() + ";" + String.valueOf(msg.getId())
						+ ";" + msg.getMsg() + "\n");
				writer.write(temp.toString());
				writer.flush();
				LOGGER.debug("Control in bckUpMsg : Completed writing the message onto the back up file");
				writer.close();
			} catch (final IOException e) {
				LOGGER.error("ERROR : While writing the received message onto file");
				return false;
			}
		}
		return true;
	}
}
