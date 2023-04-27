package chav1961.funnypro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Locale;

import javax.script.ScriptException;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.streams.charsource.ReaderCharSource;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.JStateString;

/**
 * <p>This class implements swing-based UI to interact with Funny Prolog.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 
 * @last.update 0.0.2
 */
class JScreen extends JFrame implements LocaleChangeListener, LoggerFacadeOwner {
	private static final long 			serialVersionUID = 3667603693613366899L;
	
	private static final String			KEY_APPLICATION_CAPTION = "JScreen.caption";
	private static final String			KEY_INITIAL_HELP = "JScreen.status.initialHelp";
	private static final String			KEY_ABOUT_TITLE = "JScreen.about.title";
	private static final String			KEY_ABOUT_CONTENT = "JScreen.about.content";
	private static final String			KEY_TOTAL_FILES_CONSULTED = "JScreen.message.consulted";
	private static final String			KEY_FACT_RULE_BASE_PREPARED = "JScreen.message.frb.prepared";
	private static final String			KEY_VM_STARTED = "JScreen.message.vm.started";
	private static final String			KEY_VM_STOPPED = "JScreen.message.vm.stopped";
	
	private static final FilterCallback	FRB_FILTER = FilterCallback.ofWithExtension("Funny Prolog Fact/rule base", "frb", "*.frb");
	private static final FilterCallback	FPR_FILTER = FilterCallback.ofWithExtension("Funny Prolog source files", "fpr", "*.fpr");
	
	private static final String			MENU_FILE_PREPARE_FRB = "menu.file.preparefrb";
	private static final String			MENU_ACTIONS_PARAMETERS = "menu.actions.parameters";

	private static final String[]		MENUS = {
											MENU_FILE_PREPARE_FRB,
											MENU_ACTIONS_PARAMETERS
										};
	
	private static final long 			FILE_PREPARE_FRB = 1L << 0;
	private static final long 			ACTIONS_PARAMETERS = 1L << 1;
	
	private final Localizer					parent, localizer;
	private final FunnyProEngine			fpe;
	private final JMenuBar					menu;
	private final JEditorPane				log = new JEditorPane("text/html","");
	private final JTextArea					console = new JTextArea();
	private final JStateString				ss;
	private final JEnableMaskManipulator	emm;

	enum MessageType {
		CONSOLE_INPUT(StyleConstants.ColorConstants.Foreground, Color.BLUE),
		INTERMEDIATE_RESULT(StyleConstants.ColorConstants.Foreground, Color.LIGHT_GRAY, StyleConstants.ColorConstants.Italic, true),
		TOTAL_RESULT(StyleConstants.ColorConstants.Foreground, Color.BLACK, StyleConstants.ColorConstants.Bold, true),
		PROCESSING_ERROR(StyleConstants.ColorConstants.Foreground, Color.RED);
		
		private final SimpleAttributeSet	sas = new SimpleAttributeSet(); 
		
		MessageType(final Object... styles) {
			for (int index = 0; index < styles.length/2; index++) {
				sas.addAttribute(styles[2*index+0], styles[2*index+1]);
			}
		}
		
		public SimpleAttributeSet getAttributes() {
			return sas;
		}
	}
	
	JScreen(final Localizer parent, final ContentMetadataInterface xda, final FunnyProEngine fpe, final LoggerFacade logger) throws IOException, EnvironmentException {
		final JSplitPane	split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		final JScrollPane	scrollLog = new JScrollPane(log);
		final JScrollPane	scrollConsole = new JScrollPane(console);
		
		this.parent = parent;
		this.fpe = fpe;
		this.localizer = LocalizerFactory.getLocalizer(xda.getRoot().getLocalizerAssociated());
		this.parent.push(this.localizer);
		this.ss = new JStateString(this.localizer);
		this.localizer.addLocaleChangeListener(this);
		
		this.menu = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")),JMenuBar.class);
		this.emm = new JEnableMaskManipulator(MENUS, menu);
		SwingUtils.assignActionListeners(menu, this);

		emm.setEnableMaskOff(FILE_PREPARE_FRB | ACTIONS_PARAMETERS);
		
		log.setEditable(false);
		console.setRows(5);
		split.setLeftComponent(scrollLog);
		split.setRightComponent(scrollConsole);
		split.setDividerSize(5);
		split.setDividerLocation(400);
		
		setJMenuBar(menu);
		getContentPane().add(split,BorderLayout.CENTER);
		getContentPane().add(ss,BorderLayout.SOUTH);
		
		SwingUtils.assignActionKey(console, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), (e)->{
			if (console.getSelectedText() != null) {
				processSentence(console.getSelectedText());
			}
			else {
				processSentence(console.getText());
			}
		}, "execute");
		console.requestFocusInWindow();
		
