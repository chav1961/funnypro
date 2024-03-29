package chav1961.funnypro.core;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginItem;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class ExternalPluginsRepoTest {

	@Test
	public void basicAndSPITest() throws SyntaxException, IOException {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		props.setProperty(IFProVM.PROP_DONT_LOAD_ALL_PLUGINS, "true");
		
		try(final EntitiesRepo			entities = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props);
			final ExternalPluginsRepo	repo = new ExternalPluginsRepo(PureLibSettings.CURRENT_LOGGER,props)) {
			final Set<String>			pluginList = new HashSet<>();
			
			Assert.assertEquals(PureLibSettings.CURRENT_LOGGER, repo.getDebug());
			Assert.assertEquals(props,repo.getParameters());
			
			for (PluginItem item : repo.allPlugins()) {
				pluginList.add(item.getDescriptor().getPluginDescription());
			}
			Assert.assertEquals(1 ,pluginList.size());
			
			repo.prepare(entities);
		}
		
		try{new ExternalPluginsRepo(null,props);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new ExternalPluginsRepo(PureLibSettings.CURRENT_LOGGER,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

}
