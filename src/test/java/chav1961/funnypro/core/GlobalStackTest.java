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
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.ExternalEntityDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.AndChainStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.StackTopType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;

public class GlobalStackTest {

	@Test
	public void basicStaticTest() throws Exception {
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
		Assert.assertEquals(GlobalStack.getBoundStackTop(entity,ch).getTopType(),StackTopType.bounds);
		Assert.assertEquals(GlobalStack.getBoundStackTop(entity,ch).getChangeChain(),ch);
		Assert.assertEquals(entity,GlobalStack.getBoundStackTop(entity,ch).getMark());
		try{GlobalStack.getBoundStackTop(null,ch);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{GlobalStack.getBoundStackTop(entity,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		
		final Iterable<String>	str = new ArrayList<>();
		Assert.assertEquals(GlobalStack.getIteratorStackTop(str,String.class).getTopType(),StackTopType.iterator);
		Assert.assertEquals(GlobalStack.getIteratorStackTop(str,String.class).getIterator(),str);
		try{GlobalStack.getIteratorStackTop(null,String.class);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (NullPointerException exc) {
		}
		try{GlobalStack.getIteratorStackTop(str,null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertEquals(StackTopType.temporary,GlobalStack.getTemporaryStackTop(entity).getTopType());
		Assert.assertEquals(GlobalStack.getTemporaryStackTop(entity).getEntity(),entity);
		try{GlobalStack.getTemporaryStackTop(null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (NullPointerException exc) {
		}

		final ExternalEntityDescriptor<?> 	desc = (ExternalEntityDescriptor<?>) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[] {ExternalEntityDescriptor.class},(a,b,c)->{return null;});
		final Object						localData = new Object();
		
		Assert.assertEquals(StackTopType.external,GlobalStack.getExternalStackTop(desc,localData).getTopType());
		Assert.assertTrue(desc == GlobalStack.getExternalStackTop(desc,localData).getDescriptor());
		Assert.assertEquals(localData,GlobalStack.getExternalStackTop(desc,localData).getLocalData());
		try{GlobalStack.getExternalStackTop(null,localData);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
	}
	
	@Test
	public void basicTest() throws Exception {
		final Properties	parm = Utils.mkProps();
		final IFProEntity	entity1 = new AnonymousEntity(), entity2 = new IntegerEntity(100);	
		
		try(final EntitiesRepo	repo = new EntitiesRepo(PureLibSettings.CURRENT_LOGGER,parm);
			final GlobalStack	stack = new GlobalStack(PureLibSettings.CURRENT_LOGGER,parm,repo)) {
		
			Assert.assertEquals(PureLibSettings.CURRENT_LOGGER,stack.getDebug());
			Assert.assertEquals(parm,stack.getParameters());
			Assert.assertEquals(repo,stack.getRepo());
			
			Assert.assertTrue(stack.isEmpty());
			Assert.assertNull(stack.peek());
			Assert.assertNull(stack.getTopType());
			
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

			try{new GlobalStack(null,parm,repo);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{new GlobalStack(PureLibSettings.CURRENT_LOGGER,null,repo);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{new GlobalStack(PureLibSettings.CURRENT_LOGGER,parm,null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
		}
	}
}
