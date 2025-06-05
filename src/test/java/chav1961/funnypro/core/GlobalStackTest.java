package chav1961.funnypro.core;


import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.ExternalEntityDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.AndChainStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.StackTopType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;

public class GlobalStackTest {

	@Test
	public void basicStaticTest() throws Exception {
		final IFProEntity		mark = new AnonymousEntity();	
		final IFProEntity		entity = new AnonymousEntity();	

		Assert.assertEquals(StackTopType.orChain,GlobalStack.getOrChainStackTop(entity,true).getTopType());
		Assert.assertTrue(GlobalStack.getOrChainStackTop(entity,true).isFirst());
		try{GlobalStack.getOrChainStackTop(null,true);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(GlobalStack.getAndChainStackTop(entity).getTopType(),StackTopType.andChain);
		Assert.assertEquals(GlobalStack.getAndChainStackTop(entity).getEntity(),entity);
		try{GlobalStack.getAndChainStackTop(null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (NullPointerException exc) {
		}
		
		final Change			ch = new Change();
		Assert.assertEquals(GlobalStack.getBoundStackTop(mark,entity,ch).getTopType(),StackTopType.bounds);
		Assert.assertEquals(GlobalStack.getBoundStackTop(mark,entity,ch).getChangeChain(),ch);
		Assert.assertEquals(entity,GlobalStack.getBoundStackTop(mark,entity,ch).getMark());
		try{GlobalStack.getBoundStackTop(mark,null,ch);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{GlobalStack.getBoundStackTop(mark,entity,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		final Iterable<String>	str = new ArrayList<>();
		Assert.assertEquals(GlobalStack.getIteratorStackTop(mark,str,String.class).getTopType(),StackTopType.iterator);
		Assert.assertEquals(GlobalStack.getIteratorStackTop(mark,str,String.class).getIterator(),str);
		try{GlobalStack.getIteratorStackTop(mark,null,String.class);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (NullPointerException exc) {
		}
		try{GlobalStack.getIteratorStackTop(mark,str,null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(StackTopType.temporary,GlobalStack.getTemporaryStackTop(mark,entity).getTopType());
		Assert.assertEquals(GlobalStack.getTemporaryStackTop(mark,entity).getEntity(),entity);
		try{GlobalStack.getTemporaryStackTop(mark,null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (NullPointerException exc) {
		}

		final ExternalEntityDescriptor<?> 	desc = (ExternalEntityDescriptor<?>) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[] {ExternalEntityDescriptor.class},(a,b,c)->{return null;});
		final Object						localData = new Object();
		
		Assert.assertEquals(StackTopType.external,GlobalStack.getExternalStackTop(mark,desc,localData).getTopType());
		Assert.assertTrue(desc == GlobalStack.getExternalStackTop(mark,desc,localData).getDescriptor());
		Assert.assertEquals(localData,GlobalStack.getExternalStackTop(mark,desc,localData).getLocalData());
		try{GlobalStack.getExternalStackTop(mark,null,localData);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Test
	public void basicTest() throws Exception {
		final SubstitutableProperties		props = new SubstitutableProperties();
		final IFProEntity	entity1 = new AnonymousEntity(), entity2 = new IntegerEntity(100);	
		
		props.setProperty(IFProVM.PROP_DONT_LOAD_ALL_PLUGINS, "true");
		
		try(final EntitiesRepo	repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,props);
			final GlobalStack	stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,props,repo)) {
		
			Assert.assertEquals(PureLibSettings.CURRENT_LOGGER,stack.getDebug());
			Assert.assertEquals(props,stack.getParameters());
			Assert.assertEquals(repo,stack.getRepo());
			
			Assert.assertTrue(stack.isEmpty());
			Assert.assertNull(stack.peek());
			Assert.assertEquals(StackTopType.unknown, stack.getTopType());
			
			try{stack.push(null);
				Assert.fail("Mandatory exception was not detected (null argument)");
			} catch (NullPointerException exc) {
			}
			
			stack.push(GlobalStack.getAndChainStackTop(entity1));
			Assert.assertFalse(stack.isEmpty());
			Assert.assertEquals(stack.peek().getTopType(),StackTopType.andChain);
			Assert.assertEquals(StackTopType.andChain,stack.getTopType());
			Assert.assertEquals(((AndChainStackTop)stack.peek()).getEntity(),entity1);

			stack.push(GlobalStack.getAndChainStackTop(entity2));
			Assert.assertFalse(stack.isEmpty());
			Assert.assertEquals(stack.peek().getTopType(),StackTopType.andChain);
			Assert.assertEquals(((AndChainStackTop)stack.peek()).getEntity(),entity2);
			
			AndChainStackTop	top = (AndChainStackTop) stack.pop();
			Assert.assertFalse(stack.isEmpty());
			Assert.assertEquals(top.getTopType(),StackTopType.andChain);
			Assert.assertEquals(top.getEntity(),entity2);
			
			top = (AndChainStackTop) stack.pop();
			Assert.assertTrue(stack.isEmpty());
			Assert.assertEquals(top.getTopType(),StackTopType.andChain);
			Assert.assertEquals(top.getEntity(),entity1);

			try {stack.pop();
				Assert.fail("Mandatory exception was not detected (stack exhausted)");
			} catch (IllegalStateException exc) {
			}
			
			stack.push(GlobalStack.getAndChainStackTop(entity1));
			stack.push(GlobalStack.getAndChainStackTop(entity2));
			Assert.assertFalse(stack.isEmpty());
			stack.clear();
			Assert.assertTrue(stack.isEmpty()); 

			try{new GlobalStack(null,props,repo);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{new GlobalStack(PureLibSettings.CURRENT_LOGGER,null,repo);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{new GlobalStack(PureLibSettings.CURRENT_LOGGER,props,null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
		}
	}
}
