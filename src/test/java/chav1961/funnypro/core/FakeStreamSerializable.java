package chav1961.funnypro.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import chav1961.funnypro.core.interfaces.IFProStreamSerializable;

public class FakeStreamSerializable implements IFProStreamSerializable {
	@Override public void serialize(DataOutputStream target) throws IOException {}
	@Override public void deserialize(DataInputStream source) throws IOException {}
}
