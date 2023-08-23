package chav1961.funnypro.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import chav1961.funnypro.core.interfaces.IFProStreamSerializable;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface.Walker;

class CommonUtil {
	private static final int					SERIALIZATION_TREE_MAGIC = 0x12123030;
	
	/**
	 * <pSerialize string to the output stream</p>
	 * @param target output stream to serialize string to
	 * @param value string to serialize
	 * @throws IOException
	 */
	public static void writeString(final DataOutput target, final CharSequence value) throws IOException, NullPointerException {
		if (target == null) {
			throw new NullPointerException("Target can't be null");
		}
		else if (value == null) {
			target.writeInt(-1);
		}
		else {
			final int	len = value.length();
			
			target.writeInt(len);
			for (int index = 0; index < len; index++) {
				target.writeChar(value.charAt(index));
			}
		}
	}

	/**
	 * <p>Deserialize string from input stream</p> 
	 * @param source input stream to deserialize string from
	 * @return deserialized string
	 * @throws IOException
	 */
	public static String readString(final DataInput source) throws IOException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source can't be null");
		}
		else {
			final int	total = source.readInt();
			
			if (total == -1) {
				return null;
			}
			else if (total == 0) {
				return "";
			}
			else {
				final char[]	buffer = new char[total];
				
				for (int index = 0, maxIndex = buffer.length; index < maxIndex; index++) {
					buffer[index] = source.readChar();
				}
				return new String(buffer);
			}
		}
	}
	
	/**
	 * <p>Serialize And-Or Tree content to output stream</p>
	 * @param target stream to serialize tree to
	 * @param tree three to serialize
	 * @throws IOException
	 */
	public static void writeTree(final DataOutput target, final SyntaxTreeInterface<?> tree) throws IOException, NullPointerException {
		if (target == null) {
			throw new NullPointerException("Target stream can't be null");
		}
		else if (tree == null) {
			throw new NullPointerException("Tree to serialize can't be null");
		}
		else {
			target.writeInt(SERIALIZATION_TREE_MAGIC);			// Tree magic.
			target.writeLong(tree.size());						// Size of the tree
			tree.walk(new Walker(){								// Tree content
				@Override
				public boolean process(char[] name, int len, long id, Object cargo) {
					try{target.writeLong(id);
						writeString(target, tree.getName(id));
						if (tree.getCargo(id) != null && (tree.getCargo(id) instanceof IFProStreamSerializable)) {
							target.writeByte(1);
							((IFProStreamSerializable)tree.getCargo(id)).serialize(target);
						}
						else {
							target.writeByte(0);
						}
						return true;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			target.writeLong(SERIALIZATION_TREE_MAGIC);			// Tree magic end.
		}
	}
	
	/**
	 * <p>Deserialize And-Or Tree content from the input stream</p> 
	 * @param source content source
	 * @param tree tree to deserialize content to
	 * @throws IOException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void readTree(final DataInput source, final SyntaxTreeInterface tree, final Supplier<IFProStreamSerializable> getter) throws IOException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source stream can't be null");
		}
		else if (tree == null) {
			throw new NullPointerException("Tree to deserialize to can't be null");
		}
		else if (getter == null) {
			throw new NullPointerException("Content class to deserialize to can't be null");
		}
		else if (source.readInt() != SERIALIZATION_TREE_MAGIC) {
			throw new IOException("Illegal magic number in the tree serialization stream");
		}
		else {
			final long	amount = source.readLong();				// Size of the tree
			
			for (long count = 0; count < amount; count++) {
				final long		id = source.readLong();
				final String	name = readString(source);
				final int		hasCargo = source.readByte();
				
				if (hasCargo == 1) {
					final IFProStreamSerializable	inst = getter.get();
					
					inst.deserialize(source);
					tree.placeName((CharSequence)name, id, inst);
				}
				else {
					tree.placeName((CharSequence)name, id, null);
				}
			}
			if (source.readLong() != SERIALIZATION_TREE_MAGIC) {
				throw new IOException("Illegal magic naumber in the tree serialization stream");
			}
		}
	}
}
