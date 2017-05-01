package chav1961.funnypro.core;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.interfaces.IFProQuickList;

public class QuickListTest {

	@Test
	public void basicTest() {
		final IFProQuickList<String>		list = new QuickList<>();

		for (int index = 10; index <= 24; index++) {
			Assert.assertFalse(list.contains(index));
			list.insert(index,String.valueOf(index));
			Assert.assertTrue(list.contains(index));
			Assert.assertEquals(list.get(index),String.valueOf(index));
		}
		
		Assert.assertFalse(list.contains(9));
		list.insert(9,String.valueOf(9));
		Assert.assertTrue(list.contains(9));
		Assert.assertEquals(list.get(9),String.valueOf(9));
		
		Assert.assertFalse(list.contains(26));
		list.insert(26,String.valueOf(26));
		Assert.assertTrue(list.contains(26));
		Assert.assertEquals(list.get(26),String.valueOf(26));

		Assert.assertFalse(list.contains(25));
		list.insert(25,String.valueOf(25));
		Assert.assertTrue(list.contains(25));
		Assert.assertEquals(list.get(25),String.valueOf(25));
		
		for (int index = 9; index <= 26; index++) {
			Assert.assertTrue(list.contains(index));
		}
		
		int count = 0;
		for (Long item : list.content()) {
			count++;
		}
		Assert.assertEquals(count,18);
		Assert.assertEquals(list.size(),18);
	}
}
