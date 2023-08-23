package chav1961.funnypro.core;

import java.util.HashSet;
import java.util.Set;

import chav1961.funnypro.core.StandardResolver.RegisteredEntities;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.purelib.basic.LongIdMap;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.interfaces.LoggerFacade;

public class GlobalDescriptor {
	public LongIdMap<QuickIds<RegisteredEntities>>	registered = new LongIdMap(QuickIds.class);
	public Set<Long>				registeredIds = new HashSet<>();
	public IFProEntitiesRepo		repo;
	public LoggerFacade				log;
	public SubstitutableProperties	parameters;
	public ParserAndPrinter			pap;
	public boolean					prepared = false;
	public boolean					trace = false;
	
	@Override public String toString() {return "GlobalDescriptor [registered=" + registered + ", repo=" + repo + ", parameters=" + parameters + ", prepared=" + prepared  + ", trace=" + trace + "]";}
}