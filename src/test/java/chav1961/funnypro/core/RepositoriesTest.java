package chav1961.funnypro.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.OperatorDefEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
import chav1961.funnypro.core.entities.StringEntity;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo.Classification;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginItem;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorSort;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProRepo.NameAndArity;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.purelib.basic.DefaultLoggerFacade;
import chav1961.purelib.basic.OrdinalSyntaxTree;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.charsource.ReaderCharSource;
import chav1961.purelib.streams.chartarget.WriterCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;


public class RepositoriesTest {

	@Test
	public void factRuleRepoTest() throws IOException {
		final LoggerFacade		log = new DefaultLoggerFacade();
		final SubstitutableProperties	props = new SubstitutableProperties();
		final SyntaxTreeInterface<?>	sti = new OrdinalSyntaxTree<>(); 
		final FactRuleRepo		frr = new FactRuleRepo(log, props, sti);
		final IFProEntity		temp = new PredicateEntity(12345,new StringEntity(67890),new IntegerEntity(13579));
		
		int	count = 0;
		for (IFProEntity item : frr.call(temp)) {
			Assert.assertNotNull(item);
			count++;
		}
		Assert.assertEquals(count,0);
		
		frr.assertZ(temp);
		
		count = 0;
		for (IFProEntity item : frr.call(temp)) {
			Assert.assertNotNull(item);
			count++;
		}
		Assert.assertEquals(count,1);
		
		frr.retractAll(temp);
		
		count = 0;
		for (IFProEntity item : frr.call(temp)) {
			Assert.assertNotNull(item);
			count++;
		}
		Assert.assertEquals(count,0);
		
		frr.assertZ(new PredicateEntity(12345,new AnonymousEntity(),new RealEntity(100)));
		frr.assertA(new PredicateEntity(12345,new StringEntity(12345),new IntegerEntity(0)));
		frr.assertZ(temp);
		
		count = 0;
		for (IFProEntity item : frr.call(temp)) {
			Assert.assertNotNull(item);
			count++;
		}
		Assert.assertEquals(count,3);
		
		final FactRuleRepo			newFrr = new FactRuleRepo(log, props, sti);
		
/*		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final DataOutputStream		dos = new DataOutputStream(baos)) {
			
			frr.serialize(dos);			dos.flush();
			
			try(final InputStream		is = new ByteArrayInputStream(baos.toByteArray());
				final DataInputStream	dis = new DataInputStream(is)) {
				
				newFrr.deserialize(dis);
				
				count = 0;
				for (IFProEntity item : newFrr.call(temp)) {
					Assert.assertNotNull(item);
					count++;
				}
				Assert.assertEquals(count,3);
			}
			
			frr.retractAll(temp);
			
			try(final InputStream		is = new ByteArrayInputStream(baos.toByteArray());
				final DataInputStream	dis = new DataInputStream(is)) {
				
				frr.deserialize(dis);
				
				count = 0;
				for (IFProEntity item : frr.call(temp)) {
					Assert.assertNotNull(item);
					count++;
				}
				Assert.assertEquals(count,3);
			}
		}
		*/
	}
	
