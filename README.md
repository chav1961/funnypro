# Funny Prolog

This project is a simple Prolog language interpreter, based on **I.Bratko** book predicates/operators set. This interpreter is written on Java, and can be used both console-oriented application and a new Script language inside Java VM (using standard **javax.script** package functionality).

## Getting Started

### Prerequisites

To use Funny Prolog interpreter, you need installed Java 1.8 version 31 and later. Goto [ORACLE Java download](https://www.oracle.com/downloads/index.html) center and download this software

### Installing

Download [executable jar](https://github.com/chav1961/funnypro/blob/mvn-repo/com/github/chav1961/funnypro/0.0.1/funnypro-0.0.1.jar) or whole project from [GitHub](https://github.com/chav1961/funnypro) repository and place it to any directory you wish. Use [Maven](https://maven.apache.org/) to get access to the project:

```XML
	<dependency>
		<groupId>com.github.chav1961</groupId>
		<artifactId>funnypro</artifactId>
		<version>0.0.1</version>
	<dependency>
```

Also add repository GitHub repository description to your pom file:

```XML
	<repositories>
	    <repository>
	        <id>purelib-mvn-repo</id>
	        <url>https://raw.github.com/chav1961/funnypro/mvn-repo/</url>
	        <snapshots>
	            <enabled>true</enabled>
	            <updatePolicy>always</updatePolicy>
	        </snapshots>
	    </repository>
	</repositories>
```

To use this project as a standalone console application (command string mode), type :

```
java -jar funnypro-0.0.1.jar
```

After prompt appears, type any Prolog sentences. Type "**quit.**" to exit from the console application.

To use this project inside Java VM, include this archive in your classpath. To get access to Funny Prolog interpreter from Java VM, use code template:

```Java
    . . .
    final ScriptEngineManager factory = new ScriptEngineManager();
    final ScriptEngine engine = factory.getEngineByName("FunnyProlog");
    . . .
```

See Java API for **javax.script** package and JavaDoc of this project for more details

## Built With

* [Eclipse](www.eclipse.org) - The Eclipse IDE
* [Maven](https://maven.apache.org/) - Dependency Management
* [Pure Lib](https://github.com/chav1961/purelib) - My project with a set of useful classes

## Authors

* **Alexander Charnomyrdin** - *Initial work* 

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* **I.Bratko** as indirect inspirator of the project

