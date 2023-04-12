package chav1961.funnypro.core;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.purelib.basic.OrdinalSyntaxTree;
import chav1961.purelib.basic.growablearrays.InOutGrowableByteArray;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class CommonUtilsTest {
	@Test
	public void stringManipulationTest() throws IOException {
		try(final InOutGrowableByteArray	iogba = new InOutGrowableByteArray(false)) {
			CommonUtil.writeString(iogba,null);
			CommonUtil.writeString(iogba,"test string");
			
			try{CommonUtil.writeString(null,"test string");
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			iogba.flush();
			iogba.reset();
			Assert.assertNull(CommonUtil.readString(iogba));
			Assert.assertEquals("test string",CommonUtil.readString(iogba));

			try{CommonUtil.readString(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

	@Test
	public void treeManipulationTest() throws IOException {
		final SyntaxTreeInterface<IFProEntity>	sti = new OrdinalSyntaxTree<>(), newSti = new OrdinalSyntaxTree<>(); 
		
		try(final InOutGrowableByteArray	iogba = new InOutGrowableByteArray(false)) {
			sti.placeName((CharSequence)"name1",null);
			sti.placeName((CharSequence)"name2",null);

			CommonUtil.writeTree(iogba,sti);
			
			try{CommonUtil.writeTree(null,sti);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{CommonUtil.writeTree(iogba,null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			iogba.flush();
			iogba.reset();

			CommonUtil.readTree(iogba,newSti,()->null);
			Assert.assertTrue(newSti.seekName((CharSequence)"name1") >= 0);
			Assert.assertTrue(newSti.seekName((CharSequence)"name2") >= 0);
			Assert.assertFalse(newSti.seekName((CharSequence)"unknown") >= 0);
			
			try{CommonUtil.readTree(null,newSti,()->null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{CommonUtil.readTree(iogba,null,()->null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{CommonUtil.readTree(iogba,newSti,null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
			
		}
	}
}
