package chav1961.funnypro.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import chav1961.funnypro.core.interfaces.IFProDebug;


class DefaultDebugger implements IFProDebug {
	private static final Map<String,Logger>	loggerCache = new HashMap<String,Logger>();
	
	private final Properties			props;
	private final Logger				log;
	private final boolean				inTransaction;
	private final long					transactionStart;
	private final String				mark;
	private final List<LoggerRecord>	history = new ArrayList<>();
	
	public DefaultDebugger(final Properties props) {
		if (props == null) {
			throw new IllegalArgumentException("Properties can't be null"); 
		}
		else {
			this.log = Logger.getGlobal();
			this.inTransaction = false;
			this.transactionStart = 0;
			this.mark = null;
			this.props = props;
		}
	}

	private DefaultDebugger(final Logger log, final String mark, final Properties props) {
		this.log = log;
		this.inTransaction = true;
		this.transactionStart = System.currentTimeMillis();
		this.mark = mark;
		this.props = props;
	}
	
	@Override
	public void close() throws IOException {
		if (isInsideTransaction()) {
			for (LoggerRecord item : history) {
				final String	content = String.format("%1$s: %2$9d ms %3$s", item.mark, item.timestamp
										, item.parameters == null || item.parameters.length == 0 
											? item.format 
											: String.format(item.format, item.parameters)
										);
				switch (item.level) {
					case trace		:	directPrint(Level.FINEST,content,item.t);	break;
					case debug		:	directPrint(Level.FINE,content,item.t);		break;
					case info		:	directPrint(Level.INFO,content,item.t);		break;
					case warning	:	directPrint(Level.WARNING,content,item.t);	break;
					case error		:	directPrint(Level.SEVERE,content,item.t);	break;
					default			:	throw new UnsupportedOperationException("Unsupported severity level ["+item.level+"] detected!");
				}
			}
			history.clear();
		}
	}

	@Override
	public IFProDebug push(final String mark) {
		if (mark == null || mark.isEmpty()) {
			throw new IllegalArgumentException("Mark can't be null"); 
		}
		else if (!mark.contains(":")) {
			throw new IllegalArgumentException("Illegal mark format. Need to be <loggerName>:<anySequence>"); 
		}
		else {
			final String	logger = mark.substring(0,mark.indexOf(':')), seq = mark.substring(mark.indexOf(':')+1);
			final Logger	nested;
			
			synchronized(loggerCache) {
				if (!loggerCache.containsKey(logger)) {
					loggerCache.put(logger,Logger.getLogger(logger));
				}
				nested = loggerCache.get(logger);
			}
			
			return new DefaultDebugger(nested, seq, props);
		}
	}

	@Override
	public void log(final SeverityLevel level, final String format, final Object... parameters) {
		log(level,null,format,parameters);
	}

	@Override
	public void log(final SeverityLevel level, final Throwable t, final String format, final Object... parameters) {
		if (level == null) {
			throw new IllegalArgumentException("Level can't be null"); 
		}
		else if (format == null || format.isEmpty()) {
			throw new IllegalArgumentException("Format can't be null or empty"); 
		}
		else if (isInsideTransaction()) {
			history.add(this.new LoggerRecord(level,t,format,parameters));
		}
		else {
			switch (level) {
				case trace		:
					directPrint(Level.FINEST,parameters == null || parameters.length == 0 ? format : String.format(format, parameters),t);
					break;
				case debug		:
					directPrint(Level.FINE,parameters == null || parameters.length == 0 ? format : String.format(format, parameters),t);
					break;
				case info		:
					directPrint(Level.INFO,parameters == null || parameters.length == 0 ? format : String.format(format, parameters),t);
					break;
				case warning	:
					directPrint(Level.WARNING,parameters == null || parameters.length == 0 ? format : String.format(format, parameters),t);
					break;
				case error		:
					directPrint(Level.SEVERE,parameters == null || parameters.length == 0 ? format : String.format(format, parameters),t);
					break;
				default	:
					throw new UnsupportedOperationException("Unsupported severity level ["+level+"] detected!");
			}
		}
	}

	@Override
	public boolean isInsideTransaction() {
		return inTransaction;
	}

	@Override
	public void rollback() {
		history.clear();
	}

	private void directPrint(final Level level, final String content, final Throwable t) {
		if (t == null) {
			log.log(level, content);
		}
		else {
			log.log(level, content, t);
		}
	}
	
	private class LoggerRecord {
		public long				timestamp;
		public String			mark;
		public SeverityLevel	level;
		public String			format;
		public Object[]			parameters;
		public Throwable		t;
		
		public LoggerRecord(final SeverityLevel level, final Throwable t, final String format, final Object[] parameters) {
			this.timestamp = System.currentTimeMillis() - transactionStart;
			this.mark = DefaultDebugger.this.mark;
			this.level = level;
			this.format = format;
			this.t = t;
			this.parameters = parameters;
		}

		@Override
		public String toString() {
			return "LoggerRecord [timestamp=" + timestamp + ", mark=" + mark + ", level=" + level + ", format=" + format + ", parameters=" + Arrays.toString(parameters) + ", t=" + t + "]";
		}
	}
}
