package chav1961.funnypro.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
import chav1961.funnypro.core.entities.StringEntity;
import chav1961.funnypro.core.entities.VariableEntity;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProVariable;

public class EntitiesTest {

	@Test
	public void entitiesTest() throws IOException {
		final AnonymousEntity	anon = new AnonymousEntity();
		
		Assert.assertEquals(anon.getEntityId(),IFProEntity.ANON_ENTITY_ID);
		Assert.assertEquals(anon.getEntityType(),EntityType.anonymous);
		Assert.assertEquals(serializeAndDeserialize(anon),anon);
		
		final IntegerEntity		integer = new IntegerEntity(12345);
	
		Assert.assertEquals(integer.getEntityId(),12345);
		Assert.assertEquals(integer.getEntityType(),EntityType.integer);
		Assert.assertEquals(serializeAndDeserialize(integer),integer);
	
		final RealEntity		real = new RealEntity(12345.6789);
		
		Assert.assertEquals(real.getEntityId(),Double.doubleToLongBits(12345.6789));
		Assert.assertEquals(real.getEntityType(),EntityType.real);
		Assert.assertEquals(serializeAndDeserialize(real),real);
	
		final StringEntity		string = new StringEntity(12345);
		
		Assert.assertEquals(string.getEntityId(),12345);
		Assert.assertEquals(string.getEntityType(),EntityType.string);
		Assert.assertEquals(serializeAndDeserialize(string),string);

		final VariableEntity	var = new VariableEntity(12345);
		
		Assert.assertEquals(var.getEntityId(),12345);
		Assert.assertEquals(var.getEntityType(),EntityType.variable);
		Assert.assertEquals(((IFProVariable)var).getChain(),var);
		Assert.assertEquals(serializeAndDeserialize(var),var);
		
		ListEntity		list = new ListEntity(integer,real);
		
		Assert.assertEquals(list.getEntityId(),IFProEntity.LIST_ENTITY_ID);
		Assert.assertEquals(list.getEntityType(),EntityType.list);
		Assert.assertEquals(list.getChild(),integer);
		Assert.assertEquals(list.getTail(),real);
		Assert.assertEquals(serializeAndDeserialize(list),list);

		OperatorEntity	op = new OperatorEntity(100,OperatorType.xfx,12345);
		
		op.setLeft(anon).setRight(string);
		
		Assert.assertEquals(op.getEntityId(),12345);
		Assert.assertEquals(op.getEntityType(),EntityType.operator);
		Assert.assertEquals(op.getPriority(),100);
		Assert.assertEquals(op.getOperatorType(),OperatorType.xfx);
		Assert.assertEquals(op.getLeft(),anon);
		Assert.assertEquals(op.getRight(),string);		
		Assert.assertEquals(serializeAndDeserialize(op),op);

		op = new OperatorEntity(100,OperatorType.fx,12345);
		
		op.setRight(string);
		
		Assert.assertEquals(op.getEntityId(),12345);
		Assert.assertEquals(op.getEntityType(),EntityType.operator);
		Assert.assertEquals(op.getPriority(),100);
		Assert.assertEquals(op.getOperatorType(),OperatorType.fx);
		Assert.assertNull(op.getLeft());
		Assert.assertEquals(op.getRight(),string);
		Assert.assertEquals(serializeAndDeserialize(op),op);

		op = new OperatorEntity(100,OperatorType.xf,12345);
		
		op.setLeft(string);
		
		Assert.assertEquals(op.getEntityId(),12345);
		Assert.assertEquals(op.getEntityType(),EntityType.operator);
		Assert.assertEquals(op.getPriority(),100);
		Assert.assertEquals(op.getOperatorType(),OperatorType.xf);
		Assert.assertEquals(op.getLeft(),string);
		Assert.assertNull(op.getRight());
		Assert.assertEquals(serializeAndDeserialize(op),op);
		
		final PredicateEntity	pred = new PredicateEntity(12345,integer,real,string);

		Assert.assertEquals(pred.getEntityId(),12345);
		Assert.assertEquals(pred.getEntityType(),EntityType.predicate);
		Assert.assertEquals(pred.getArity(),3);
		Assert.assertArrayEquals(pred.getParameters(),new IFProEntity[]{integer,real,string});
		Assert.assertEquals(serializeAndDeserialize(pred),pred);
	}

	private IFProEntity serializeAndDeserialize(final IFProEntity entity) throws IOException {
		try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
			
			FProUtil.serialize(baos,entity);
			baos.flush();
			
			try(final InputStream			bais = new ByteArrayInputStream(baos.toByteArray());) {
				return FProUtil.deserialize(bais);
			}
		}
	}
}
