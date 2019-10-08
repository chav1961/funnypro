package chav1961.funnypro.core;

import java.util.HashSet;
import java.util.Set;

import chav1961.funnypro.core.interfaces.IFProQuickList;
import chav1961.purelib.basic.LongIdMap;

class QuickList<D> extends LongIdMap<D> implements IFProQuickList<D> {
	final Set<Long>	keys = new HashSet<>();
	int				size = 0;

	public QuickList(Class<D> contentType) throws NullPointerException {
		super(contentType);
	}

	@Override
	public boolean contains(final long key) {
		return super.contains(key);
	}

	@Override
	public Iterable<Long> content() {
		return keys;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public D get(final long key) {
		return super.get(key);
	}

	@Override
	public void insert(final long key, final D data) {
		if (!contains(key)) {
			keys.add(key);
			size++;
		}
		super.put(key,data);
	}
}