		SwingUtils.assignExitMethod4MainWindow(this, ()->quit());
		SwingUtils.centerMainWindow(this, 0.85f);
		pack();

		message(Severity.info, KEY_INITIAL_HELP);
		
		fillLocalizedStrings();
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(menu, oldLocale, newLocale);
		ss.localeChanged(oldLocale, newLocale);
		fillLocalizedStrings();
	}

	@Override
	public LoggerFacade getLogger() {
		return ss;
	}
	
	@OnAction("action:/exit")
	private void quit() {
		this.setVisible(false);
		this.dispose();
	}
	
	@OnAction("action:/prepareFRB")
	private void prepareFRB() throws LocalizationException, IOException {
		try(final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"))) {
			
			for (String item : JFileSelectionDialog.select(this, localizer, fsi,
							JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE|JFileSelectionDialog.OPTIONS_FOR_SAVE|JFileSelectionDialog.OPTIONS_APPEND_EXTENSION,
							FRB_FILTER)) {
				
				try(final FileSystemInterface	frb = fsi.clone().open(item).push("../").mkDir().pop().create()) {
					try(final OutputStream		os = frb.write()) {
						
						fpe.newFRB(os);
					}
					message(Severity.info, KEY_FACT_RULE_BASE_PREPARED, item);
				} catch (ContentException | IOException e) {
					message(Severity.error, e, e.getLocalizedMessage());
				}
			}
		}
	}

	@OnAction("action:/consultFile")
	private void consultContent() throws LocalizationException, IOException {
		try(final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/"))) {
			int	count = 0;
			
			for (String item : JFileSelectionDialog.select(this, localizer, fsi, 
						JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE|JFileSelectionDialog.OPTIONS_CAN_MULTIPLE_SELECT|JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS|JFileSelectionDialog.OPTIONS_FOR_OPEN|JFileSelectionDialog.OPTIONS_APPEND_EXTENSION,
						FPR_FILTER)) {
				try(final FileSystemInterface	fpr = fsi.clone().open(item)) {
					try(final Reader			rdr = fpr.charRead(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
						
						message(Severity.trace,"Consulting %1$s...",item);
						fpe.consult(new ReaderCharSource(rdr,false));
					}
					count++;
				} catch (SyntaxException | IOException e) {
					message(Severity.error, e, e.getLocalizedMessage());
				}
			}
			if (count > 0) {
				message(Severity.info, KEY_TOTAL_FILES_CONSULTED, count);
			}
		}
	}
	
	@OnAction("action:/startVM")
	private void startVM() throws LocalizationException {
		if (!fpe.isTurnedOn()) {
			try{fpe.turnOn(null);
				emm.setEnableMaskOff(FILE_PREPARE_FRB | ACTIONS_PARAMETERS);
				message(Severity.info, KEY_VM_STARTED);
			} catch (ContentException | IOException e) {
				message(Severity.error,e,e.getLocalizedMessage());
			}
		}
	}
	
	@OnAction("action:/stopVM")
	private void stopVM() throws LocalizationException {
		if (fpe.isTurnedOn()) {
			try{fpe.turnOff(null);
				emm.setEnableMaskOn(FILE_PREPARE_FRB | ACTIONS_PARAMETERS);
				message(Severity.info, KEY_VM_STOPPED);
			} catch (ContentException | IOException e) {
				message(Severity.error,e,e.getLocalizedMessage());
			}
		}
	}

	@OnAction("action:/VMParameters")
	private void setupVM() throws LocalizationException {
	}

	@OnAction("action:builtin:/builtin.languages")
	private void changeLang(final Hashtable<String,String[]> query) throws LocalizationException {
		localizer.setCurrentLocale(Locale.forLanguageTag(query.get("lang")[0]));
	}
	
	@OnAction("action:builtin:/builtin.lookAndFeel")
	private void changeLaf(final Hashtable<String,String[]> query) throws LocalizationException {
		try{UIManager.setLookAndFeel(query.get("laf")[0]);
	        SwingUtilities.updateComponentTreeUI(this);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			message(Severity.error,e,e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/about")
	private void showAboutScreen() throws URISyntaxException {
		SwingUtils.showAboutScreen(this, localizer, KEY_ABOUT_TITLE, KEY_ABOUT_CONTENT, getClass().getResource("avatar.jpg").toURI(), new Dimension(300,300));
	}

	protected void processSentence(final String sentence) {
		append2Log(MessageType.CONSOLE_INPUT, sentence);
		
		try{final String	result;
		
			if (sentence.startsWith(":-")) {
				result = fpe.goal(sentence,new IFProVM.IFProCallback() {
					int	count = 0;
					
					@Override
					public boolean onResolution(final String[] names, final IFProEntity[] resolvedVariables, final String[] printedValues) throws SyntaxException, PrintingException {
						if (resolvedVariables.length > 0) {
							final StringBuilder 	sb = new StringBuilder("\n__________");
							
							for (int index = 0; index < names.length; index++) {
								sb.append("\n ").append(names[index]).append(" = ").append(resolvedVariables[index]);
							}
							append2Log(MessageType.INTERMEDIATE_RESULT,sb.substring(1));
						}
						return true;
					}
					
					@Override 
					public void afterLastCall() {
						append2Log(MessageType.INTERMEDIATE_RESULT, "Total : "+count);
					}
				}) ? "true" : "false";
			}
			else if (sentence.startsWith("?-")) {
				result = fpe.question(sentence,new IFProVM.IFProCallback() {
					int	count = 0;
					
					@Override
					public boolean onResolution(final String[] names, final IFProEntity[] resolvedVariables, final String[] printedValues) throws SyntaxException, PrintingException {
						if (resolvedVariables.length > 0) {
							final StringBuilder 	sb = new StringBuilder("\n__________");
							
							for (int index = 0; index < names.length; index++) {
								sb.append("\n ").append(names[index]).append(" = ").append(printedValues[index]);
							}
							append2Log(MessageType.INTERMEDIATE_RESULT,sb.substring(1));
						}
						count++;
						return true;
					}
					
					@Override 
					public void afterLastCall() {
						append2Log(MessageType.INTERMEDIATE_RESULT, "Total : "+count);
					}
				}) ? "true" : "false";
			}
			else {
				fpe.eval(sentence);
				result = "term asserted";
			}
		
			append2Log(MessageType.TOTAL_RESULT, result);
		} catch (ContentException | ScriptException | IOException e) {
			append2Log(MessageType.PROCESSING_ERROR,e.getMessage());
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}

	protected void append2Log(final MessageType type, final String content) {
		try{
			log.getDocument().insertString(log.getDocument().getLength(), content+'\n', type.getAttributes());
		} catch (BadLocationException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		setTitle(localizer.getValue(KEY_APPLICATION_CAPTION));
	}

	private void message(final Severity info, final String format, final Object... parameters) {
		final String	text = localizer.containsKey(format) ? localizer.getValue(format) : format;
		
		if (parameters == null || parameters.length == 0) {
			getLogger().message(info, text);
		}
		else {
			getLogger().message(info, text, parameters);
		}
	}

	private void message(final Severity info, final Throwable t, final String format, final Object... parameters) {
		final String	text = localizer.containsKey(format) ? localizer.getValue(format) : format;
		
		if (parameters == null || parameters.length == 0) {
			getLogger().message(info,t,text);
		}
		else {
			getLogger().message(info, t, text, parameters);
		}
	}
}
