package chav1961.funnypro.core;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.purelib.basic.OrdinalSyntaxTree;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.funnypro.core.FProUtil;
import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.FProUtil.ContentType;

public class FProUtilTest {
	@Test
	public void simpleParserStaticTest()  {
		final int[][]	locations = new int[5][];
		
		try{FProUtil.simpleParser(null, 0, "%", locations);			// Illegal calls
			Assert.fail("Mandatory exception was not detected (null parsed string)");
		} catch (IllegalArgumentException exc) {
		} 
		try{FProUtil.simpleParser("".toCharArray(), 0, "%", locations);
			Assert.fail("Mandatory exception was not detected (empty parsed string)");
		} catch (IllegalArgumentException exc) {
		}

		try{FProUtil.simpleParser("abcde".toCharArray(), 0, (String)null, locations);
			Assert.fail("Mandatory exception was not detected (null template)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, (char[])null, locations);
			Assert.fail("Mandatory exception was not detected (null template)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "", locations);
			Assert.fail("Mandatory exception was not detected (empty template)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, new char[0], locations);
			Assert.fail("Mandatory exception was not detected (empty template)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{FProUtil.simpleParser("abcde".toCharArray(), 10, "abcde", locations);
			Assert.fail("Mandatory exception was not detected (start position outside source)");
		} catch (IllegalArgumentException exc) {
		}
		

		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "%", locations);			// Template errors 
			Assert.fail("Mandatory exception was not detected (truncated escape)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "%?", locations);
			Assert.fail("Mandatory exception was not detected (unknown escape char)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "%1s", locations);
			Assert.fail("Mandatory exception was not detected (unknown escape char)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "%100d", locations);
			Assert.fail("Mandatory exception was not detected (escape index value out of bounds)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "%1:1s", locations);
			Assert.fail("Mandatory exception was not detected (unknown escape char)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "a{b|c", locations);
			Assert.fail("Mandatory exception was not detected (unpaired brackets)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "a{b|c|d", locations); 
			Assert.fail("Mandatory exception was not detected (unpaired brackets)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "a[b", locations);
			Assert.fail("Mandatory exception was not detected (unpaired brackets)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "a<b", locations);
			Assert.fail("Mandatory exception was not detected (unpaired brackets)");
		} catch (IllegalArgumentException exc) {
		}
		try{FProUtil.simpleParser("abcde".toCharArray(), 0, "a<b>,..", locations);
			Assert.fail("Mandatory exception was not detected (missing '...')");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertEquals(FProUtil.simpleParser("abcd".toCharArray(), 0, "abcde", locations),-1);						// Simple matching
		Assert.assertEquals(FProUtil.simpleParser("abcde".toCharArray(), 0, "abcde", locations),5);
		
		Assert.assertEquals(FProUtil.simpleParser("a{|}[]<>%z".toCharArray(), 0, "a%{%|%}%[%]%<%>%%z", locations),10);	// Single escaped matching
				
		Assert.assertEquals(FProUtil.simpleParser("a\t\r\n b".toCharArray(), 0, "a%bb", locations),6);					// Template escaped matching
		Assert.assertEquals(FProUtil.simpleParser("abcde".toCharArray(), 0, "a%ce", locations),5);
		Assert.assertEquals(FProUtil.simpleParser("a123e".toCharArray(), 0, "a%de", locations),5);
		Assert.assertEquals(FProUtil.simpleParser("ae".toCharArray(), 0, "a%de", locations),2);
		
		Assert.assertEquals(FProUtil.simpleParser("abcde".toCharArray(), 0, "a%3ce", locations),5);						// Template escaped matching with storing
		Assert.assertArrayEquals(locations[3],new int[]{1,4});
		Assert.assertEquals(FProUtil.simpleParser("ae".toCharArray(), 0, "a%3ce", locations),2);
		Assert.assertArrayEquals(locations[3],new int[]{1,1});
		Assert.assertEquals(FProUtil.simpleParser("abcde".toCharArray(), 0, "a%3c", locations),5);
		Assert.assertArrayEquals(locations[3],new int[]{1,5});
		Assert.assertEquals(FProUtil.simpleParser("a".toCharArray(), 0, "a%3c", locations),-1);
		Assert.assertEquals(FProUtil.simpleParser("abcde".toCharArray(), 0, "abc%3:10mde", locations),5);
		Assert.assertArrayEquals(locations[3],new int[]{10,10});
		
		Assert.assertEquals(FProUtil.simpleParser("abcde".toCharArray(), 0, "ab[cd]e", locations),5);					// Optionals
		Assert.assertEquals(FProUtil.simpleParser("abe".toCharArray(), 0, "ab[cd]e", locations),3);
		Assert.assertEquals(FProUtil.simpleParser("abce".toCharArray(), 0, "ab[cd]e", locations),-1);
		Assert.assertEquals(FProUtil.simpleParser("abc".toCharArray(), 0, "[abc]", locations),3);
		Assert.assertEquals(FProUtil.simpleParser("a".toCharArray(), 0, "a[bc]", locations),1);
		
		Assert.assertEquals(FProUtil.simpleParser("abcde".toCharArray(), 0, "ab{c|d}de", locations),5);					// Alternatives
		Assert.assertEquals(FProUtil.simpleParser("abdde".toCharArray(), 0, "ab{c|d}de", locations),5);
		Assert.assertEquals(FProUtil.simpleParser("abede".toCharArray(), 0, "ab{c|d}de", locations),-1);
		Assert.assertEquals(FProUtil.simpleParser("c".toCharArray(), 0, "{c|d}", locations),1);
		Assert.assertEquals(FProUtil.simpleParser("d".toCharArray(), 0, "{c|d}", locations),1);
		Assert.assertEquals(FProUtil.simpleParser("e".toCharArray(), 0, "{c|d}", locations),-1);
		
		Assert.assertEquals(FProUtil.simpleParser("ab,b,bcd".toCharArray(), 0, "a<b>,...cd", locations),8);				// Repeatables
		Assert.assertEquals(FProUtil.simpleParser("abcd".toCharArray(), 0, "a<b>,...cd", locations),4);
		Assert.assertEquals(FProUtil.simpleParser("acd".toCharArray(), 0, "a<b>,...cd", locations),-1);
		Assert.assertEquals(FProUtil.simpleParser("ab,b".toCharArray(), 0, "a<b>,...", locations),4);
		Assert.assertEquals(FProUtil.simpleParser("ab".toCharArray(), 0, "a<b>,...", locations),2);
		Assert.assertEquals(FProUtil.simpleParser("a".toCharArray(), 0, "a<b>,...", locations),-1);

		Assert.assertEquals(FProUtil.simpleParser("ab,cde".toCharArray(), 0, "a<%1c>,...de", locations),6);				// Clauses with escapes and storing
		Assert.assertArrayEquals(locations[1],new int[]{1,2,3,4});
		Assert.assertEquals(FProUtil.simpleParser("abcde".toCharArray(), 0, "a{b%1c|c%1c}", locations),5);
		Assert.assertArrayEquals(locations[1],new int[]{2,5});
		Assert.assertEquals(FProUtil.simpleParser("abcde".toCharArray(), 0, "a[b%1c]e", locations),5);
		Assert.assertArrayEquals(locations[1],new int[]{2,4});
		
		Assert.assertEquals(FProUtil.simpleParser("ab,c,cde".toCharArray(), 0, "a<{b|c[d]}>,...e", locations),8);		// Nested clauses
		
		Assert.assertEquals(FProUtil.simpleParser("ab|c|cde".toCharArray(), 0, "a<{b|c[d]}>%|...e", locations),8);		// Nested clauses with escapes

		Assert.assertEquals(FProUtil.simpleParser("ab1,c2x,cd3xe".toCharArray(), 0, "a<{b%1d|c%2c[d%3c]x}>,...e", locations),13);	// Nested clauses with storing
		Assert.assertArrayEquals(locations[1],new int[]{2,3});
		Assert.assertArrayEquals(locations[2],new int[]{5,6,9,9});
		Assert.assertArrayEquals(locations[3],new int[]{10,11});
	}

	@Test
	public void unificationStaticTest() {
		final Change[]	list = new Change[1];

		try{FProUtil.unify(null, null, null);
			Assert.fail("Mandatory exception was not detected (null changes array)");
		} catch (IllegalArgumentException exc) {
		}

		Assert.assertTrue(FProUtil.unify(null, null,list));
		Assert.assertFalse(FProUtil.unify(new AnonymousEntity(), null,list));
		Assert.assertFalse(FProUtil.unify(null,new AnonymousEntity(),list));

		Assert.assertTrue(FProUtil.unify(new AnonymousEntity(),new AnonymousEntity(),list));
		Assert.assertTrue(FProUtil.unify(new IntegerEntity(123),new AnonymousEntity(),list));
		Assert.assertTrue(FProUtil.unify(new AnonymousEntity(),new IntegerEntity(123),list));
		
		Assert.assertTrue(FProUtil.unify(new IntegerEntity(123),new IntegerEntity(123),list));
		Assert.assertFalse(FProUtil.unify(new IntegerEntity(123),new IntegerEntity(456),list));
		Assert.assertTrue(FProUtil.unify(new RealEntity(123),new RealEntity(123),list));
		Assert.assertFalse(FProUtil.unify(new RealEntity(123),new RealEntity(456),list));
		Assert.assertTrue(FProUtil.unify(new StringEntity(123),new StringEntity(123),list));
		Assert.assertFalse(FProUtil.unify(new StringEntity(123),new StringEntity(456),list));
		
		Assert.assertTrue(FProUtil.unify(new ListEntity(null,null),new ListEntity(null,null),list));
		Assert.assertTrue(FProUtil.unify(new ListEntity(new IntegerEntity(123),new AnonymousEntity()),new ListEntity(new IntegerEntity(123),new AnonymousEntity()),list));
		Assert.assertFalse(FProUtil.unify(new ListEntity(new IntegerEntity(123),new IntegerEntity(456)),new ListEntity(new IntegerEntity(123),new IntegerEntity(789)),list));
		
		Assert.assertTrue(FProUtil.unify(new OperatorEntity(100,OperatorType.xfx,123),new OperatorEntity(100,OperatorType.xfx,123),list));
		Assert.assertFalse(FProUtil.unify(new OperatorEntity(110,OperatorType.xfx,123),new OperatorEntity(100,OperatorType.xfx,123),list));
		Assert.assertFalse(FProUtil.unify(new OperatorEntity(100,OperatorType.fx,123),new OperatorEntity(100,OperatorType.xfx,123),list));
		Assert.assertTrue(FProUtil.unify(new OperatorEntity(100,OperatorType.xfx,123).setLeft(new IntegerEntity(123)).setRight(new IntegerEntity(456)),new OperatorEntity(100,OperatorType.xfx,123).setLeft(new IntegerEntity(123)).setRight(new IntegerEntity(456)),list));
		Assert.assertFalse(FProUtil.unify(new OperatorEntity(100,OperatorType.xfx,123).setLeft(new IntegerEntity(123)).setRight(new IntegerEntity(456)),new OperatorEntity(100,OperatorType.xfx,123).setLeft(new IntegerEntity(123)).setRight(new IntegerEntity(789)),list));

		Assert.assertTrue(FProUtil.unify(new PredicateEntity(100,new IntegerEntity(123),new StringEntity(456)),new PredicateEntity(100,new IntegerEntity(123),new StringEntity(456)),list));
		Assert.assertFalse(FProUtil.unify(new PredicateEntity(100,new IntegerEntity(123),new StringEntity(456)),new PredicateEntity(100,new IntegerEntity(123)),list));
		Assert.assertFalse(FProUtil.unify(new PredicateEntity(100,new IntegerEntity(123),new StringEntity(456)),new PredicateEntity(100,new IntegerEntity(123),new StringEntity(789)),list));
		
		// Variable tests:
		
		final IFProVariable		var1 = new VariableEntity(123), var2 = new VariableEntity(456), var3 = new VariableEntity(123);
		Assert.assertTrue(FProUtil.unify(var1,var3,list));		// Unify "the same" variable
		Assert.assertNotNull(list[0]);
		Assert.assertEquals(var1.getChain(),var3);
		Assert.assertEquals(var3.getChain(),var1);

		FProUtil.unbind(list[0]);								// undo joining
		Assert.assertEquals(var1.getChain(),var1);
		Assert.assertEquals(var3.getChain(),var3);
		list[0] = null;
		
		Assert.assertTrue(FProUtil.unify(var1,var2,list));		// Join two lists for the variables
		Assert.assertNotNull(list[0]);
		Assert.assertEquals(var1.getChain(),var2);
		Assert.assertEquals(var2.getChain(),var1);
		
		FProUtil.unbind(list[0]);								// undo joining
		Assert.assertEquals(var1.getChain(),var1);
		Assert.assertEquals(var2.getChain(),var2);
		list[0] = null;
		
		Assert.assertTrue(FProUtil.unify(new ListEntity(var1,var2),new ListEntity(var1,var2),list));		// Test the same variable
		Assert.assertNull(list[0]);

		// Bind variables:

		final IFProList			boundList = new ListEntity(new IntegerEntity(666),var1);
		
		var1.setParent(boundList);		// It's important!
		Assert.assertTrue(FProUtil.unify(boundList,new ListEntity(new IntegerEntity(666),new IntegerEntity(789)),list));		// Bind list.tail
		Assert.assertNotNull(list[0]);
		Assert.assertEquals(boundList.getTail().getEntityId(),789);
		
		FProUtil.unbind(list[0]);								// undo joining
		Assert.assertEquals(boundList.getTail().getEntityId(),123);
		
		
		final IFProList			boundList1 = new ListEntity(new IntegerEntity(666),var1);
		
		var1.setParent(boundList1);		// It's important!
		Assert.assertTrue(FProUtil.unify(new ListEntity(new IntegerEntity(666),new IntegerEntity(789)),boundList1,list));		// Bind list.tail from another side
		Assert.assertNotNull(list[0]);
		Assert.assertEquals(boundList1.getTail().getEntityId(),789);
		
		FProUtil.unbind(list[0]);								// undo joining
		Assert.assertEquals(boundList1.getTail().getEntityId(),123);
		
		final IFProList			boundList2 = new ListEntity(var1,new IntegerEntity(789));
		
		var1.setParent(boundList2);		// It's important!
		Assert.assertTrue(FProUtil.unify(new ListEntity(new IntegerEntity(666),new IntegerEntity(789)),boundList2,list));		// Bind list.child
		Assert.assertNotNull(list[0]);
		Assert.assertEquals(boundList2.getChild().getEntityId(),666);
		
		FProUtil.unbind(list[0]);								// undo joining
		Assert.assertEquals(boundList2.getChild().getEntityId(),123);
		
		final IFProOperator		boundOpLeft = new OperatorEntity(100,OperatorType.xf,666).setLeft(var1);
		
		var1.setParent(boundOpLeft);		// It's important!
		Assert.assertTrue(FProUtil.unify(new OperatorEntity(100,OperatorType.xf,666).setLeft(new IntegerEntity(789)),boundOpLeft,list));		// Bind operator.left
		Assert.assertNotNull(list[0]);
		Assert.assertEquals(boundOpLeft.getLeft().getEntityId(),789);
		
		FProUtil.unbind(list[0]);								// undo joining
		Assert.assertEquals(boundOpLeft.getLeft().getEntityId(),123);

		final IFProOperator		boundOpRight = new OperatorEntity(100,OperatorType.fx,666).setRight(var1);
		
		var1.setParent(boundOpRight);		// It's important!
		Assert.assertTrue(FProUtil.unify(new OperatorEntity(100,OperatorType.fx,666).setRight(new IntegerEntity(789)),boundOpRight,list));		// Bind operator.right
		Assert.assertNotNull(list[0]);
		Assert.assertEquals(boundOpRight.getRight().getEntityId(),789);
		
		FProUtil.unbind(list[0]);								// undo joining
		Assert.assertEquals(boundOpRight.getRight().getEntityId(),123);

		final IFProPredicate	boundPredicate = new PredicateEntity(666,new IntegerEntity(456),var1);
		
		var1.setParent(boundPredicate);		// It's important!
		Assert.assertTrue(FProUtil.unify(new PredicateEntity(666,new IntegerEntity(456),new IntegerEntity(789)),boundPredicate,list));		// Bind predicate
		Assert.assertNotNull(list[0]);
		Assert.assertEquals(boundPredicate.getParameters()[1].getEntityId(),789);
		
		FProUtil.unbind(list[0]);								// undo joining
		Assert.assertEquals(boundPredicate.getParameters()[1].getEntityId(),123);
	}

	@Test
	public void otherStaticTest() {
		final SyntaxTreeInterface<?>	st = new OrdinalSyntaxTree<>();
		
		IFProEntity		left;
		
		
		left = new AnonymousEntity();
		Assert.assertEquals(left,FProUtil.duplicate(left));
		Assert.assertTrue(FProUtil.isIdentical(left,FProUtil.duplicate(left)));
		checkType(left,new ContentType[] {ContentType.Anon, ContentType.Atomic});
		FProUtil.removeEntity(st,left);

		left = new IntegerEntity(100);
		Assert.assertEquals(left,FProUtil.duplicate(left));
		Assert.assertTrue(FProUtil.isIdentical(left,FProUtil.duplicate(left)));
		checkType(left,new ContentType[] {ContentType.Integer, ContentType.Number, ContentType.NonVar, ContentType.Atomic});
		FProUtil.removeEntity(st,left);

		left = new RealEntity(123.456);
		Assert.assertEquals(left,FProUtil.duplicate(left));
		Assert.assertTrue(FProUtil.isIdentical(left,FProUtil.duplicate(left)));
		checkType(left,new ContentType[] {ContentType.Float, ContentType.Number, ContentType.NonVar, ContentType.Atomic});
		FProUtil.removeEntity(st,left);

		left = new StringEntity(123);
		Assert.assertEquals(left,FProUtil.duplicate(left));
		Assert.assertTrue(FProUtil.isIdentical(left,FProUtil.duplicate(left)));
		checkType(left,new ContentType[] {ContentType.Atom, ContentType.NonVar, ContentType.Atomic});
		FProUtil.removeEntity(st,left);

		left = new OperatorEntity(100,OperatorType.xfy,123).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200));
		Assert.assertEquals(left,FProUtil.duplicate(left));
		Assert.assertTrue(FProUtil.isIdentical(left,FProUtil.duplicate(left)));
		checkType(left,new ContentType[] {ContentType.Compound, ContentType.NonVar});
		FProUtil.removeEntity(st,left);

		left = new PredicateEntity(100,new IntegerEntity(100),new IntegerEntity(200));
		Assert.assertEquals(left,FProUtil.duplicate(left));
		Assert.assertTrue(FProUtil.isIdentical(left,FProUtil.duplicate(left)));
		checkType(left,new ContentType[] {ContentType.Atom, ContentType.Atomic, ContentType.NonVar});
		FProUtil.removeEntity(st,left);

		left = new ListEntity(new IntegerEntity(100),new IntegerEntity(200));
		Assert.assertTrue(FProUtil.isIdentical(left,FProUtil.duplicate(left)));
		Assert.assertEquals(left,FProUtil.duplicate(left));
		FProUtil.removeEntity(st,left);
		
		try{FProUtil.isEntityA(left,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		left = new PredicateEntity(100,new IntegerEntity(100),new IntegerEntity(200));
		Assert.assertTrue(FProUtil.isIdentical(left,left));
		Assert.assertFalse(FProUtil.isIdentical(null,left));
		Assert.assertFalse(FProUtil.isIdentical(left,null));
		Assert.assertFalse(FProUtil.isIdentical(left,new IntegerEntity(100)));
		Assert.assertFalse(FProUtil.isIdentical(left,new PredicateEntity(100,new IntegerEntity(100),new IntegerEntity(300))));

		left = new OperatorEntity(100,OperatorType.xfx,100).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200));
		Assert.assertTrue(FProUtil.isIdentical(left,new OperatorEntity(100,OperatorType.xfx,100).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200))));
		Assert.assertFalse(FProUtil.isIdentical(left,new OperatorEntity(100,OperatorType.xfx,100).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(300))));
	}

	@Test
	public void serializationStaticTest() throws IOException {
		try(final InOutGrowableByteArray	iogba = new InOutGrowableByteArray(false)) {
			final IFProEntity	entity1 = new AnonymousEntity();
			final IFProEntity	entity2 = new IntegerEntity(100);
			final IFProEntity	entity3 = new RealEntity(123.456);
			final IFProEntity	entity4 = new StringEntity(123);
			final IFProEntity	entity5 = new OperatorEntity(100,OperatorType.xfy,123).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200));
			final IFProEntity	entity6 = new PredicateEntity(100,new IntegerEntity(100),new IntegerEntity(200));
			final IFProEntity	entity7 = new ListEntity(new IntegerEntity(100),new IntegerEntity(200));
			final IFProEntity	entity8 = new VariableEntity(100);
			
			FProUtil.serialize(iogba,entity1);
			FProUtil.serialize(iogba,entity2);
			FProUtil.serialize(iogba,entity3);
			FProUtil.serialize(iogba,entity4);
			FProUtil.serialize(iogba,entity5);
			FProUtil.serialize(iogba,entity6);
			FProUtil.serialize(iogba,entity7);
			FProUtil.serialize(iogba,entity8);
			
			try{FProUtil.serialize(null,entity8);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{FProUtil.serialize(iogba,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			iogba.flush();			
			iogba.reset();
			
			Assert.assertEquals(entity1,FProUtil.deserialize(iogba));
			Assert.assertEquals(entity2,FProUtil.deserialize(iogba));
			Assert.assertEquals(entity3,FProUtil.deserialize(iogba));
			Assert.assertEquals(entity4,FProUtil.deserialize(iogba));
			Assert.assertEquals(entity5,FProUtil.deserialize(iogba));
			Assert.assertEquals(entity6,FProUtil.deserialize(iogba));
			Assert.assertEquals(entity7,FProUtil.deserialize(iogba));
			Assert.assertEquals(entity8,FProUtil.deserialize(iogba));

			try{FProUtil.deserialize(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

	@Test
	public void utilStaticTest() throws ContentException, IOException {
		final IFProList	list = FProUtil.toList(Arrays.asList("1","2","3"), (v)->new StringEntity(Integer.valueOf(v)));
		
		Assert.assertEquals(1, list.getChild().getEntityId());
		Assert.assertEquals(2, ((IFProList)list.getTail()).getChild().getEntityId());
		Assert.assertEquals(3, ((IFProList)((IFProList)list.getTail()).getTail()).getChild().getEntityId());

		final IFProList	emptyList = FProUtil.toList(Arrays.asList(), (v)->new AnonymousEntity());

		Assert.assertNull(emptyList.getChild());
		Assert.assertNull(emptyList.getTail());

		try{FProUtil.toList(null, (v)->new AnonymousEntity());
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{FProUtil.toList(Arrays.asList(), null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}	

	private void checkType(final IFProEntity entity, final ContentType[] trues) {
		final Set<ContentType>	falseSet = new HashSet<>();
		final Set<ContentType>	trueSet = new HashSet<>();
		
		trueSet.addAll(Arrays.asList(trues));
		falseSet.addAll(Arrays.asList(ContentType.values()));
		falseSet.removeAll(trueSet);
		
		for (ContentType item : trueSet) {
			Assert.assertTrue("Assertion failed: item="+item,FProUtil.isEntityA(entity,item));
		}
		for (ContentType item : falseSet) {
			Assert.assertFalse("Assertion failed: item="+item,FProUtil.isEntityA(entity,item));
		}
	}

}
