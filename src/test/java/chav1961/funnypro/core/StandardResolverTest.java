package chav1961.funnypro.core;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable.ResolveRC;
import chav1961.purelib.basic.DefaultLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.streams.charsource.ArrayCharSource;
import chav1961.purelib.streams.charsource.ReaderCharSource;
import chav1961.purelib.streams.chartarget.WriterCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

public class StandardResolverTest {

	@Test
	public void goalSetTest() throws Exception {
		final LoggerFacade				log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo			repo = new EntitiesRepo(log,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(log,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(log,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final Object				global = sr.onLoad(log,new Properties(),repo);
			
			try(final InputStream		is = new FileInputStream("./src/test/resources/chav1961/funnypro/core/environment.fpro");
				final Reader			rdr = new InputStreamReader(is);) {	// Prepare environment
				final CharacterSource	cs = new ReaderCharSource(rdr,false);
				final CharacterTarget	ct = new WriterCharTarget(System.err,false);

				pap.parseEntities(cs,new FProParserCallback(){
											@Override
											public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
												try{ct.put("Consult");
													pap.putEntity(entity,ct);
													ct.put("\n");
												} catch (PrintingException e) {
													e.printStackTrace();
													throw new IOException(e.getMessage());
												}
												repo.predicateRepo().assertZ(entity);
												return true;
											}
										}
				);
			}
			
			try(final InputStream		is = new FileInputStream("./src/test/resources/chav1961/funnypro/core/trues.fpro");
				final Reader			rdr = new InputStreamReader(is);) {	// Test true goals
				final CharacterSource	cs = new ReaderCharSource(rdr,false);
				final CharacterTarget	ct = new WriterCharTarget(System.err,false);

				pap.parseEntities(cs,new FProParserCallback(){
											@Override
											public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
												try{ct.put("Process true goal: ");
													pap.putEntity(entity,ct);
													ct.put("\n");
												} catch (PrintingException e) {
													e.printStackTrace();
													throw new IOException(e.getMessage());
												}
												processing(sr,global,stack,entity,vars,true);
												vars.clear();
												return true;
											}
										}
				);
			}
			
			try(final InputStream		is = new FileInputStream("./src/test/resources/chav1961/funnypro/core/falses.fpro");
				final Reader			rdr = new InputStreamReader(is);) {	// test false goals
				final CharacterSource	cs = new ReaderCharSource(rdr,false);
				final CharacterTarget	ct = new WriterCharTarget(System.err,false);

				pap.parseEntities(cs,new FProParserCallback(){
											@Override
											public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
												try{ct.put("Process true goal: ");
													pap.putEntity(entity,ct);
													ct.put("\n");
												} catch (PrintingException e) {
													e.printStackTrace();
													throw new IOException(e.getMessage());
												}
												processing(sr,global,stack,entity,vars,false);
												vars.clear();
												return true;
											}
										}
				);
			}
			sr.onRemove(global);
		}
	}

	@Test
	public void performanceTest() throws Exception {
		final LoggerFacade				log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo			repo = new EntitiesRepo(log,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(log,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(log,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final Object				global = sr.onLoad(log,new Properties(),repo);
			
			try(final InputStream		is = new FileInputStream("./src/test/resources/chav1961/funnypro/core/environment.fpro");
				final Reader			rdr = new InputStreamReader(is);) {	// Prepare environment
				final CharacterSource	cs = new ReaderCharSource(rdr,false);
				final CharacterTarget	ct = new WriterCharTarget(System.err,false);

				pap.parseEntities(cs,new FProParserCallback(){
											@Override
											public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
												try{ct.put("Consult");
													pap.putEntity(entity,ct);
													ct.put("\n");
												} catch (PrintingException e) {
													e.printStackTrace();
													throw new IOException(e.getMessage());
												}
												repo.predicateRepo().assertZ(entity);
												return true;
											}
										}
				);
			}

			final char[]	content;
			
			try(final InputStream		is = new FileInputStream("./src/test/resources/chav1961/funnypro/core/trues.fpro");
				final Reader			rdr = new InputStreamReader(is);
				final Writer			wr = new StringWriter()) {
				
				Utils.copyStream(rdr,wr);
				content = wr.toString().toCharArray();
			}
			
			final long	start = System.currentTimeMillis();
			System.err.println("Starting...");
			
			for (int index = 0; index < 1000000;  index++) {
				try{final CharacterSource	cs = new ArrayCharSource(content);
					final CharacterTarget	ct = new WriterCharTarget(System.err,false);
	
					pap.parseEntities(cs,new FProParserCallback(){
												@Override
												public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
//													try{ct.put("Process true goal: ");
//														pap.putEntity(entity,ct);
//														ct.put("\n");
//													} catch (PrintingException e) {
//														e.printStackTrace();
//														throw new IOException(e.getMessage());
//													}
													processing(sr,global,stack,entity,vars,true);
													vars.clear();
													return true;
												}
											}
					);
				} finally{
				}
			}
			System.err.println("Duration="+(System.currentTimeMillis()-start));
			
			sr.onRemove(global);
		}
	}
	
	
	private void processing(final StandardResolver sr, final Object global, final IFProGlobalStack stack, final IFProEntity goal, final List<IFProVariable> vars, final boolean awaitedResult) throws FProException {
		final IFProCallback	callback = new IFProCallback(){
								@Override public void beforeFirstCall() {}
								@Override
								public boolean onResolution(final Map<String, Object> resolvedVariables) throws FProParsingException, FProPrintingException {
//									System.err.println("Call: "+resolvedVariables);
									return true;
								}
								@Override public void afterLastCall() {}
							}; 
		Assert.assertTrue(stack.isEmpty());

		final Object		local = sr.beforeCall(global,stack,vars,callback);
		
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
