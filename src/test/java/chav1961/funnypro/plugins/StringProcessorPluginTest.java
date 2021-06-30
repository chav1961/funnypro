package chav1961.funnypro.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.TestEntityRepo;
import chav1961.funnypro.core.GlobalStack;
import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.StandardResolverTest;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.funnypro.core.interfaces.IResolvable.ResolveRC;
import chav1961.purelib.basic.PureLibSettings;
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
		final StringProcessorPlugin	spp = new StringProcessorPlugin();
		final IFProEntitiesRepo		repo = new TestEntityRepo();
		final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.NULL_LOGGER, Utils.mkProps(), repo);
		final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
		final StringBuilder			sb = new StringBuilder();
		
		final Object				spg = spp.onLoad(PureLibSettings.NULL_LOGGER, Utils.mkProps(), repo);
		
		
		pap.parseEntities(new StringCharSource("split(\"1:2\",\":\",[\"\",\"\"]) ."),(entity,vars)->{
			final IResolvable<?, ?> rec = spp.getPluginDescriptors()[0].getPluginEntity().getResolver();
			
			StandardResolverTest.processing(rec,spg,stack,entity,vars,true,sb);
			return true;
		});

		
		
		spp.onRemove((StringProcessorGlobal)spg);
	}
}
