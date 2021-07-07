package chav1961.funnypro.plugins;

import java.sql.Connection;

import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.purelib.basic.ReusableInstances;

class DatabaseProcessorGlobal {
	final ReusableInstances<DatabaseProcessorLocal>	collection = new ReusableInstances<>(()->new DatabaseProcessorLocal(),(i)->{i.callback = null; i.stack = null; i.vars = null; return i;});
	final Connection		conn;
	IFProEntitiesRepo		repo;
	IFProParserAndPrinter	pap;
	long					andId, orId, isId, inId;

	enum OperatorType {
		OpUnknown, OpAnd, OpOr, OpNot, OpCompare, OpIn, OpBetween, OpLike, OpIs
	}
	
	public DatabaseProcessorGlobal(Connection conn) {
		this.conn = conn;
	}
	
	OperatorType classify(final IFProOperator entity) {
		return OperatorType.OpAnd;
	}
}
