package chav1961.funnypro.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

import chav1961.funnypro.core.interfaces.IFProStreamSerializable;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class CommonUtil {
	private static final int					SERIALIZATION_TREE_MAGIC = 0x12123030;

	/**
	 * <p>Make properties form key/value pairs</p>
	 * @param keysAndValues pair of key and value
	 * @return created properties. The class of this properties is SubstitutableProperties.
	 */
	public static Properties mkProps(final String... keysAndValues) {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		if (keysAndValues.length % 2 != 0) {
			throw new IllegalArgumentException("Unpaired key/value was detected. Amount of parameters need to be even.");
		}
		else {
			for (int index = 0; index < keysAndValues.length; index += 2) {
				if (keysAndValues[index] == null || keysAndValues[index].isEmpty()) {
					throw new IllegalArgumentException("Key parameter on index ["+index+"] is null or empty!");
				}
				else if (keysAndValues[index+1] == null || keysAndValues[index+1].isEmpty()) {
					throw new IllegalArgumentException("Value parameter on index ["+(index+1)+"] is null or empty!");
				}
				else {
					props.setProperty(keysAndValues[index],keysAndValues[index+1]);
				}
			}
		}
		
		return props;
	}
	
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
	
	public static void writeString(final OutputStream target, final String value) throws IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target can't be null");
		}
		else if (value == null) {
			writeInt(target,-1);
		}
		else {
			final byte[]	data = value.getBytes();
			
			writeInt(target,data.length);
			target.write(data);
		}
	}

	/**
	 * <p>Serialize integer to output stream</p>
	 * @param target output stream to serialize integer to
	 * @param value integer to serialize
	 * @throws IOException
	 */
	
	public static void writeInt(final OutputStream target, final int value) throws IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target can't be null");
		}
		else {
			target.write((byte)value & 0xFF);
			target.write((byte)(value >> 8) & 0xFF);
			target.write((byte)(value >> 16) & 0xFF);
			target.write((byte)(value >> 24) & 0xFF);
		}
	}
	
	/**
	 * <p>Serialize long to output stream</p>
	 * @param target output stream to serialize long to
	 * @param value long to serialize
	 * @throws IOException
	 */

	public static void writeLong(final OutputStream target, final long value) throws IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target can't be null");
		}
		else {
			target.write((byte)value & 0xFF);
			target.write((byte)(value >> 8) & 0xFF);
			target.write((byte)(value >> 16) & 0xFF);
			target.write((byte)(value >> 24) & 0xFF);
			target.write((byte)(value >> 32) & 0xFF);
			target.write((byte)(value >> 40) & 0xFF);
			target.write((byte)(value >> 48) & 0xFF);
			target.write((byte)(value >> 56) & 0xFF);
		}
	}

	/**
	 * <p>Deserialize string from input stream</p> 
	 * @param source input stream to deserialize string from
	 * @return deserialized string
	 * @throws IOException
	 */
	public static String readString(final InputStream source) throws IOException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null");
		}
		else {
			final int	total = readInt(source);
			
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
					return new String(buffer);
				}
			}
		}
	}
	
	/**
	 * <p>Deserialize integer from input stream</p> 
	 * @param source input stream to deserialize integer from
	 * @return deserialized integer
	 * @throws IOException
	 */
	public static int readInt(final InputStream source) throws IOException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null");
		}
		else {
			int		result = 0, value;
			
			for (int index = 0; index <= 3; index++) {
				if ((value = source.read()) == -1) {
					throw new EOFException("End of file when reading integer");
				}
				else {
					result |= ((value & 0xFF) << (8 * index));
				}
			}
			
			return result;
		}
	}

	/**
	 * <p>Deserialize long from input stream</p> 
	 * @param source input stream to deserialize long from
	 * @return deserialized long
	 * @throws IOException
	 */
	public static long readLong(final InputStream source) throws IOException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null");
		}
		else {
			long	result = 0;
			long	value;
			
			for (int index = 0; index <= 7; index++) {
				if ((value = source.read()) == -1) {
					throw new EOFException("End of file when reading long");
				}
				else {
					result |= ((value & 0xFF) << (8 * index));
				}
			}
			
			return result;
		}
	}

	/**
	 * <p>Serialize And-Or Tree content to output stream</p>
	 * @param target stream to serialize tree to
	 * @param tree three to serialize
	 * @throws IOException
	 */
	public static void writeTree(final OutputStream target, final SyntaxTreeInterface tree) throws IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target stream can't be null");
		}
		else if (tree == null) {
			throw new IllegalArgumentException("Tree to serialize can't be null");
		}
		else {
			writeInt(target,SERIALIZATION_TREE_MAGIC);			// Tree magic.
			writeLong(target,tree.size());						// Size of the tree
			tree.walk(id -> {									// Upload all data in the loop
				try{writeLong(target,id);
					writeString(target,tree.getName(id));
					if (tree.getCargo(id) != null && (tree.getCargo(id) instanceof IFProStreamSerializable)) {
						writeInt(target,10);
						((IFProStreamSerializable)tree.getCargo(id)).serialize(target);
					}
					else {
						writeInt(target,0);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			writeLong(target,SERIALIZATION_TREE_MAGIC);			// Tree magic end.
		}
	}
	
	/**
	 * <p>Deserialize And-Or Tree content from the input stream</p> 
	 * @param source content source
	 * @param tree tree to deserialize content to
	 * @throws IOException 
	 */
	public static void readTree(final InputStream source, final SyntaxTreeInterface tree, final Class content) throws IOException {
		if (source == null) {
			throw new IllegalArgumentException("Source stream can't be null");
		}
		else if (tree == null) {
			throw new IllegalArgumentException("Tree to deserialize to can't be null");
		}
		else if (readInt(source) != SERIALIZATION_TREE_MAGIC) {
			throw new IOException("Illegal magic naumber in the tree serialization stream");
		}
		else {
			final long	amount = readLong(source);				// Size of the tree
			
			for (long count = 0; count < amount; count++) {
				final long		id = readLong(source);
				final String	name = readString(source);
				final int		hasCargo = readInt(source);
				
				if (hasCargo == 1) {
					try{final IFProStreamSerializable	inst = (IFProStreamSerializable) content.newInstance();
					
						inst.deserialize(source);
						tree.placeName(name.toCharArray(),0,name.length(),id,inst);
					} catch (InstantiationException | IllegalAccessException e) {
						throw new IOException("Instantiation failed when deserialise tree content: "+e.getClass().getName()+" ("+e.getMessage()+")");
					}
				}
				else {
					tree.placeName(name.toCharArray(),0,name.length(),id,null);
				}
			}
			if (readLong(source) != SERIALIZATION_TREE_MAGIC) {
				throw new IOException("Illegal magic naumber in the tree serialization stream");
			}
		}
	}
}
