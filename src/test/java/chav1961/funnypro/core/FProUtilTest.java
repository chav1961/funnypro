package chav1961.funnypro.core;


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
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.FProUtil;
import chav1961.funnypro.core.FProUtil.Change;

public class FProUtilTest {
	@Test
	public void simpleParserStaticTest() throws FProParsingException {
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
	public void unificationStaticTest() throws FProParsingException {
		final Change[]	list = new Change[1];

		try{FProUtil.unify(null, null,null);
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
}
