package chav1961.funnypro.core;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
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
			final ParserAndPrinter		pap = new ParserAndPrinter(PureLibSettings.CURRENT_LOGGER,props,repo);
			
			final StandardResolver		sr = new StandardResolver();
			
			final GlobalDescriptor		global = sr.onLoad(PureLibSettings.CURRENT_LOGGER,props,repo);

			try{sr.onLoad(PureLibSettings.CURRENT_LOGGER,props,repo);
				Assert.fail("");
			} catch (NullPointerException exc) {
			}
			try{sr.onLoad(PureLibSettings.CURRENT_LOGGER,props,repo);
			} catch (NullPointerException exc) {
			}
			try{sr.onLoad(PureLibSettings.CURRENT_LOGGER,props,repo);
			} catch (NullPointerException exc) {
			}
			
			sr.onRemove(global);
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
