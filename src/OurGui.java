
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.SwingConstants;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextArea;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTabbedPane;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.JInternalFrame;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

public class OurGui extends JFrame {

	private JPanel contentPane;
	public JTextField textField;
	private JButton btnNewButton;
	private JTextArea textArea;
	public JTextArea textArea_1;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private String sendTo = "/ALL";
	public static List<String> nodes;
	private String chats = "";
	
	
	 // The host to connect to. Set this to localhost when using the audio interface tool.
    private static String SERVER_IP = "netsys.ewi.utwente.nl"; //"127.0.0.1";
    // The port to connect to. 8954 for the simulation server.
    private static int SERVER_PORT = 8954;
    // The frequency to use.
    private static int frequency = 20553;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OurGui frame = new OurGui(new MyProtocol(SERVER_IP, SERVER_PORT, frequency));
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public OurGui(MyProtocol m) {
		nodes = new ArrayList<String>();
		this.setName("Chat Box -->" + m.getName());
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 749, 560);
		contentPane = new JPanel();
		contentPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		contentPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		
		textField = new JTextField();
		
		textField.setPreferredSize(new Dimension(25, 22));
		textField.setHorizontalAlignment(SwingConstants.LEFT);
		textField.setForeground(Color.BLUE);
		textField.setBackground(Color.WHITE);
		textField.setFont(new Font("Comic Sans MS", Font.PLAIN, 13));
		textField.setColumns(10);
		
		btnNewButton = new JButton("Send");
		
		
		
		
		JRadioButton rdbtnNewRadioButton = new JRadioButton("0");
		buttonGroup.add(rdbtnNewRadioButton);
		rdbtnNewRadioButton.setFont(new Font("Comic Sans MS", Font.PLAIN, 13));
		rdbtnNewRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendTo = "/0";
			}
		});
		
		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("1");
		rdbtnNewRadioButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendTo = "/1";
			}
		});
		buttonGroup.add(rdbtnNewRadioButton_1);
		rdbtnNewRadioButton_1.setFont(new Font("Comic Sans MS", Font.PLAIN, 13));
		
		JRadioButton rdbtnNewRadioButton_2 = new JRadioButton("2");
		rdbtnNewRadioButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendTo = "/2";
			}
		});
		
		buttonGroup.add(rdbtnNewRadioButton_2);
		rdbtnNewRadioButton_2.setFont(new Font("Comic Sans MS", Font.PLAIN, 13));
		
		JRadioButton rdbtnNewRadioButton_3 = new JRadioButton("3");
		rdbtnNewRadioButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendTo = "/3";
			}
		});
		buttonGroup.add(rdbtnNewRadioButton_3);
		rdbtnNewRadioButton_3.setFont(new Font("Comic Sans MS", Font.PLAIN, 13));
		
		JRadioButton rdbtnNewRadioButton_4 = new JRadioButton("All");
		rdbtnNewRadioButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendTo = "/ALL";
			}
		});
		
		rdbtnNewRadioButton_4.setSelected(true);
		buttonGroup.add(rdbtnNewRadioButton_4);
		
		
