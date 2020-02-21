package chav1961.funnypro.core;


import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable.ResolveRC;
import chav1961.purelib.basic.DefaultLoggerFacade;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.streams.charsource.ArrayCharSource;
import chav1961.purelib.streams.charsource.StringCharSource;
import chav1961.purelib.streams.chartarget.StringBuilderCharTarget;
import chav1961.purelib.streams.chartarget.WriterCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

public class StandardResolverTest {
	@Test
	public void lifeCycleTest() throws Exception {
		final Properties	props = Utils.mkProps();
		
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props)){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,props,repo);
			final List<IFProVariable>	vars = new ArrayList<>();
			
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,props,repo);
			final long					trueId = repo.termRepo().placeName("true",null);

			try{sr.onLoad(null,props,repo);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.onLoad(PureLibSettings.CURRENT_LOGGER,null,repo);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.onLoad(PureLibSettings.CURRENT_LOGGER,props,null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}

			final LocalDescriptor		local = sr.beforeCall(global, stack, vars, (n,r,p)->true);
			
			try{sr.beforeCall(null, stack, vars, (n,r,p)->true);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.beforeCall(global, null, vars, (n,r,p)->true);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.beforeCall(global, stack, null, (n,r,p)->true);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.beforeCall(global, stack, vars, null);
				Assert.fail("Mandatory exception was not detected (null 4-th argument)");
			} catch (NullPointerException exc) {
			}
			
			Assert.assertEquals(ResolveRC.True, sr.firstResolve(global, local, new PredicateEntity(trueId)));

			try{sr.firstResolve(null, local, new PredicateEntity(trueId));
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.firstResolve(global, null, new PredicateEntity(trueId));
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.firstResolve(global, local, null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}

			Assert.assertEquals(ResolveRC.False, sr.nextResolve(global, local, new PredicateEntity(trueId)));

			try{sr.nextResolve(null, local, new PredicateEntity(trueId));
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.nextResolve(global, null, new PredicateEntity(trueId));
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.nextResolve(global, local, null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
			
			sr.endResolve(global, local, new PredicateEntity(trueId));
			
			try{sr.endResolve(null, local, new PredicateEntity(trueId));
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.endResolve(global, null, new PredicateEntity(trueId));
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.endResolve(global, local, null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
			
			sr.afterCall(global, local);

			try{sr.afterCall(null, local);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{sr.afterCall(global, null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			sr.onRemove(global);
			
			try{sr.onRemove(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			} 
		}
	}
	
	@Test
	public void simpleTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StringBuilder			sb = new StringBuilder();
			
			pap.parseEntities(new StringCharSource("?-trace ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-notrace ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-spy ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-true ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-false ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			pap.parseEntities(new StringCharSource("?-var(X) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-var(100) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			pap.parseEntities(new StringCharSource("?-nonvar(100) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-nonvar(_) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			pap.parseEntities(new StringCharSource("?-atom(test) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-atom(100) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			pap.parseEntities(new StringCharSource("?-integer(100) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-integer(100.3) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-integer(test) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			pap.parseEntities(new StringCharSource("?-float(100.3) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-float(100) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-float(test) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			pap.parseEntities(new StringCharSource("?-number(100) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-number(100.2) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-number(test) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			pap.parseEntities(new StringCharSource("?-atomic(100) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-atomic(\"test\") ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-atomic(test) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-atomic(_) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb); 
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-atomic(X > 0) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			
			pap.parseEntities(new StringCharSource("?-compound(X > 0) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-compound(100) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			
			sr.onRemove(global);
		}
	}
	
	@Test
	public void staticCalculationTest() throws Exception {
		try(final EntitiesRepo		repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final StandardResolver	sr = new StandardResolver();
			final GlobalDescriptor	global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final long[]			forInteger = new long[2];
			final double[]			forReal = new double[2];
			final long				plusId = repo.termRepo().seekName("+"), minusId = repo.termRepo().seekName("-");
			final long				mulId = repo.termRepo().seekName("*"), divId = repo.termRepo().seekName("/"); 
			final long				intDivId = repo.termRepo().seekName("//"), modId = repo.termRepo().seekName("mod"); 
			final long				expId = repo.termRepo().seekName("**"); 

			// Integer operands
			Assert.assertEquals(100,StandardResolver.calculate(global, new IntegerEntity(100), forInteger, forReal).getEntityId());
			
			Assert.assertEquals(-100,StandardResolver.calculate(global, new OperatorEntity(200,OperatorType.fy,minusId).setRight(new IntegerEntity(100)), forInteger, forReal).getEntityId());
			Assert.assertEquals(125,StandardResolver.calculate(global, new OperatorEntity(200,OperatorType.xfx,expId).setLeft(new IntegerEntity(5)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId());

			Assert.assertEquals(30,StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,mulId).setLeft(new IntegerEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId());
			Assert.assertEquals(3,StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,intDivId).setLeft(new IntegerEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId());
			Assert.assertEquals(1,StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,modId).setLeft(new IntegerEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId());

			Assert.assertEquals(13,StandardResolver.calculate(global, new OperatorEntity(500,OperatorType.yfx,plusId).setLeft(new IntegerEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId());
			Assert.assertEquals(7,StandardResolver.calculate(global, new OperatorEntity(500,OperatorType.yfx,minusId).setLeft(new IntegerEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId());

			// Real operands
			Assert.assertEquals(100,Double.longBitsToDouble(StandardResolver.calculate(global, new RealEntity(100), forInteger, forReal).getEntityId()),0.001);
			
			Assert.assertEquals(-100,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(200,OperatorType.fy,minusId).setRight(new RealEntity(100)), forInteger, forReal).getEntityId()),0.001);
			Assert.assertEquals(125,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(200,OperatorType.xfx,expId).setLeft(new RealEntity(5)).setRight(new RealEntity(3)), forInteger, forReal).getEntityId()),0.001);

			Assert.assertEquals(30,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,mulId).setLeft(new RealEntity(10)).setRight(new RealEntity(3)), forInteger, forReal).getEntityId()),0.001);
			Assert.assertEquals(3.3333333333,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,divId).setLeft(new RealEntity(10)).setRight(new RealEntity(3)), forInteger, forReal).getEntityId()),0.001);
			Assert.assertEquals(3,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,intDivId).setLeft(new RealEntity(10)).setRight(new RealEntity(3)), forInteger, forReal).getEntityId()),0.001);
			Assert.assertEquals(1,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,modId).setLeft(new RealEntity(10)).setRight(new RealEntity(3)), forInteger, forReal).getEntityId()),0.001);

			Assert.assertEquals(13,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(500,OperatorType.yfx,plusId).setLeft(new RealEntity(10)).setRight(new RealEntity(3)), forInteger, forReal).getEntityId()),0.001);
			Assert.assertEquals(7,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(500,OperatorType.yfx,minusId).setLeft(new RealEntity(10)).setRight(new RealEntity(3)), forInteger, forReal).getEntityId()),0.001);

			// Mixed operands
			Assert.assertEquals(125,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(200,OperatorType.xfx,expId).setLeft(new RealEntity(5)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId()),0.001);

			Assert.assertEquals(30,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,mulId).setLeft(new RealEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId()),0.001);
			Assert.assertEquals(3.3333333333,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,divId).setLeft(new RealEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId()),0.001);
			Assert.assertEquals(3.3333333333,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,divId).setLeft(new IntegerEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId()),0.001);
			Assert.assertEquals(3,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,intDivId).setLeft(new RealEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId()),0.001);
			Assert.assertEquals(1,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(400,OperatorType.yfx,modId).setLeft(new RealEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId()),0.001);
 
			Assert.assertEquals(13,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(500,OperatorType.yfx,plusId).setLeft(new RealEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId()),0.001);
			Assert.assertEquals(7,Double.longBitsToDouble(StandardResolver.calculate(global, new OperatorEntity(500,OperatorType.yfx,minusId).setLeft(new RealEntity(10)).setRight(new IntegerEntity(3)), forInteger, forReal).getEntityId()),0.001);
			
			// Exceptions
			try {StandardResolver.calculate(global, null, forInteger, forReal);
				Assert.fail("Mandatory exception was not detected (null 2-nd operand)");
			} catch (NullPointerException exc) {
			}
			try {StandardResolver.calculate(global, new VariableEntity(100), forInteger, forReal);
				Assert.fail("Mandatory exception was not detected (neither number nor operator inside expression)");
			} catch (UnsupportedOperationException exc) {
			}
			try {StandardResolver.calculate(global, new OperatorEntity(1000,OperatorType.yfx,999), forInteger, forReal);
				Assert.fail("Mandatory exception was not detected (unregistered operator inside expression)");
			} catch (UnsupportedOperationException exc) {
			}
			
			sr.onRemove(global);
		}
	}

	@Test
	public void staticComparisonTest() throws Exception {
		try(final EntitiesRepo		repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final StandardResolver	sr = new StandardResolver();
			final GlobalDescriptor	global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final long[]			forInteger = new long[2];
			final double[]			forReal = new double[2];
			final long				pred1 = repo.termRepo().placeName("pred1",null), pred2 = repo.termRepo().placeName("pred2",null);
			final long				var1 = repo.termRepo().placeName("Var1",null), var2 = repo.termRepo().placeName("Var2",null);
			final long				text1 = repo.stringRepo().placeName("text1",null), text2 = repo.stringRepo().placeName("text2",null);
			final long				plusId = repo.termRepo().seekName("+"), minusId = repo.termRepo().seekName("-"); 
			
			
			Assert.assertTrue(StandardResolver.compare(global,new IntegerEntity(200), new IntegerEntity(100), forInteger, forReal) > 0);
			Assert.assertTrue(StandardResolver.compare(global,new RealEntity(100), new RealEntity(200), forInteger, forReal) < 0);

			IntegerEntity	ie = new IntegerEntity(100);
			
			Assert.assertEquals(0,StandardResolver.lexicalCompare(repo,ie,ie));
			Assert.assertEquals(-1,StandardResolver.lexicalCompare(repo,null,ie));
			Assert.assertEquals(1,StandardResolver.lexicalCompare(repo,ie,null));
			
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new IntegerEntity(200), new IntegerEntity(100)) > 0);
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new RealEntity(200), new RealEntity(100)) > 0);
			
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new StringEntity(text1), new StringEntity(text2)) < 0);
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new StringEntity(text1), new StringEntity(text1)) == 0);
			
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new AnonymousEntity(), new AnonymousEntity()) == 0);
			
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new VariableEntity(var1), new VariableEntity(var2)) < 0);
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new VariableEntity(var1), new VariableEntity(var1)) == 0);
			
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new ListEntity(new IntegerEntity(100),new IntegerEntity(200)),new ListEntity(new IntegerEntity(100),new IntegerEntity(300))) < 0);
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new ListEntity(new IntegerEntity(100),new IntegerEntity(200)),new ListEntity(new IntegerEntity(100),new IntegerEntity(200))) == 0);

			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new PredicateEntity(pred1),new PredicateEntity(pred2)) < 0);
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new PredicateEntity(pred1,new IntegerEntity(100)),new PredicateEntity(pred1,new IntegerEntity(100))) == 0);
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new PredicateEntity(pred1,new IntegerEntity(100)),new PredicateEntity(pred1,new IntegerEntity(200))) < 0);

			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new OperatorEntity(500,OperatorType.yfx,plusId).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(100)),new OperatorEntity(500,OperatorType.yfx,plusId).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(200))) < 0);
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new OperatorEntity(500,OperatorType.yfx,plusId).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(100)),new OperatorEntity(500,OperatorType.yfx,plusId).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(100))) == 0);
			Assert.assertTrue(StandardResolver.lexicalCompare(repo,new OperatorEntity(500,OperatorType.yfx,plusId).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(100)),new OperatorEntity(500,OperatorType.yfx,minusId).setLeft(new IntegerEntity(100)).setRight(new IntegerEntity(100))) < 0);
			
			sr.onRemove(global);
		}
	}
	 
	@Test
	public void staticList2PredicateTest() throws Exception {
		try(final EntitiesRepo		repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final StandardResolver	sr = new StandardResolver();
			final GlobalDescriptor	global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);

			final PredicateEntity	pe = new PredicateEntity(100,new IntegerEntity(100),new RealEntity(100),new AnonymousEntity());
			Assert.assertTrue(FProUtil.isIdentical(pe,StandardResolver.list2Predicate(StandardResolver.predicate2List(pe))));
			
			sr.onRemove(global);
		}
	}	

	@Test
	public void nonBackTrackingTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StringBuilder			sb = new StringBuilder();
			
			pap.parseEntities(new StringCharSource("?-10 < 20 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-10 @< 20 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			
			pap.parseEntities(new StringCharSource("?-10 =< 20 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-10 @<= 20 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-20 > 10 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-20 @> 10 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-20 >= 10 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-20 @>= 10 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
		
			pap.parseEntities(new StringCharSource("?-10 == 10 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-10 =:= 10 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-20 \\== 10 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-20 =\\= 10 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-20 is 30 - 10 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-not fail ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			sr.onRemove(global);
		}
	}
	
	@Test
	public void temporaryUnificationTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StringBuilder			sb = new StringBuilder();

			// Test =..
			pap.parseEntities(new StringCharSource("?-pred(100) =.. [pred,100] ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-X =.. [pred,100] ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=pred(100)\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-pred(100) =.. X ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=[pred,100]\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-pred(100) =.. [pred, 200] ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-X =.. Y."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			// Test name(X,Y)
			pap.parseEntities(new StringCharSource("?-name(X,\"100\") ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=100\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-name(pred(100),X) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=\"pred(100)\"\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-name(pred(100),pred(100)) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			
			pap.parseEntities(new StringCharSource("?-name(X,X) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			// Test arg(X,N,Y)
			pap.parseEntities(new StringCharSource("?-arg(pred(100),1,100) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-arg(pred(X),1,X) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-arg(pred(X),1,Y) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\nY=Y\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-arg(X,1,Y) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-arg(100,1,Y) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-arg(X,1,100) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			// Test functor(X,Y,N)
			pap.parseEntities(new StringCharSource("?-functor(pred(100),pred,1) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-functor(pred(100),X,1) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=pred\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-functor(pred(100),pred,X) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=1\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-functor(pred(100),X,Y) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=pred\nY=1\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-functor(X,pred,2) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=pred(_,_)\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-functor(X,Y,Z) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-functor(X,Y,1) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-functor(pred(100),pred,2) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			
			// Test unification
			pap.parseEntities(new StringCharSource("?-X = 10 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=10\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-X = Y ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\nY=Y\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-10 \\= 20 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-10 = 20 ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
 			
			sr.onRemove(global);
		}
	}

	@Test
	public void assertionTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StringBuilder			sb = new StringBuilder();
			final long					predId = repo.termRepo().placeName("pred",null);
			final IFProPredicate		template = new PredicateEntity(predId,new AnonymousEntity());
			
			int	count = 0;
			for (IFProEntity item : repo.predicateRepo().call(template)) {
				count++;
			}
			Assert.assertEquals(0,count);
			
			pap.parseEntities(new StringCharSource("assert(pred(100)) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("assertz(pred(200)) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("asserta(pred(_)) ."),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			
			count = 0;
			for (IFProEntity item : repo.predicateRepo().call(template)) {
				count++;
			}
			Assert.assertEquals(3,count);
			
			sr.onRemove(global);
		}
	}
	
	@Test
	public void iterationTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StringBuilder			sb = new StringBuilder();
			final long					predId = repo.termRepo().placeName("pred",null);
			final IFProPredicate		template = new PredicateEntity(predId,new AnonymousEntity());

			pap.parseEntities(new StringCharSource("assert(pred(100)) . assert(pred(200)) . assert(pred(300)) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});

			int count = 0;
			for (IFProEntity item : repo.predicateRepo().call(template)) {
				count++;
			}
			Assert.assertEquals(3,count);
			
			// Test call(X)
			pap.parseEntities(new StringCharSource("?-call(pred(X)) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=100\nCall:\nX=200\nCall:\nX=300\n",sb.toString());

			pap.parseEntities(new StringCharSource("?-call(unknown) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			
			// Test Name/Arity
			pap.parseEntities(new StringCharSource("?-X/1 ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=pred\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-pred/X ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=1\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-X/Y ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=pred\nY=1\n",sb.toString());
			
			pap.parseEntities(new StringCharSource("?-X/2 ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});
			
			// Test memberof
			pap.parseEntities(new StringCharSource("?-memberOf(1,[1,2,3]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-memberOf(X,[1,2,3]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=1\nCall:\nX=2\nCall:\nX=3\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-memberOf(4,[1,2,3]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			// Test bagof(X,Y,Z)
			pap.parseEntities(new StringCharSource("?-bagof(X,pred(X),Y) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\nY=[100,200,300]\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-bagof(X,pred(X),[100,200,300]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-bagof(X,pred(X),[100,200|_]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-bagof(X,pred(X),[200|_]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			// Test setof(X,Y,Z)
			pap.parseEntities(new StringCharSource("?-setof(X,pred(X),Y) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\nY=[100,200,300]\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-setof(X,pred(X),[100,200,300]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-setof(X,pred(X),[100,200|_]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-setof(X,pred(X),[200|_]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			// Test findall(X,Y,Z)
			pap.parseEntities(new StringCharSource("?-findall(X,pred(X),Y) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\nY=[100,200,300]\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-findall(X,pred(X),[100,200,300]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-findall(X,pred(X),[100,200|_]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-findall(X,unknown(X),[]) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\n",sb.toString());

			// Test retract(X)
			pap.parseEntities(new StringCharSource("?-retract(pred(_)) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nCall:\nCall:\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-retract(pred(_)) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			count = 0;
			for (IFProEntity item : repo.predicateRepo().call(template)) {
				count++;
			}
			Assert.assertEquals(0,count);
			
			sr.onRemove(global);
		}
	}

	@Test
	public void andOrNotTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StringBuilder			sb = new StringBuilder();

			// Test AND
			pap.parseEntities(new StringCharSource("?-X = 10, Y = 10, X = Y ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=10\nY=10\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-X = 10, Y = 20, X = Y ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			// Test OR
			pap.parseEntities(new StringCharSource("?-X = 10, Y = 10, X = Y; X = 20, Y = 20, X = Y ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=10\nY=10\nCall:\nX=20\nY=20\n",sb.toString());
			pap.parseEntities(new StringCharSource("?-X = 10, Y = 20, X = Y; X = 20, Y = 10, X = Y ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,false,sb);
				return true;
			});

			// Test NOT
			pap.parseEntities(new StringCharSource("?-not (X = 10, Y = 20, X = Y) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			Assert.assertEquals("Call:\nX=X\nY=Y\n",sb.toString());

			sr.onRemove(global);
		}
	}

	@Test
	public void ruledEntityTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StringBuilder			sb = new StringBuilder();

			pap.parseEntities(new StringCharSource("pred(0) ."),(e,v)->{repo.predicateRepo().assertZ(e); return false;});
			pap.parseEntities(new StringCharSource("pred(Z):- Z < 0, !, fail ."),(e,v)->{repo.predicateRepo().assertZ(e); return false;});
			pap.parseEntities(new StringCharSource("pred(Z):- Z1 is Z - 1, pred(Z1) ."),(e,v)->{repo.predicateRepo().assertZ(e); return false;});

			// Test rules
			pap.parseEntities(new StringCharSource("?-pred(0) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-pred(1) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-pred(2) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			
			sr.onRemove(global);
		}
	}

	@Test
	public void cutOperatorTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StringBuilder			sb = new StringBuilder();

			pap.parseEntities(new StringCharSource("pred(0) ."),(e,v)->{repo.predicateRepo().assertZ(e); return false;});
			pap.parseEntities(new StringCharSource("pred(Z):- Z < 0, !, fail ."),(e,v)->{repo.predicateRepo().assertZ(e); return false;});
			pap.parseEntities(new StringCharSource("pred(Z):- Z1 is Z - 1, pred(Z1) ."),(e,v)->{repo.predicateRepo().assertZ(e); return false;});

			// Test rules
			pap.parseEntities(new StringCharSource("?-pred(0) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-pred(1) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			pap.parseEntities(new StringCharSource("?-pred(2) ." ),(entity,vars)->{
				processing(sr,global,stack,entity,vars,true,sb);
				return true;
			});
			
			sr.onRemove(global);
		}
	}
	
	@Test
	public void externalCallTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StringBuilder			sb = new StringBuilder();
			
			sr.onRemove(global);
		}
	}
	
//	@Test
	public void goalSetTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			CharacterSource				cs;
			
			cs = new ArrayCharSource(URIUtils.loadCharsFromURI(new File("./src/test/resources/chav1961/funnypro/core/environment.fpro").toURI()));

			pap.parseEntities(cs,new FProParserCallback(){
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
											try{pap.putEntity(entity,new WriterCharTarget(System.err,true));
											} catch (PrintingException e) {
												e.printStackTrace();
											}
											System.err.println();
											repo.predicateRepo().assertZ(entity);
											return true;
										}
									} 
			);
			
			cs = new ArrayCharSource(URIUtils.loadCharsFromURI(new File("./src/test/resources/chav1961/funnypro/core/trues.fpro").toURI()));

			pap.parseEntities(cs,new FProParserCallback(){
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
											try{pap.putEntity(entity,new WriterCharTarget(System.err,true));
											} catch (PrintingException e) {
												e.printStackTrace();
											}
											System.err.println();
											processing(sr,global,stack,entity,vars,true);
											vars.clear();
											return true;
										}
									}
			);
			
			cs = new ArrayCharSource(URIUtils.loadCharsFromURI(new File("./src/test/resources/chav1961/funnypro/core/falses.fpro").toURI()));

			pap.parseEntities(cs,new FProParserCallback(){
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
											try{pap.putEntity(entity,new WriterCharTarget(System.err,true));
											} catch (PrintingException e) {
												e.printStackTrace();
											}
											System.err.println();
											processing(sr,global,stack,entity,vars,false);
											vars.clear();
											return true;
										}
									}
			);
			sr.onRemove(global);
		}
	}

	//@Test
	public void performanceTest() throws Exception {
		try(final EntitiesRepo			repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,new Properties(),repo);
			final CharacterSource		cs = new ArrayCharSource(URIUtils.loadCharsFromURI(new File("./src/test/resources/chav1961/funnypro/core/environment.fpro").toURI()));

			pap.parseEntities(cs,new FProParserCallback(){	// Prepare environment
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
											repo.predicateRepo().assertZ(entity);
											return true;
										}
									}
			);

			final char[]	content = URIUtils.loadCharsFromURI(new File("./src/test/resources/chav1961/funnypro/core/trues.fpro").toURI());
			
			System.err.println("Starting...");
			final long	start = System.nanoTime();
			
			for (int index = 0; index < 1000000; index++) {
				pap.parseEntities(content,0,new FProParserCallback(){
												@Override
												public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
													try{pap.putEntity(entity,new WriterCharTarget(System.err,true));
													} catch (PrintingException e) {
														e.printStackTrace();
													}
													processing(sr,global,stack,entity,vars,true);
													vars.clear();
													return true;
												}
											}
				);
			}
			System.err.println("Duration = "+((System.nanoTime()-start)/1000000)+" msec");
			
			sr.onRemove(global);
		}
	}

	private void processing(final StandardResolver sr, final GlobalDescriptor global, final IFProGlobalStack stack, final IFProEntity goal, final List<IFProVariable> vars, final boolean awaitedResult) throws SyntaxException {
		final IFProCallback	callback = new IFProCallback(){
								@Override public void beforeFirstCall() {}
								@Override public void afterLastCall() {}
								@Override
								public boolean onResolution(String[] names, IFProEntity[] resolvedValues, String[] printedValues) throws SyntaxException {
									return true;
								}
							}; 
		processing(sr,global,stack,goal,vars,awaitedResult,callback);
	}

	private void processing(final StandardResolver sr, final GlobalDescriptor global, final IFProGlobalStack stack, final IFProEntity goal, final List<IFProVariable> vars, final boolean awaitedResult, final StringBuilder sb) throws SyntaxException {
		sb.setLength(0);
		final IFProCallback	callback = new IFProCallback(){
								@Override public void beforeFirstCall() {}
								@Override public void afterLastCall() {}
								@Override
								public boolean onResolution(String[] names, IFProEntity[] resolvedValues, String[] printedValues) throws SyntaxException {
									try{final CharacterTarget	target = new StringBuilderCharTarget(sb);
										
										target.put("Call:\n");
										for (int index = 0; index < names.length; index++) {
											target.put(names[index]).put('=').put(printedValues[index]).put('\n');
										}
										return true;
									} catch (PrintingException e) {
										throw new SyntaxException(0,0,e.getLocalizedMessage());
									}
								}
							}; 
		processing(sr,global,stack,goal,vars,awaitedResult,callback);
	}

	private void processing(final StandardResolver sr, final GlobalDescriptor global, final IFProGlobalStack stack, final IFProEntity goal, final List<IFProVariable> vars, final boolean awaitedResult, final IFProCallback callback) throws SyntaxException {
		Assert.assertTrue(stack.isEmpty());

		final LocalDescriptor	local = sr.beforeCall(global,stack,vars,callback);
		
		try{if (sr.firstResolve(global,local,goal) == ResolveRC.True) {
				while (sr.nextResolve(global,local,goal) == ResolveRC.True) {}
				Assert.assertTrue(awaitedResult);
				sr.endResolve(global,local,goal);
			}
			else {
				Assert.assertFalse(awaitedResult);
			}
		} finally {
			sr.afterCall(global,local);
		}
		
		Assert.assertTrue(stack.isEmpty());
	}
}
