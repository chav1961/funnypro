package chav1961.funnypro.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({QuickListTest.class, FProUtilTest.class,
		ParserAndPrinterTest.class, RepositoriesTest.class , VMTest.class,
		GlobalStackTest.class, StandardResolverTest.class})
public class AllTests {

}
