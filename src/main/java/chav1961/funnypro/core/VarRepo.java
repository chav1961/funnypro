package chav1961.funnypro.core;

import java.util.Arrays;
import java.util.List;

import chav1961.funnypro.core.VarRepo.VariableChain;
import chav1961.funnypro.core.entities.VariableEntity;
import chav1961.funnypro.core.interfaces.IFProVariable;

class VarRepo implements AutoCloseable {
	private static final int			INITIAL_ARRAYS_SIZE = 64;
	
	private final int					initialSize;
	private final List<IFProVariable>	vars;
	VariableChain[]						varRepo = null;
	int									varCount = 0;
	
	VarRepo() {
		this(null,INITIAL_ARRAYS_SIZE);
	}
	
	VarRepo(final List<IFProVariable> vars) {
		this(vars,INITIAL_ARRAYS_SIZE);
	}

	VarRepo(final int initialSize) {
		this(null,initialSize);
	}
	
	VarRepo(final List<IFProVariable> vars, final int initialSize) {
		this.vars = vars;
		this.initialSize = initialSize;
	}
	
	@Override
	public void close() throws RuntimeException {
		if (varCount > 0) {			// Link all identical variables to ring chains
 			if (vars != null) {		// Need fill variables list. Insert new fake element into chain to get access to variable chains from abroad
				for (int index = varRepo.length-varCount, maxIndex = varRepo.length; index < maxIndex; index++) {
					varRepo[index].chain = new VariableEntity(varRepo[index].chain.getEntityId()).setChain(varRepo[index].chain);
				}
			}
			for (int index = varRepo.length-varCount, maxIndex = varRepo.length; index < maxIndex; index++) {	// Make a ring chain for all identical variables in the entity
				IFProVariable	start = varRepo[index].chain;
				
				while (start.getChain() != start) {	// The same last variable awlays has chain to self (setted inside it's constructor) 
					start = start.getChain();
				}
				start.setChain(varRepo[index].chain);
			}
			if (vars != null) {	// Extract fake elements into variable list
				for (int index = varRepo.length-varCount, maxIndex = varRepo.length; index < maxIndex; index++) {
					vars.add(varRepo[index].chain);
				}
			}
		}
	}
	
	void storeVariable(final IFProVariable entity) {
		final VariableChain		vc = new VariableChain(entity.getEntityId(),entity);
		
		if (varCount == 0) {
			varRepo = new VariableChain[initialSize];
			Arrays.fill(varRepo,new VariableChain(-Integer.MAX_VALUE));
			varRepo[initialSize-1] = vc;
			varCount++;
		}
		else {
			int				found = Arrays.binarySearch(varRepo,vc);
			
			if (found >= 0) {
				entity.setChain(varRepo[found].chain);
				varRepo[found].chain = entity;
			}
			else {
				if (varCount >= varRepo.length) {
					final VariableChain[]	newRepo = new VariableChain[2*varRepo.length];
					
					Arrays.fill(newRepo,new VariableChain(-Integer.MAX_VALUE));
					System.arraycopy(varRepo,0,newRepo,varRepo.length,varRepo.length);
					varRepo = newRepo;
					found = Arrays.binarySearch(varRepo,vc);
				}
				if (-found > varRepo.length) {
					System.arraycopy(varRepo,1,varRepo,0,-2-found);
					varRepo[-2-found] = vc;
				}
				else if (found == -1) {
					System.arraycopy(varRepo,0,varRepo,1,varRepo.length-1);
					varRepo[0] = vc;
				}
				else {
					System.arraycopy(varRepo,1,varRepo,0,-2-found);
					varRepo[-2-found] = vc;
				}
				varCount++;
			}
		}
	}	
	
	@Override
	public String toString() {
		return "VarRepo [vars=" + vars + ", varRepo=" + Arrays.toString(varRepo) + ", varCount=" + varCount + "]";
	}

	
	static class VariableChain implements Comparable<VariableChain>{
		public long				id;
		public IFProVariable	chain = null;
		
		public VariableChain(final long id) {
			this.id = id;
		}

		public VariableChain(final long id, final IFProVariable chain) {
			this.id = id;		this.chain = chain;

		}
		
		@Override
		public int compareTo(final VariableChain o) {
			return o.id < id ? 1 : (o.id > id ? -1 : 0);
		}

		@Override public String toString() {return "VariableChain [id=" + id + "]";}		
	}
}