package chav1961.funnypro.core;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.OperatorDefEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
import chav1961.funnypro.core.entities.StringEntity;
import chav1961.funnypro.core.entities.VariableEntity;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.purelib.basic.DefaultLoggerFacade;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.streams.chartarget.StringBuilderCharTarget;
import chav1961.purelib.streams.interfaces.CharacterTarget;

public class ParserAndPrinterTest {
	private static final String		EXPRESSION = "predicate(123,123.456,\"mzinana\",X,_,test(X)):-predicate([X|_]),predicate((789,200));predicate(test)";

	@Test
	public void simpleParserTest() throws Exception {
		final LoggerFacade			log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo		repo = new EntitiesRepo(log,new Properties())) {
			final ParserAndPrinter	pap = new ParserAndPrinter(log,new Properties(),repo);

			final IFProEntity		integer = buildEntity(pap,"122 .");
			Assert.assertEquals(integer.getEntityType(),EntityType.integer);
			Assert.assertEquals(integer.getEntityId(),122);

			final IFProEntity		real = buildEntity(pap,"122.345 .");
			Assert.assertEquals(real.getEntityType(),EntityType.real);
			Assert.assertEquals(Double.longBitsToDouble(real.getEntityId()),122.345,0.0001);
			
			final IFProEntity		string = buildEntity(pap,"\"test string\".");
			Assert.assertEquals(string.getEntityType(),EntityType.string);
			Assert.assertEquals(string.getEntityId(),repo.stringRepo().seekName("test string"));

			final IFProEntity		anon = buildEntity(pap,"_.");
			Assert.assertEquals(anon.getEntityType(),EntityType.anonymous);
			
			final IFProEntity		term = buildEntity(pap,"testString.");
			Assert.assertEquals(term.getEntityType(),EntityType.predicate);
			Assert.assertEquals(term.getEntityId(),repo.termRepo().seekName("testString"));
		}
	}

	@Test
	public void listParserTest() throws Exception {
		final LoggerFacade			log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo		repo = new EntitiesRepo(log,new Properties())) {
			final ParserAndPrinter	pap = new ParserAndPrinter(log,new Properties(),repo);

			final IFProEntity		emptyList = buildEntity(pap,"[] .");
			Assert.assertEquals(emptyList.getEntityType(),EntityType.list);
			Assert.assertNull(((IFProList)emptyList).getChild());
			Assert.assertNull(((IFProList)emptyList).getTail());

			final IFProEntity		oneList = buildEntity(pap,"[123] .");
			Assert.assertEquals(oneList.getEntityType(),EntityType.list);
			Assert.assertEquals(((IFProList)oneList).getChild().getEntityId(),123);
			Assert.assertNull(((IFProList)oneList).getTail());

			final IFProEntity		twoList = buildEntity(pap,"[123,456] .");
			Assert.assertEquals(twoList.getEntityType(),EntityType.list);
			Assert.assertEquals(((IFProList)twoList).getChild().getEntityId(),123);
			Assert.assertEquals(((IFProList)((IFProList)twoList).getTail()).getEntityType(),EntityType.list);
			Assert.assertEquals(((IFProList)((IFProList)((IFProList)twoList).getTail())).getChild().getEntityId(),456);

			final IFProEntity		threeList = buildEntity(pap,"[123,456,789] .");
			Assert.assertEquals(threeList.getEntityType(),EntityType.list);
			Assert.assertEquals(((IFProList)threeList).getChild().getEntityId(),123);
			Assert.assertEquals(((IFProList)((IFProList)threeList).getTail()).getChild().getEntityId(),456);
			Assert.assertEquals(((IFProList)((IFProList)((IFProList)threeList).getTail()).getTail()).getChild().getEntityId(),789);
			
			final IFProEntity		threeListWithTail = buildEntity(pap,"[123,456,789|X] .");
			Assert.assertEquals(threeListWithTail.getEntityType(),EntityType.list);
			Assert.assertEquals(((IFProList)threeListWithTail).getChild().getEntityId(),123);
			Assert.assertEquals(((IFProList)((IFProList)threeListWithTail).getTail()).getChild().getEntityId(),456);
			Assert.assertEquals(((IFProList)((IFProList)((IFProList)threeListWithTail).getTail()).getTail()).getChild().getEntityId(),789);
			Assert.assertEquals(((IFProList)((IFProList)((IFProList)threeListWithTail).getTail()).getTail()).getTail().getEntityType(),EntityType.variable);

			final IFProEntity		nested = buildEntity(pap,"[123,[456|X]|Y] .");
			Assert.assertEquals(nested.getEntityType(),EntityType.list);
			Assert.assertEquals(((IFProList)nested).getChild().getEntityId(),123);
			Assert.assertEquals(((IFProList)((IFProList)nested).getTail()).getChild().getEntityType(),EntityType.list);
			Assert.assertEquals(((IFProList)((IFProList)((IFProList)nested).getTail()).getChild()).getChild().getEntityId(),456);
			Assert.assertEquals(((IFProList)((IFProList)((IFProList)nested).getTail()).getChild()).getTail().getEntityType(),EntityType.variable);
			Assert.assertEquals(((IFProList)((IFProList)nested).getTail()).getTail().getEntityType(),EntityType.variable);
			
			try{buildEntity(pap,"[123,456,789|0] .");
				Assert.fail("Mandatory exception was not detected (tail of the list can be variable only)");
			} catch (SyntaxException exc) {				
			}
			try{buildEntity(pap,"[123,456,789|0 .");
				Assert.fail("Mandatory exception was not detected (unclosed bracket)");
			} catch (SyntaxException exc) {				
			}
		}
	}

	@Test
	public void bracketsParserTest() throws Exception {
		final LoggerFacade			log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo		repo = new EntitiesRepo(log,new Properties())) {
			final ParserAndPrinter	pap = new ParserAndPrinter(log,new Properties(),repo);

			Assert.assertNull(buildEntity(pap,"()."));
			
			final IFProEntity		single = buildEntity(pap,"(123).");
			Assert.assertEquals(single.getEntityType(),EntityType.integer);
			Assert.assertEquals(single.getEntityId(),123);

			final IFProEntity		anded = buildEntity(pap,"(123,456).");
			Assert.assertEquals(anded.getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)anded).getLeft().getEntityId(),123);
			Assert.assertEquals(((IFProOperator)anded).getRight().getEntityId(),456);

			final IFProEntity		oredAnded = buildEntity(pap,"(123,456;789).");
			Assert.assertEquals(oredAnded.getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)oredAnded).getLeft().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)((IFProOperator)oredAnded).getLeft()).getLeft().getEntityId(),123);
			Assert.assertEquals(((IFProOperator)((IFProOperator)oredAnded).getLeft()).getRight().getEntityId(),456);
			Assert.assertEquals(((IFProOperator)oredAnded).getRight().getEntityId(),789);

			final IFProEntity		nested = buildEntity(pap,"((123,456),789).");
			Assert.assertEquals(anded.getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)nested).getLeft().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)((IFProOperator)nested).getLeft()).getLeft().getEntityId(),123);
			Assert.assertEquals(((IFProOperator)((IFProOperator)nested).getLeft()).getRight().getEntityId(),456);
			Assert.assertEquals(((IFProOperator)nested).getRight().getEntityId(),789);

			try{buildEntity(pap,"(123 .");
				Assert.fail("Mandatory exception was not detected (unclosed bracket)");
			} catch (SyntaxException exc) {				
			}
		}
	}
	
	
	@Test
	public void predicateParserTest() throws Exception {
		final LoggerFacade			log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo		repo = new EntitiesRepo(log,new Properties())) {
			final ParserAndPrinter	pap = new ParserAndPrinter(log,new Properties(),repo);
			
			final IFProEntity		arity0 = buildEntity(pap,"predicate.");
			Assert.assertEquals(arity0.getEntityType(),EntityType.predicate);
			Assert.assertEquals(((IFProPredicate)arity0).getArity(),0);
			
			final IFProEntity		arity1 = buildEntity(pap,"predicate(123).");
			Assert.assertEquals(arity1.getEntityType(),EntityType.predicate);
			Assert.assertEquals(((IFProPredicate)arity1).getArity(),1);
			Assert.assertEquals(((IFProPredicate)arity1).getParameters()[0].getEntityId(),123);

			final IFProEntity		arity2 = buildEntity(pap,"predicate(123,456).");
			Assert.assertEquals(arity2.getEntityType(),EntityType.predicate);
			Assert.assertEquals(((IFProPredicate)arity2).getArity(),2);
			Assert.assertEquals(((IFProPredicate)arity2).getParameters()[0].getEntityId(),123);
			Assert.assertEquals(((IFProPredicate)arity2).getParameters()[1].getEntityId(),456);

			final IFProEntity		arity3 = buildEntity(pap,"predicate(123,456,789).");
			Assert.assertEquals(arity2.getEntityType(),EntityType.predicate);
			Assert.assertEquals(((IFProPredicate)arity3).getArity(),3);
			Assert.assertEquals(((IFProPredicate)arity3).getParameters()[0].getEntityId(),123);
			Assert.assertEquals(((IFProPredicate)arity3).getParameters()[1].getEntityId(),456);
			Assert.assertEquals(((IFProPredicate)arity3).getParameters()[2].getEntityId(),789);

			final IFProEntity		nested = buildEntity(pap,"predicate(predicate(123)).");
			Assert.assertEquals(nested.getEntityType(),EntityType.predicate);
			Assert.assertEquals(((IFProPredicate)nested).getArity(),1);
			Assert.assertEquals(((IFProPredicate)nested).getParameters()[0].getEntityType(),EntityType.predicate);
			Assert.assertEquals(((IFProPredicate)((IFProPredicate)nested).getParameters()[0]).getArity(),1);
			Assert.assertEquals(((IFProPredicate)((IFProPredicate)nested).getParameters()[0]).getParameters()[0].getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProPredicate)((IFProPredicate)nested).getParameters()[0]).getParameters()[0].getEntityId(),123);

			final IFProEntity		withPrefix = buildEntity(pap,":-predicate(123).");
			Assert.assertEquals(withPrefix.getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)withPrefix).getRight().getEntityType(),EntityType.predicate);
			Assert.assertEquals(((IFProPredicate)((IFProOperator)withPrefix).getRight()).getArity(),1);
			Assert.assertEquals(((IFProPredicate)((IFProOperator)withPrefix).getRight()).getParameters()[0].getEntityId(),123);
		}
	}

	@Test
	public void unaryPriorityParserTest() throws Exception {
		final LoggerFacade			log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo		repo = new EntitiesRepo(log,new Properties())) {
			final ParserAndPrinter	pap = new ParserAndPrinter(log,new Properties(),repo);
			
			final long				id200x = repo.termRepo().placeName(":200",null);
			final long				id200y = repo.termRepo().placeName("::200",null);
			final long				id150x = repo.termRepo().placeName(":150",null);
			final long				id150y = repo.termRepo().placeName("::150",null);
			
			repo.putOperatorDef(new OperatorDefEntity(200,OperatorType.fx,id200x));
			repo.putOperatorDef(new OperatorDefEntity(200,OperatorType.fy,id200y));
			repo.putOperatorDef(new OperatorDefEntity(150,OperatorType.fx,id150x));
			repo.putOperatorDef(new OperatorDefEntity(150,OperatorType.fy,id150y));

			repo.putOperatorDef(new OperatorDefEntity(200,OperatorType.xf,id200x));
			repo.putOperatorDef(new OperatorDefEntity(200,OperatorType.yf,id200y));
			repo.putOperatorDef(new OperatorDefEntity(150,OperatorType.xf,id150x));
			repo.putOperatorDef(new OperatorDefEntity(150,OperatorType.yf,id150y));
			
			final IFProEntity		yyLeft = buildEntity(pap,"::200 ::150 123 .");
			Assert.assertEquals(yyLeft.getEntityType(),EntityType.operator);
			Assert.assertEquals(yyLeft.getEntityId(),id200y);
			Assert.assertEquals(((IFProOperator)yyLeft).getRight().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)yyLeft).getRight().getEntityId(),id150y);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yyLeft).getRight()).getRight().getEntityId(),123);

			final IFProEntity		yyLeft2 = buildEntity(pap,"::200 ::200 123 .");
			Assert.assertEquals(yyLeft2.getEntityType(),EntityType.operator);
			Assert.assertEquals(yyLeft2.getEntityId(),id200y);
			Assert.assertEquals(((IFProOperator)yyLeft2).getRight().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)yyLeft2).getRight().getEntityId(),id200y);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yyLeft2).getRight()).getRight().getEntityId(),123);
			
			try{buildEntity(pap,"::150 ::200 123.");
				Assert.fail("Mandatory exception was not detected (illegal priority nesting)");
			} catch (SyntaxException exc) {
			}

			try{buildEntity(pap," :200 :200 123.");
				Assert.fail("Mandatory exception was not detected (illegal priority nesting because fx)");
			} catch (SyntaxException exc) {
			}
			
			final IFProEntity		yyRight = buildEntity(pap,"123 ::150 ::200 .");
			Assert.assertEquals(yyRight.getEntityType(),EntityType.operator);
			Assert.assertEquals(yyRight.getEntityId(),id200y);
			Assert.assertEquals(((IFProOperator)yyRight).getLeft().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)yyRight).getLeft().getEntityId(),id150y);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yyRight).getLeft()).getLeft().getEntityId(),123);

			final IFProEntity		yyRight2 = buildEntity(pap,"123 ::200 ::200 .");
			Assert.assertEquals(yyRight2.getEntityType(),EntityType.operator);
			Assert.assertEquals(yyRight2.getEntityId(),id200y);
			Assert.assertEquals(((IFProOperator)yyRight2).getLeft().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)yyRight2).getLeft().getEntityId(),id200y);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yyRight2).getLeft()).getLeft().getEntityId(),123);
			
			try{buildEntity(pap,"123 ::200 ::150 .");
				Assert.fail("Mandatory exception was not detected (illegal priority nesting)");
			} catch (SyntaxException exc) {
			}			

			try{buildEntity(pap,"123 :200 :200 .");
				Assert.fail("Mandatory exception was not detected (illegal priority nesting because xf)");
			} catch (SyntaxException exc) {
			}			

			final IFProEntity		yyBoth = buildEntity(pap,"::200 ::150 123 ::150 ::200.");
			Assert.assertEquals(yyBoth.getEntityType(),EntityType.operator);
			Assert.assertEquals(yyBoth.getEntityId(),id200y);
			Assert.assertEquals(((IFProOperator)yyBoth).getLeft().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)yyBoth).getLeft().getEntityId(),id200y);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yyBoth).getLeft()).getRight().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yyBoth).getLeft()).getRight().getEntityId(),id150y);
			Assert.assertEquals(((IFProOperator)((IFProOperator)((IFProOperator)yyBoth).getLeft()).getRight()).getLeft().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)((IFProOperator)((IFProOperator)yyBoth).getLeft()).getRight()).getLeft().getEntityId(),id150y);
			Assert.assertEquals(((IFProOperator)((IFProOperator)((IFProOperator)((IFProOperator)yyBoth).getLeft()).getRight()).getLeft()).getRight().getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProOperator)((IFProOperator)((IFProOperator)((IFProOperator)yyBoth).getLeft()).getRight()).getLeft()).getRight().getEntityId(),123);

			final IFProEntity		yyBoth2 = buildEntity(pap,":200 :150 123 :150 :200.");
			Assert.assertEquals(yyBoth2.getEntityType(),EntityType.operator);
			Assert.assertEquals(yyBoth2.getEntityId(),id200x);
			Assert.assertEquals(((IFProOperator)yyBoth2).getLeft().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)yyBoth2).getLeft().getEntityId(),id200x);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yyBoth2).getLeft()).getRight().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yyBoth2).getLeft()).getRight().getEntityId(),id150x);
			Assert.assertEquals(((IFProOperator)((IFProOperator)((IFProOperator)yyBoth2).getLeft()).getRight()).getLeft().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)((IFProOperator)((IFProOperator)yyBoth2).getLeft()).getRight()).getLeft().getEntityId(),id150x);
			Assert.assertEquals(((IFProOperator)((IFProOperator)((IFProOperator)((IFProOperator)yyBoth2).getLeft()).getRight()).getLeft()).getRight().getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProOperator)((IFProOperator)((IFProOperator)((IFProOperator)yyBoth2).getLeft()).getRight()).getLeft()).getRight().getEntityId(),123);
		}
	}

	@Test
	public void binaryPriorityParserTest() throws Exception {
		final LoggerFacade			log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo		repo = new EntitiesRepo(log,new Properties())) {
			final ParserAndPrinter	pap = new ParserAndPrinter(log,new Properties(),repo);
			
			final long				xfx = repo.termRepo().placeName("::xfx::",null);
			final long				xfy = repo.termRepo().placeName("::xfy::",null);
			final long				yfx = repo.termRepo().placeName("::yfx::",null);
			final long				fx1 = repo.termRepo().placeName("::fx1::",null);
			final long				fx2 = repo.termRepo().placeName("::fx2::",null);
			
			repo.putOperatorDef(new OperatorDefEntity(300,OperatorType.fx,fx1));
			repo.putOperatorDef(new OperatorDefEntity(200,OperatorType.xfx,xfx));
			repo.putOperatorDef(new OperatorDefEntity(100,OperatorType.xfy,xfy));
			repo.putOperatorDef(new OperatorDefEntity(100,OperatorType.yfx,yfx));
			repo.putOperatorDef(new OperatorDefEntity(50,OperatorType.fx,fx2));

			final IFProEntity		yfxLeft = buildEntity(pap,"123 ::yfx:: 456 ::yfx:: 789 .");
			Assert.assertEquals(yfxLeft.getEntityType(),EntityType.operator);
			Assert.assertEquals(yfxLeft.getEntityId(),yfx);
			Assert.assertEquals(((IFProOperator)yfxLeft).getLeft().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)yfxLeft).getLeft().getEntityId(),yfx);
			Assert.assertEquals(((IFProOperator)yfxLeft).getRight().getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProOperator)yfxLeft).getRight().getEntityId(),789);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yfxLeft).getLeft()).getLeft().getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yfxLeft).getLeft()).getLeft().getEntityId(),123);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yfxLeft).getLeft()).getRight().getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProOperator)((IFProOperator)yfxLeft).getLeft()).getRight().getEntityId(),456);

			final IFProEntity		xfyRight = buildEntity(pap,"123 ::xfy:: 456 ::xfy:: 789 .");
			Assert.assertEquals(xfyRight.getEntityType(),EntityType.operator);
			Assert.assertEquals(xfyRight.getEntityId(),xfy);
			Assert.assertEquals(((IFProOperator)xfyRight).getLeft().getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProOperator)xfyRight).getLeft().getEntityId(),123);
			Assert.assertEquals(((IFProOperator)xfyRight).getRight().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)xfyRight).getRight().getEntityId(),xfy);
			Assert.assertEquals(((IFProOperator)((IFProOperator)xfyRight).getRight()).getLeft().getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProOperator)((IFProOperator)xfyRight).getRight()).getLeft().getEntityId(),456);
			Assert.assertEquals(((IFProOperator)((IFProOperator)xfyRight).getRight()).getRight().getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProOperator)((IFProOperator)xfyRight).getRight()).getRight().getEntityId(),789);
			
			try{buildEntity(pap,"123 ::xfx:: 456 ::xfx:: 789 .");
				Assert.fail("Mandatory exception was not detected (illegal priority nesting because xfx)");
			} catch (SyntaxException exc) {
			}

			final IFProEntity		fx1xfy = buildEntity(pap,"::fx1:: 123 ::xfy:: 456 .");
			Assert.assertEquals(fx1xfy.getEntityType(),EntityType.operator);
			Assert.assertEquals(fx1xfy.getEntityId(),fx1);
			Assert.assertEquals(((IFProOperator)fx1xfy).getRight().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)fx1xfy).getRight().getEntityId(),xfy);
			Assert.assertEquals(((IFProOperator)((IFProOperator)fx1xfy).getRight()).getLeft().getEntityId(),123);
			Assert.assertEquals(((IFProOperator)((IFProOperator)fx1xfy).getRight()).getRight().getEntityId(),456);

			final IFProEntity		fx2xfy = buildEntity(pap,"::fx2:: 123 ::xfy:: 456 .");
			Assert.assertEquals(fx2xfy.getEntityType(),EntityType.operator);
			Assert.assertEquals(fx2xfy.getEntityId(),xfy);
			Assert.assertEquals(((IFProOperator)fx2xfy).getLeft().getEntityType(),EntityType.operator);
			Assert.assertEquals(((IFProOperator)fx2xfy).getLeft().getEntityId(),fx2);
			Assert.assertEquals(((IFProOperator)((IFProOperator)fx2xfy).getLeft()).getRight().getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProOperator)((IFProOperator)fx2xfy).getLeft()).getRight().getEntityId(),123);
			Assert.assertEquals(((IFProOperator)fx2xfy).getRight().getEntityType(),EntityType.integer);
			Assert.assertEquals(((IFProOperator)fx2xfy).getRight().getEntityId(),456);
		}
	}
	

	@Test
	public void variableParserTest() throws Exception {
		final LoggerFacade			log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo		repo = new EntitiesRepo(log,new Properties())) {
			final ParserAndPrinter	pap = new ParserAndPrinter(log,new Properties(),repo);

			final IFProEntity		variable = buildEntity(pap,"Variable.");
			Assert.assertEquals(variable.getEntityType(),EntityType.variable);
			Assert.assertEquals(variable.getEntityId(),repo.termRepo().seekName("Variable"));
			
			final IFProEntity		twoVariable = buildEntity(pap,"[Variable1|Variable2].");
			Assert.assertEquals(twoVariable.getEntityType(),EntityType.list);
			Assert.assertEquals(((IFProList)twoVariable).getChild().getEntityType(),EntityType.variable);
			Assert.assertEquals(((IFProList)twoVariable).getTail().getEntityType(),EntityType.variable);

			final List<IFProVariable>	list = new ArrayList<>();
			buildEntity(pap,"[Variable1|Variable2].",list);
			Assert.assertEquals(list.size(),2);
			 
			final IFProEntity		sameVariable = buildEntity(pap,"[Variable|Variable].");
			Assert.assertEquals(sameVariable.getEntityType(),EntityType.list);
			Assert.assertEquals(((IFProList)sameVariable).getChild().getEntityType(),EntityType.variable);
			Assert.assertEquals(((IFProList)sameVariable).getTail().getEntityType(),EntityType.variable);
			Assert.assertEquals(((IFProList)sameVariable).getChild().getEntityId(),((IFProList)sameVariable).getTail().getEntityId());
			
			list.clear();
			buildEntity(pap,"[Variable|Variable].",list);
			Assert.assertEquals(list.size(),1);
			
			int				count = 0;
			IFProVariable 	start = list.get(0);
			do {start = start.getChain();
				count++;
			} while (start != list.get(0));
			Assert.assertEquals(count,3);
		}
	}
	
	@Test
	public void putEntityTest() throws Exception {
		final LoggerFacade			log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo		repo = new EntitiesRepo(log,new Properties())) {
			final ParserAndPrinter	pap = new ParserAndPrinter(log,new Properties(),repo);
			final StringBuilder		sb = new StringBuilder();
			final CharacterTarget	ct = new StringBuilderCharTarget(sb);
		
			pap.putEntity(new AnonymousEntity(),ct);
			Assert.assertEquals(sb.toString(),"_");		
			sb.setLength(0);
			
			pap.putEntity(new IntegerEntity(123),ct);
			Assert.assertEquals(sb.toString(),"123");
			sb.setLength(0);

			pap.putEntity(new RealEntity(123.456),ct);
			Assert.assertEquals(sb.toString(),"123.456");
			sb.setLength(0);

			final long		stringId = repo.stringRepo().placeName("mzinana",null);
			pap.putEntity(new StringEntity(stringId),ct);
			Assert.assertEquals(sb.toString(),"\"mzinana\"");
			sb.setLength(0);

			final long		varId = repo.termRepo().placeName("Variable",null);
			pap.putEntity(new VariableEntity(varId),ct);
			Assert.assertEquals(sb.toString(),"Variable");
			sb.setLength(0);
			
			pap.putEntity(new ListEntity(new IntegerEntity(123),new VariableEntity(varId)),ct);
			Assert.assertEquals(sb.toString(),"[123|Variable]");
			sb.setLength(0);

			final long		predId = repo.termRepo().placeName("predicate",null);
			pap.putEntity(new PredicateEntity(predId),ct);
			Assert.assertEquals(sb.toString(),"predicate");
			sb.setLength(0);

			pap.putEntity(new PredicateEntity(predId,new IFProEntity[]{new IntegerEntity(123),new IntegerEntity(456)}),ct);
			Assert.assertEquals(sb.toString(),"predicate(123,456)");
			sb.setLength(0);

			final long		opId = repo.termRepo().placeName("***",null);
			repo.putOperatorDef(new OperatorDefEntity(100,OperatorType.fx,opId));
			repo.putOperatorDef(new OperatorDefEntity(100,OperatorType.xf,opId));
			repo.putOperatorDef(new OperatorDefEntity(100,OperatorType.xfx,opId));
			
			pap.putEntity(new OperatorEntity(100,OperatorType.fx,opId).setRight(new IntegerEntity(123)),ct);
			Assert.assertEquals(sb.toString(),"***123");
			sb.setLength(0);
			
			pap.putEntity(new OperatorEntity(100,OperatorType.xf,opId).setLeft(new IntegerEntity(123)),ct);
			Assert.assertEquals(sb.toString(),"123***");
			sb.setLength(0);

			pap.putEntity(new OperatorEntity(100,OperatorType.xfx,opId).setLeft(new IntegerEntity(123)).setRight(new IntegerEntity(456)),ct);
			Assert.assertEquals(sb.toString(),"123***456");
			sb.setLength(0);
			
			final char[]	buffer = new char[100];
			final int		filled = pap.putEntity(new IntegerEntity(123),buffer,0);
			
			Assert.assertEquals(filled,3);
			Assert.assertEquals(new String(buffer,0,filled),"123");
		}		
	}

	@Test
	public void complexTest() throws Exception {
		final LoggerFacade			log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo		repo = new EntitiesRepo(log,new Properties())) {
			final ParserAndPrinter	pap = new ParserAndPrinter(log,new Properties(),repo);

			final IFProEntity		entity = buildEntity(pap,EXPRESSION+".");
			final StringBuilder		sb = new StringBuilder();
			final CharacterTarget	ct = new StringBuilderCharTarget(sb);
			
			pap.putEntity(entity,ct);
			
			Assert.assertEquals(sb.toString(),EXPRESSION.replace("(789,200)","789,200"));
		}
	}


	private IFProEntity buildEntity(final ParserAndPrinter pap, final String source) throws ContentException, IOException {
		final IFProEntity[]	result = new IFProEntity[1];
		
		pap.parseEntities(source.toCharArray(),0,new FProParserCallback(){
								@Override
								public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
									result[0] = entity;
									return true;
								}
							}
		);
		return result[0];
	}

	private IFProEntity buildEntity(final ParserAndPrinter pap, final String source, final List<IFProVariable> varList) throws ContentException, IOException {
		final IFProEntity[]	result = new IFProEntity[1];
		
		pap.parseEntities(source.toCharArray(),0,new FProParserCallback(){
								@Override
								public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
									result[0] = entity;
									varList.addAll(vars);
									return true;
								}
							}
		);
		return result[0];
	}
}
