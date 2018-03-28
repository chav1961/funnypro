package chav1961.funnypro;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import chav1961.funnypro.FrameUtils.MenuIcon;
import chav1961.funnypro.core.exceptions.FProException;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

class JScreen extends FrameTemplate {
	private static final long 		serialVersionUID = 3667603693613366899L;
	
	private final FunnyProEngine	fpe;
	private final JTextArea			log = new JTextArea();
	private final JTextArea			console = new JTextArea();
	private File					currentDir = new File("./");
	
	JScreen(final FunnyProEngine fpe) throws IOException {
		super("(c) 2017, Alexander V.Chernomirdin aka chav1961, Funny Prolog V.R=0.0.1",null);
		final JSplitPane	split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		final JScrollPane	scroll = new JScrollPane(log);
		
		this.fpe = fpe;
		
		setMenu(FrameUtils.menuBar(
				FrameUtils.menu("file","File",
						FrameUtils.menu("file.new","New",0,new FrameUtils.ToolTip("Start Funny Prolog with empty fact/rule base")),
						FrameUtils.standardMenuItem(FrameUtils.StandardMenu.openFile
													,new FrameUtils.Caption("Open...")
													,new FrameUtils.ToolTip("Start Funny Prolog and load presisent database into fact/rule base")
													,new FrameUtils.FSMTerminal("file.open")
													,new FrameUtils.Callback(new FrameUtils.OpenFileUICallback() {
																@Override public FileFilter getFileFilter() {return new FileNameExtensionFilter("Funny prolog database","*.frb");}
																@Override public File getCurrentDir() {return currentDir;}
																@Override public boolean allowMultiSelection() {return false;}
															}
														)
													),
						FrameUtils.standardMenuItem(FrameUtils.StandardMenu.saveFile
													,new FrameUtils.Caption("Save as...")
													,new FrameUtils.ToolTip("Save fact/rule base to the persistent database")
													,new FrameUtils.FSMTerminal("file.save")
													,new FrameUtils.Callback(new FrameUtils.SaveFileUICallback() {
																@Override public FileFilter getFileFilter() {return new FileNameExtensionFilter("Funny prolog database","*.frb");}
																@Override public File getCurrentDir() {return currentDir;}
																@Override public boolean allowMultiSelection() {return false;}
															}
														)
													),
						new JSeparator(),
						FrameUtils.standardMenuItem(FrameUtils.StandardMenu.openFile
								,new FrameUtils.Caption("Prepare database")
								,new FrameUtils.ToolTip("Prepare empty Funny Prolog fact/rule base")
								,new FrameUtils.FSMTerminal("file.prepare")
								,new FrameUtils.Callback(new FrameUtils.OpenFileUICallback() {
											@Override public FileFilter getFileFilter() {return new FileNameExtensionFilter("Funny prolog database","*.frb");}
											@Override public File getCurrentDir() {return currentDir;}
											@Override public boolean allowMultiSelection() {return false;}
										}
									)
								),
						FrameUtils.standardMenuItem(FrameUtils.StandardMenu.openFile
								,new FrameUtils.Caption("Consult file(s)...")
								,new FrameUtils.ToolTip("Consult selected files into Funny Prolog fact/rule base")
								,new FrameUtils.FSMTerminal("file.consult")
								,new FrameUtils.Callback(new FrameUtils.OpenFileUICallback() {
											@Override public FileFilter getFileFilter() {return new FileNameExtensionFilter("Funny prolog database","*.frb");}
											@Override public File getCurrentDir() {return currentDir;}
											@Override public boolean allowMultiSelection() {return true;}
										}
									)
								),
						new JSeparator(),
						FrameUtils.standardMenuItem(FrameUtils.StandardMenu.quit,new FrameUtils.ToolTip("Quit Funny Prolog"))
				),
				FrameUtils.menu("actions","Actions",
						FrameUtils.menu("actions.start","Start VM",0,new FrameUtils.ToolTip("Start Funny Prolog VM")),
						FrameUtils.menu("actions.stop","Stop VM",0,new FrameUtils.ToolTip("Stop Funny Prolog VM"))
				),
				FrameUtils.menu("tools","Tools",
						FrameUtils.standardMenuItem(FrameUtils.StandardMenu.lookandfeel,new FrameUtils.ToolTip("Change look & feel"))
				),
				FrameUtils.menu("help","Help",
						FrameUtils.standardMenuItem(FrameUtils.StandardMenu.about
								, new FrameUtils.Caption("About Funny Prolog")
								, new FrameUtils.Content(Utils.fromResource(this.getClass().getResource("./about.html")))
								, new FrameUtils.MenuIcon(this.getClass().getResource("./icon.png"))
								, new FrameUtils.ToolTip("About Funny Prolog")
						)
				)
			)
		);
		
		log.setEditable(false);
		console.setRows(5);
		split.setLeftComponent(scroll);
		split.setRightComponent(console);
		split.setDividerSize(5);
		
		getContentPane().add(split,BorderLayout.CENTER);
				
		message(Severity.info,"To execute Funny Prolog sentence, type Ctrl-Enter...");
		pack();
		
		split.setDividerLocation(0.8);
		console.requestFocus();
		console.addKeyListener(new KeyListener(){
				@Override public void keyTyped(final KeyEvent e) {}
				@Override public void keyReleased(final KeyEvent e) {}
				
				@Override 
				public void keyPressed(final KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER && (e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
						processSentence();
					}
				}
			}
		);
		setVisible(true);
	}

	@Override
	protected void processTerminal(final String terminal, final Object... parameters) {
		switch (terminal) {
			case "file.new"		:
				System.err.println("Terminal1: "+terminal);
				break;
			case "file.open"	:
				System.err.println("Terminal2: "+terminal);
				break;
			default :
				System.err.println("Terminal: "+terminal);
		}
	}

	protected void processSentence() {
		
	}
}
