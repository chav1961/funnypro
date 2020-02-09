module chav1961.funnypro {
	requires java.desktop;
	requires java.scripting;
	requires java.xml;
	requires java.sql;
	requires java.rmi;
	requires java.management;
	requires jdk.httpserver;
	requires java.compiler;
	requires jdk.javadoc;
	requires chav1961.purelib;

	opens chav1961.funnypro to chav1961.purelib;
	
	uses javax.script.ScriptEngineFactory;
	provides javax.script.ScriptEngineFactory with chav1961.funnypro.FunnyProEngineFactory;
	
	uses chav1961.funnypro.core.interfaces.FProPluginList;
	provides chav1961.funnypro.core.interfaces.FProPluginList with 
				chav1961.funnypro.core.StandardResolver,
				chav1961.funnypro.pluginexample.TutorialPlugin;
}
