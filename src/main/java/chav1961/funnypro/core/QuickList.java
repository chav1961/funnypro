package chav1961.funnypro.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.funnypro.core.interfaces.IFProQuickList;

class QuickList<D> implements IFProQuickList<D> {
	private final Content<D>	fill = new Content<D>(-Integer.MAX_VALUE,null); 
	private Content<D>[]		list = new Content[16]; 		
	private int					amount = 0;
	
	public QuickList() {
		Arrays.fill(list,fill);
	}

	@Override
	public boolean contains(long key) {
		return Arrays.binarySearch(list,new Content<D>(key,null)) >= 0;
	}

	@Override
	public Iterable<Long> content() {
		final List<Long>		result = new ArrayList<>();
		
		for (Content<D> item : list) {
			if (item.key != -Integer.MAX_VALUE) {
				result.add(item.key);
			}
		}
		return result;
	}

	@Override
	public int size() {
		int	result = 0;
		
		for (Content<D> item : list) {
			if (item.key != -Integer.MAX_VALUE) {
				result++;
			}
		}
		return result;
	}
	
	@Override
	public D get(long key) {
		if (contains(key)) {
			return list[Arrays.binarySearch(list,new Content<D>(key,null))].data;
		}
		else {
			return null;
		}
	}

	@Override
	public void insert(long key, D data) {
		final Content<D>	content = new Content<D>(key,data);
		int					found = Arrays.binarySearch(list,content); 
		
		if (found < 0) {
			if (amount >= list.length) {
				final Content<D>[]	newList = new Content[2*list.length];
				
				Arrays.fill(newList,fill);
				System.arraycopy(list,0,newList,list.length,list.length);
				list = newList;
				found = Arrays.binarySearch(list,content); 
			}
			if (-found > list.length) {
				System.arraycopy(list,1,list,0,-2-found);
				list[-2-found] = content;
			}
			else if (found == -1) {
				System.arraycopy(list,0,list,1,list.length-1);
				list[0] = content;
			}
			else {
				System.arraycopy(list,1,list,0,-2-found);
				list[-2-found] = content;
			}
			amount++;
		}
		else {
			throw new IllegalArgumentException("Attempt to insert existent key ["+key+"] into the array");
		}
		
	}

	private static class Content<D> implements Comparable<Content<D>>{
		long	key;
		D		data;
		
		public Content(final long key, final D data) {
			this.key = key;
			this.data = data;
		}

		@Override public String toString() {return "Content [key=" + key + ", data=" + data + "]";}

		@Override
		public int compareTo(final Content<D> another) {
			return this.key < another.key ? -1 : (this.key == another.key ? 0 : 1);
		}
	}

}
