package chav1961.funnypro.core;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import chav1961.funnypro.core.StandardResolver.QuickIds;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.purelib.basic.LongIdMap;
import chav1961.purelib.basic.interfaces.LoggerFacade;

class GlobalDescriptor {
	public LongIdMap<QuickIds>		registered = new LongIdMap<>(QuickIds.class);
	public Set<Long>				registeredIds = new HashSet<>();
	public IFProEntitiesRepo		repo;
	public LoggerFacade				log;
	public Properties				parameters;
	public boolean					prepared = false;
	
	@Override public String toString() {return "GlobalDescriptor [registered=" + registered + ", repo=" + repo + ", parameters=" + parameters + ", prepared=" + prepared + "]";}
}