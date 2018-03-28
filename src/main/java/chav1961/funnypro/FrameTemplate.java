package chav1961.funnypro;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import chav1961.funnypro.FrameUtils.CargoItem;
import chav1961.funnypro.FrameUtils.CommonUICallback;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public class FrameTemplate extends JFrame {
	private static final long 		serialVersionUID = 4059510527492997549L;
	private static final int		STATUS_HEIGHT = 20;

	private final ActionListener	actionListener = new ActionListener(){
										@Override
										public void actionPerformed(ActionEvent e) {
											processTerminal(e.getActionCommand());
										}
									};
	private final LoggerFacade		logger;
	private final StatusString		status = new StatusString();
	private FSMItem[]				states = new FSMItem[0]; 
	private int						currentFSMState = 0;

	@FunctionalInterface
	protected interface WalkCallback {
		void process(final JComponent component);
	}
	
	protected FrameTemplate(final String caption, final LoggerFacade log) throws IOException {
		super(caption);
		final Dimension 	screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		this.logger = log;
		setMinimumSize(new Dimension(200,200));
		setPreferredSize(new Dimension(3*screenSize.width/4,3*screenSize.height/4));
		setLocation(new Point(screenSize.width/8,screenSize.height/8));

		getContentPane().add(status,BorderLayout.SOUTH);
	}

	protected void setMenu(final JMenuBar bar) {
		if (bar == null) {
			throw new IllegalArgumentException("Menu bar can't be null"); 
		}
		else {
			getContentPane().add(bar,BorderLayout.NORTH);
			walk(bar,component->{
				if (component instanceof CargoItem) {
					((JMenuItem)component).addActionListener(
							new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									((CargoItem<?>)component).getCargo().execute(FrameTemplate.this);
								}
							}
					);
				}
				else if (component instanceof JMenuItem) {
					((JMenuItem)component).addActionListener(actionListener);
				}
			});
		}
	}
	
	protected void setFSMGraph(final String graphDescription) {
		if (graphDescription == null || graphDescription.isEmpty()) {
			throw new IllegalArgumentException("Graph description can't be null");
		}
		else {
			states = new FSMItem[0];
		}
	}
	
	protected JComponent getMenuItem(final String nameOrAction) {
		return null;
	}

	protected void message(final Severity severity, final String text, final Object... parameters) {
		final String	message = parameters.length == 0 ? text : String.format(text,parameters);
		final String	content;
		
		switch (severity) {
			case trace		: content = "<font color=lightGray>"+message+"</font><br>"; break;
			case debug		: content = "<font color=darkGray>"+message+"</font><br>"; break;
			case info		: content = "<font color=black>"+message+"</font><br>"; break;
			case warning	: content = "<font color=blue>"+message+"</font><br>"; break;
			case error		: content = "<font color=red>"+message+"</font><br>"; break;
			case severe		: content = "<font color=red><b>"+message+"</b></font><br>"; break;
			default : content = message; break;
		}
		status.message("<html><head></head><body>"+content+"<body><html>");
		if (logger != null) {
			logger.message(severity,message);
		}
	}
	
	protected ProgressInterface getProgressInterface(final boolean cancellable) {
		return status.getProgressInterface(cancellable);
	}
	
	protected int getCurrentFSMState() {
		return 0;
	}
	
	protected void processTerminal(final String terminal, final Object... parameters) {
		
	}
	
	protected void processQuit() {
		this.setVisible(false);
		this.dispose();
	}

	protected void walk(final JComponent root, final WalkCallback callback) {
		if (root != null) {
			callback.process(root);
			
			if (root instanceof JMenu) {
				for (int index = 0; index < ((JMenu)root).getMenuComponentCount(); index++) {
					if (((JMenu)root).getMenuComponent(index) instanceof JComponent) {
						walk((JComponent)((JMenu)root).getMenuComponent(index),callback);
					}
				}
			}
			else {
				for (int index = 0; index < root.getComponentCount(); index++) {
					if (root.getComponent(index) instanceof JComponent) {
						walk((JComponent)root.getComponent(index),callback);
					}
				}
			}
		}
	}

	@Override
	protected void processWindowEvent(final WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			processQuit();
		}
		else {
			super.processWindowEvent(e);
		}
	}
	
	private static class FSMItem {
		public int			currentState;
		public String		currentCaption;
		public String		currentTooltip;
		public boolean		isTerminal;
		public FSMJump[]	jumps;
		public String[]		enables;
		public String[]		disables;
	}

	private static class FSMJump {
		public String		terminal;
		public int			newState;
	}
	
	private static class StatusString extends JPanel {
		private static final long 	serialVersionUID = 5395749717067047653L;
		
		private final CardLayout	layout;
		private final JLabel		text = new JLabel(" ");
		private final JProgressBar	bar = new JProgressBar();
		private final JLabel		cancel = new JLabel("...");
		private boolean				cancelPressed = false; 
		
		StatusString() {
			super(new CardLayout());
			final Dimension 		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			final JPanel			barPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			this.layout = (CardLayout) getLayout();
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			
			text.setPreferredSize(new Dimension(screenSize.width/2,STATUS_HEIGHT));
			add(text,"card1");

			bar.setPreferredSize(new Dimension(screenSize.width/4,STATUS_HEIGHT));
			barPanel.add(bar);
			barPanel.add(cancel);
			add(barPanel,"card2");
			
			layout.show(this,"card1");
		}
		
		void message(final String message) {
			text.setText(message == null ? " " : message);
		}
		
		ProgressInterface getProgressInterface(final boolean cancellable) {
			cancelPressed = false;
			cancel.setVisible(cancellable);
			layout.show(this,"card2");
			
			return new ProgressInterface() {
				@Override
				public ProgressInterface setRange(final int from, final int to) {
					bar.setMaximum((int) from);
					bar.setMinimum((int) to);
					return this;
				}

				@Override
				public ProgressInterface setPos(final int pos) {
					bar.setValue((int) pos);
					return this;
				}

				@Override
				public float getPos() {
					return bar.getValue();
				}

				@Override
				public ProgressInterface setText(final String text) {
					bar.setString(text == null ? "" : text);
					return this;
				}

				@Override
				public String getText() {
					return bar.getString();
				}

				@Override
				public boolean isCancellingRequired() {
					return cancellable && cancelPressed;
				}

				@Override
				public void close() {
					layout.show(StatusString.this,"card1");
				}
			};
		}
	}
}
