/*******************************************************************************
 * Copyright (c) 2012 Arvind Nanjundappa.
 *  
 *  All rights reserved. This program and the accompanying materials was created as 
 *  part of fulfilment of project requirement for OS II taught by Dr. Donggang Liu .
 ******************************************************************************/
package edu.uta.os.client;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import edu.uta.os.client.helper.Constants;
import edu.uta.os.client.helper.Message;

/**
 * AdvisorProcess is a subclass of Process class.It simulates the advisor
 * process responsible for making a random decision for incoming request post'd
 * by the student process.Also it's responsible for posting the decision to MQS
 * which is intended for the Notifier process.If there are no incoming messages
 * the process sleeps for 3 seconds and again poll's the MQS for incoming
 * messages.
 * 
 * @author Arvind Nanjundappa
 * 
 */
public class AdvisorProcess extends Process {
	private JFrame frmAdvisorProcess;
	private JTextArea incomingText;
	private JTextArea outgoingText;
	private JTextArea pingArea;
	private static boolean status;
	// Random function used to decide , weather to give permission to student or
	// not
	private Random decisionMaker = new Random();
	private JLabel lblPingResponses;

	/**
	 * Constructor used to initialise and create Advisor application.
	 */
	public AdvisorProcess() {
		initializeLookAndFeel();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					frmAdvisorProcess.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		while (status) {
			try {
				// Polling the MQS for messages after every 7 seconds
				Thread.sleep(7000);
				// Call to remote method to get messages from MQS
				Object[] Messages = getMessage(Constants.REQUEST);
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
	 * the look and feel of AdvisorProcess UI.Along to this on initial load the
	 * process makes rpc to fetch any message from MQ that is waiting for
	 * advisor process to come up.
	 */
	@Override
	public void initializeLookAndFeel() {
		frmAdvisorProcess = new JFrame();
		frmAdvisorProcess.setTitle("Advisor Process");
		frmAdvisorProcess.setBounds(100, 100, 778, 502);
		frmAdvisorProcess.setResizable(false);
		frmAdvisorProcess.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Setting it to absolute layout.
		frmAdvisorProcess.getContentPane().setLayout(null);

		JLabel lblIncomingMessages = new JLabel("Incoming Messages");
		lblIncomingMessages.setBounds(20, 21, 126, 14);
		frmAdvisorProcess.getContentPane().add(lblIncomingMessages);

		// Area responsible for displaying incoming requests.
		incomingText = new JTextArea();
		incomingText.setWrapStyleWord(true);
		incomingText.setBounds(20, 46, 306, 243);
		frmAdvisorProcess.getContentPane().add(incomingText);

		JLabel lblOutgoingMessages = new JLabel(
				"Outgoing Messages Post'd to Notifier via MQS");
		lblOutgoingMessages.setBounds(441, 21, 288, 14);
		frmAdvisorProcess.getContentPane().add(lblOutgoingMessages);

		// Area responsible for displaying decision made for incoming request's
		// from student.
		outgoingText = new JTextArea();
		outgoingText.setWrapStyleWord(true);
		outgoingText.setBounds(436, 46, 293, 243);
		frmAdvisorProcess.getContentPane().add(outgoingText);

		lblPingResponses = new JLabel("Ping Responses");
		lblPingResponses.setBounds(20, 308, 109, 14);
		frmAdvisorProcess.getContentPane().add(lblPingResponses);

		pingArea = new JTextArea();
		pingArea.setBounds(20, 333, 709, 119);
		frmAdvisorProcess.getContentPane().add(pingArea);

		Object[] Messages;
		try {
			//load properties from property file and set up for RPC calls
			initializePropertiesAndRPC();
			// Request any message from MQS which exists prior to Advisor start-up.
			Messages = getMessage(Constants.REQUEST);
			processMSG(Messages);
		} catch (Exception e) {
			e.printStackTrace();
		}
		status = true;
	}

	/**
	 * processMSG is a overridden method used to process a message that was
	 * received from MQ. A random generator is used here to decide if a student
	 * can take a course or not, once a decision is made it puts a message on MQ
	 * destined for Notifier process and eventually to student.
	 * 
	 * @param Messages
	 *            POJO representing an incoming message's / request's
	 */
	@Override
	public void processMSG(Object[] Messages) {
		int backlogMsg = Messages.length;

		// Check to see if there are any incoming messages from MQS
		if (Messages.length != 0) {
			int counter = 0;
			while (counter < backlogMsg) {
				final String[] realMsg = Messages[counter].toString()
						.split(";");
				// Creating the POJO to represent the Message
				final Message temp = new Message(realMsg[0].toString(),
						realMsg[1].toString(), realMsg[2].toString(),
						Long.parseLong(realMsg[3].toString()),
						realMsg[4].toString());
				final String Msg = realMsg[4].toString();
				final String[] msg = Msg.split(",");
				StringBuffer incomingMessages = new StringBuffer();
				incomingMessages.append(incomingText.getText() + "\n");
				incomingMessages.append(msg[0] + " requested for " + msg[1]);
				// Refresh the Advisor display with incoming Messages.
				incomingText.setText(incomingMessages.toString());
				incomingText.revalidate();
				incomingText.repaint();
				// Random Generator to take a decision on student request
				if (decisionMaker.nextBoolean()) {
					msg[2] = "Accepted";
				} else
					msg[2] = "Rejected";
				temp.setMsg(msg[0] + "," + msg[1] + "," + msg[2]);
				temp.setId(Calendar.getInstance().getTimeInMillis());
				temp.setSource(Constants.ADVISOR);
				temp.setDestination(Constants.NOTIFIER);
				temp.setType(Constants.NOTIFICATION);
				try {
					pushMessage(temp);
					/*
					 * Once the advisor receives a request, as said initially it
					 * is responsible to send MQS a acknowledge i.e delete
					 * message in our case
					 */
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
