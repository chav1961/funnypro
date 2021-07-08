package chav1961.funnypro.plugins;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import chav1961.funnypro.core.QuickIds;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.plugins.DatabaseProcessorPlugin.RegisteredEntities;
import chav1961.purelib.basic.LongIdMap;
import chav1961.purelib.basic.ReusableInstances;

class DatabaseProcessorGlobal {
	final ReusableInstances<DatabaseProcessorLocal>	collection = new ReusableInstances<>(()->new DatabaseProcessorLocal(),(i)->{i.callback = null; i.stack = null; i.vars = null; return i;});
	final Connection			conn;
	final LongIdMap<QuickIds<RegisteredEntities>>	registered = new LongIdMap(QuickIds.class);
	final Set<Long>				registeredIds = new HashSet<>();
	IFProEntitiesRepo			repo;
	IFProParserAndPrinter		pap;
	long						andId, orId, isId, inId;

	enum OperatorType {
		OpUnknown, OpAnd, OpOr, OpNot, OpIn, OpBetween, OpLike, OpIs,
		OpEq, OpNe, OpLt, OpLe, OpGt, OpGe
	}
	
	public DatabaseProcessorGlobal(final Connection conn) {
		this.conn = conn;
	}
}
