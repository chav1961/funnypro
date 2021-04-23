package chav1961.funnypro.core.entities;

import java.util.Arrays;

import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IResolvable;

/**
 * <p>This class describes external plugin description entity in the expression tree</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class ExternalPluginEntity<G,L> implements IFProExternalEntity<G,L> {
	private final long			pluginId;
	private final String		pluginName, pluginProducer;
	private final int[]			pluginVersion;
	private final IResolvable<G,L>	resolver;
	private IFProEntity			parent;

	/**
	 * <p>Constructor of the object</p>
	 * @param entity plugin descriptor
	 */
	public ExternalPluginEntity(final IFProExternalEntity<G,L> entity){
		this(entity.getEntityId(),entity.getPluginName(),entity.getPluginProducer(),entity.getPluginVersion(),entity.getResolver());
	}
	
	/**
	 * <p>Constructor of the object</p>
	 * @param pluginId any unique long 
	 * @param pluginName any plugin name
	 * @param pluginProducer plugin producer name
	 * @param pluginVersion any non-empty version number
	 * @param resolver any implementation of plugin resolver
	 */
	public ExternalPluginEntity(final long pluginId, final String pluginName, final String pluginProducer, final int[] pluginVersion, final IResolvable<G,L> resolver){
		this(null,pluginId,pluginName,pluginProducer,pluginVersion,resolver);
	}
	
	/**
	 * <p>Constructor of the object</p>
	 * @param parent parent node of the description 
	 * @param pluginId any unique long 
	 * @param pluginName any plugin name
	 * @param pluginProducer plugin producer name
	 * @param pluginVersion any non-empty version number
	 * @param resolver any implementation of plugin resolver
	 */
	public ExternalPluginEntity(final IFProEntity parent, final long pluginId, final String pluginName, final String pluginProducer, final int[] pluginVersion, final IResolvable<G,L> resolver){
		if (pluginName == null || pluginName.isEmpty()) {
			throw new IllegalArgumentException("Plugin name can't be null or empty");
		}
		else if (pluginProducer == null || pluginProducer.isEmpty()) {
			throw new IllegalArgumentException("Plugin producer can't be null or empty");
		}
		else if (pluginVersion == null || pluginVersion.length == 0) {
			throw new IllegalArgumentException("Plugin version can't be null or empty");
		}
		else if (resolver == null) {
			throw new IllegalArgumentException("Resolver can't be null");
		}
		else {
			this.parent = parent;			this.pluginId = pluginId;	
			this.pluginName = pluginName;	this.pluginProducer = pluginProducer;
			this.resolver = resolver;		this.pluginVersion = pluginVersion.clone();
		}
	}
	
	@Override public EntityType getEntityType() {return EntityType.externalplugin;}
	@Override public long getEntityId() {return pluginId;}
	@Override public IFProEntity setEntityId(long entityId) {return this;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}
	@Override public String getPluginName() {return pluginName;}
	@Override public String getPluginProducer() {return pluginProducer;}
	@Override public int[] getPluginVersion() {return pluginVersion.clone();}
	@Override public IResolvable<G,L> getResolver() {return resolver;}

	@Override
	public String toString() {
		return "EnternalPluginEntity [pluginName=" + pluginName + ", pluginProducer=" + pluginProducer + ", pluginVersion=" + Arrays.toString(pluginVersion) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pluginName == null) ? 0 : pluginName.hashCode());
		result = prime * result + ((pluginProducer == null) ? 0 : pluginProducer.hashCode());
		result = prime * result + ((pluginVersion == null) ? 0 : Arrays.hashCode(pluginVersion));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		@SuppressWarnings("unchecked")
		ExternalPluginEntity<G,L> other = (ExternalPluginEntity<G,L>) obj;
		if (pluginName == null) { if (other.pluginName != null) return false;
		} else if (!pluginName.equals(other.pluginName)) return false;
		if (pluginProducer == null) { if (other.pluginProducer != null) return false;
		} else if (!pluginProducer.equals(other.pluginProducer)) return false;
		if (pluginVersion == null) { if (other.pluginVersion != null) return false;
		} else if (!Arrays.equals(pluginVersion,other.pluginVersion)) return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ExternalPluginEntity<G, L> clone() throws CloneNotSupportedException {
		return (ExternalPluginEntity<G, L>)super.clone();
	}
}
