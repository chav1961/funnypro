package chav1961.funnypro.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.EntitiesRepo;
import chav1961.funnypro.core.GlobalDescriptor;
import chav1961.funnypro.core.GlobalStack;
import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.StandardResolver;
import chav1961.funnypro.core.StandardResolverTest;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.funnypro.core.interfaces.IResolvable.ResolveRC;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.charsource.StringCharSource;

public class StringProcessorPluginTest {
	@Test
	public void basicTest() {
		final StringProcessorPlugin	spp = new StringProcessorPlugin();
		
		Assert.assertEquals(StringProcessorPlugin.PLUGIN_NAME, spp.getName());
		Assert.assertArrayEquals(StringProcessorPlugin.PLUGIN_VERSION, spp.getVersion());
		
		final PluginDescriptor[]	desc = spp.getPluginDescriptors();
		
		Assert.assertNotNull(desc);
		Assert.assertEquals(1,desc.length);
		Assert.assertNotNull(desc[0].getPluginEntity());
	}

	@Test
	public void lifeCycleTest() throws ContentException, NullPointerException, IOException {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		props.setProperty(IFProVM.PROP_DONT_LOAD_ALL_PLUGINS, "true");
		
		final StandardResolver		sr = new StandardResolver();
		final StringProcessorPlugin	spp = new StringProcessorPlugin();
		final IFProEntitiesRepo		repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER, props);
		final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER, props, repo);
		final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER, props,repo);
		final GlobalDescriptor		srg = sr.onLoad(PureLibSettings.CURRENT_LOGGER, props, repo);
		final StringProcessorGlobal	spg = spp.onLoad(PureLibSettings.CURRENT_LOGGER, props, repo);
		final StringBuilder			sb = new StringBuilder();
		
		// split/3 predicate test
		pap.parseEntities(new StringCharSource("?- split(X,\":\",[\"left\",\"right\"]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			Assert.assertEquals("Call:\nX=\"left:right\"\n", sb.toString());
			return true;
		});
		pap.parseEntities(new StringCharSource("?- split(X,\":\",[]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			Assert.assertEquals("Call:\nX=\"\"\n", sb.toString());
			return true;
		});
		pap.parseEntities(new StringCharSource("?- split(X,\"\",[\"left\",\"right\"]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			Assert.assertEquals("Call:\nX=\"leftright\"\n", sb.toString());
			return true;
		});

		pap.parseEntities(new StringCharSource("?- split(\"left:right\",\":\",[\"left\",X]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			Assert.assertEquals("Call:\nX=\"right\"\n", sb.toString());
			return true;
		});
		
		pap.parseEntities(new StringCharSource("?- inList(\"3\",[\"1\",\"2\",\"3\"]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			Assert.assertEquals("Call:\n", sb.toString());
			return true;
		});
		pap.parseEntities(new StringCharSource("?- inList(\"4\",[\"1\",\"2\",\"3\"]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,false,sb);
			return true;
		});
		pap.parseEntities(new StringCharSource("?- inList(\"4\",[]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,false,sb);
			return true;
		});

		pap.parseEntities(new StringCharSource("?- inList(X,[\"1\",\"2\",\"3\"]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			Assert.assertEquals("Call:\nX=\"1\"\nCall:\nX=\"2\"\nCall:\nX=\"3\"\n", sb.toString());
			return true;
		});
		pap.parseEntities(new StringCharSource("?- inList(X,[]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,false,sb);
			return true;
		});
		pap.parseEntities(new StringCharSource("?- inList(X,[\"1\",\"2\"|X]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = sr.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,srg,stack,entity,vars,true,sb);
			Assert.assertEquals("Call:\nX=\"1\"\nCall:\nX=\"2\"\nCall:\nX=X\n", sb.toString());
			return true;
		});
		
		spp.onRemove(spg);
		sr.onRemove(srg);
	}
}
