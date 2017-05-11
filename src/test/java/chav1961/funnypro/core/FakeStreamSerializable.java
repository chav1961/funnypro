package chav1961.funnypro.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import chav1961.funnypro.core.interfaces.IFProStreamSerializable;

public class FakeStreamSerializable implements IFProStreamSerializable {
	@Override public void serialize(OutputStream target) throws IOException {}
	@Override public void deserialize(InputStream source) throws IOException {}
}
