package chav1961.funnypro.core;


import java.io.File;
import java.io.IOException;
import java.util.List;
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
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.streams.charsource.ArrayCharSource;
import chav1961.purelib.streams.interfaces.CharacterSource;

public class StandardResolverTest {

	@Test
	public void goalSetTest() throws Exception {
		final LoggerFacade				log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo			repo = new EntitiesRepo(log,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(log,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(log,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final GlobalDescriptor		global = sr.onLoad(log,new Properties(),repo);
			CharacterSource				cs;
			
			cs = new ArrayCharSource(URIUtils.loadCharsFromURI(new File("./src/test/resources/chav1961/funnypro/core/environment.fpro").toURI()));

			pap.parseEntities(cs,new FProParserCallback(){
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
											repo.predicateRepo().assertZ(entity);
											return true;
										}
									}
			);
			
			cs = new ArrayCharSource(URIUtils.loadCharsFromURI(new File("./src/test/resources/chav1961/funnypro/core/trues.fpro").toURI()));

			pap.parseEntities(cs,new FProParserCallback(){
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
											processing(sr,global,stack,entity,vars,true);
											vars.clear();
											return true;
										}
									}
			);
			
			cs = new ArrayCharSource(URIUtils.loadCharsFromURI(new File("./src/test/resources/chav1961/funnypro/core/falses.fpro").toURI()));

			pap.parseEntities(cs,new FProParserCallback(){
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
											processing(sr,global,stack,entity,vars,false);
											vars.clear();
											return true;
										}
									}
			);
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
			final GlobalDescriptor		global = sr.onLoad(log,new Properties(),repo);
			final CharacterSource		cs = new ArrayCharSource(URIUtils.loadCharsFromURI(new File("./src/test/resources/chav1961/funnypro/core/environment.fpro").toURI()));

			pap.parseEntities(cs,new FProParserCallback(){	// Prepare environment
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
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
												public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
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
	
	private void processing(final StandardResolver sr, final GlobalDescriptor global, final IFProGlobalStack stack, final IFProEntity goal, final List<IFProVariable> vars, final boolean awaitedResult) throws FProException {
		final IFProCallback	callback = new IFProCallback(){
								@Override public void beforeFirstCall() {}
								@Override
								public boolean onResolution(final String[] names, final Object[] resolvedVariables) throws FProParsingException, FProPrintingException {
//									System.err.println("Call: "+resolvedVariables);
									return true;
								}
								@Override public void afterLastCall() {}
							}; 
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
