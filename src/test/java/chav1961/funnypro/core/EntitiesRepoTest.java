package chav1961.funnypro.core;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.entities.OperatorDefEntity;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo.Classification;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorSort;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProRepo;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.streams.charsource.StringCharSource;
import chav1961.purelib.streams.chartarget.WriterCharTarget;

public class EntitiesRepoTest {
	@Test
	public void basicTest() throws RuntimeException, IOException {
		final SubstitutableProperties		props = new SubstitutableProperties();
		
		try(final EntitiesRepo	repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props)) {
			Assert.assertEquals(PureLibSettings.CURRENT_LOGGER,repo.getDebug());
			Assert.assertEquals(props,repo.getParameters());
			
			Assert.assertEquals(Classification.anonymous,repo.classify(repo.termRepo().seekName("_")));
			Assert.assertEquals(Classification.op,repo.classify(repo.termRepo().seekName("op")));
			Assert.assertEquals(Classification.operator,repo.classify(repo.termRepo().seekName(":-")));
			Assert.assertEquals(Classification.operator,repo.classify(repo.termRepo().seekName("?-")));
			Assert.assertEquals(Classification.term,repo.classify(repo.termRepo().seekName("a")));
			
			Assert.assertEquals(OperatorType.fx,repo.operatorType(repo.termRepo().seekName("fx")));
			Assert.assertEquals(OperatorType.fy,repo.operatorType(repo.termRepo().seekName("fy")));
			Assert.assertEquals(OperatorType.xf,repo.operatorType(repo.termRepo().seekName("xf")));
			Assert.assertEquals(OperatorType.yf,repo.operatorType(repo.termRepo().seekName("yf")));
			Assert.assertEquals(OperatorType.xfx,repo.operatorType(repo.termRepo().seekName("xfx")));
			Assert.assertEquals(OperatorType.xfy,repo.operatorType(repo.termRepo().seekName("xfy")));
			Assert.assertEquals(OperatorType.yfx,repo.operatorType(repo.termRepo().seekName("yfx")));

			try{repo.operatorType(repo.termRepo().seekName(":-"));
				Assert.fail("Mandatory exception was not detected (unknown id for operator type)");
			} catch (IllegalArgumentException exc) {
			}
		}

