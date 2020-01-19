package chav1961.funnypro.core;

import java.util.Iterator;

import chav1961.funnypro.core.FProUtil.Change;
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
	public static class IterableNameAndArity implements Iterable<IFProEntity> {
		private final Iterator<NameAndArity>		iterator;
		private final IFProOperator					def;
		
		public IterableNameAndArity(final IFProEntitiesRepo repo, final IFProOperator nameAndArity) {
			if (repo == null) {
				throw new IllegalArgumentException("Repo can't be null"); 
			}
			else if (nameAndArity == null) {
				throw new IllegalArgumentException("Name and arity can't be null"); 
			}
			else if (nameAndArity.getLeft() == null || nameAndArity.getRight() == null) {
				iterator = null;
			}
			else if (nameAndArity.getLeft().getEntityType() == EntityType.variable || nameAndArity.getLeft().getEntityType() == EntityType.anonymous) {
				if (nameAndArity.getRight().getEntityType() == EntityType.variable || nameAndArity.getRight().getEntityType() == EntityType.anonymous) {
					iterator = repo.predicateRepo().content(-1).iterator();
				}
				else if (nameAndArity.getRight().getEntityType() == EntityType.integer && nameAndArity.getRight().getEntityId() >= IFProPredicate.MIN_ARITY && nameAndArity.getRight().getEntityId() <= IFProPredicate.MAX_ARITY) {
					iterator = repo.predicateRepo().content((int)nameAndArity.getRight().getEntityId()).iterator();
				}
				else {
					iterator = null;
				}
			}
			else if (nameAndArity.getLeft().getEntityType() == EntityType.predicate) {
				if (nameAndArity.getRight().getEntityType() == EntityType.variable || nameAndArity.getRight().getEntityType() == EntityType.anonymous) {
					iterator = repo.predicateRepo().content(-1,nameAndArity.getLeft().getEntityId()).iterator();
				}
				else if (nameAndArity.getRight().getEntityType() == EntityType.integer && nameAndArity.getRight().getEntityId() >= IFProPredicate.MIN_ARITY && nameAndArity.getRight().getEntityId() <= IFProPredicate.MAX_ARITY) {
					iterator = repo.predicateRepo().content((int)nameAndArity.getRight().getEntityId(),nameAndArity.getLeft().getEntityId()).iterator();
				}
				else {
					iterator = null;
				}
			}
			else {
				iterator = null;
			}
			def = nameAndArity;
		}		
		
		@Override 
		public Iterator<IFProEntity> iterator() {return new Iterator<IFProEntity>(){
				@Override public boolean hasNext() {return iterator != null && iterator.hasNext();}
	
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
		
		public IterableCall(final IFProEntitiesRepo repo, final IFProPredicate call) {
			if (repo == null) {
				throw new IllegalArgumentException("Repo can't be null"); 
			}
			else if (call == null) {
				throw new IllegalArgumentException("Name and arity can't be null"); 
			}
			else if (call.getArity() != 1) {
				iterator = null;
			}
			else if (call.getParameters()[0].getEntityType() == EntityType.variable || call.getParameters()[0].getEntityType() == EntityType.anonymous) {
				iterator = null;
			}
			else {
				iterator = repo.predicateRepo().call(call.getParameters()[0]).iterator();
			}
			template = call.getParameters()[0];	// Important order - don't change this string with the next one 
			callIterator = new Iterator<IFProEntity>(){
				final Change[]	temp = new Change[1];
				IFProEntity		item;
			
				@Override 
				public boolean hasNext() {
					if (iterator != null && iterator.hasNext()) {
						
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
				
				@Override public IFProEntity next() {return item;}
			};
		}		
		
		@Override  public Iterator<IFProEntity> iterator() {return callIterator;}		
	}
	
	public static class IterableCallBagof implements Iterable<IFProEntity> {
		private final Iterator<IFProEntity>		iterator, bagofIterator;
		private final IFProEntity				template,image;
		
		public IterableCallBagof(final IFProEntitiesRepo repo, final IFProPredicate call) {
			if (repo == null) {
				throw new IllegalArgumentException("Repo can't be null"); 
			}
			else if (call == null) {
				throw new IllegalArgumentException("Name and arity can't be null"); 
			}
			else if (call.getArity() != 3) {
				iterator = null;
			}
			else if (call.getParameters()[1].getEntityType() == EntityType.variable || call.getParameters()[1].getEntityType() == EntityType.anonymous) {
				iterator = null;
			}
			else {
				iterator = repo.predicateRepo().call(call.getParameters()[1]).iterator();
			}
			template = call.getParameters()[1];
			image = call.getParameters()[0];		// Important order - don't change this string with the next one
			bagofIterator = new Iterator<IFProEntity>(){
								final Change[]	temp = new Change[1];
								IFProEntity		item;
							
								@Override 
								public boolean hasNext() {
									if (iterator != null && iterator.hasNext()) {
										
										do{ item = iterator.next();
											if (FProUtil.unify(template,item,temp)) {
												item = FProUtil.duplicate(image);								
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
					
								@Override public IFProEntity next() {return item;}
							};
		}		
		
		@Override public Iterator<IFProEntity> iterator() {return bagofIterator;}		
	}

	public static class IterableList implements Iterable<IFProEntity> {
		private final IFProList				list;
		private final IFProEntity			template;
		private final Iterator<IFProEntity>	iterator;
		
		public IterableList(final IFProPredicate call) {
			this.list = (IFProList) call.getParameters()[0];
			this.template = call.getParameters()[1];
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
	
				@Override public IFProEntity next() {return item;}
			};
		}
		
		@Override public Iterator<IFProEntity> iterator() {return iterator;}		
	}
}
