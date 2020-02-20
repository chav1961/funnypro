package chav1961.funnypro.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.entities.VariableEntity;
import chav1961.funnypro.core.interfaces.IFProVariable;

public class VarRepoTest {
	@Test
	public void basicTest() {
		final VarRepo	repo = new VarRepo(4);
		
		Assert.assertEquals(0,repo.varCount);
		
		repo.storeVariable(new VariableEntity(300));
		repo.storeVariable(new VariableEntity(400));
		repo.storeVariable(new VariableEntity(500));
		repo.storeVariable(new VariableEntity(200));
		repo.storeVariable(new VariableEntity(100));
		repo.storeVariable(new VariableEntity(700));
		repo.storeVariable(new VariableEntity(600));
		repo.storeVariable(new VariableEntity(800));
		Assert.assertEquals(8,repo.varCount);
		Assert.assertEquals(8,repo.varRepo.length);
		Assert.assertEquals(100,repo.varRepo[0].id);
		Assert.assertEquals(800,repo.varRepo[7].id);
		
		repo.storeVariable(new VariableEntity(100));
		Assert.assertEquals(8,repo.varCount);
		
		repo.close();
		Assert.assertEquals(repo.varRepo[0].chain.getChain(),repo.varRepo[0].chain.getChain().getChain());	// 2 elements in the chain
		Assert.assertEquals(repo.varRepo[7].chain.getChain(),repo.varRepo[7].chain.getChain());				// 1 element in the chain
	}

	@Test
	public void fillingTest() {
		final List<IFProVariable>	list = new ArrayList<>();
		final VarRepo				repo = new VarRepo(list,4);
		
		Assert.assertEquals(0,repo.varCount);
		
		repo.storeVariable(new VariableEntity(300));
		repo.storeVariable(new VariableEntity(400));
		repo.storeVariable(new VariableEntity(500));
		repo.storeVariable(new VariableEntity(200));
		repo.storeVariable(new VariableEntity(100));
		repo.storeVariable(new VariableEntity(700));
		repo.storeVariable(new VariableEntity(600));
		repo.storeVariable(new VariableEntity(800));
		Assert.assertEquals(8,repo.varCount);
		Assert.assertEquals(8,repo.varRepo.length);
		Assert.assertEquals(100,repo.varRepo[0].id);
		Assert.assertEquals(800,repo.varRepo[7].id);
		
		repo.storeVariable(new VariableEntity(100));
		Assert.assertEquals(8,repo.varCount);
		
		repo.close();
		Assert.assertEquals(repo.varRepo[0].chain.getChain(),repo.varRepo[0].chain.getChain().getChain().getChain());	// 2 elements in the chain
		Assert.assertEquals(repo.varRepo[7].chain.getChain(),repo.varRepo[7].chain.getChain().getChain());				// 3 element in the chain
		
		Assert.assertEquals(8,list.size());
	}
}
