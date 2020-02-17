package chav1961.funnypro.core.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.FProUtil;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;

public class EntitiesTest {
	@Test
	public void anonymousEntityTest() {
		final AnonymousEntity	e1 = new AnonymousEntity(), e2 = new AnonymousEntity(null);
		
		Assert.assertEquals(EntityType.anonymous,e1.getEntityType());
		Assert.assertEquals(IFProEntity.ANON_ENTITY_ID,e1.getEntityId());
		Assert.assertEquals(e1,e2);
		Assert.assertEquals(e1.toString(),e2.toString());
		
		e1.setParent(e2);
		Assert.assertEquals(e2,e1.getParent());
		e1.setEntityId(456);
		Assert.assertEquals(IFProEntity.ANON_ENTITY_ID,e1.getEntityId());
	}

	@Test
	public void integerEntityTest() {
		final IntegerEntity	e1 = new IntegerEntity(123), e2 = new IntegerEntity(123,null); 

		Assert.assertEquals(EntityType.integer,e1.getEntityType());
		Assert.assertEquals(123,e1.getEntityId());
		Assert.assertEquals(e1,e2);
		Assert.assertEquals(e1.toString(),e2.toString());

		e1.setParent(e2);
		Assert.assertEquals(e2,e1.getParent());
		e1.setEntityId(456);
		Assert.assertEquals(456,e1.getEntityId());
	}

	@Test
	public void realEntityTest() {
		final RealEntity	e1 = new RealEntity(123), e2 = new RealEntity(123,null); 

		Assert.assertEquals(EntityType.real,e1.getEntityType());
		Assert.assertEquals(123,Double.longBitsToDouble(e1.getEntityId()),0.001);
		Assert.assertEquals(e1,e2);
		Assert.assertEquals(e1.toString(),e2.toString());

		e1.setParent(e2);
		Assert.assertEquals(e2,e1.getParent());
		e1.setEntityId(Double.doubleToLongBits(456));
		Assert.assertEquals(456,Double.longBitsToDouble(e1.getEntityId()),0.001);
	}

	@Test
	public void stringEntityTest() {
		final StringEntity	e1 = new StringEntity(123), e2 = new StringEntity(123,null); 

		Assert.assertEquals(EntityType.string,e1.getEntityType());
		Assert.assertEquals(123,e1.getEntityId());
		Assert.assertEquals(e1,e2);
		Assert.assertEquals(e1.toString(),e2.toString());

		e1.setParent(e2);
		Assert.assertEquals(e2,e1.getParent());
	}

	@Test
	public void variableEntityTest() {
		final VariableEntity	e1 = new VariableEntity(123), e2 = new VariableEntity(123,null); 

		Assert.assertEquals(EntityType.variable,e1.getEntityType());
		Assert.assertEquals(123,e1.getEntityId());
		Assert.assertEquals(e1,e2);
		Assert.assertEquals(e1.toString(),e2.toString());
		Assert.assertEquals(e1,e1.getChain());

		e1.setParent(e2);
		Assert.assertEquals(e2,e1.getParent());
		e1.setEntityId(456);
		Assert.assertEquals(456,e1.getEntityId());
		e1.setChain(e2);
		Assert.assertEquals(e2,e1.getChain());
	}

	@Test
	public void listEntityTest() {
		final IntegerEntity	ie1 = new IntegerEntity(123), ie2 = new IntegerEntity(456); 
		final IntegerEntity	ie3 = new IntegerEntity(123), ie4 = new IntegerEntity(456); 
		final ListEntity	e1 = new ListEntity(ie1,ie2), e2 = new ListEntity(ie3,ie4); 

		Assert.assertEquals(EntityType.list,e1.getEntityType());
		Assert.assertEquals(IFProEntity.LIST_ENTITY_ID,e1.getEntityId());
		Assert.assertEquals(e1,e2);
		Assert.assertEquals(e1.toString(),e2.toString());

		e1.setParent(e2);
		Assert.assertEquals(e2,e1.getParent());
		e1.setChild(e2);
		Assert.assertEquals(e2,e1.getChild());
		e1.setTail(e2);
		Assert.assertEquals(e2,e1.getTail());

		e1.setEntityId(456);
		Assert.assertEquals(IFProEntity.LIST_ENTITY_ID,e1.getEntityId());
	}

