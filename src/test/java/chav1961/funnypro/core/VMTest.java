package chav1961.funnypro.core;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.DefaultLoggerFacade;
import chav1961.purelib.basic.DottedVersion;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.streams.charsource.StringCharSource;
import chav1961.purelib.streams.chartarget.StringBuilderCharTarget;


public class VMTest {
	private static final IFProCallback	EMPTY = new IFProCallback() {
													@Override public void beforeFirstCall() {}
													@Override public boolean onResolution(String[] names, IFProEntity[] resolvedValues, String[] printedValues) throws SyntaxException, PrintingException {return false;}
													@Override public void afterLastCall() {}
												};
	
	@Test
	public void lifeCycleTest() throws Exception {
		final SubstitutableProperties	props = new SubstitutableProperties();
		final LoggerFacade		log = new DefaultLoggerFacade();
		
		props.setProperty(IFProVM.PROP_DONT_LOAD_ALL_PLUGINS, "true");
		
		try(final IFProVM		vm = new PseudoFProVM(log,props)) {
			
			Assert.assertFalse(vm.isTurnedOn());
			try{vm.newFRB(null);
				Assert.fail("Mandatory exception was not detected (null parameters)");
			} catch (IllegalArgumentException exc) {
			}
			
			final ByteArrayOutputStream		baos = new ByteArrayOutputStream();
			
			vm.newFRB(baos);
			
			vm.turnOn(null);
			Assert.assertTrue(vm.isTurnedOn());
			try{vm.turnOn(null);
				Assert.fail("Mandatory exception was not detected (duplicate turn on)");
			} catch (IllegalStateException exc) {
			}
			try{vm.newFRB(new ByteArrayOutputStream());
				Assert.fail("Mandatory exception was not detected (create database when VM is turned on)");
			} catch (IllegalStateException exc) {
			}
			
			vm.turnOff(null);
			Assert.assertFalse(vm.isTurnedOn());
			try{vm.turnOff(null);
				Assert.fail("Mandatory exception was not detected (duplicate turn off)");
			} catch (IllegalStateException exc) {
			}

			vm.turnOn(new ByteArrayInputStream(baos.toByteArray()));
			
			try{vm.consult(null);
				Assert.fail("Mandatory exception was not detected (null parametetrs)");
			} catch (IllegalArgumentException exc) {
			}
			try{vm.save(null);
				Assert.fail("Mandatory exception was not detected (null parametetrs)");
			} catch (IllegalArgumentException exc) {
			}
			vm.consult(new StringCharSource("predicate(100)."));
			
			final StringBuilder	save = new StringBuilder();
			
			vm.save(new StringBuilderCharTarget(save));
			Assert.assertEquals(save.toString(),"predicate(100) .\n");
			
			Assert.assertTrue(vm.question("?-predicate(X)",EMPTY));
			try{vm.question(null,EMPTY);
				Assert.fail("Mandatory exception was not detected (null parametetrs)");
			} catch (IllegalArgumentException exc) {
			}
			try{vm.question("?-predicate(X)",null);
				Assert.fail("Mandatory exception was not detected (null parametetrs)");
			} catch (IllegalArgumentException exc) {
			}
			try{vm.question(":-predicate(X)",EMPTY);
				Assert.fail("Mandatory exception was not detected (string is not a question)");
			} catch (SyntaxException exc) {
			}

			Assert.assertTrue(vm.goal(":-predicate(X)",EMPTY));
			try{vm.goal(null,EMPTY);
				Assert.fail("Mandatory exception was not detected (null parametetrs)");
			} catch (IllegalArgumentException exc) {
			}
			try{vm.goal("?-predicate(X)",null);
				Assert.fail("Mandatory exception was not detected (null parametetrs)");
			} catch (IllegalArgumentException exc) {
			}
			try{vm.goal("?-predicate(X)",EMPTY);
				Assert.fail("Mandatory exception was not detected (string is not a goal)");
			} catch (SyntaxException exc) {
			}
		}
	}
}


class PseudoFProVM extends FProVM {

	public PseudoFProVM(LoggerFacade log, SubstitutableProperties prop) {
		super(log, prop);
	}

	protected ResolvableAndGlobal<GlobalDescriptor,LocalDescriptor> getStandardResolver() {
		return new ResolvableAndGlobal(
			new IResolvable<GlobalDescriptor,LocalDescriptor>() {
			@Override public ResolveRC nextResolve(GlobalDescriptor global, LocalDescriptor local, IFProEntity values) throws SyntaxException {return ResolveRC.False;}
			@Override public String getName() {return "test";}
			@Override public DottedVersion getVersion() {return DottedVersion.ZERO;}
			@Override public LocalDescriptor beforeCall(GlobalDescriptor global, IFProGlobalStack gs, List<IFProVariable> vars,IFProCallback callback) {return null;}
			@Override public ResolveRC firstResolve(GlobalDescriptor global, LocalDescriptor local, IFProEntity values) throws SyntaxException {return ResolveRC.True;}
			@Override public void endResolve(GlobalDescriptor global, LocalDescriptor local, IFProEntity values) throws SyntaxException {}			
			@Override public void afterCall(GlobalDescriptor global, LocalDescriptor local) {}
			@Override public void onRemove(GlobalDescriptor global) {}
			@Override public GlobalDescriptor onLoad(LoggerFacade debug, SubstitutableProperties parameters, IFProEntitiesRepo repo) throws SyntaxException {return null;}
		},
		null);
	}
}