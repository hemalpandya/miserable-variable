import org.apache.log4j.*;

import java.io.*;
import java.util.*;

public class L{
	
	static Xlogger logger = Xlogger.getLogger(L.class.getName());
	public static void main(String[] args) {   		
		setupLogging(args);

		Xlogger.beginContext();
		logger.debug("*debug main*");		
		sleep();
		logger.info("*info main*");
		foo();
		logger.warn("*warn main*");
		sleep();
		logger.error("*error main*");		
		Xlogger.endContext();
	}

	static void foo() {
		Xlogger.beginContext();
		sleep();
		logger.debug("*debug foo*");		
		sleep();
		logger.info("*info foo*");
		sleep();
		logger.warn("*warn foo*");
		sleep();
		logger.error("*error foo*");		
		Xlogger.endContext();
	}

	static Random random = new Random(System.currentTimeMillis());
	static java.text.SimpleDateFormat dateFormatter = 
		new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

	static void sleep() {
		try {
			Thread.currentThread().sleep(random.nextInt(3000));
		}
		catch (Exception e) {}
	}

	static void setupLogging(String args[]){
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.addAppender(new ConsoleAppender(
										   new PatternLayout("%d{ISO8601}|%t|%m%n")));
		if (args.length == 0) {
			rootLogger.setLevel(Level.DEBUG);
		} else {
			rootLogger.setLevel(Level.toLevel(args[0]));
		}	   
	}

	
	static class Xlogger  {
		static Xlogger getLogger(String name) {
			return new Xlogger(Logger.getLogger(name));
		}
		
		private final Logger _logger;
		private Xlogger(Logger logger) {
			_logger = logger;
		}

		static class LogData {
			final long _ts;
			final String _format;
			final Object _args;
			
			LogData(String format, Object[] args) {
				_ts = System.currentTimeMillis();
				_format = format;
				_args = args;
			}
		}
		
		static class Context {
			static ThreadLocal<Stack<LogData>> _context = new ThreadLocal<Stack<LogData>>()  {
				@Override protected Stack<LogData> initialValue() {
					return new Stack<LogData>();
				}
			};

			static void begin() {
				_context.get().push(null);
			}
			static void end() {
				while (_context.get().pop() != null)
					;
			}

			static void push(String format, Object... args) {
				_context.get().push(new LogData(format, args));
			}
			static LogData pop() {
				return _context.get().pop();
			}			
		}
		
		void debug(String format, Object... args) {
			log(Level.DEBUG, format, args);
		}
		void info(String format, Object... args) {
			log(Level.INFO, format, args);
		}
		void warn(String format, Object... args) {
			log(Level.WARN, format, args);
		}
		void error(String format, Object... args) {
			log(Level.ERROR, format, args);
			LogData logData;
			while ((logData = Context.pop()) != null) {
				log(Level.ERROR, "-->" + dateFormatter.format(new Date(logData._ts)) + ":" + 
					logData._format, logData._args);
			}
			Context.begin();
		}

		void log(Level level, String format, Object... args) {		
			if (_logger.isEnabledFor(level)) {
				_logger.log(level, String.format(format, args));
			} else {
				Context.push(format, args);
			}
		}

		static void beginContext() {
			Context.begin();
		}
		static void endContext() {
			Context.end();
		}
	}
}
