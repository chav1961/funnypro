package chav1961.funnypro.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import chav1961.funnypro.core.interfaces.IFProStreamSerializable;

public class FakeStreamSerializable implements IFProStreamSerializable {
	@Override public void serialize(DataOutput target) throws IOException {}
	@Override public void deserialize(DataInput source) throws IOException {}
}
