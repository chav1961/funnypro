package chav1961.funnypro.plugins;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.EntitiesRepo;
import chav1961.funnypro.core.GlobalDescriptor;
import chav1961.funnypro.core.GlobalStack;
import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.StandardResolver;
import chav1961.funnypro.core.StandardResolverTest;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.streams.charsource.StringCharSource;

public class DatabaseProcessorPluginTest {

	@Test
	public void basicTest() {
		final DatabaseProcessorPlugin	spp = new DatabaseProcessorPlugin();
		
		Assert.assertEquals(DatabaseProcessorPlugin.PLUGIN_NAME, spp.getName());
		Assert.assertArrayEquals(DatabaseProcessorPlugin.PLUGIN_VERSION, spp.getVersion());
		
		final PluginDescriptor[]	desc = spp.getPluginDescriptors();
		
		Assert.assertNotNull(desc);
		Assert.assertEquals(1,desc.length);
		Assert.assertNotNull(desc[0].getPluginEntity());
	}

	@Test
	public void lifeCycleTest() throws ContentException, NullPointerException, IOException {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		props.setProperty(DatabaseProcessorPlugin.PROP_CONN_STRING, "jdbc:edb://localhost:5444/edb?connectTimeout=0");
		props.setProperty(DatabaseProcessorPlugin.PROP_CONN_USER, "enterprisedb");
		props.setProperty(DatabaseProcessorPlugin.PROP_CONN_PASSWD, "sasa21");
		
		final StandardResolver			sr = new StandardResolver();
		final DatabaseProcessorPlugin	dbpp = new DatabaseProcessorPlugin();
		final IFProEntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER, props);
		final IFProGlobalStack			stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER, props, repo);
		final GlobalDescriptor			srg = sr.onLoad(PureLibSettings.CURRENT_LOGGER, props, repo);
		final ParserAndPrinter			pap = srg.pap;
		final DatabaseProcessorGlobal	spg = dbpp.onLoad(PureLibSettings.CURRENT_LOGGER, props, repo);
		final StringBuilder				sb = new StringBuilder();

		// assertDb/1 predicate test
		sb.setLength(0);	// Full unification
		pap.parseEntities(new StringCharSource("?- assertDb(testtable(f1(1),f2(1),f3(\'one\'))) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			return true;
		});
		// assertDb/2 predicate test
		sb.setLength(0);	// Full unification
		pap.parseEntities(new StringCharSource("?- assertDb(testtable(f1,f2,f3),testtable(f1(1),f2(1),f3(\'one\'))) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			return true;
		});
		
		// callDb/1 predicate test
		sb.setLength(0);	// Full unification
		pap.parseEntities(new StringCharSource("?- callDb(testtable(f1(F1),f2(F2),f3(F3))) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			Assert.assertEquals("Call:\nF1=1\nF2=1.0\nF3=\"one\"\nCall:\nF1=2\nF2=2.0\nF3=\"two\"\nCall:\nF1=3\nF2=3.0\nF3=\"three\"\n", sb.toString());
			return true;
		});
		sb.setLength(0);	// Partial unification
		pap.parseEntities(new StringCharSource("?- callDb(testtable(f1(F1),f2(F2),f3(\"three\"))) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			Assert.assertEquals("Call:\nF1=3\nF2=3.0\n", sb.toString());
			return true;
		});

		// callDb/2 predicate test
		sb.setLength(0);	// Full unification
		pap.parseEntities(new StringCharSource("?- callDb(testtable(f1(F1),f2(F2),f3(F3)),(f3 == \"three\")) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			Assert.assertEquals("Call:\nF1=3\nF2=3.0\nF3=\"three\"\n", sb.toString());
			return true;
		});

		// retractDb/2 predicate test
		sb.setLength(0);	// Full unification
		pap.parseEntities(new StringCharSource("?- retractDb(testtable,(f3 == \"four\")) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,false,sb);
			return true;
		});
		sb.setLength(0);	// Full unification
		pap.parseEntities(new StringCharSource("?- retractDb(testtable,(f3 == \"three\")) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			return true;
		});

		// retractDb/1 predicate test
		sb.setLength(0);	// Full unification
		pap.parseEntities(new StringCharSource("?- retractDb(testtable) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			return true;
		});
		sb.setLength(0);	// Full unification
		pap.parseEntities(new StringCharSource("?- retractDb(testtable) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,false,sb);
			return true;
		});
		
		dbpp.onRemove(spg);
		sr.onRemove(srg);
	}
	
}
