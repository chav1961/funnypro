package chav1961.funnypro.core.interfaces;

import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This interface supports entities repo functionality</p>
 * @author chav1961
 */
public interface IFProEntitiesRepo extends IStreamSerializable, AutoCloseable {
	public interface SerializableString extends IStreamSerializable, IFProStringSerializable {
	}

	public enum Classification {
		operator, term, anonymous, variable, op, extern
	}

	/**
	 * <p>Get string repository for the system</p>
	 * @return string repository
	 */
	SyntaxTreeInterface<SerializableString> stringRepo();
 
	/**
	 * <p>Get term repository for the system</p>
	 * @return term repository
	 */
	SyntaxTreeInterface<SerializableString> termRepo();
	
	/**
	 * <p>Get fact/rule base for the system</p>
	 * @return fact/rule base for the system
	 */
	IFProRepo predicateRepo();
	
	/**
	 * <p>Get external plugin repository for the system</p> 
	 * @return external plugin repository
	 */
	IFProExternalPluginsRepo pluginsRepo();

	/**
	 * <p>Classify the given entity id</p>
	 * @param id entity id to classify</p>
	 * @return
	 */
	Classification classify(long id);
	
	/**
	 * <p>CLassify id as operator type</p>
	 * @param id id to classify
	 * @return operator type or null if classification failed 
	 */
	OperatorType operatorType(long id);
	
	/**
	 * <p.Get operator definitions under the given condition. Returned set will be ordered by priority:</p>
	 * <ul>
	 * <li>from min to max value, if minPrty < maxPrty</li>
	 * <li>from max to min value, if minPrty > maxPrty</li>
	 * </ul> 
	 * @param id operator id to get definitions
	 * @param minPrty min available operator priority. See remarks above 
	 * @param maxPrty max available operator priority. See remarks above
	 * @param types operator types to select.
	 * @return operator declarations t\for the given condition
	 */
	IFProOperator[] getOperatorDef(long id, int minPrty, int maxPrty, OperatorType... types);
	
	/**
	 * <p>Place operator definition in the repo</p>
	 * @param op operator definition to place
	 */
	void putOperatorDef(IFProOperator op);
	
	/**
	 * <p>Get priorities of all operators in the system.
	 * @return operator priorities ordered from MIN to MAX;
	 */
	int[] getOperatorPriorities();
	
	/**
	 * <p>Get all operators registered
	 * @return all operators registered. Can be empty but not null
	 */
	Iterable<IFProOperator> registeredOperators();
	
	/**
	 * <p>Consult data to the repo</p>
	 * @param source source to consult data from
	 * @throws FProParsingException
	 */
	void consult(CharacterSource source) throws FProParsingException;
	
	/**
	 * <p>Consult data to the repo</p>
	 * @param source source to consult data from
	 * @param from start position to consult data
	 * @return end position after consulting data
	 * @throws FProParsingException
	 */
	int consult(char[] source, int from) throws FProParsingException;
	
	/**
	 * <p>Save repo to the given target</p> 
	 * @param target target to save repo to
	 * @throws FProPrintingException
	 */
	void save(CharacterTarget target) throws FProPrintingException;

	/**
	 * <p>Save repo to the given target</p> 
	 * @param target target to save repo to
	 * @param from start position to save repo
	 * @return end position after repo saving
	 * @throws FProPrintingException
	 */
	int save(char[] target, int from) throws FProPrintingException;
}
