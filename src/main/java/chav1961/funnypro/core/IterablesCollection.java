package chav1961.funnypro.core;

import java.util.Iterator;

import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.FProUtil.ContentType;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProRepo.NameAndArity;

class IterablesCollection {
	private static final Iterator<NameAndArity>	NULL_NA_ITERATOR = new Iterator<NameAndArity>() {
													@Override public boolean hasNext() {return false;}
													@Override public NameAndArity next() {return null;}
													
												};
	private static final Iterator<IFProEntity>	NULL_ENTITY_ITERATOR = new Iterator<IFProEntity>() {
													@Override public boolean hasNext() {return false;}
													@Override public IFProEntity next() {return null;}
												};
	
	public static class IterableNameAndArity implements Iterable<IFProEntity> {
		private final Iterator<NameAndArity>		iterator;
		private final IFProOperator					def;
		
		public IterableNameAndArity(final IFProEntitiesRepo repo, final IFProOperator nameAndArity) {
			if (repo == null) {
				throw new NullPointerException("Repo can't be null"); 
			}
			else if (nameAndArity == null) { 
				throw new NullPointerException("Name and arity can't be null"); 
			}
			else if (nameAndArity.getLeft() == null || nameAndArity.getRight() == null) {
				iterator = NULL_NA_ITERATOR; 
			}
			else if (!FProUtil.isEntityA(nameAndArity.getLeft(),ContentType.NonVar)) {
				if (!FProUtil.isEntityA(nameAndArity.getRight(),ContentType.NonVar)) {
					iterator = repo.predicateRepo().content(-1).iterator();
				}
				else if (nameAndArity.getRight().getEntityType() == EntityType.integer && nameAndArity.getRight().getEntityId() >= IFProPredicate.MIN_ARITY && nameAndArity.getRight().getEntityId() <= IFProPredicate.MAX_ARITY) {
					iterator = repo.predicateRepo().content((int)nameAndArity.getRight().getEntityId()).iterator();
				}
				else {
					iterator = NULL_NA_ITERATOR;
				}
			}
			else if (nameAndArity.getLeft().getEntityType() == EntityType.predicate) {
				if (!FProUtil.isEntityA(nameAndArity.getRight(),ContentType.NonVar)) {
					iterator = repo.predicateRepo().content(-1,nameAndArity.getLeft().getEntityId()).iterator();
				}
				else if (nameAndArity.getRight().getEntityType() == EntityType.integer && nameAndArity.getRight().getEntityId() >= IFProPredicate.MIN_ARITY && nameAndArity.getRight().getEntityId() <= IFProPredicate.MAX_ARITY) {
					iterator = repo.predicateRepo().content((int)nameAndArity.getRight().getEntityId(),nameAndArity.getLeft().getEntityId()).iterator();
				}
				else {
					iterator = NULL_NA_ITERATOR;
				}
			}
			else {
				iterator = NULL_NA_ITERATOR;
			}
			this.def = nameAndArity;
		}		
		
		@Override 
		public Iterator<IFProEntity> iterator() {return new Iterator<IFProEntity>(){
				@Override public boolean hasNext() {return iterator.hasNext();}
	
				@Override
				public IFProEntity next() {
					final NameAndArity	naa = iterator.next();
					
					return new OperatorEntity(def.getPriority(),def.getOperatorType(),def.getEntityId()).setLeft(new PredicateEntity(naa.getId())).setRight(new IntegerEntity(naa.getArity()));
				}
			};
		}		
	}
	
	public static class IterableCall implements Iterable<IFProEntity> {
		private final Iterator<IFProEntity>		iterator, callIterator;
		private final IFProEntity				template;
		
