package chav1961.funnypro.plugins;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.purelib.basic.ReusableInstances;
import chav1961.purelib.streams.charsource.ReaderCharSource;
import chav1961.purelib.streams.chartarget.WriterCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

class IOProcessorGlobal {
	final ReusableInstances<IOProcessorLocal>	collection = new ReusableInstances<>(
														()->new IOProcessorLocal(), 
														(i)->{i.callback = null; i.stack = null; i.vars = null; return i;}
													);
	final IFProEntity[]		forEntity = new IFProEntity[1];

	IFProEntitiesRepo		repo;
	IFProParserAndPrinter	pap;
	CharacterTarget			currentWriter = new WriterCharTarget(System.out, true);
	CharacterSource			currentReader = new ReaderCharSource(new InputStreamReader(System.in), true);
	Reader					openedReader = null;
	PrintStream				openedWriter = null;
}
