package chav1961.funnypro.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.Assert;

import chav1961.funnypro.core.StandardResolver.StandardOperators;
import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.OperatorDefEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
import chav1961.funnypro.core.entities.StringEntity;
import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo.Classification;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginItem;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProRepo.NameAndArity;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.purelib.basic.DefaultLoggerFacade;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.streams.charsource.ReaderCharSource;
import chav1961.purelib.streams.chartarget.WriterCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;


public class RepositoriesTest {

	@Test
	public void factRuleRepoTest() throws IOException {
		final LoggerFacade		log = new DefaultLoggerFacade();
		final FactRuleRepo		frr = new FactRuleRepo(log,new Properties());
		final IFProEntity		temp = new PredicateEntity(12345,new StringEntity(67890),new IntegerEntity(13579));
		
		int	count = 0;
		for (IFProEntity item : frr.call(temp)) {
			count++;
		}
		Assert.assertEquals(count,0);
		
		frr.assertZ(temp);
		
		count = 0;
		for (IFProEntity item : frr.call(temp)) {
			count++;
		}
		Assert.assertEquals(count,1);
		
		frr.retractAll(temp);
		
		count = 0;
		for (IFProEntity item : frr.call(temp)) {
			count++;
		}
		Assert.assertEquals(count,0);
		
		frr.assertZ(new PredicateEntity(12345,new AnonymousEntity(),new RealEntity(100)));
		frr.assertA(new PredicateEntity(12345,new StringEntity(12345),new IntegerEntity(0)));
		frr.assertZ(temp);
		
		count = 0;
		for (IFProEntity item : frr.call(temp)) {
			count++;
		}
		Assert.assertEquals(count,3);
		
		final FactRuleRepo			newFrr = new FactRuleRepo(log,new Properties());
		
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			frr.serialize(baos);	baos.flush();
			
			try(final InputStream	is = new ByteArrayInputStream(baos.toByteArray())) {
				newFrr.deserialize(is);
				
				count = 0;
				for (IFProEntity item : newFrr.call(temp)) {
					count++;
				}
				Assert.assertEquals(count,3);
			}
			
			frr.retractAll(temp);
			
			try(final InputStream	is = new ByteArrayInputStream(baos.toByteArray())) {
				frr.deserialize(is);
				
				count = 0;
				for (IFProEntity item : frr.call(temp)) {
					count++;
				}
				Assert.assertEquals(count,3);
			}
		}
	}
	
	@Test
	public void entitiesRepoTest() throws IOException, FProParsingException {
		final LoggerFacade		log = new DefaultLoggerFacade();
		final EntitiesRepo		repo = new EntitiesRepo(log,new Properties());
		
		final long				stringId = repo.stringRepo().placeName("test string",null);
		final long				termId = repo.termRepo().placeName("predicate",null);
		final long				operatorId = repo.termRepo().placeName("=:=",null);
		final IFProEntity		pred = new PredicateEntity(termId,new StringEntity(stringId),new IntegerEntity(12345));
		final IFProOperator		opDef = new OperatorDefEntity(100,OperatorType.fx,operatorId);
		final IFProOperator		opDef2 = new OperatorDefEntity(100,OperatorType.xf,operatorId);
		final IFProOperator		op = new OperatorEntity(opDef);
		final IFProOperator		op2 = new OperatorEntity(opDef2);
		
		Assert.assertEquals(repo.getOperatorDef(operatorId,IFProOperator.MIN_PRTY,IFProOperator.MAX_PRTY,OperatorType.fx).length,0);		
		repo.putOperatorDef(opDef);
		Assert.assertEquals(repo.getOperatorDef(operatorId,IFProOperator.MIN_PRTY,IFProOperator.MAX_PRTY,OperatorType.fy).length,0);		
		Assert.assertEquals(repo.getOperatorDef(operatorId,IFProOperator.MIN_PRTY,IFProOperator.MIN_PRTY,OperatorType.fx).length,0);		
		Assert.assertEquals(repo.getOperatorDef(operatorId,100,100,OperatorType.fx).length,1);		
		Assert.assertEquals(repo.getOperatorDef(operatorId,100,101,OperatorType.fx).length,1);		
		Assert.assertEquals(repo.getOperatorDef(operatorId,101,100,OperatorType.fx).length,1);		
		repo.putOperatorDef(opDef2);
		Assert.assertEquals(repo.getOperatorDef(operatorId,100,101,OperatorType.fx,OperatorType.xf).length,2);		
		Assert.assertEquals(repo.getOperatorDef(operatorId,101,100,OperatorType.fx,OperatorType.xf).length,2);		
		
		repo.predicateRepo().assertZ(pred);
		
		op.setRight(new AnonymousEntity());
		repo.predicateRepo().assertZ(op);

		Assert.assertEquals(repo.classify(termId),Classification.term);
		Assert.assertEquals(repo.classify(operatorId),Classification.operator);
		
		int count = 0;
		for (NameAndArity item : repo.predicateRepo().content(-1)) {
			count++;
		}
		Assert.assertEquals(count,2);
		
		final EntitiesRepo		newRepo = new EntitiesRepo(log,new Properties());
		
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			repo.serialize(baos);		baos.flush();
			
			try(final InputStream		is = new ByteArrayInputStream(baos.toByteArray())) {
				newRepo.deserialize(is);
			}
		}
		
		Assert.assertEquals(newRepo.stringRepo().getName(stringId),"test string");
		Assert.assertEquals(newRepo.termRepo().getName(termId),"predicate");
		Assert.assertEquals(newRepo.classify(termId),Classification.term);
		Assert.assertEquals(newRepo.classify(operatorId),Classification.operator);

		count = 0;
		for (NameAndArity item : newRepo.predicateRepo().content(-1)) {
			count++;
		}
		Assert.assertEquals(count,2);
		
		count = 0;
		for (IFProEntity item : newRepo.predicateRepo().call(pred)) {
			count++;
		}
		Assert.assertEquals(count,1);
		 
		count = 0;
		for (IFProEntity item : newRepo.predicateRepo().call(op)) {
			count++;
		}
		Assert.assertEquals(count,1);

		count = 0;
		for (IFProEntity item : newRepo.predicateRepo().call(new PredicateEntity(-1))) {
			count++;
		}
		Assert.assertEquals(count,0);
	}	

	@Test
	public void externalPluginsRepoTest() throws Exception {
		final LoggerFacade		log = new DefaultLoggerFacade();
		final EntitiesRepo		repo = new EntitiesRepo(log,new Properties());
		
		try(final IFProExternalPluginsRepo	xrepo = new ExternalPluginsRepo(log, new Properties())) {
			xrepo.prepare(repo);
			
			int	count = 0;			
			for (PluginItem item : xrepo.allPlugins()) {
				Assert.assertEquals(item.getDescriptor().getPluginDescription(),StandardResolver.PLUGIN_DESCRIPTION);
				Assert.assertEquals(item.getDescriptor().getPluginEntity().getPluginName(),StandardResolver.PLUGIN_NAME);
				Assert.assertEquals(item.getDescriptor().getPluginEntity().getPluginProducer(),StandardResolver.PLUGIN_PRODUCER);
				Assert.assertArrayEquals(item.getDescriptor().getPluginEntity().getPluginVersion(),StandardResolver.PLUGIN_VERSION);
				count++;
			}
			Assert.assertEquals(count,1);
			
			count = 0;			
			for (PluginItem item : xrepo.seek(StandardResolver.PLUGIN_NAME,StandardResolver.PLUGIN_PRODUCER,StandardResolver.PLUGIN_VERSION)) {
				Assert.assertEquals(item.getDescriptor().getPluginDescription(),StandardResolver.PLUGIN_DESCRIPTION);
				Assert.assertEquals(item.getDescriptor().getPluginEntity().getPluginName(),StandardResolver.PLUGIN_NAME);
				Assert.assertEquals(item.getDescriptor().getPluginEntity().getPluginProducer(),StandardResolver.PLUGIN_PRODUCER);
				Assert.assertArrayEquals(item.getDescriptor().getPluginEntity().getPluginVersion(),StandardResolver.PLUGIN_VERSION);
				count++;
			}
			Assert.assertEquals(count,1);
	
			for (StandardOperators item : StandardResolver.OPS) {
				final long	id = repo.termRepo().seekName(item.text);
				Assert.assertTrue(id != -1);
				Assert.assertEquals(repo.classify(id),Classification.operator);
			}
		}
	}
	
	
	@Test
	public void parsersTest() throws IOException, FProException, ContentException, PrintingException {
		final LoggerFacade			log = new DefaultLoggerFacade();
		final EntitiesRepo			repo = new EntitiesRepo(log,new Properties());
		final ParserAndPrinter		pap = new ParserAndPrinter(log,new Properties(),repo);
		final List<IFProEntity>		data = new ArrayList<IFProEntity>(); 
		
		try(final InputStream		is = new FileInputStream("./src/test/resources/chav1961/funnypro/core/parser.fpro");
			final Reader			rdr = new InputStreamReader(is);
			final Writer			wr = new OutputStreamWriter(System.err)) {
			final CharacterSource	cs = new ReaderCharSource(rdr,false);
			final CharacterTarget	ct = new WriterCharTarget(wr,false);
			
			pap.parseEntities(cs,new FProParserCallback(){
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
											try{pap.putEntity(entity,ct);
											} catch (PrintingException e) {
												e.printStackTrace();
												throw new IOException(e.getMessage());
											}
											data.add(entity);
											return true;
										}
									}
			);
		}
		Assert.assertEquals(data.size(),7); 
		
		try(final Writer			wr = new StringWriter()) {
			final CharacterTarget	ct = new WriterCharTarget(wr,false);
			
			for (IFProEntity item : data) {
				pap.putEntity(item,ct);
			}
			wr.flush();
			System.err.println("Printed data is: "+wr.toString());
		}
		
	}
}