		public IterableCall(final IFProEntitiesRepo repo, final IFProPredicate call) {	// call(pred(...)) or call(op(...)) only
			if (repo == null) {
				throw new NullPointerException("Repo can't be null"); 
			}
			else if (call == null) {
				throw new NullPointerException("Name and arity can't be null"); 
			}
			else if (call.getArity() != 1 || !FProUtil.isEntityA(call.getParameters()[0],ContentType.NonVar)) {
				this.iterator = NULL_ENTITY_ITERATOR;
			}
			else {
				this.iterator = repo.predicateRepo().call(call.getParameters()[0]).iterator();
			}
			this.template = call.getArity() == 1 ? call.getParameters()[0] : null;	// Important order - don't change this string with the next one 
			
			this.callIterator = new Iterator<IFProEntity>(){
				final Change[]	temp = new Change[1];
				IFProEntity		item;
			
				@Override 
				public boolean hasNext() {
					if (iterator.hasNext()) {
						
						do{ item = iterator.next();
							if (FProUtil.unify(template,item,temp)) {
								FProUtil.unbind(temp[0]);
								return true;
							}
							else {
								FProUtil.unbind(temp[0]);
							}
						} while (iterator.hasNext());
						
						return false;
					}
					else {
						return false;
					}
				}
				
				@Override 
				public IFProEntity next() {
					return item;
				}
			};
		}		
		
		@Override
		public Iterator<IFProEntity> iterator() {
			return callIterator;
		}		
	}
	
	public static class IterableCallBagof implements Iterable<IFProEntity> {
		private final Iterator<IFProEntity>		iterator, bagofIterator;
		private final IFProEntity				template;
		
		public IterableCallBagof(final IFProEntitiesRepo repo, final IFProPredicate call) throws NullPointerException {
			if (repo == null) {
				throw new NullPointerException("Repo can't be null"); 
			}
			else if (call == null) {
				throw new NullPointerException("Name and arity can't be null"); 
			}
			else if (call.getArity() != 3 || !(call.getParameters()[1].getEntityType() == EntityType.predicate || call.getParameters()[1].getEntityType() == EntityType.operator)) {
				this.iterator = NULL_ENTITY_ITERATOR;
			}
			else {
				this.iterator = repo.predicateRepo().call(call.getParameters()[1]).iterator();
			}
			if (call.getArity() == 3) {				
				this.template = call.getParameters()[1];
			}
			else {
				this.template = null;
			}
			this.bagofIterator = new Iterator<IFProEntity>(){
								final Change[]	temp = new Change[1];
								IFProEntity		item;
							
								@Override 
								public boolean hasNext() {
									if (iterator != null && iterator.hasNext()) {
										
										do{ item = iterator.next();
											if (FProUtil.unify(template,item,temp)) {
												item = FProUtil.duplicate(call.getParameters()[0]);								
												FProUtil.unbind(temp[0]);
												return true;
											}
											else {
												FProUtil.unbind(temp[0]);
											}
										} while (iterator.hasNext());
										
										return false; 
									}
									else {
										return false;
									}
								}
					
								@Override 
								public IFProEntity next() {
									return item;
								}
							};
		}		
		
		@Override 
		public Iterator<IFProEntity> iterator() {
			return bagofIterator;
		}		
	}

	public static class IterableList implements Iterable<IFProEntity> {
		private final IFProList				list;
		private final IFProEntity			template;
		private final Iterator<IFProEntity>	iterator;
		
		public IterableList(final IFProPredicate call) {
			if (call == null) {
				throw new NullPointerException("Call predicate can't be null"); 
			}
			else if (call.getArity() != 2 || call.getParameters()[1].getEntityType() != EntityType.list) {
				this.list = null;
				this.template = null;
				this.iterator = NULL_ENTITY_ITERATOR;
			}
			else {
				this.list = (IFProList) call.getParameters()[1];
				this.template = call.getParameters()[0];	// Important order - don't change this string with the next one
				
				this.iterator = new Iterator<IFProEntity>(){
					final Change[]	temp = new Change[1];
					IFProEntity		item;
					IFProList		start = list;
				
					@Override 
					public boolean hasNext() {
						if (start != null && start.getEntityType() == EntityType.list) {
							do{if (FProUtil.unify(template,start.getChild(),temp)) {
									FProUtil.unbind(temp[0]);
									item = start.getChild();
									start = (IFProList) start.getTail();
									return true;
								}
								else {
									FProUtil.unbind(temp[0]);
								}
							} while ((start = (IFProList) start.getTail()) != null && start.getEntityType() == EntityType.list);
							
							return false;
						}
						else {
							return false;
						}
					}
		
					@Override 
					public IFProEntity next() {
						return item;
					}
				};
			}
		}
		
		@Override 
		public Iterator<IFProEntity> iterator() {
			return iterator;
		}		
	}
}
