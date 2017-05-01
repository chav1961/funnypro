package chav1961.funnypro.core;


import java.util.ArrayList;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.AndChainStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.StackTopType;
import chav1961.purelib.basic.DefaultLoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade;

public class GlobalStackTest {

	@Test
	public void basicStaticTest() throws Exception {
		final IFProEntity		entity = new AnonymousEntity();	
		
		Assert.assertEquals(GlobalStack.getAndChainStackTop(entity).getTopType(),StackTopType.andChain);
		Assert.assertEquals(GlobalStack.getAndChainStackTop(entity).getEntity(),entity);
		try{GlobalStack.getAndChainStackTop(null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		final Change			ch = new Change();
		Assert.assertEquals(GlobalStack.getBoundStackTop(entity,ch).getTopType(),StackTopType.bounds);
		Assert.assertEquals(GlobalStack.getBoundStackTop(entity,ch).getChangeChain(),ch);
		try{GlobalStack.getBoundStackTop(null,ch);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{GlobalStack.getBoundStackTop(entity,null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		final Iterable<String>	str = new ArrayList<>();
		Assert.assertEquals(GlobalStack.getIteratorStackTop(str,String.class).getTopType(),StackTopType.iterator);
		Assert.assertEquals(GlobalStack.getIteratorStackTop(str,String.class).getIterator(),str);
		try{GlobalStack.getIteratorStackTop(null,String.class);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{GlobalStack.getIteratorStackTop(str,null);
			Assert.fail("Mandatory exception was not detected (null argument)");
		} catch (IllegalArgumentException exc) {
		}
	}
	
	@Test
	public void basicTest() throws Exception {
		final LoggerFacade			log = new DefaultLoggerFacade();
		final IFProEntity			entity1 = new AnonymousEntity(), entity2 = new IntegerEntity(100);	
		
		try(final EntitiesRepo		repo = new EntitiesRepo(log,new Properties());
			final IFProGlobalStack	stack = new GlobalStack(log,new Properties(),repo)) {
		
			Assert.assertTrue(stack.isEmpty());
			
			try{stack.push(null);
				Assert.fail("Mandatory exception was not detected (null argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			stack.push(GlobalStack.getAndChainStackTop(entity1));
			Assert.assertFalse(stack.isEmpty());
			Assert.assertEquals(stack.peek().getTopType(),StackTopType.andChain);
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
			
			stack.push(GlobalStack.getAndChainStackTop(entity1));
			stack.push(GlobalStack.getAndChainStackTop(entity2));
			Assert.assertFalse(stack.isEmpty());
			stack.clear();
			Assert.assertTrue(stack.isEmpty());
		}
	}
}
