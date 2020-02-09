package chav1961.funnypro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.MimeTypeParseException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.script.ScriptException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.streams.charsource.ReaderCharSource;
import chav1961.purelib.streams.chartarget.StringBuilderCharTarget;
import chav1961.purelib.streams.interfaces.CharacterTarget;
import chav1961.purelib.ui.swing.SwingModelUtils;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;

/**
 * <p>This class implements swing-based UI to interact with Funny Prolog.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1 last update 0.0.2
 */
class JScreen extends JFrame implements LocaleChangeListener {
	private static final long 			serialVersionUID = 3667603693613366899L;
	private static final String			APPLICATION_CAPTION = "JScreen.caption";
	private static final String			INITIAL_HELP = "JScreen.status.initialHelp";
	private static final String			ABOUT_TITLE = "JScreen.about.title";
	private static final String			ABOUT_CONTENT = "JScreen.about.content";

	private static final FilterCallback	FRB_FILTER = new FilterCallback() {
											final String[]	fileMask = new String[]{"*.frb"};
											@Override public String getFilterName() {return "Funny Prolog Fact/rule base";}
											@Override public String[] getFileMask() {return fileMask;}
											@Override public boolean accept(FileSystemInterface item) throws IOException {return item.isDirectory() || item.getName().endsWith(".frb");}
										};
	private static final FilterCallback	FPR_FILTER = new FilterCallback() {
											final String[]	fileMask = new String[]{"*.fpr"};
											@Override public String getFilterName() {return "Funny Prolog source files";}
											@Override public String[] getFileMask() {return fileMask;}
											@Override public boolean accept(FileSystemInterface item) throws IOException {return item.isDirectory() || item.getName().endsWith(".frb");}
										};
	
	private final Localizer			parent, localizer;
	private final FunnyProEngine	fpe;
	private final LoggerFacade		logger;
	private final JMenuBar			menu;
	private final JEditorPane		log = new JEditorPane("text/html","");
	private final JTextArea			console = new JTextArea();
	private final JStateString		ss;

	enum MessageType {
		CONSOLE_INPUT,
		INTERMEDIATE_RESULT,
		TOTAL_RESULT,
		PROCESSING_ERROR
	}
	
