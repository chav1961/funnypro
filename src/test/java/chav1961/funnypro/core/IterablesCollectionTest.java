package chav1961.funnypro.core;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
import chav1961.funnypro.core.entities.VariableEntity;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProRepo;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;

public class IterablesCollectionTest {
	@Test
	public void nameAndArityTest() throws NullPointerException, IOException {
		final SubstitutableProperties		props = new SubstitutableProperties();
		
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
				Assert.assertTrue(((IFProOperator)item).getLeft().getEntityId() == 100 || ((IFProOperator)item).getLeft().getEntityId() == 200);
				Assert.assertTrue(((IFProOperator)item).getRight().getEntityId() == 1 || ((IFProOperator)item).getRight().getEntityId() == 2);
				count++;
			}
			Assert.assertEquals(2,count);

			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableNameAndArity(entities,new OperatorEntity(100,OperatorType.xfx,100).setLeft(new PredicateEntity(100)).setRight(new VariableEntity(300)))) {
				Assert.assertEquals(100,((IFProOperator)item).getLeft().getEntityId());
				Assert.assertEquals(1,((IFProOperator)item).getRight().getEntityId());
				count++;
			}
			Assert.assertEquals(1,count);

			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableNameAndArity(entities,new OperatorEntity(100,OperatorType.xfx,100).setLeft(new VariableEntity(200)).setRight(new IntegerEntity(2)))) {
				Assert.assertEquals(200,((IFProOperator)item).getLeft().getEntityId());
				Assert.assertEquals(2,((IFProOperator)item).getRight().getEntityId());
				count++;
			}
			Assert.assertEquals(1,count);

			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableNameAndArity(entities,new OperatorEntity(100,OperatorType.xfx,100).setLeft(new PredicateEntity(100)).setRight(new IntegerEntity(1)))) {
				Assert.assertEquals(100,((IFProOperator)item).getLeft().getEntityId());
				Assert.assertEquals(1,((IFProOperator)item).getRight().getEntityId());
				count++;
			}
			Assert.assertEquals(1,count);

			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableNameAndArity(entities,new OperatorEntity(100,OperatorType.xfx,100).setLeft(new PredicateEntity(100)).setRight(new IntegerEntity(2)))) {
				count++;
			}
			Assert.assertEquals(0,count);
			
			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableNameAndArity(entities,new OperatorEntity(100,OperatorType.xfx,100).setLeft(new PredicateEntity(100)).setRight(new PredicateEntity(100)))) {
				count++;
			}
			Assert.assertEquals(0,count);
			
			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableNameAndArity(entities,new OperatorEntity(100,OperatorType.xfx,100).setLeft(new VariableEntity(100)).setRight(new PredicateEntity(100)))) {
				count++;
			}
			Assert.assertEquals(0,count);

			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableNameAndArity(entities,new OperatorEntity(100,OperatorType.xfx,100).setLeft(new IntegerEntity(100)).setRight(new PredicateEntity(100)))) {
				count++;
			}
			Assert.assertEquals(0,count);
			
			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableNameAndArity(entities,new OperatorEntity(100,OperatorType.xfx,100))) {
				count++;
			}
			Assert.assertEquals(0,count);
			
			try {new IterablesCollection.IterableNameAndArity(null,new OperatorEntity(100,OperatorType.xfx,100));
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {new IterablesCollection.IterableNameAndArity(entities,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
		}
	} 

	@Test
	public void callTest() throws NullPointerException, IOException {
		final SubstitutableProperties		props = new SubstitutableProperties();
		
		try(final EntitiesRepo		entities = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER, props)) {
			final IFProRepo			frr = entities.predicateRepo(); 
			final PredicateEntity	pe1 = new PredicateEntity(100, new IntegerEntity(100)), pe2 = new PredicateEntity(100, new IntegerEntity(200)), pe3 = new PredicateEntity(100, new IntegerEntity(300));
			final OperatorEntity	oe1 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200));
			final OperatorEntity	oe2 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(300)).setRight(new IntegerEntity(400));
			
			frr.assertZ(pe1);
			frr.assertZ(pe2);
			frr.assertZ(pe3); 
			frr.assertZ(oe1);
			frr.assertZ(oe2);
			
			final Set<Long>			selected = new HashSet<>();
			for (IFProEntity item : new IterablesCollection.IterableCall(entities,new PredicateEntity(1000,new PredicateEntity(100, new AnonymousEntity())))) {
				Assert.assertEquals(EntityType.predicate,item.getEntityType());
				Assert.assertEquals(100,item.getEntityId());
				Assert.assertEquals(1,((IFProPredicate)item).getArity());
				selected.add(((IFProPredicate)item).getParameters()[0].getEntityId());
			}
			Assert.assertEquals(3,selected.size());
			
			selected.clear();
			for (IFProEntity item : new IterablesCollection.IterableCall(entities,new PredicateEntity(1000,new PredicateEntity(100, new IntegerEntity(200))))) {
				Assert.assertEquals(EntityType.predicate,item.getEntityType());
				Assert.assertEquals(100,item.getEntityId());
				Assert.assertEquals(1,((IFProPredicate)item).getArity());
				selected.add(((IFProPredicate)item).getParameters()[0].getEntityId());
			}
			Assert.assertEquals(1,selected.size());

			selected.clear();
			for (IFProEntity item : new IterablesCollection.IterableCall(entities,new PredicateEntity(1000,new OperatorEntity(999,OperatorType.xfx,200).setLeft(new AnonymousEntity()).setRight(new AnonymousEntity())))) {
				Assert.assertEquals(EntityType.operator,item.getEntityType());
				Assert.assertEquals(200,item.getEntityId());
				Assert.assertEquals(999,((IFProOperator)item).getPriority());
				selected.add(((IFProOperator)item).getLeft().getEntityId());
				selected.add(((IFProOperator)item).getRight().getEntityId());
			}
			Assert.assertEquals(4,selected.size());

			selected.clear();
			for (IFProEntity item : new IterablesCollection.IterableCall(entities,new PredicateEntity(1000,new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(100)).setRight(new AnonymousEntity())))) {
				Assert.assertEquals(EntityType.operator,item.getEntityType());
				Assert.assertEquals(200,item.getEntityId());
				Assert.assertEquals(999,((IFProOperator)item).getPriority());
				selected.add(((IFProOperator)item).getLeft().getEntityId());
				selected.add(((IFProOperator)item).getRight().getEntityId());
			}
			Assert.assertEquals(2,selected.size());
			
			int 	count = 0;
			for (IFProEntity item : new IterablesCollection.IterableCall(entities,new PredicateEntity(1000))) {
				count++;
			}
			Assert.assertEquals(0,count);

			try {new IterablesCollection.IterableCall(null,new PredicateEntity(1000));
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {new IterablesCollection.IterableCall(entities,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

	@Test
	public void bagofTest() throws NullPointerException, IOException {
		final SubstitutableProperties		props = new SubstitutableProperties();
		
		try(final EntitiesRepo		entities = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER, props)) {
			final IFProRepo			frr = entities.predicateRepo(); 
			final PredicateEntity	pe1 = new PredicateEntity(100, new IntegerEntity(100)), pe2 = new PredicateEntity(100, new IntegerEntity(200)), pe3 = new PredicateEntity(100, new IntegerEntity(300));
			final OperatorEntity	oe1 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200));
			final OperatorEntity	oe2 = (OperatorEntity) new OperatorEntity(999,OperatorType.xfx,200).setLeft(new IntegerEntity(300)).setRight(new IntegerEntity(400));
			
			frr.assertZ(pe1);
			frr.assertZ(pe2);
			frr.assertZ(pe3); 
			frr.assertZ(oe1);
			frr.assertZ(oe2);
			
			final Set<Long>			selected = new HashSet<>();
			
			final VariableEntity	ve1 = new VariableEntity(100), ve2 = new VariableEntity(100);
			final PredicateEntity	pr1 = new PredicateEntity(200, ve1), pr2 = new PredicateEntity(100, ve2);
			
			ve1.setChain(ve2);		// Link identical variables
			ve2.setChain(ve1);
			
			for (IFProEntity item : new IterablesCollection.IterableCallBagof(entities,new PredicateEntity(1,pr1,pr2,new AnonymousEntity()))) {
				Assert.assertEquals(EntityType.predicate,item.getEntityType());
				Assert.assertEquals(200,item.getEntityId());
				Assert.assertEquals(1,((IFProPredicate)item).getArity());
				selected.add(((IFProPredicate)item).getParameters()[0].getEntityId());
			}
			Assert.assertEquals(3,selected.size());

			final PredicateEntity	pr3 = new PredicateEntity(200, new AnonymousEntity()), pr4 = new PredicateEntity(100, new IntegerEntity(300));

			selected.clear();
			for (IFProEntity item : new IterablesCollection.IterableCallBagof(entities,new PredicateEntity(1,pr3,pr4,new AnonymousEntity()))) {
				Assert.assertEquals(EntityType.predicate,item.getEntityType());
				Assert.assertEquals(200,item.getEntityId());
				Assert.assertEquals(1,((IFProPredicate)item).getArity());
				selected.add(((IFProPredicate)item).getParameters()[0].getEntityId());
			}
			Assert.assertEquals(1,selected.size());
			
			int	count = 0;
			for (IFProEntity item : new IterablesCollection.IterableCallBagof(entities,new PredicateEntity(1,new AnonymousEntity(),new IntegerEntity(100),new AnonymousEntity()))) {
				count++;
			}
			Assert.assertEquals(0,count); 
			
			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableCallBagof(entities,new PredicateEntity(1))) {
				count++;
			}
			Assert.assertEquals(0,count);
			
			try {new IterablesCollection.IterableCallBagof(null,new PredicateEntity(1000));
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {new IterablesCollection.IterableCallBagof(entities,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

	@Test
	public void listTest() throws NullPointerException, IOException {
		final SubstitutableProperties		props = new SubstitutableProperties();
		
		try(final EntitiesRepo		entities = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER, props)) {
			final IFProList			start = new ListEntity(new IntegerEntity(100), new ListEntity(new RealEntity(100), null));
		
			int	count = 0;
			for (IFProEntity item : new IterablesCollection.IterableList(new PredicateEntity(1,new VariableEntity(100),start))) {
				count++;
			}
			Assert.assertEquals(2,count);

			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableList(new PredicateEntity(1,new IntegerEntity(100),start))) {
				count++;
			}
			Assert.assertEquals(1,count);

			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableList(new PredicateEntity(1,new IntegerEntity(200),start))) {
				count++;
			}
			Assert.assertEquals(0,count); 

			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableList(new PredicateEntity(1,new IntegerEntity(100),new IntegerEntity(200)))) {
				count++;
			}
			Assert.assertEquals(0,count); 

			count = 0;
			for (IFProEntity item : new IterablesCollection.IterableList(new PredicateEntity(1,start))) {
				count++;
			}
			Assert.assertEquals(0,count); 
 
			try {new IterablesCollection.IterableList(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}
}