	@Test
	public void operatorDefEntityTest() {
		final OperatorDefEntity	e1 = new OperatorDefEntity(1000,OperatorType.xfx,123), e2 = new OperatorDefEntity(1000,OperatorType.xfx,123); 

		Assert.assertEquals(EntityType.operatordef,e1.getEntityType());
		Assert.assertEquals(123,e1.getEntityId());
		Assert.assertEquals(1000,e1.getPriority());
		Assert.assertEquals(OperatorType.xfx,e1.getOperatorType());
		Assert.assertEquals(e1,e2);
		Assert.assertEquals(e1.toString(),e2.toString());

		e1.setParent(e2);
		Assert.assertEquals(e2,e1.getParent());
		e1.setEntityId(456);
		Assert.assertEquals(456,e1.getEntityId());
	}

	@Test
	public void operatorEntityTest() {
		final OperatorEntity	e1 = new OperatorEntity(1000,OperatorType.xfx,123), e2 = new OperatorEntity(1000,OperatorType.xfx,123); 

		Assert.assertEquals(EntityType.operator,e1.getEntityType());
		Assert.assertEquals(123,e1.getEntityId());
		Assert.assertEquals(1000,e1.getPriority());
		Assert.assertEquals(OperatorType.xfx,e1.getOperatorType());
		Assert.assertEquals(e1,e2);
		Assert.assertEquals(e1.toString(),e2.toString());

		e1.setParent(e2);
		Assert.assertEquals(e2,e1.getParent());
		e1.setEntityId(456);
		Assert.assertEquals(456,e1.getEntityId());
		e1.setLeft(e2);
		Assert.assertEquals(e2,e1.getLeft());
		e1.setRight(e2);
		Assert.assertEquals(e2,e1.getRight());
		e1.setRule(e2);
		Assert.assertEquals(e2,e1.getRule());
	}

