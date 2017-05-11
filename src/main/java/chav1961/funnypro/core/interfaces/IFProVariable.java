package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes FPro list entities</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProVariable extends IFProEntity {
	/**
	 * <p>Get variable chain</p>
	 * @return variable chain
	 */
	IFProVariable getChain();
	
	/**
	 * <p>Set variable chain</p>
	 * @param chain chain to set.
	 * @return self
	 */
	IFProVariable setChain(IFProVariable chain);
}
