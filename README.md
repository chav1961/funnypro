# Funny Prolog

This project is a simple Prolog language interpreter written by Java. Syntax of the Prolog is based on the I.Bratko book. 
It can be used as console application, so can be directly accessed from Java VM via the javax.script abilities.

## Getting Started

This project requires Java SE runtime 1.8-31 or later. Download **funnypro-0.0.1-SNAPSHOT.jar** from the repository to any directory on your computer and type:

````sh
java -jar funnypro-0.0.1-SNAPSHOT.jar
````

To exit from console command mode, type "quit.".

To use this interpreter inside Java VM, include this jar in your classpath. This is an example of the class, that supports console-command mode:

````java
public class Application {
	public static void main(String[] args) throws ScriptException {
		try(final Reader	in = new InputStreamReader(System.in);
			final Writer	out = new OutputStreamWriter(System.out); 
			final Writer	err = new OutputStreamWriter(System.err)) {
			
 			final ScriptEngineManager 	factory = new ScriptEngineManager();
			final ScriptEngine 			engine = factory.getEngineByName("FunnyProlog");

 			((FunnyProEngine)engine).console(in,out,err);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
````

## Documentation

Documentation (Wiki and JavaDoc) will be published in the beginning of June 2017

## Authors

* **ALexander Chernomyrdin** aka chav1961- *Initial work*

Nice to get any feedbacks from us.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

