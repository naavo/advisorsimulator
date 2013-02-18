/*******************************************************************************
 * Copyright (c) 2012 Arvind Nanjundappa.
 *  
 *  All rights reserved. This program and the accompanying materials was created as 
 *  part of fulfilment of project requirement for OS II taught by Dr. Donggang Liu .
 ******************************************************************************/
package edu.uta.os.client;

import java.awt.EventQueue;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import edu.uta.os.client.Process;
import edu.uta.os.client.helper.Constants;
import edu.uta.os.client.helper.Message;

/**
 * NotifierProcess is sub class of Process simulating the Notifier process
 * required by the project.It receives decision from Advisor via MQS and post's
 * the message to Student process via MQS.Work's on principal of polling
 * ,queries for new messages from MQS every 7 seconds as per the requirements.
 * 
 * @author Arvind Nanjundappa
 * 
 */
public class NotifierProcess extends Process {

	private JFrame frmNotificationProcess;
	private static boolean status;
	private JTextArea incomingText;
	private JTextArea outgoingText;
	private JTextArea pingArea;

	/**
	 * Constructor used to initialise and create the Notifier process.
	 * 
	 */
	public NotifierProcess() {
		initializeLookAndFeel();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					frmNotificationProcess.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		while (status) {
			try {
				// Polling the MQS for messages after every 7 seconds
				Thread.sleep(7000);
				// Call to method to get messages from MQS
				Object[] Messages = getMessage(Constants.NOTIFICATION);
				processMSG(Messages);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * initializeLookAndFeel is overridden method used by the constructor to set
	 * the look and feel of NotifierProcess UI.Along with this, on initial load
	 * the process makes rpc to fetch any message from MQ that is waiting for
	 * Notifier process to come up.
	 */
	@Override
	public void initializeLookAndFeel() {
		frmNotificationProcess = new JFrame();
		frmNotificationProcess.setTitle("Notification Process");
		frmNotificationProcess.setBounds(100, 100, 719, 520);
		frmNotificationProcess.setResizable(false);
		frmNotificationProcess.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmNotificationProcess.getContentPane().setLayout(null);

		// Text area representing incoming decisions made by Advisor
		incomingText = new JTextArea();
		incomingText.setWrapStyleWord(true);
		incomingText.setBounds(25, 34, 291, 279);
		frmNotificationProcess.getContentPane().add(incomingText);

		// Text area used to represent outgoing messages to student
		outgoingText = new JTextArea();
		outgoingText.setWrapStyleWord(true);
		outgoingText.setBounds(415, 34, 262, 279);
		frmNotificationProcess.getContentPane().add(outgoingText);

		JLabel lblIncomingMessages = new JLabel("Incoming Messages:");
		lblIncomingMessages.setBounds(25, 9, 118, 14);
		frmNotificationProcess.getContentPane().add(lblIncomingMessages);

		JLabel lblMessagesPostdTo = new JLabel(
				"Messages Post'd to Student via  MQS");
		lblMessagesPostdTo.setBounds(415, 9, 229, 14);
		frmNotificationProcess.getContentPane().add(lblMessagesPostdTo);

		JLabel lblPingMessages = new JLabel("Ping Responses");
		lblPingMessages.setBounds(25, 351, 134, 14);
		frmNotificationProcess.getContentPane().add(lblPingMessages);

		pingArea = new JTextArea();
		pingArea.setBounds(25, 377, 652, 68);
		frmNotificationProcess.getContentPane().add(pingArea);
		
		Object[] Messages;
		try {
			//load properties from property file and set up for RPC calls
			initializePropertiesAndRPC();
			// Polling MQS after process comes up
			Messages = getMessage(Constants.NOTIFICATION);
			processMSG(Messages);
		} catch (Exception e) {
			e.printStackTrace();
		}
		status = true;
	}

	/**
	 * processMSG is a overridden method used to process a message that was
	 * received from MQ. The message i.e the decision received is put on MQ
	 * destined for student process.
	 * 
	 * @param Messages
	 *            POJO representing an incoming message's / request's
	 */
	@Override
	public void processMSG(Object[] Messages) {

		int backlogMsg = Messages.length;

		if (Messages.length != 0) {
			int counter = 0;
			while (counter < backlogMsg) {
				String[] realMsg = Messages[counter].toString().split(";");

				final Message temp = new Message(realMsg[0].toString(),
						realMsg[1].toString(), realMsg[2].toString(),
						Long.parseLong(realMsg[3].toString()),
						realMsg[4].toString());
				final String Msg = realMsg[4].toString();
				final String[] msg = Msg.split(",");
				final StringBuffer incomingMessages = new StringBuffer();
				incomingMessages.append(incomingText.getText() + "\n");
				incomingMessages.append(msg[0] + " requested for " + msg[1]
						+ " which was " + msg[2]);
				incomingText.setText(incomingMessages.toString());
				incomingText.revalidate();
				incomingText.repaint();
				temp.setId(Calendar.getInstance().getTimeInMillis());
				temp.setSource(Constants.NOTIFIER);
				temp.setDestination(Constants.STUDENT);
				temp.setType(Constants.DECISSION);
				try {
					// Pushing Message onto MQ destined for Student process
					pushMessage(temp);
					deleteMessage(Long.parseLong(realMsg[3]));

				} catch (Exception e) {
					e.printStackTrace();
				}
				StringBuffer outgoingMessages = new StringBuffer();
				outgoingMessages.append(outgoingText.getText() + "\n");
				outgoingMessages.append(msg[0] + " -> " + msg[1] + " -> "
						+ msg[2]);
				outgoingText.setText(outgoingMessages.toString());
				outgoingText.revalidate();
				counter++;
			}
		} else {
			StringBuffer incomingMessages = new StringBuffer();
			incomingMessages.append(Calendar.getInstance().getTime()
					+ " No Messages for me on MQ @ this time");
			pingArea.setText(incomingMessages.toString());
			pingArea.revalidate();
			pingArea.repaint();
		}
	}
}
