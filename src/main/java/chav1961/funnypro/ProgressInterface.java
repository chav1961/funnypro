package chav1961.funnypro;

import java.io.Closeable;

public interface ProgressInterface extends Closeable {
	ProgressInterface setRange(int from, int to);
	ProgressInterface setPos(int pos);
	float getPos();
	ProgressInterface setText(String text);
	String getText();
	boolean isCancellingRequired();
	
	@Override
	void close();
}
