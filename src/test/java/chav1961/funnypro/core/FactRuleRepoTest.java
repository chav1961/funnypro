package chav1961.funnypro.core; 

import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProRepo.NameAndArity;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class FactRuleRepoTest {
	@Test
	public void basicTest() {
		final SubstitutableProperties	props = new SubstitutableProperties();
		final SyntaxTreeInterface<?>	sti = new AndOrTree<>();
		
		final FactRuleRepo	frr = new FactRuleRepo(PureLibSettings.CURRENT_LOGGER, props, sti);
		
		Assert.assertEquals(PureLibSettings.CURRENT_LOGGER,frr.getDebug());
		Assert.assertEquals(props,frr.getParameters());
		Assert.assertFalse(frr.canUseInMultiThread());
		Assert.assertFalse(frr.canUseForMultiThreadOnResolution());
		
		try {new FactRuleRepo(null, props, sti);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {new FactRuleRepo(PureLibSettings.CURRENT_LOGGER, null, sti);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try {new FactRuleRepo(PureLibSettings.CURRENT_LOGGER, props, null);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void assertAndRetractTest() {
		final SubstitutableProperties	props = new SubstitutableProperties();
		final SyntaxTreeInterface<?>	sti = new AndOrTree<>();
		final FactRuleRepo		frr = new FactRuleRepo(PureLibSettings.CURRENT_LOGGER, props, sti);
		final PredicateEntity	pe1 = new PredicateEntity(100, new IntegerEntity(100)), pe2 = new PredicateEntity(100, new IntegerEntity(200)), pe3 = new PredicateEntity(100, new IntegerEntity(300));
		final PredicateEntity	peCall = new PredicateEntity(100, new AnonymousEntity());
		final OperatorEntity	oe1 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200));
		final OperatorEntity	oe2 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(300)).setRight(new IntegerEntity(400));
		final OperatorEntity	oeCall = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new AnonymousEntity()).setRight(new AnonymousEntity());
		int			count;
		
		frr.assertZ(pe1);
		frr.assertA(pe2);
		frr.assertZ(pe3);
		count = 0;
		for (NameAndArity item : frr.content(1,100)) {
			Assert.assertEquals(EntityType.predicate,item.getType());
			Assert.assertEquals(1,item.getArity());
			Assert.assertEquals(100,item.getId());
			count++;
		}
		Assert.assertEquals(1,count); 

		count = 0;
		for (IFProEntity item : frr.call(peCall,0)) {
			Assert.assertEquals(EntityType.predicate,item.getEntityType());
			Assert.assertEquals(1,((IFProPredicate)item).getArity());
			Assert.assertEquals(100,item.getEntityId());
			count++;
		}
		Assert.assertEquals(3,count); 
		
		frr.assertZ(oe1);
		frr.assertA(oe2);
		count = 0;
		for (NameAndArity item : frr.content(2,200)) {
			Assert.assertEquals(EntityType.operator,item.getType());
			Assert.assertEquals(2,item.getArity());
			Assert.assertEquals(200,item.getId());
			count++;
		}
		Assert.assertEquals(1,count);

		count = 0;
		for (IFProEntity item : frr.call(oeCall,0)) {
			Assert.assertEquals(EntityType.operator,item.getEntityType());
			Assert.assertEquals(200,item.getEntityId());
			count++;
		}
		Assert.assertEquals(2,count); 
		
		count = 0;
		for (NameAndArity item : frr.content(-1)) {
			count++;
		}
		Assert.assertEquals(2,count);
		
		try {frr.assertZ(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {frr.assertZ(new IntegerEntity(100));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}

		try {frr.assertA(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {frr.assertA(new IntegerEntity(100));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		 
		try {frr.call(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {frr.call(new IntegerEntity(100));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		
		frr.retractAll(200,2);
		count = 0;
		for (NameAndArity item : frr.content(2,200)) {
			count++;
		}
		Assert.assertEquals(0,count);

		frr.retractAll(new PredicateEntity(100, new IntegerEntity(400)));
		count = 0;
		for (IFProEntity item : frr.call(peCall)) {
			Assert.assertEquals(EntityType.predicate,item.getEntityType());
			Assert.assertEquals(1,((IFProPredicate)item).getArity());
			Assert.assertEquals(100,item.getEntityId());
			count++;
		}
		Assert.assertEquals(3,count); 

		frr.retractAll(new PredicateEntity(100, new IntegerEntity(200)));
		count = 0;
		for (IFProEntity item : frr.call(peCall)) {
			Assert.assertEquals(EntityType.predicate,item.getEntityType());
			Assert.assertEquals(1,((IFProPredicate)item).getArity());
			Assert.assertEquals(100,item.getEntityId());
			count++;
		}
		Assert.assertEquals(2,count); 

		frr.retractFirst(new PredicateEntity(100, new IntegerEntity(200)));
		count = 0;
		for (IFProEntity item : frr.call(peCall)) {
			Assert.assertEquals(EntityType.predicate,item.getEntityType());
			Assert.assertEquals(1,((IFProPredicate)item).getArity());
			Assert.assertEquals(100,item.getEntityId());
			count++;
		}
		Assert.assertEquals(2,count); 

		frr.retractFirst(new PredicateEntity(100, new AnonymousEntity()));
		count = 0;
		for (IFProEntity item : frr.call(peCall)) {
			Assert.assertEquals(EntityType.predicate,item.getEntityType());
			Assert.assertEquals(1,((IFProPredicate)item).getArity());
			Assert.assertEquals(100,item.getEntityId());
			count++;
		}
		Assert.assertEquals(1,count); 

		frr.assertZ(oe1);
		frr.assertA(oe2);
		 
		try {frr.assertZ(oe1);
			Assert.fail("Mandatory exception was not detected (attempt to insert the same item twise)");
		} catch (IllegalArgumentException exc) {
		}
		
		frr.retractFirst(new OperatorEntity(999,OperatorType.xfx,200));
		count = 0;
		for (IFProEntity item : frr.call(oeCall)) {
			Assert.assertEquals(EntityType.operator,item.getEntityType());
			Assert.assertEquals(200,item.getEntityId());
			count++;
		}
		Assert.assertEquals(1,count); 
		
		try {frr.retractAll(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {frr.retractAll(new IntegerEntity(100));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try {frr.retractFirst(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {frr.retractFirst(new IntegerEntity(100));
			Assert.fail("Mandatory exception was not detected (illegal 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void serializationTest() throws IOException {
		final SubstitutableProperties	props = new SubstitutableProperties();
		final SyntaxTreeInterface<?>	sti = new AndOrTree<>();
		final FactRuleRepo		frr = new FactRuleRepo(PureLibSettings.CURRENT_LOGGER, props, sti);
		final PredicateEntity	pe1 = new PredicateEntity(100, new IntegerEntity(100)), pe2 = new PredicateEntity(100, new IntegerEntity(200)), pe3 = new PredicateEntity(100, new IntegerEntity(300));
		final PredicateEntity	peCall = new PredicateEntity(100, new AnonymousEntity());
		final OperatorEntity	oe1 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200));
		final OperatorEntity	oe2 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(300)).setRight(new IntegerEntity(400));
		final OperatorEntity	oeCall = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new AnonymousEntity()).setRight(new AnonymousEntity());

		frr.assertA(pe1);
		frr.assertZ(pe2);
		frr.assertA(pe3);
		
		frr.assertA(oe1);
		frr.assertZ(oe2);
		
		final FactRuleRepo		newFrr = new FactRuleRepo(PureLibSettings.CURRENT_LOGGER, props, sti);
		
		try(final InOutGrowableByteArray	iogba = new InOutGrowableByteArray(false)) {
			frr.serialize(iogba);
			iogba.flush();
			iogba.reset();
			newFrr.deserialize(iogba);
		}
		
		int count = 0;
		for (IFProEntity item : newFrr.call(peCall)) {
			Assert.assertEquals(EntityType.predicate,item.getEntityType());
			Assert.assertEquals(1,((IFProPredicate)item).getArity());
			Assert.assertEquals(100,item.getEntityId());
			count++;
		}
		Assert.assertEquals(3,count); 

		count = 0;
		for (IFProEntity item : newFrr.call(oeCall)) {
			Assert.assertEquals(EntityType.operator,item.getEntityType());
			Assert.assertEquals(200,item.getEntityId());
			count++;
		}
		Assert.assertEquals(2,count);
		
		try {frr.serialize(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try {frr.deserialize(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}

}
