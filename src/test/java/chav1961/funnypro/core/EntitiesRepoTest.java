package chav1961.funnypro.core;

import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.interfaces.IFProRepo;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;

public class EntitiesRepoTest {
	@Test
	public void basicTest() throws RuntimeException, IOException {
		final Properties		props = Utils.mkProps();
		
		try(final EntitiesRepo	repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props)) {
			Assert.assertEquals(PureLibSettings.CURRENT_LOGGER,repo.getDebug());
			Assert.assertEquals(props,repo.getParameters());
		}

		try{new EntitiesRepo(null,props);
			Assert.fail("Mandatory exception wad not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,null);
			Assert.fail("Mandatory exception wad not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void ligeCycleTest() throws RuntimeException, IOException {
		final Properties		props = Utils.mkProps();
		
		try(final EntitiesRepo	repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props)) {
			final IFProRepo 	preds = repo.predicateRepo();
			
		}
	}
}
