package chav1961.funnypro.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CommonUtilsTest.class, EntitiesRepoTest.class, ExternalPluginsRepoTest.class, FactRuleRepoTest.class,
		FProUtilTest.class, GlobalStackTest.class, ParserAndPrinterTest.class,
		QuickListTest.class, RepositoriesTest.class, VarRepoTest.class, VMTest.class })

public class AllTests {

}