	@Test
	public void entitiesRepoTest() throws Exception {
		final LoggerFacade		log = new DefaultLoggerFacade();
		final SubstitutableProperties		props = new SubstitutableProperties();
		
		props.setProperty(IFProVM.PROP_DONT_LOAD_ALL_PLUGINS, "true");
		
		try(final EntitiesRepo	repo = new EntitiesRepo(log,props)) {
			final long				stringId = repo.stringRepo().placeName("test string",null);
			final long				termId = repo.termRepo().placeName("predicate",null);
			final long				operatorId = repo.termRepo().placeName("=:=",null);
			final IFProEntity		pred = new PredicateEntity(termId,new StringEntity(stringId),new IntegerEntity(12345));
			final IFProOperator		opDef = new OperatorDefEntity(100,OperatorType.fx,operatorId);
			final IFProOperator		opDef2 = new OperatorDefEntity(100,OperatorType.xf,operatorId);
			final IFProOperator		op = new OperatorEntity(opDef);
			
			Assert.assertEquals(repo.getOperatorDef(operatorId,IFProOperator.MIN_PRTY,IFProOperator.MAX_PRTY,OperatorSort.postfix).length,0);		
			repo.putOperatorDef(opDef);
			Assert.assertEquals(repo.getOperatorDef(operatorId,IFProOperator.MIN_PRTY,IFProOperator.MAX_PRTY,OperatorSort.prefix).length,0);		
			Assert.assertEquals(repo.getOperatorDef(operatorId,IFProOperator.MIN_PRTY,IFProOperator.MIN_PRTY,OperatorSort.postfix).length,0);		
			Assert.assertEquals(repo.getOperatorDef(operatorId,100,100,OperatorSort.postfix).length,1);		
			Assert.assertEquals(repo.getOperatorDef(operatorId,100,101,OperatorSort.postfix).length,1);		
			Assert.assertEquals(repo.getOperatorDef(operatorId,101,100,OperatorSort.postfix).length,1);		
			repo.putOperatorDef(opDef2);
//			Assert.assertEquals(repo.getOperatorDef(operatorId,100,101,OperatorType.fx,OperatorType.xf).length,2);		
//			Assert.assertEquals(repo.getOperatorDef(operatorId,101,100,OperatorType.fx,OperatorType.xf).length,2);		
			
			repo.predicateRepo().assertZ(pred);
			
			op.setRight(new AnonymousEntity());
			repo.predicateRepo().assertZ(op);
	
			Assert.assertEquals(repo.classify(termId),Classification.term);
			Assert.assertEquals(repo.classify(operatorId),Classification.operator);
			
			int count = 0;
			for (NameAndArity item : repo.predicateRepo().content(-1)) {
				Assert.assertNotNull(item);
				count++;
			}
			Assert.assertEquals(count,2);
			
			try(final EntitiesRepo		newRepo = new EntitiesRepo(log,props)) {
				try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
					final DataOutputStream		dos = new DataOutputStream(baos)) {
					
					repo.serialize(dos);		dos.flush();
					
					try(final InputStream		is = new ByteArrayInputStream(baos.toByteArray());
						final DataInputStream	dis = new DataInputStream(is)) {
						newRepo.deserialize(dis);
					}
				}
				
				Assert.assertEquals(newRepo.stringRepo().getName(stringId),"test string");
				Assert.assertEquals(newRepo.termRepo().getName(termId),"predicate");
				Assert.assertEquals(newRepo.classify(termId),Classification.term);
				Assert.assertEquals(newRepo.classify(operatorId),Classification.operator);
		
				count = 0;
				for (NameAndArity item : newRepo.predicateRepo().content(-1)) {
					Assert.assertNotNull(item);
					count++;
				}
				Assert.assertEquals(count,2);
				
				count = 0;
				for (IFProEntity item : newRepo.predicateRepo().call(pred)) {
					Assert.assertNotNull(item);
					count++;
				}
				Assert.assertEquals(count,1);
				 
				count = 0;
				for (IFProEntity item : newRepo.predicateRepo().call(op)) {
					Assert.assertNotNull(item);
					count++;
				}
				Assert.assertEquals(count,1);
		
				count = 0;
				for (IFProEntity item : newRepo.predicateRepo().call(new PredicateEntity(-1))) {
					Assert.assertNotNull(item);
					count++;
				}
				Assert.assertEquals(count,0);
			}
		}
	}	

	@Test
	public void externalPluginsRepoTest() throws Exception {
		final SubstitutableProperties		props = new SubstitutableProperties();
		final LoggerFacade		log = new DefaultLoggerFacade();
		
		props.setProperty(IFProVM.PROP_DONT_LOAD_ALL_PLUGINS, "true");
		
		final EntitiesRepo		repo = new EntitiesRepo(log,props);
		
		try(final IFProExternalPluginsRepo	xrepo = new ExternalPluginsRepo(log, props)) {
			xrepo.prepare(repo);
			
			int	count = 0;			
			for (PluginItem item : xrepo.allPlugins()) {
				count++;
			}
			Assert.assertEquals(count,1);
			
			count = 0;			
			for (PluginItem item : xrepo.seek(StandardResolver.PLUGIN_NAME,StandardResolver.PLUGIN_PRODUCER,StandardResolver.PLUGIN_VERSION)) {
				Assert.assertEquals(item.getDescriptor().getPluginDescription(),StandardResolver.PLUGIN_DESCRIPTION);
				Assert.assertEquals(item.getDescriptor().getPluginEntity().getPluginName(),StandardResolver.PLUGIN_NAME);
				Assert.assertEquals(item.getDescriptor().getPluginEntity().getPluginProducer(),StandardResolver.PLUGIN_PRODUCER);
				Assert.assertEquals(item.getDescriptor().getPluginEntity().getPluginVersion(),StandardResolver.PLUGIN_VERSION);
				count++;
			}
			Assert.assertEquals(count,1);
	
			for (RegisteredOperators item : StandardResolver.OPS) {
				final long	id = repo.termRepo().seekName(item.text);
				Assert.assertTrue(id != -1);
				Assert.assertEquals(repo.classify(id),Classification.operator);
			}
		}
	}
	
	
	@Test
	public void parsersTest() throws IOException, ContentException, PrintingException {
		final LoggerFacade			log = new DefaultLoggerFacade();
		final SubstitutableProperties		props = new SubstitutableProperties();
		
		props.setProperty(IFProVM.PROP_DONT_LOAD_ALL_PLUGINS, "true");
		
		final EntitiesRepo			repo = new EntitiesRepo(log,props);
		final ParserAndPrinter		pap = new ParserAndPrinter(log,props,repo);
		final List<IFProEntity>		data = new ArrayList<IFProEntity>(); 
		
		try(final InputStream		is = new FileInputStream("./src/test/resources/chav1961/funnypro/core/parser.fpro");
			final Reader			rdr = new InputStreamReader(is);
			final Writer			wr = new OutputStreamWriter(System.err)) {
			final CharacterSource	cs = new ReaderCharSource(rdr,false);
			final CharacterTarget	ct = new WriterCharTarget(wr,false);
			
			pap.parseEntities(cs,new FProParserCallback(){
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
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
