package chav1961.funnypro.core;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProRepo.NameAndArity;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;

public class FactRuleRepoTest {
	@Test
	public void basicTest() {
		final Properties	props = Utils.mkProps();
		final FactRuleRepo	frr = new FactRuleRepo(PureLibSettings.CURRENT_LOGGER, props);
		
		Assert.assertEquals(PureLibSettings.CURRENT_LOGGER,frr.getDebug());
		Assert.assertEquals(props,frr.getParameters());
		Assert.assertFalse(frr.canUseInMultiThread());
		Assert.assertFalse(frr.canUseForMultiThreadOnResolution());
		
		try {new FactRuleRepo(null, props);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new FactRuleRepo(PureLibSettings.CURRENT_LOGGER, null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void assertAndRetractTest() {
		final Properties		props = Utils.mkProps();
		final FactRuleRepo		frr = new FactRuleRepo(PureLibSettings.CURRENT_LOGGER, props);
		final PredicateEntity	pe1 = new PredicateEntity(100, new IntegerEntity(100)), pe2 = new PredicateEntity(100, new IntegerEntity(200));
		final OperatorEntity	oe1 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200));
		final OperatorEntity	oe2 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(300)).setRight(new IntegerEntity(400));
		int			count;
		
		frr.assertZ(pe1);
		frr.assertA(pe2);
		count = 0;
		for (NameAndArity item : frr.content(1,100)) {
			Assert.assertEquals(EntityType.predicate,item.getType());
			Assert.assertEquals(1,item.getArity());
			Assert.assertEquals(100,item.getId());
			count++;
		}
		Assert.assertEquals(2,count);
		
		frr.assertZ(oe1);
		frr.assertA(oe2);
		count = 0;
		for (NameAndArity item : frr.content(2,200)) {
			Assert.assertEquals(EntityType.operator,item.getType());
			Assert.assertEquals(2,item.getArity());
			Assert.assertEquals(200,item.getId());
			count++;
		}
		Assert.assertEquals(2,count);
		
		try {frr.assertZ(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {frr.assertZ(new IntegerEntity(100));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (UnsupportedOperationException exc) {
		}

		try {frr.assertA(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {frr.assertA(new IntegerEntity(100));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (UnsupportedOperationException exc) {
		}
		
		frr.retractAll(200,2);
		count = 0;
		for (NameAndArity item : frr.content(2,200)) {
			count++;
		}
		Assert.assertEquals(0,count);
	}
}