	@Test
	public void staticOperatorEntityTest() {
		Assert.assertEquals(999,IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.xf,123)));
		Assert.assertEquals(999,IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.fx,123)));
		Assert.assertEquals(1000,IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.yf,123)));
		Assert.assertEquals(1000,IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.fy,123)));

		try{IFProOperator.getUnderlyingPriority(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.xfx,123));
			Assert.fail("Mandatory exception was not detected (1-st argument can't be infix operator)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(999,IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.xfx,123),IFProOperator.LEFT));
		Assert.assertEquals(999,IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.xfx,123),IFProOperator.RIGHT));
		Assert.assertEquals(999,IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.xfy,123),IFProOperator.LEFT));
		Assert.assertEquals(1000,IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.xfy,123),IFProOperator.RIGHT));
		Assert.assertEquals(1000,IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.yfx,123),IFProOperator.LEFT));
		Assert.assertEquals(999,IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.yfx,123),IFProOperator.RIGHT));

		try{IFProOperator.getUnderlyingPriority(null,IFProOperator.LEFT);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.xf,123),IFProOperator.LEFT);
			Assert.fail("Mandatory exception was not detected (1-st argument must be infix operator)");
		} catch (IllegalArgumentException exc) {
		}
		try{IFProOperator.getUnderlyingPriority(new OperatorDefEntity(1000,OperatorType.xfx,123),999);
			Assert.fail("Mandatory exception was not detected (2-nd argument not IFProOperator.LEFT/IFProOperator.RIGHT)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void predicateEntityTest() {
		final IntegerEntity		ie1 = new IntegerEntity(123), ie2 = new IntegerEntity(456); 
		final IntegerEntity		ie3 = new IntegerEntity(123), ie4 = new IntegerEntity(456); 
		final PredicateEntity	e1 = new PredicateEntity(123,ie1,ie2), e2 = new PredicateEntity(123,ie3,ie4);

		Assert.assertEquals(EntityType.predicate,e1.getEntityType());
		Assert.assertEquals(123,e1.getEntityId());
		Assert.assertEquals(2,e1.getArity());
		Assert.assertEquals(e1,e2);
		Assert.assertEquals(e1.toString(),e2.toString());
		Assert.assertArrayEquals(new IFProEntity[]{ie1,ie2},e1.getParameters());
		
		e1.setParent(e2);
		Assert.assertEquals(e2,e1.getParent());
		e1.setEntityId(456);
		Assert.assertEquals(456,e1.getEntityId());
		e1.setParameters(new IFProEntity[]{ie3,ie4});
		Assert.assertArrayEquals(new IFProEntity[]{ie3,ie4},e1.getParameters());
		e1.setRule(e2);
		Assert.assertEquals(e2,e1.getRule());
	}

	@Test
	public void serializationTest() throws IOException {
		final AnonymousEntity	anon = new AnonymousEntity();
		
		Assert.assertEquals(anon,serializeAndDeserialize(anon));
		
		final IntegerEntity		integer = new IntegerEntity(12345);
	
		Assert.assertEquals(integer,serializeAndDeserialize(integer));
	
		final RealEntity		real = new RealEntity(12345.6789);
		
		Assert.assertEquals(real,serializeAndDeserialize(real));
	
		final StringEntity		string = new StringEntity(12345);
		
		Assert.assertEquals(string,serializeAndDeserialize(string));

		final VariableEntity	var = new VariableEntity(12345);
		
		Assert.assertEquals(var,serializeAndDeserialize(var));
		
		ListEntity		list = new ListEntity(integer,real);
		
		Assert.assertEquals(list,serializeAndDeserialize(list));

		OperatorEntity	op = new OperatorEntity(100,OperatorType.xfx,12345);
		
		op.setLeft(anon).setRight(string);
		
		Assert.assertEquals(op.getEntityId(),12345);
		Assert.assertEquals(op.getEntityType(),EntityType.operator);
		Assert.assertEquals(op.getPriority(),100);
		Assert.assertEquals(op.getOperatorType(),OperatorType.xfx);
		Assert.assertEquals(op.getLeft(),anon);
		Assert.assertEquals(op.getRight(),string);		
		Assert.assertEquals(op,serializeAndDeserialize(op));

		op = new OperatorEntity(100,OperatorType.fx,12345);
		
		op.setRight(string);
		
		Assert.assertEquals(op.getEntityId(),12345);
		Assert.assertEquals(op.getEntityType(),EntityType.operator);
		Assert.assertEquals(op.getPriority(),100);
		Assert.assertEquals(op.getOperatorType(),OperatorType.fx);
		Assert.assertNull(op.getLeft());
		Assert.assertEquals(op.getRight(),string);
		Assert.assertEquals(op,serializeAndDeserialize(op));

		op = new OperatorEntity(100,OperatorType.xf,12345);
		
		op.setLeft(string);
		
		Assert.assertEquals(op.getEntityId(),12345);
		Assert.assertEquals(op.getEntityType(),EntityType.operator);
		Assert.assertEquals(op.getPriority(),100);
		Assert.assertEquals(op.getOperatorType(),OperatorType.xf);
		Assert.assertEquals(op.getLeft(),string);
		Assert.assertNull(op.getRight());
		Assert.assertEquals(op,serializeAndDeserialize(op));
		
		final PredicateEntity	pred = new PredicateEntity(12345,integer,real,string);

		Assert.assertEquals(pred.getEntityId(),12345);
		Assert.assertEquals(pred.getEntityType(),EntityType.predicate);
		Assert.assertEquals(pred.getArity(),3);
		Assert.assertArrayEquals(pred.getParameters(),new IFProEntity[]{integer,real,string});
		PredicateEntity	pred1 = (PredicateEntity) serializeAndDeserialize(pred);
		Assert.assertEquals(pred,pred1);
	}

	private IFProEntity serializeAndDeserialize(final IFProEntity entity) throws IOException {
		try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream();
			final DataOutputStream			dos = new DataOutputStream(baos)) {
			
			FProUtil.serialize(dos,entity);
			dos.flush();
			
			try(final InputStream			bais = new ByteArrayInputStream(baos.toByteArray());
				final DataInputStream		dis = new DataInputStream(bais)) {
				
				return FProUtil.deserialize(dis);
			}
		}
	}
}
