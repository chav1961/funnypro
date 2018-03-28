package chav1961.funnypro.core;

import java.util.Arrays;
import java.util.Properties;

import chav1961.funnypro.core.StandardResolver.QuickIds;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.purelib.basic.interfaces.LoggerFacade;

class GlobalDescriptor {
	public QuickIds[]				registered;
	public IFProEntitiesRepo		repo;
	public LoggerFacade				log;
	public Properties				parameters;
	public boolean					prepared = false;
	
	@Override public String toString() {return "GlobalDescriptor [registered=" + Arrays.toString(registered) + ", repo=" + repo + ", parameters=" + parameters + ", prepared=" + prepared + "]";}
}