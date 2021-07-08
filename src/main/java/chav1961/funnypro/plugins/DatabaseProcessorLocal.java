package chav1961.funnypro.plugins;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;

class DatabaseProcessorLocal {
	final Change[]			list = new Change[1];
	IFProCallback			callback;
	IFProGlobalStack		stack;
	List<IFProVariable>		vars;
	Statement				stmt;
	ResultSet				rs;
	ResultSetMetaData		rsmd;
}
