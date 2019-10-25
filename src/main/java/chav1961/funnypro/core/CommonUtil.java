package chav1961.funnypro.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

import chav1961.funnypro.core.interfaces.IFProStreamSerializable;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface.Walker;

class CommonUtil {
	private static final int					SERIALIZATION_TREE_MAGIC = 0x12123030;
	
	/**
	 * <p>Resolve paths with the given changes</p>
	 * @param baseURI base URI. No any resolution can be higher than this path
	 * @param actualURI actual path
	 * @param changes changes in th actual path
	 * @return new resolved path 
	 */
	public static URI resolvePath(final URI baseURI, final URI actualURI, final URI changes) {
		if (baseURI == null) {
			throw new IllegalArgumentException("Base URI can't be null!");
		}
		else if (actualURI == null) {
			throw new IllegalArgumentException("Actual URI can't be null!");
		}
		else if (changes == null) {
			throw new IllegalArgumentException("Changes can't be null!");
		}
		else {
			final URI	result = changes.getPath().startsWith("/") ? URI.create(baseURI.toString()+changes.getPath().substring(1)).normalize() : actualURI.resolve(changes).normalize();
			final URI	delta = baseURI.relativize(result).normalize();
			
			if (delta.getPath().startsWith(".") || delta.getPath().startsWith("/..") || result.toString().length() < baseURI.toString().length()) {
				throw new IllegalArgumentException("Changes attempts to jump higher than base URI!");
			}
			else {
				return result;
			}
		}
	}
	
	/**
	 * <pSerialize string to the output stream</p>
	 * @param target output stream to serialize string to
	 * @param value string to serialize
	 * @throws IOException
	 */
	
	public static void writeString(final DataOutputStream target, final String value) throws IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target can't be null");
		}
		else if (value == null) {
			target.writeInt(-1);
		}
		else {
			final byte[]	data = value.getBytes("UTF-8");
			
			target.writeInt(data.length);
			target.write(data);
		}
	}

	/**
	 * <p>Deserialize string from input stream</p> 
	 * @param source input stream to deserialize string from
	 * @return deserialized string
	 * @throws IOException
	 */
	public static String readString(final DataInputStream source) throws IOException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null");
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
				final byte[]	buffer = new byte[total];
				int				displ = 0, len;
				
				while ((len = source.read(buffer,displ,total-displ)) > 0) {
					if ((displ += len) >= total-1) {
						break;
					}
				}
				if (len <= 0) {
					throw new EOFException("End of file when reading string content");
				}
				else {
					return new String(buffer,"UTF-8");
				}
			}
		}
	}
	
	/**
	 * <p>Serialize And-Or Tree content to output stream</p>
	 * @param target stream to serialize tree to
	 * @param tree three to serialize
	 * @throws IOException
	 */
	public static void writeTree(final DataOutputStream target, final SyntaxTreeInterface<?> tree) throws IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target stream can't be null");
		}
		else if (tree == null) {
			throw new IllegalArgumentException("Tree to serialize can't be null");
		}
		else {
			target.writeInt(SERIALIZATION_TREE_MAGIC);			// Tree magic.
			target.writeLong(tree.size());						// Size of the tree
			tree.walk(new Walker(){							// Tree content
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
			target.flush();
		}
	}
	
	/**
	 * <p>Deserialize And-Or Tree content from the input stream</p> 
	 * @param source content source
	 * @param tree tree to deserialize content to
	 * @throws IOException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void readTree(final DataInputStream source, final SyntaxTreeInterface tree, final Class<?> content) throws IOException {
		if (source == null) {
			throw new IllegalArgumentException("Source stream can't be null");
		}
		else if (tree == null) {
			throw new IllegalArgumentException("Tree to deserialize to can't be null");
		}
		else if (source.readInt() != SERIALIZATION_TREE_MAGIC) {
			throw new IOException("Illegal magic naumber in the tree serialization stream");
		}
		else {
			final long	amount = source.readLong();				// Size of the tree
			
			for (long count = 0; count < amount; count++) {
				final long		id = source.readLong();
				final String	name = readString(source);
				final int		hasCargo = source.readInt();
				
				if (hasCargo == 1) {
					try{final IFProStreamSerializable	inst = (IFProStreamSerializable) content.newInstance();
						final DataInputStream			dis = new DataInputStream(source);
					
						inst.deserialize(dis);
						tree.placeName(name.toCharArray(),0,name.length(),id,inst);
					} catch (InstantiationException | IllegalAccessException e) {
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