//		if (m.getName() == 0) {
//			rdbtnNewRadioButton.setVisible(false);
//		} else if (m.getName() == 1){
//			rdbtnNewRadioButton_1.setVisible(false);
//		} else if (m.getName() == 2) {
//			rdbtnNewRadioButton_2.setVisible(false);
//		} else {
//			rdbtnNewRadioButton_3.setVisible(false);
//		} 
		
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addComponent(textField, GroupLayout.PREFERRED_SIZE, 386, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rdbtnNewRadioButton)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(rdbtnNewRadioButton_1)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rdbtnNewRadioButton_2)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rdbtnNewRadioButton_3)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(rdbtnNewRadioButton_4)
					.addGap(148))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
						.addComponent(rdbtnNewRadioButton)
						.addComponent(rdbtnNewRadioButton_1)
						.addComponent(rdbtnNewRadioButton_2)
						.addComponent(rdbtnNewRadioButton_3)
						.addComponent(rdbtnNewRadioButton_4))
					.addContainerGap())
		);
		panel.setLayout(gl_panel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		JTextPane txtpnWelcomeToThis = new JTextPane();
		txtpnWelcomeToThis.setEditable(false);
		txtpnWelcomeToThis.setText("Welcome to this chat room!!\r\n\r\nPlease follow the rules or else you'll get banned!!!!!\r\nRule 1: No Spoilers\r\nRule 2: Do not advertise\r\n");
		txtpnWelcomeToThis.setForeground(Color.BLUE);
		txtpnWelcomeToThis.setFont(new Font("Comic Sans MS", Font.BOLD | Font.ITALIC, 17));
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 720, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 383, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(scrollPane_2, GroupLayout.PREFERRED_SIZE, 315, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addComponent(txtpnWelcomeToThis, GroupLayout.PREFERRED_SIZE, 314, GroupLayout.PREFERRED_SIZE))))
					.addGap(84))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 455, GroupLayout.PREFERRED_SIZE)
							.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(scrollPane_2, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(txtpnWelcomeToThis)))
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
		);
		
		textArea_1 = new JTextArea();
		textArea_1.setEditable(false);
		textArea_1.setForeground(Color.RED);
		textArea_1.setFont(new Font("Comic Sans MS", Font.PLAIN, 13));
		scrollPane_2.setViewportView(textArea_1);
		
		textArea = new JTextArea();
		textArea.setForeground(Color.BLUE);
		textArea.setEditable(false);
		textArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 13));
		scrollPane.setViewportView(textArea);
		contentPane.setLayout(gl_contentPane);
		
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String temp = sendTo + " " + textField.getText();
                String splitted = temp.split(" ")[0];
                if (splitted.length() == 2) {
                    int dest = -1;
                    try {
                        dest = Integer.parseInt(splitted.substring(1,2));
                    } catch (NumberFormatException ae) {
                        System.out.println("wrong destination, typ /0-/3");
                    }
                    if (dest >= 0 && dest <= 3) {
                        if (dest == m.getName()) {
                            writeChat("to yourself: " + temp.substring(3), dest, false);
                        } else {
                            if (m.getRouting().keySet().contains(dest)) {
                                m.sendToTCP(dest, temp.substring(3));
                                writeChat(temp.substring(3), dest, true);
                                textField.setText("");
                            } else {
                                System.out.println("Unknown destination");
                            }
                        }
                    }
                } else if (splitted.equals("/ALL")) {
                    for (Integer key: m.getRouting().keySet()) {
                        if (key != m.getName()) {
                            m.sendToTCP(key, temp.substring(5));
                            
                        }
                        textField.setText("");
                        
                    }
					textArea.append("You to all: " + temp.substring(5) + "\r\n");
                } else if (splitted.equals("/EXIT")) {
                    System.exit(0);
                } else {
                    System.out.println("invalid command");
                }
				};
			});
		
		
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String temp =sendTo + " " +  textField.getText();
		        String splitted = temp.split(" ")[0];
		        
		        if (splitted.length() == 2) {
		            int dest = -1;
		            try {
		                dest = Integer.parseInt(splitted.substring(1,2));
		            } catch (NumberFormatException ae) {
		                System.out.println("wrong destination, typ /0-/3");
		            }
		            if (dest >= 0 && dest <= 3) {
		                if (dest == m.getName()) {
		                    writeChat("to yourself: " + temp.substring(3), dest, false);
		                } else {
		                    if (m.getRouting().keySet().contains(dest)) {
		                        m.sendToTCP(dest, temp.substring(3));
		                        writeChat(temp.substring(3), dest, true);

		                        textField.setText("");
		                    } else {
		                        System.out.println("Unknown destination");
		                    }
		                }
		            }
		        } else if (splitted.equals("/ALL")) {
		            for (Integer key: m.getRouting().keySet()) {
		                if (key != m.getName()) {
		                    m.sendToTCP(key, temp.substring(5));
                        
		                }
		                
		                textField.setText("");
		            }
					textArea.append("You to all: " + temp.substring(5) + "\r\n");
		        } else if (splitted.equals("/EXIT")) {
		            System.exit(0);
		        } else {
		        	System.out.println(sendTo);
		            System.out.println("invalid command");
		        }
				};
				});
		
		
		
	}
	
	
	public synchronized void writeChat(String message, int name, boolean send) {
		chats = "";
		if (send) {
		    chats += "You to " + name + ": " + message;
		}else {
		    chats += ">> " + name + ": " + message;
		}
        textArea.append(chats + "\r\n");
	}
}
