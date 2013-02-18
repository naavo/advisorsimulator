/*******************************************************************************
 * Copyright (c) 2012 Arvind Nanjundappa.
 *  
 *  All rights reserved. This program and the accompanying materials was created as 
 *  part of fulfilment of project requirement for OS II taught by Dr. Donggang Liu .
 ******************************************************************************/
package edu.uta.os.client;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.uta.os.client.helper.Constants;
import edu.uta.os.client.helper.Message;

/**
 * Class StudentProcess is a subclass of Process.It has a frame responsible for
 * taking input from a student like name & subject for which he/she needs to ask
 * permission for.Once a request is post'd to the advisor via MQS it can wait
 * for the advisor decision and displays the user with the decision once message
 * goes through Advisor,Notifier process. If not the user can request for
 * another subject permission.
 * 
 * @author Arvind Nanjundappa
 * 
 */
public class StudentProcess extends Process {
	private JFrame frmStudentProcess;
	private JTextField textField;
	private JTextField textField_1;
	private JTextArea textArea;
	private String name, subject;
	private JButton btnRequestAdvisor;
	private static boolean status;
	private final Action action = new SwingAction();

	/**
	 * Constructor used to initialise and create Student application.
	 * 
	 */
	public StudentProcess() {
		initializeLookAndFeel();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					frmStudentProcess.setVisible(true);
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
				Object[] Messages = getMessage(Constants.DECISSION);
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
	 * the look and feel of StudentProcess UI.Along to this on initial load the
	 * process makes rpc to fetch any message from MQ that is waiting for
	 * advisor process to come up.
	 */
	@Override
	public void initializeLookAndFeel() {
		frmStudentProcess = new JFrame();
		frmStudentProcess.setTitle("Student Process");
		frmStudentProcess.setResizable(false);
		frmStudentProcess.setBounds(100, 100, 644, 381);
		frmStudentProcess.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmStudentProcess.getContentPane().setLayout(null);

		JLabel lblName = new JLabel("Name");
		lblName.setBounds(10, 59, 65, 14);
		frmStudentProcess.getContentPane().add(lblName);

		JLabel lblPermissionFor = new JLabel("Permission for");
		lblPermissionFor.setBounds(10, 84, 91, 14);
		frmStudentProcess.getContentPane().add(lblPermissionFor);

		textField = new JTextField();
		textField.setBounds(121, 56, 137, 20);
		frmStudentProcess.getContentPane().add(textField);
		textField.setColumns(10);

		textField_1 = new JTextField();
		textField_1.setBounds(121, 81, 137, 20);
		frmStudentProcess.getContentPane().add(textField_1);
		textField_1.setColumns(10);

		btnRequestAdvisor = new JButton("Request Advisor");
		btnRequestAdvisor.setAction(action);
		btnRequestAdvisor.setBounds(121, 125, 137, 23);
		frmStudentProcess.getContentPane().add(btnRequestAdvisor);

		JSeparator separator = new JSeparator();
		separator.setBounds(245, 11, 0, 242);
		frmStudentProcess.getContentPane().add(separator);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		textArea.setBounds(309, 54, 319, 228);
		textArea.setEnabled(false);
		frmStudentProcess.getContentPane().add(textArea);

		JLabel lblMessageFromAdvisor = new JLabel("Message From Advisor");
		lblMessageFromAdvisor.setBounds(309, 32, 137, 14);
		frmStudentProcess.getContentPane().add(lblMessageFromAdvisor);

		Object[] Messages;
		try {
			//load properties from property file and set up for RPC calls
			initializePropertiesAndRPC();
			// Initial polling of MQS for any messages ,just after process start's
			Messages = getMessage(Constants.DECISSION);
			processMSG(Messages);
		} catch (Exception e) {
			e.printStackTrace();
		}
		status = true;

	}

	/**
	 * processMSG() function responsible to process any incoming message for
	 * Student Process and display on the console of student process..
	 * 
	 * @param Messages
	 *            POJO representing an incoming message's / request's
	 * */
	@Override
	public void processMSG(Object[] Messages) {
		int backlogMsg = Messages.length;

		if (Messages.length != 0) {
			int counter = 0;
			while (counter < backlogMsg) {
				final String[] realMsg = Messages[counter].toString()
						.split(";");
				final String Msg = realMsg[4].toString();
				final String[] msg = Msg.split(",");
				if (msg[0].equalsIgnoreCase(name)) {
					StringBuffer temp = new StringBuffer();
					temp.append(textArea.getText() + "\n");
					temp.append(msg[0] + " requested for " + msg[1]
							+ " which was " + msg[2]);
					textArea.setText(temp.toString());
					textArea.revalidate();
					try {
						deleteMessage(Long.parseLong(realMsg[3]));
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
				counter++;
			}
		}
	}

	/**
	 * Class representing the action needed to perform on mouse click of button
	 * REQUEST on UI. i.e in our case make a rpc call to put a message on MQ
	 * destined for the AdvisorProcess
	 * 
	 * @author Arvind
	 * 
	 */
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "Request");
			putValue(SHORT_DESCRIPTION,
					"Click to Request for Permission from Advisor");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {

			name = textField.getText();
			subject = textField_1.getText();
			btnRequestAdvisor.validate();
			if (!subject.isEmpty()) {
				try {
					StringBuffer buffer = new StringBuffer();
					buffer.append(name + "," + subject + ",pending");
					Message temp = new Message(Constants.STUDENT,
							Constants.ADVISOR, Constants.REQUEST,
							Long.MIN_VALUE, buffer.toString());
					pushMessage(temp);
					textField.setEnabled(false);
					textField_1.setEnabled(true);
					textField.revalidate();
					textField_1.setText("");
					textField_1.revalidate();
				} catch (final Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
