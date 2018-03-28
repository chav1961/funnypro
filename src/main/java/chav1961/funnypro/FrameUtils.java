package chav1961.funnypro;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public class FrameUtils {
	public static final int		MENU_OPTIONS_CHECK = 0x0001;
	public static final int		MENU_OPTIONS_RADIO = 0x0002;
	
	
	public enum StandardMenu {
		openFile, saveFile, quit, help, about, lookandfeel, language
	}

	public interface AnyUICallback {
	}
	
	public interface CommonUICallback extends AnyUICallback {
		int execute(final FrameTemplate parent);
	}

	public interface OpenFileUICallback extends AnyUICallback {
		File getCurrentDir();
		boolean allowMultiSelection();
		FileFilter getFileFilter();
	}

	public interface SaveFileUICallback extends AnyUICallback {
		File getCurrentDir();
		boolean allowMultiSelection();
		FileFilter getFileFilter();
	}
	
	public interface CargoItem<T extends CommonUICallback> {
		T getCargo();
	}

	public static JMenuBar menuBar(final JComponent... components) {
		if (components == null || components.length == 0) {
			throw new IllegalArgumentException("Component list can't be null or empty list");
		}
		else {
			final JMenuBar	result = new JMenuBar();
			
			for (JComponent item : components) {
				result.add(item);
			}
			return result;
		}
	}

	public static JComponent menu(final String name, final String caption, final JComponent... components) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Menu name can't be null or empty");
		}
		else if (caption == null || caption.isEmpty()) {
			throw new IllegalArgumentException("Menu caption can't be null or empty");
		}
		else {
			final JMenu		result = new JMenu(caption);
			
			for (JComponent item : components) {
				result.add(item);
			}
			result.setName(name);
			return result;
		}
	}

	public static JComponent menu(final String action, final String caption, final int options, final Object... advanced) {
		if (action == null || action.isEmpty()) {
			throw new IllegalArgumentException("Menu action can't be null or empty");
		}
		else if (caption == null || caption.isEmpty()) {
			throw new IllegalArgumentException("Menu caption can't be null or empty");
		}
		else {
			final MenuUI	desc = fillParameters(MenuUI.class,advanced);
			final JMenuItem	item;
			
			if ((options & MENU_OPTIONS_CHECK) != 0) {
				item = desc.icon != null ? new JCheckBoxMenuItem(caption,desc.icon.getIcon()) : new JCheckBoxMenuItem(caption);
			}
			else if ((options & MENU_OPTIONS_RADIO) != 0) {
				item = desc.icon != null ? new JRadioButtonMenuItem(caption,desc.icon.getIcon()) : new JRadioButtonMenuItem(caption);
			}
			else {
				item = desc.icon != null ? new JMenuItem(caption,desc.icon.getIcon()) : new JMenuItem(caption);
			}
			item.setName(action);
			item.setActionCommand(action);
			if (desc.tooltip != null) {
				item.setToolTipText(desc.tooltip.getTooltip());
			}
			return item;
		}
	}
	
	public static JComponent standardMenuItem(final StandardMenu menuType, Object... parameters) {
		if (menuType == null) {
			throw new IllegalArgumentException("Standard menu type can't be null");
		}
		else {
			switch (menuType) {
				case openFile		:
					final OpenFileUI	openFileData = fillParameters(OpenFileUI.class,parameters);
					
					if (openFileData.caption == null) {
						throw new IllegalArgumentException("Mandatory parameter [caption(...)] is missing in the parameter list"); 
					}
					else if (openFileData.callback == null) {
						throw new IllegalArgumentException("Mandatory parameter [callback(...)] is missing in the parameter list"); 
					}
					else if (!(openFileData.callback.getCallback() instanceof OpenFileUICallback)) {
						throw new IllegalArgumentException("Parameter [callback(...)] need be instance of OpenFileUICallback interface (now is ["+openFileData.callback.getCallback().getClass().getName()+"])!"); 
					}
					else if (openFileData.terminal == null) {
						throw new IllegalArgumentException("Mandatory parameter [terminal(...)] is missing in the parameter list"); 
					}
					else {
						final CargoMenuItem<OpenFileUI>	openFileItem = new CargoMenuItem<OpenFileUI>(openFileData,openFileData.caption.getCaption());
						
						openFileItem.setName(openFileData.terminal.getFSMTerminal());
						if (openFileData.tooltip != null) {
							openFileItem.setToolTipText(openFileData.tooltip.getTooltip());
						}
						return openFileItem;
					}
				case saveFile		:
					final SaveFileUI	saveFileData = fillParameters(SaveFileUI.class,parameters);
					
					if (saveFileData.caption == null) {
						throw new IllegalArgumentException("Mandatory parameter [caption(...)] is missing in the parameter list"); 
					}
					else if (saveFileData.callback == null) {
						throw new IllegalArgumentException("Mandatory parameter [callback(...)] is missing in the parameter list"); 
					}
					else if (!(saveFileData.callback.getCallback() instanceof SaveFileUICallback)) {
						throw new IllegalArgumentException("Parameter [callback(...)] need be instance of SaveFileUICallback interface (now is ["+saveFileData.callback.getCallback().getClass().getName()+"])!"); 
					}
					else if (saveFileData.terminal == null) {
						throw new IllegalArgumentException("Mandatory parameter [terminal(...)] is missing in the parameter list"); 
					}
					else {
						final CargoMenuItem<SaveFileUI>	saveFileItem = new CargoMenuItem<SaveFileUI>(saveFileData,saveFileData.caption.getCaption());
						
						saveFileItem.setName(saveFileData.terminal.getFSMTerminal());
						if (saveFileData.tooltip != null) {
							saveFileItem.setToolTipText(saveFileData.tooltip.getTooltip());
						}
						return saveFileItem;
					}
				case quit			:
					final QuitUI	quitData = fillParameters(QuitUI.class,parameters);
					
					final CargoMenuItem<QuitUI>		quitItem = new CargoMenuItem<QuitUI>(quitData,"Quit");
					
					if (quitData.tooltip != null) {
						quitItem.setToolTipText(quitData.tooltip.getTooltip());
					}
					return quitItem;
				case help			:
					break;
				case about			:
					final AboutUI	aboutData = fillParameters(AboutUI.class,parameters);
					
					if (aboutData.caption == null) {
						throw new IllegalArgumentException("Mandatory parameter [caption(...)] is missing in the parameter list"); 
					}
					else if (aboutData.content == null) {
						throw new IllegalArgumentException("Mandatory parameter [content(...)] is missing in the parameter list"); 
					}
					else {
						final CargoMenuItem<AboutUI>	aboutItem = new CargoMenuItem<AboutUI>(aboutData,"About...");
						
						if (aboutData.tooltip != null) {
							aboutItem.setToolTipText(aboutData.tooltip.getTooltip());
						}
						return aboutItem;
					}
				case lookandfeel	:
					final JMenu		lookAndFeelMenu = new JMenu("look & feel");
					final List<JCheckBoxMenuItem>	items = new ArrayList<>();
					
					for (LookAndFeelInfo item : UIManager.getInstalledLookAndFeels()) {
						final LookAndFeelUI 	menuDesc = new LookAndFeelUI();
						final JCheckBoxMenuItem	menuItem = new CargoCheckBoxMenuItem<LookAndFeelUI>(menuDesc,item.getName());
						
						menuDesc.lookAndFeelClassName = item.getClassName();
						if (item.getName().equals(UIManager.getLookAndFeel().getName())) {
							menuItem.setSelected(true);
						}
						menuItem.addActionListener(new ActionListener(){
								@Override
								public void actionPerformed(ActionEvent e) {
									for (JCheckBoxMenuItem item : items) {
										item.setSelected(false);
									}
									menuItem.setSelected(true);
								}
							}
						);
						items.add(menuItem);
						lookAndFeelMenu.add(menuItem);
					}
					return lookAndFeelMenu;
				case language		:
					break;
			}
		}
		return null;
	}

	private static <T> T fillParameters(final Class<T> awaited, final Object[] parameters) {
		try{final T		inst = awaited.newInstance();
		
			for (Field f : awaited.getDeclaredFields()) {
				for (Object item : parameters) {
					if (item.getClass() == f.getType()) {
						f.setAccessible(true);
						f.set(inst,item);
					}
				}
			}
			return inst;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(e.getMessage(),e);
		}
	}
	
	public static class MenuIcon {
		private final Icon	icon;
		
		public MenuIcon(final Icon icon) {
			if (icon == null) {
				throw new IllegalArgumentException("Icon can't be null"); 
			}
			else {
				this.icon = icon;
			}
		}
		
		public MenuIcon(final URL icon) throws IOException {
			if (icon == null) {
				throw new IllegalArgumentException("Icon URL can't be null"); 
			}
			else {
				final BufferedImage	bi = ImageIO.read(icon);

				this.icon = new ImageIcon(bi);
			}
		}
		
		public Icon getIcon() {
			return icon;
		}

		@Override
		public String toString() {
			return "MenuIcon";
		}
	}

	public static class Caption {
		private final String			caption;
		private final CaptionCallback	callback;
		
		protected interface CaptionCallback {
			String getMenuTooltip(final String name);
		}
		
		public Caption(final String text) {
			if (text == null || text.isEmpty()) {
				throw new IllegalArgumentException("Caption text can't be null or empty string");
			}
			else {
				this.caption = text;
				this.callback = null;
			}
		}
		
		public Caption(final CaptionCallback callback) {
			if (callback == null) {
				throw new IllegalArgumentException("Tooltip callback can't be null");
			}
			else {
				this.caption = null;
				this.callback = callback;
			}
		}
		
		public String getCaption() {
			return caption != null ? caption : callback.getMenuTooltip("");
		}

		@Override
		public String toString() {
			return "Caption [Caption=" + caption + ", callback=" + callback + "]";
		}
	}

	public static class Content {
		private final String			content;
		private final ContentCallback	callback;
		
		protected interface ContentCallback {
			String getContent(final String name);
		}
		
		public Content(final String text) {
			if (text == null || text.isEmpty()) {
				throw new IllegalArgumentException("Caption text can't be null or empty string");
			}
			else {
				this.content = text;
				this.callback = null;
			}
		}
		
		public Content(final ContentCallback callback) {
			if (callback == null) {
				throw new IllegalArgumentException("Tooltip callback can't be null");
			}
			else {
				this.content = null;
				this.callback = callback;
			}
		}
		
		public String getContent() {
			return content != null ? content : callback.getContent("");
		}

		@Override
		public String toString() {
			return "Content [Content=" + content + ", callback=" + callback + "]";
		}
	}
	
	public static class ToolTip {
		private final String			tooltip;
		private final ToolTipCallback	callback;
		
		protected interface ToolTipCallback {
			String getMenuTooltip(final String name);
		}
		
		public ToolTip(final String text) {
			if (text == null || text.isEmpty()) {
				throw new IllegalArgumentException("Tooltip text can't be null or empty string");
			}
			else {
				this.tooltip = text;
				this.callback = null;
			}
		}
		
		public ToolTip(final ToolTipCallback callback) {
			if (callback == null) {
				throw new IllegalArgumentException("Tooltip callback can't be null");
			}
			else {
				this.tooltip = null;
				this.callback = callback;
			}
		}
		
		public String getTooltip() {
			return tooltip != null ? tooltip : callback.getMenuTooltip("");
		}

		@Override
		public String toString() {
			return "Tooltip [prompt=" + tooltip + ", callback=" + callback + "]";
		}
	}

	public static class Prompt {
		private final String			prompt;
		private final PromptCallback	callback;
		
		protected interface PromptCallback {
			String getMenuPrompt(final String name);
		}
		
		public Prompt(final String text) {
			if (text == null || text.isEmpty()) {
				throw new IllegalArgumentException("Prompt text can't be null or empty string");
			}
			else {
				this.prompt = text;
				this.callback = null;
			}
		}
		
		public Prompt(final PromptCallback callback) {
			if (callback == null) {
				throw new IllegalArgumentException("Prompt callback can't be null");
			}
			else {
				this.prompt = null;
				this.callback = callback;
			}
		}
		
		public String getPrompt() {
			return prompt != null ? prompt : callback.getMenuPrompt("");
		}

		@Override
		public String toString() {
			return "Prompt [prompt=" + prompt + ", callback=" + callback + "]";
		}
	}

	public static class Callback {
		private final AnyUICallback	callback;
		
		public Callback(final AnyUICallback callback) {
			if (callback == null) {
				throw new IllegalArgumentException("Callback can't be null");
			}
			else {
				this.callback = callback;
			}
		}
		
		public AnyUICallback getCallback() {
			return callback;
		}

		@Override
		public String toString() {
			return "Callback [callback=" + callback + "]";
		}
	}
	
	public static class FSMTerminal {
		private String 	_FSMTerminal;
		
		public FSMTerminal(final String _FSMTerminal) {
			if (_FSMTerminal == null || _FSMTerminal.isEmpty()) {
				throw new IllegalArgumentException("_FSMTerminal can't be null");
			}
			else {
				this._FSMTerminal = _FSMTerminal;
			}
		}
		
		public String getFSMTerminal() {
			return _FSMTerminal;
		}

		@Override
		public String toString() {
			return "FSMTerminal [_FSMTerminal=" + _FSMTerminal + "]";
		}
		
	}

	static class MenuUI implements CommonUICallback {
		ToolTip		tooltip;
		Prompt		prompt;
		MenuIcon	icon;
		FSMTerminal	terminal;

		@Override
		public int execute(final FrameTemplate parent) {
			return 0;
		}

		@Override
		public String toString() {
			return "MenuUI [tooltip=" + tooltip + ", prompt=" + prompt + ", icon=" + icon + ", terminal=" + terminal + "]";
		}
	}
	
	static class OpenFileUI implements CommonUICallback {
		Caption		caption;
		ToolTip		tooltip;
		Prompt		prompt;
		MenuIcon	icon;
		FSMTerminal	terminal;
		Callback	callback;
		
		@Override
		public int execute(final FrameTemplate parent) {
			final JFileChooser	chooser = new JFileChooser();
			
			chooser.setCurrentDirectory(((OpenFileUICallback)callback.getCallback()).getCurrentDir());
			chooser.setMultiSelectionEnabled(((OpenFileUICallback)callback.getCallback()).allowMultiSelection());
			chooser.setFileFilter(((OpenFileUICallback)callback.getCallback()).getFileFilter());
			chooser.setDialogTitle(caption.getCaption());

			final int	result = chooser.showOpenDialog(parent);
			
			if (result == JFileChooser.APPROVE_OPTION) {
				if (((OpenFileUICallback)callback.getCallback()).allowMultiSelection()) {
					parent.processTerminal(terminal.getFSMTerminal(),chooser.getCurrentDirectory(),chooser.getSelectedFiles());
				}
				else {
					parent.processTerminal(terminal.getFSMTerminal(),chooser.getCurrentDirectory(),chooser.getSelectedFile());
				}
			}
			return result;
		}
	}

	static class SaveFileUI implements CommonUICallback {
		Caption		caption;
		ToolTip		tooltip;
		Prompt		prompt;
		MenuIcon	icon;
		FSMTerminal	terminal;
		Callback	callback;
		
		@Override
		public int execute(final FrameTemplate parent) {
			final JFileChooser	chooser = new JFileChooser();
			
			chooser.setCurrentDirectory(((SaveFileUICallback)callback.getCallback()).getCurrentDir());
			chooser.setMultiSelectionEnabled(((SaveFileUICallback)callback.getCallback()).allowMultiSelection());
			chooser.setFileFilter(((SaveFileUICallback)callback.getCallback()).getFileFilter());
			chooser.setDialogTitle(caption.getCaption());

			final int	result = chooser.showSaveDialog(parent);
			
			if (result == JFileChooser.APPROVE_OPTION) {
				if (((SaveFileUICallback)callback.getCallback()).allowMultiSelection()) {
					parent.processTerminal(terminal.getFSMTerminal(),chooser.getCurrentDirectory(),chooser.getSelectedFiles());
				}
				else {
					parent.processTerminal(terminal.getFSMTerminal(),chooser.getCurrentDirectory(),chooser.getSelectedFile());
				}
			}
			return result;
		}
	}

	static class QuitUI implements CommonUICallback {
		ToolTip		tooltip;
		Prompt		prompt;
		MenuIcon	icon;
		FSMTerminal	terminal;

		@Override
		public int execute(final FrameTemplate parent) {
			if (terminal != null) {
				parent.processTerminal(terminal.getFSMTerminal());
			}
			else {
				parent.processQuit();
			}
			return 0;
		}

		@Override
		public String toString() {
			return "QuitUI [tooltip=" + tooltip + ", prompt=" + prompt + ", icon=" + icon + ", terminal=" + terminal + "]";
		}
	}

	static class LookAndFeelUI implements CommonUICallback {
		String		lookAndFeelClassName;
		ToolTip		tooltip;
		
		@Override
		public int execute(final FrameTemplate parent) {
			try{UIManager.setLookAndFeel(lookAndFeelClassName);
				SwingUtilities.updateComponentTreeUI(parent);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
				parent.message(Severity.error,"Selected Look &amp; feel can't be set: %1$s",e.getMessage());
			}
			return 0;
		}
	}
	
	static class HelpUI {
		
	}

	static class AboutUI implements CommonUICallback {
		Caption		caption;
		Content		content;
		ToolTip		tooltip;
		MenuIcon	icon;
		
		@Override
		public int execute(final FrameTemplate parent) {
			final JPanel	panel = new JPanel(new BorderLayout());
			
			if (icon != null) {
				panel.add(new JLabel(icon.getIcon()),BorderLayout.WEST);
			}
			panel.add(new JLabel(content.getContent()),BorderLayout.CENTER);
			JOptionPane.showMessageDialog(parent,panel,caption.getCaption(),JOptionPane.PLAIN_MESSAGE);
			return JOptionPane.OK_OPTION;
		}
	}

	static class DialogUI {
		
	}

	static class WizardUI {
		
	}
	
	static class CargoMenuItem<T extends CommonUICallback> extends JMenuItem implements CargoItem<T> {
		private static final long serialVersionUID = -2618446395663565529L;

		private final T	cargo;
		
		CargoMenuItem(final T cargo) {
			this.cargo = cargo;
		}
		
		CargoMenuItem(final T cargo, final Action a) {
			super(a);
			this.cargo = cargo;
		}
		
		CargoMenuItem(final T cargo, final Icon icon) {
			super(icon);
			this.cargo = cargo;
		}
		
		CargoMenuItem(final T cargo, final String text) {
			super(text);
			this.cargo = cargo;
		}
		
		CargoMenuItem(final T cargo, final String text, final Icon icon) {
			super(text,icon);
			this.cargo = cargo;
		}
		
		CargoMenuItem(final T cargo, final String text, final int mnemonic) {
			super(text,mnemonic);
			this.cargo = cargo;
		}		
		
		@Override
		public T getCargo() {
			return cargo;
		}
	}

	static class CargoCheckBoxMenuItem<T extends CommonUICallback> extends JCheckBoxMenuItem implements CargoItem<T> {
		private static final long serialVersionUID = 7391015352976487237L;

		private final T	cargo;
		
		public CargoCheckBoxMenuItem(final T cargo) {
			super();
			this.cargo = cargo;
		}

		public CargoCheckBoxMenuItem(final T cargo, final Action a) {
			super(a);
			this.cargo = cargo;
		}

		public CargoCheckBoxMenuItem(final T cargo, final Icon icon) {
			super(icon);
			this.cargo = cargo;
		}

		public CargoCheckBoxMenuItem(final T cargo, final String text, final boolean b) {
			super(text, b);
			this.cargo = cargo;
		}

		public CargoCheckBoxMenuItem(final T cargo, final String text, final Icon icon, final boolean b) {
			super(text, icon, b);
			this.cargo = cargo;
		}

		public CargoCheckBoxMenuItem(final T cargo, final String text, final Icon icon) {
			super(text, icon);
			this.cargo = cargo;
		}

		public CargoCheckBoxMenuItem(final T cargo, final String text) {
			super(text);
			this.cargo = cargo;
		}

		@Override
		public T getCargo() {
			return cargo;
		}
	}
}
