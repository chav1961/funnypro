package chav1961.funnypro.core;

import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.VariableEntity;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProRepo;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;

public class IterablesCollectionTest {
	@Test
	public void test() throws NullPointerException, IOException {
		final Properties			props = Utils.mkProps();
		
		try(final EntitiesRepo		entities = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER, props)) {
			final IFProRepo			frr = entities.predicateRepo(); 
			final PredicateEntity	pe1 = new PredicateEntity(100, new IntegerEntity(100)), pe2 = new PredicateEntity(100, new IntegerEntity(200)), pe3 = new PredicateEntity(100, new IntegerEntity(300));
			final PredicateEntity	peCall = new PredicateEntity(100, new AnonymousEntity());
			final OperatorEntity	oe1 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200));
			final OperatorEntity	oe2 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(300)).setRight(new IntegerEntity(400));
			final OperatorEntity	oeCall = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new AnonymousEntity()).setRight(new AnonymousEntity());
			
			frr.assertZ(pe1);
			frr.assertZ(pe2);
			frr.assertZ(pe3);
			frr.assertZ(oe1);
			frr.assertZ(oe2);
			
			int	count = 0;
			for (IFProEntity item : new IterablesCollection.IterableNameAndArity(entities,new OperatorEntity(100,OperatorType.xfx,100).setLeft(new VariableEntity(200)).setRight(new VariableEntity(300)))) {
				count++;
			}
			Assert.assertEquals(2,count);
		}
	}
}
