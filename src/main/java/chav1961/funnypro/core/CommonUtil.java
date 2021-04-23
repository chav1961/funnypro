package chav1961.funnypro.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
	
	public static void writeString(final DataOutput target, final String value) throws IOException, NullPointerException {
		if (target == null) {
			throw new NullPointerException("Target can't be null");
		}
		else if (value == null) {
			target.writeInt(-1);
		}
		else {
			final char[]	data = value.toCharArray();
			
			target.writeInt(data.length);
			for (char symbol : data) {
				target.writeChar(symbol);
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
				int				displ = 0, len;
				
				for (int index = 0, maxIndex = buffer.length; index < maxIndex; index++) {
					buffer[index] = source.readChar();
				}
//				while ((len = source.read(buffer,displ,total-displ)) > 0) {
//					if ((displ += len) >= total-1) {
//						break;
//					}
//				}
//				if (len <= 0) {
//					throw new EOFException("End of file when reading string content");
//				}
//				else {
					return new String(buffer);
//				}
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
						writeString(target,tree.getName(id));
						if (tree.getCargo(id) != null && (tree.getCargo(id) instanceof IFProStreamSerializable)) {
							target.writeInt(10);
							((IFProStreamSerializable)tree.getCargo(id)).serialize(target);
						}
						else {
							target.writeInt(0);
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
	public static void readTree(final DataInput source, final SyntaxTreeInterface tree, final Class<?> content) throws IOException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source stream can't be null");
		}
		else if (tree == null) {
			throw new NullPointerException("Tree to deserialize to can't be null");
		}
		else if (content == null) {
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
				final int		hasCargo = source.readInt();
				
				if (hasCargo == 1) {
					try{final IFProStreamSerializable	inst = (IFProStreamSerializable) content.getConstructor().newInstance();
					
						inst.deserialize(source);
						tree.placeName(name.toCharArray(),0,name.length(),id,inst);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw new IOException("Instantiation failed when deserialise tree content: "+e.getClass().getName()+" ("+e.getMessage()+")");
					}
				}
				else {
					tree.placeName(name.toCharArray(),0,name.length(),id,null);
				}
			}
			if (source.readLong() != SERIALIZATION_TREE_MAGIC) {
				throw new IOException("Illegal magic naumber in the tree serialization stream");
			}
		}
	}
}