		try{new EntitiesRepo(null,props);
			Assert.fail("Mandatory exception wad not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,null);
			Assert.fail("Mandatory exception wad not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void operatorsTest() throws RuntimeException, IOException {
		final SubstitutableProperties		props = new SubstitutableProperties();
		
		try(final EntitiesRepo	repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props)) {
			final long			op1Id = repo.termRepo().placeName("<<<>>>",null), op2Id = repo.termRepo().placeName(">>><<<",null);
			final IFProOperator	op1 = new OperatorDefEntity(100,OperatorType.xfx,op1Id), op2 = new OperatorDefEntity(200,OperatorType.xfx,op2Id);
			final IFProOperator	op3 = new OperatorDefEntity(100,OperatorType.fx,op1Id), op4 = new OperatorDefEntity(200,OperatorType.xfx,op1Id);  
			
			repo.putOperatorDef(op1);
			repo.putOperatorDef(op2);
			repo.putOperatorDef(op3);
			repo.putOperatorDef(op4);
			
			try {repo.putOperatorDef(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {repo.putOperatorDef(op2);
				Assert.fail("Mandatory exception was not detected (duplicate definition)");
			} catch (IllegalArgumentException exc) {
			}
			
			final Set<Long>		registeredIds = new HashSet<>();
			for (IFProOperator item : repo.registeredOperators()) {
				registeredIds.add(item.getEntityId());
			}
			Assert.assertTrue(registeredIds.contains(op1Id));
			Assert.assertTrue(registeredIds.contains(op2Id));
			
			int count = 0;
			for (IFProOperator item : repo.getOperatorDef(op1Id,100,100,OperatorSort.infix)) {
				Assert.assertEquals(op1,item);
				count++;
			}
			Assert.assertEquals(1,count);

			count = 0;
			for (IFProOperator item : repo.getOperatorDef(op1Id,100,100,OperatorSort.postfix)) {
				Assert.assertEquals(op3,item);
				count++;
			}
			Assert.assertEquals(1,count);
		
			count = 0;	// Prefix ops are missing in the frb
			for (IFProOperator item : repo.getOperatorDef(op1Id,100,100,OperatorSort.prefix)) {
				count++;
			}
			Assert.assertEquals(0,count);

			count = 0;
			for (IFProOperator item : repo.getOperatorDef(op1Id,100,200,OperatorSort.infix)) {
				Assert.assertEquals(op1Id,item.getEntityId());
				count++;
			}
			Assert.assertEquals(2,count);
			
			count = 0;	// Inverted priorities
			for (IFProOperator item : repo.getOperatorDef(op1Id,200,100,OperatorSort.infix)) {
				Assert.assertEquals(op1Id,item.getEntityId());
				count++;
			}
			Assert.assertEquals(2,count);

			count = 0;	// Unknown op id 
			for (IFProOperator item : repo.getOperatorDef(-1,100,100,OperatorSort.prefix)) {
				count++;
			}
			Assert.assertEquals(0,count);
			
			try {repo.getOperatorDef(op1Id,-1,100,OperatorSort.prefix);
				Assert.fail("Mandatory exception was not detected (min priority out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try {repo.getOperatorDef(op1Id,1,9999,OperatorSort.prefix);
				Assert.fail("Mandatory exception was not detected (max priority out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try {repo.getOperatorDef(op1Id,100,100,null);
				Assert.fail("Mandatory exception was not detected (null 4-th argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

	@Test
	public void serializationTest() throws RuntimeException, IOException {
		final SubstitutableProperties		props = new SubstitutableProperties();
		
		try(final EntitiesRepo	repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props)) {
			final long			op1Id = repo.termRepo().placeName("<<<>>>",null), op2Id = repo.termRepo().placeName(">>><<<",null);
			final IFProOperator	op1 = new OperatorDefEntity(100,OperatorType.xfx,op1Id), op2 = new OperatorDefEntity(200,OperatorType.xfx,op2Id);
			final IFProOperator	op3 = new OperatorDefEntity(100,OperatorType.fx,op1Id), op4 = new OperatorDefEntity(200,OperatorType.xfx,op1Id);  
			
			repo.putOperatorDef(op1);
			repo.putOperatorDef(op2);
			repo.putOperatorDef(op3);
			repo.putOperatorDef(op4);

			try(final InOutGrowableByteArray	iogba = new InOutGrowableByteArray(false)) {
				repo.serialize(iogba);
				iogba.flush();
				iogba.reset();
				
				try(final EntitiesRepo	newRepo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props)) {
					final long			op1NewId = repo.termRepo().placeName("<<<>>>",null), op2NewId = repo.termRepo().placeName(">>><<<",null);
					final Set<Long>		registeredIds = new HashSet<>();

					for (IFProOperator item : newRepo.registeredOperators()) {
						registeredIds.add(item.getEntityId());
					}
					Assert.assertFalse(registeredIds.contains(op1NewId));
					Assert.assertFalse(registeredIds.contains(op2NewId));
					
					newRepo.deserialize(iogba);
					
					for (IFProOperator item : newRepo.registeredOperators()) {
						registeredIds.add(item.getEntityId());
					}
					Assert.assertTrue(registeredIds.contains(op1NewId));
					Assert.assertTrue(registeredIds.contains(op2NewId));
				}
			}
			
			try {repo.serialize(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {repo.deserialize(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

	@Test
	public void consultAndSaveTest() throws RuntimeException, IOException, SyntaxException, PrintingException {
		final SubstitutableProperties		props = new SubstitutableProperties();
		
		try(final EntitiesRepo	repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props)) {
			repo.consult(new StringCharSource(Utils.fromResource(this.getClass().getResource("consult.fpro"),"UTF-8")));

			try {repo.consult(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			try(final Writer	wr = new StringWriter()) {
				repo.save(new WriterCharTarget(wr,true));
				wr.flush();
				
				Assert.assertEquals("a(Z):-Z<0,!,false . a(0) . a(Z):-Z1 is Z-1,a(Z1) . ",wr.toString());
			}
			
			try {repo.save(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
		}

		try(final EntitiesRepo	repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props)) {
			repo.consult(Utils.fromResource(this.getClass().getResource("consult.fpro"),"UTF-8").toCharArray(),0);

			try {repo.consult(null,0);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {repo.consult("test".toCharArray(),-1);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try {repo.consult("test".toCharArray(),100);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			
			final char[]	target = new char[1000];
			final int		len = repo.save(target,0);
				
			Assert.assertEquals("a(Z):-Z<0,!,false . a(0) . a(Z):-Z1 is Z-1,a(Z1) . ",new String(target,0,len));
			
			try {repo.save(null,0);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try {repo.save(target,-1);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			try {repo.save(target,target.length+10);
				Assert.fail("Mandatory exception was not detected (2-nd argument out of range)");
			} catch (IllegalArgumentException exc) {
			}
			
		}
	}
}
