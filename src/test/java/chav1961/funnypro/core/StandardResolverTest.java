package chav1961.funnypro.core;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
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
import chav1961.purelib.basic.interfaces.LoggerFacade;

public class StandardResolverTest {

	@Test
	public void goalSetTest() throws Exception {
		final LoggerFacade				log = new DefaultLoggerFacade();
		
		try(final EntitiesRepo			repo = new EntitiesRepo(log,new Properties())){
			final IFProGlobalStack		stack = new GlobalStack(log,new Properties(),repo);
			final ParserAndPrinter		pap = new ParserAndPrinter(log,new Properties(),repo);
			final StandardResolver		sr = new StandardResolver();
			final Object				global = sr.onLoad(log,new Properties(),repo);
			
			IFProEntity					entity;

			try(final InputStream		is = new FileInputStream("./src/test/resources/environment.fpro");
				final Reader			rdr = new InputStreamReader(is);
				final Writer			wr = new OutputStreamWriter(new OutputStream(){public void write(int data){}})) {	// Prepare environment

				pap.parseEntities(rdr,new FProParserCallback(){
											@Override
											public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
												wr.write("Consult");
												pap.putEntity(entity,wr);
												wr.write("\n");
												repo.predicateRepo().assertZ(entity);
												return true;
											}
										}
				);
			}
			
			try(final InputStream		is = new FileInputStream("./src/test/resources/trues.fpro");
				final Reader			rdr = new InputStreamReader(is);
				final Writer			wr = new OutputStreamWriter(new OutputStream(){public void write(int data){}})) {	// Test true goals

				pap.parseEntities(rdr,new FProParserCallback(){
											@Override
											public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
												wr.write("Process true goal: ");
												pap.putEntity(entity,wr);
												wr.write("\n");
												processing(sr,global,stack,entity,vars,true);
												vars.clear();
												return true;
											}
										}
				);
			}
			
			try(final InputStream		is = new FileInputStream("./src/test/resources/falses.fpro");
					final Reader			rdr = new InputStreamReader(is);
					final Writer			wr = new OutputStreamWriter(new OutputStream(){public void write(int data){}})) {	// test false goals

					pap.parseEntities(rdr,new FProParserCallback(){
												@Override
												public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProException, IOException {
													wr.write("Process true goal: ");
													pap.putEntity(entity,wr);
													wr.write("\n");
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
	
	private void processing(final StandardResolver sr, final Object global, final IFProGlobalStack stack, final IFProEntity goal, final List<IFProVariable> vars, final boolean awaitedResult) throws FProException {
		final IFProCallback	callback = new IFProCallback(){
								@Override public void beforeFirstCall() {}
								@Override
								public boolean onResolution(final Map<String, Object> resolvedVariables) throws FProParsingException, FProPrintingException {
									System.err.println("Call: "+resolvedVariables);
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