	JScreen(final Localizer parent, final ContentMetadataInterface xda, final FunnyProEngine fpe, final LoggerFacade logger) throws IOException, EnvironmentException {
		final Dimension		screen = Toolkit.getDefaultToolkit().getScreenSize();
		final JSplitPane	split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		final JScrollPane	scroll = new JScrollPane(log);
		
		this.parent = parent;
		this.fpe = fpe;
		this.logger = logger;
		this.localizer = LocalizerFactory.getLocalizer(xda.getRoot().getLocalizerAssociated());
		this.parent.push(this.localizer);
		this.ss = new JStateString(this.localizer);
		localizer.addLocaleChangeListener(this);
		
		this.menu = SwingModelUtils.toMenuEntity(xda.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")),JMenuBar.class);
		SwingUtils.assignActionListeners(this.menu,this);
		
		((JMenuItem)SwingUtils.findComponentByName(this.menu,"menu.file.preparefrb")).setEnabled(false);
		((JMenuItem)SwingUtils.findComponentByName(this.menu,"menu.actions.parameters")).setEnabled(false);
		
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
						if (console.getSelectedText() != null) {
							processSentence(console.getSelectedText());
						}
						else {
							processSentence(console.getText());
						}
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

	@OnAction("action:/exit")
	private void quit() {
		this.setVisible(false);
		this.dispose();
	}
	
	@OnAction("action:/prepareFRB")
	private void prepareFRB() throws LocalizationException, IOException {
		try(final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/e:/"))) {
			for (String item : JFileSelectionDialog.select(this,localizer,fsi,JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE|JFileSelectionDialog.OPTIONS_FOR_SAVE,FRB_FILTER)) {
				final String	relativeURI = item.endsWith(".frb") ? item : item+".frb";
				
				try(final FileSystemInterface	frb = fsi.clone().open(relativeURI).push("../").mkDir().pop().create()) {
					try(final OutputStream		os = frb.write()) {
						fpe.newFRB(os);
						os.flush();
					}
					message(Severity.info,"New fact/rule base %1$s was prepared",relativeURI);
				} catch (FProException | IOException e) {
					message(Severity.error,e.getLocalizedMessage());
				}
			}
		}
	}

	@OnAction("action:/consultFile")
	private void consultContent() throws LocalizationException, IOException {
		try(final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:/e:/"))) {
			int	count = 0;
			for (String item : JFileSelectionDialog.select(this,localizer,fsi,JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE|JFileSelectionDialog.OPTIONS_CAN_MULTIPLE_SELECT|JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS|JFileSelectionDialog.OPTIONS_FOR_OPEN,FPR_FILTER)) {
				
				try(final FileSystemInterface	frb = fsi.clone().open(item)) {
					try(final Reader			rdr = frb.charRead()) {
						
						message(Severity.trace,"Consulting %1$s...",item);
						fpe.consult(new ReaderCharSource(rdr,false));
					}
					count++;
				} catch (FProException | IOException e) {
					message(Severity.error,e.getLocalizedMessage());
				}
			}
			if (count > 0) {
				message(Severity.info,"Total %1$d files were consulted",count);
			}
		}
	}
	
	@OnAction("action:/startVM")
	private void startVM() throws LocalizationException {
		if (!fpe.isTurnedOn()) {
			try{fpe.turnOn(null);
				((JMenuItem)SwingUtils.findComponentByName(this.menu,"menu.actions.parameters")).setEnabled(false);
				((JMenuItem)SwingUtils.findComponentByName(this.menu,"menu.file.preparefrb")).setEnabled(false);
				message(Severity.info,"start");
			} catch (FProException | IOException e) {
				message(Severity.error,e.getLocalizedMessage());
			}
		}
	}
	
	@OnAction("action:/stopVM")
	private void stopVM() throws LocalizationException {
		if (fpe.isTurnedOn()) {
			try{fpe.turnOff(null);
				((JMenuItem)SwingUtils.findComponentByName(this.menu,"menu.actions.parameters")).setEnabled(true);
				((JMenuItem)SwingUtils.findComponentByName(this.menu,"menu.file.preparefrb")).setEnabled(true);
				message(Severity.info,"stop");
			} catch (FProException | IOException e) {
				message(Severity.error,e.getLocalizedMessage());
			}
		}
	}

	@OnAction("action:/VMParameters")
	private void setupVM() throws LocalizationException {
	}
	
	@OnAction("action:/about")
	private void showAboutScreen() {
		try{final JEditorPane 	pane = new JEditorPane("text/html",null);
			final Icon			icon = new ImageIcon(this.getClass().getResource("avatar.jpg"));
			
			try(final Reader	rdr = localizer.getContent(ABOUT_CONTENT,new MimeType("text","x-wiki.creole"),new MimeType("text","html"))) {
				pane.read(rdr,null);
			}
			pane.setEditable(false);
			pane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			pane.setPreferredSize(new Dimension(300,300));
			pane.addHyperlinkListener(new HyperlinkListener() {
								@Override
								public void hyperlinkUpdate(final HyperlinkEvent e) {
									if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
										try{Desktop.getDesktop().browse(e.getURL().toURI());
										} catch (URISyntaxException | IOException exc) {
											exc.printStackTrace();
										}
									}
								}
			});
			
			JOptionPane.showMessageDialog(this,pane,localizer.getValue(ABOUT_TITLE),JOptionPane.PLAIN_MESSAGE,icon);
		} catch (LocalizationException | MimeTypeParseException | IOException e) {
			ss.message(Severity.error,e.getLocalizedMessage());
		}
	}

	protected void processSentence(final String sentence) {
		append2Log(MessageType.CONSOLE_INPUT,sentence);
		
		try{final String	result;
		
			if (sentence.startsWith(":-")) {
				result = fpe.goal(sentence,new IFProVM.IFProCallback() {
					int	count = 0;
					@Override
					public boolean onResolution(final String[] names, final IFProEntity[] resolvedVariables, final String[] printedValues) throws FProParsingException, FProPrintingException {
						if (resolvedVariables.length > 0) {
							final StringBuilder 	sb = new StringBuilder("\n__________");
							
							for (int index = 0; index < names.length; index++) {
								sb.append("\n ").append(names[index]).append(" = ").append(resolvedVariables[index]);
							}
							append2Log(MessageType.INTERMEDIATE_RESULT,sb.substring(1));
						}
						return true;
					}
					
					@Override public void beforeFirstCall() {}
					
					@Override 
					public void afterLastCall() {
						append2Log(MessageType.INTERMEDIATE_RESULT,"Total : "+count);
					}
				}) ? "true" : "false";
			}
			else if (sentence.startsWith("?-")) {
				result = fpe.question(sentence,new IFProVM.IFProCallback() {
					int	count = 0;
					@Override
					public boolean onResolution(final String[] names, final IFProEntity[] resolvedVariables, final String[] printedValues) throws FProParsingException, FProPrintingException {
						if (resolvedVariables.length > 0) {
							final StringBuilder 	sb = new StringBuilder("\n__________");
							final CharacterTarget	target = new StringBuilderCharTarget(sb);
							
							for (int index = 0; index < names.length; index++) {
								sb.append("\n ").append(names[index]).append(" = ").append(printedValues[index]);
							}
							append2Log(MessageType.INTERMEDIATE_RESULT,sb.substring(1));
						}
						count++;
						return true;
					}
					
					@Override public void beforeFirstCall() {}
					
					@Override 
					public void afterLastCall() {
						append2Log(MessageType.INTERMEDIATE_RESULT,"Total : "+count);
					}
				}) ? "true" : "false";
			}
			else {
				fpe.eval(sentence);
				result = "term asserted";
			}
		
			append2Log(MessageType.TOTAL_RESULT,result);
		} catch (FProException | ScriptException | IOException e) {
			append2Log(MessageType.PROCESSING_ERROR,e.getMessage());
			ss.message(Severity.error,e.getLocalizedMessage());
		}
	}

	protected void append2Log(final MessageType type, final String content) {
		try{final SimpleAttributeSet	as = new SimpleAttributeSet();
		
			switch (type) {
				case CONSOLE_INPUT:
					as.addAttribute(StyleConstants.ColorConstants.Foreground,Color.BLUE);
					break;
				case INTERMEDIATE_RESULT:
					as.addAttribute(StyleConstants.ColorConstants.Foreground,Color.LIGHT_GRAY);
					as.addAttribute(StyleConstants.ColorConstants.Italic,true);
					break;
				case PROCESSING_ERROR:
					as.addAttribute(StyleConstants.ColorConstants.Foreground,Color.RED);
					break;
				case TOTAL_RESULT:
					as.addAttribute(StyleConstants.ColorConstants.Foreground,Color.BLACK);
					as.addAttribute(StyleConstants.ColorConstants.Bold,true);
					break;
				default:
					as.addAttribute(StyleConstants.ColorConstants.Foreground,Color.BLACK);
					break;
			}
			
			log.getDocument().insertString(log.getDocument().getLength(),content+'\n',as);
		} catch (BadLocationException e) {
			ss.message(Severity.error,e.getLocalizedMessage());
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		// TODO Auto-generated method stub
		setTitle(localizer.getValue(APPLICATION_CAPTION));
	}

	private void message(final Severity info, final String format, final Object... parameters) throws IllegalArgumentException, LocalizationException {
		final String	text = localizer.containsKey(format) ? localizer.getValue(format) : format;
		
		if (parameters == null || parameters.length == 0) {
			ss.message(info,text);
		}
		else {
			ss.message(info,text,parameters);
		}
	}
}
