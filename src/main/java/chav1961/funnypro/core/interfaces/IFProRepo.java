package chav1961.funnypro.core.interfaces;

import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;

/**
 * <p>This interface describes FPro repository.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProRepo {
	/**
	 * <p>This interface describes all the entity types in the fact/rule base</p>
	 * @author chav1961
	 */
	public interface NameAndArity {
		/**
		 * <p>Get entity type</p>
		 * @return entity type
		 */
		EntityType getType();
		
		/**
		 * <p>Get entity id</p>
		 * @return entity id
		 */
		long getId();
		
		/**
		 * <p>Get entity arity</p>
		 * @return entity arity
		 */
		int getArity();
	}
	
	/**
	 * <p>Can use this repository in multiThread environment</p> 
	 * @return true if yes. When true, all methods need to be thread-safe!
	 */
	boolean canUseInMultiThread();

	/**
	 * <p>Can use this repository for muitlThread resolution. When canUseInMultiThread returns false, need return false too!</p>
	 * @return true if yes. When true, using of retractAll and call with 'module' parameter are available, else their call need throws UnsupportedOperationException
	 */
	boolean canUseForMultiThreadOnResolution();
	
	/**
	 * <p>Assert predicate to the beginning of the chain. This method id thread-safe</p>
	 * @param value value to assert
	 */
	void assertA(IFProEntity value);
	
	/**
	 * <p>Assert predicate to the end of the chain. This method id thread-safe</p>
	 * @param value value to assert
	 */
	void assertZ(IFProEntity value);

	/**
	 * <p>Retract all predicates matched with this predicate. This method is thread-safe</p>
	 * @param value template to match
	 */
	void retractAll(IFProEntity value);

	/**
	 * <p>Retract all predicates matched with this predicate. This method is not thread-safe and need to be special management</p>
	 * @param value template to match
	 * @param module thread seq to distribute resource processing to some threads
	 */
	void retractAll(IFProEntity value, int module);

	/**
	 * <p>Retract all predicates matched with this predicate. This method is thread-safe</p>
	 * @param id predicate id to remove
	 * @param arity predicate arity to remove
	 */
	void retractAll(long id, int arity);
	
	/**
	 * <p>Retract first entity in the entities chain</p> 
	 * @param value entity to retract
	 * @return true if retraction was successful
	 */
	boolean retractFirst(IFProEntity value);
	
	/**
	 * <p>Select all predicates matched with this predicate</p>
	 * @param value template to match
	 * @return list of matched predicates. Can be empty but not null. If some predicates will be retracted from the repo, list will skip it on the next(). 
	 * If some predicated will be asserted into the repo, they will not be appeared here before ending iterative process 
	 */
	Iterable<IFProEntity> call(IFProEntity value);

	/**
	 * <p>Select all predicates matched with this predicate</p>
	 * @param value template to match
	 * @param module thread seq to distribute resource processing to some threads
	 * @return list of matched predicates. Can be empty but not null. If some predicates will be retracted from the repo, list will skip it on the next(). 
	 * If some predicated will be asserted into the repo, they will not be appeared here before ending iterative process 
	 */
	Iterable<IFProEntity> call(IFProEntity value, int module);
	
	/**
	 * <p>Select content of the database types</p>
	 * @param arity arity to select. Type -1 to select all arities
	 * @param ids ids to select. Don't type any values to select all types
	 * @return names and arities for database content. Can be empty but not null
	 */
	Iterable<NameAndArity> content(int arity, long... ids);
}
