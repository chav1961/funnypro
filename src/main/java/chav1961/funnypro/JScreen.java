package chav1961.funnypro;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.script.ScriptException;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.XMLDescribedApplication;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;

/**
 * <p>This class implements swing-based UI to interact with Funny Prolog.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 last update 0.0.2
 */
class JScreen extends JFrame implements LocaleChangeListener {
	private static final long 		serialVersionUID = 3667603693613366899L;
	private static final int		STATUS_HEIGHT = 24;
	private static final String		APPLICATION_CAPTION = "JScreen.caption";
	private static final String		INITIAL_HELP = "JScreen.status.initialHelp";

	private final Localizer			parent, localizer;
	private final FunnyProEngine	fpe;
	private final LoggerFacade		logger;
	private final JMenuBar			menu;
	private final JEditorPane		log = new JEditorPane("text/html","");
	private final JTextArea			console = new JTextArea();
	private final StatusString		ss = new StatusString();
	private File					currentDir = new File("./");
	
	JScreen(final Localizer parent, final XMLDescribedApplication xda, final FunnyProEngine fpe, final LoggerFacade logger) throws IOException, EnvironmentException {
		final Dimension		screen = Toolkit.getDefaultToolkit().getScreenSize();
		final JSplitPane	split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		final JScrollPane	scroll = new JScrollPane(log);
		
		this.parent = parent;
		this.fpe = fpe;
		this.logger = logger;
		this.localizer = xda.getLocalizer();
		this.parent.push(this.localizer);
		localizer.addLocaleChangeListener(this);
		
		this.menu = xda.getEntity("mainmenu",JMenuBar.class,null);
		SwingUtils.assignActionListeners(this.menu,this);
		
		log.setEditable(false);
		console.setRows(5);
		split.setLeftComponent(scroll);
		split.setRightComponent(console);
		split.setDividerSize(5);
		
		getContentPane().add(menu,BorderLayout.NORTH);
		getContentPane().add(split,BorderLayout.CENTER);
		getContentPane().add(ss,BorderLayout.SOUTH);
				
		message(Severity.info,INITIAL_HELP);
		pack();
		
		console.requestFocus();
		console.addKeyListener(new KeyListener(){
				@Override public void keyTyped(final KeyEvent e) {}
				@Override public void keyReleased(final KeyEvent e) {}
				
				@Override 
				public void keyPressed(final KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER && (e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
						processSentence(console.getSelectedText());
					}
				}
			}
		);
		fillLocalizedStrings();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(3*screen.width/4,3*screen.height/4));
		setPreferredSize(new Dimension(3*screen.width/4,3*screen.height/4));
		setLocationRelativeTo(null);
		split.setDividerLocation(0.8);
		setVisible(true);
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		((LocaleChangeListener)menu).localeChanged(oldLocale, newLocale);
	}

	@OnAction("exit")
	private void quit() {
		this.setVisible(false);
		this.dispose();
	}
	

	protected void processSentence(final String sentence) {
		final SimpleAttributeSet	as = new SimpleAttributeSet();
		
		as.addAttribute(StyleConstants.ColorConstants.Foreground,Color.BLUE);
		as.addAttribute(StyleConstants.ColorConstants.Bold,true);
		try{log.getDocument().insertString(log.getDocument().getLength(),sentence+'\n',as);
		
			final boolean	result = fpe.goal(sentence,new IFProVM.IFProCallback() {
								@Override
								public boolean onResolution(final Map<String, Object> resolvedVariables) throws FProParsingException, FProPrintingException {
									// TODO Auto-generated method stub
									if (resolvedVariables.size() > 0) {
										return true;
									}
									else {
										return true;
									}
								}
								
								@Override
								public void beforeFirstCall() {
									// TODO Auto-generated method stub
								}
								
								@Override
								public void afterLastCall() {
									// TODO Auto-generated method stub
								}
							});
			log.getDocument().insertString(log.getDocument().getLength(),result+"\n",as);
		} catch (BadLocationException | FProException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void fillLocalizedStrings() throws LocalizationException {
		// TODO Auto-generated method stub
		setTitle(localizer.getValue(APPLICATION_CAPTION));
	}

	private void message(final Severity info, final String format, final Object... parameters) throws IllegalArgumentException, LocalizationException {
		final String	text = localizer.containsKey(format) ? localizer.getValue(format) : format;
		
		if (parameters == null || parameters.length == 0) {
			ss.message(text);
		}
		else {
			ss.message(String.format(text,parameters));
		}
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
