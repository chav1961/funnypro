package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IResolvable;

public class EnternalPluginEntity implements IFProExternalEntity {
	private final long			pluginId;
	private final String		pluginName, pluginProducer;
	private final int[]			pluginVersion;
	private final IResolvable	resolver;
	private IFProEntity			parent;

	public EnternalPluginEntity(final IFProExternalEntity entity){
		this(entity.getEntityId(),entity.getPluginName(),entity.getPluginProducer(),entity.getPluginVersion(),entity.getResolver());
	}
	
	public EnternalPluginEntity(final long pluginId, final String pluginName, final String pluginProducer, final int[] pliginVersion, final IResolvable resolver){
		this(null,pluginId,pluginName,pluginProducer,pliginVersion,resolver);
	}
	
	public EnternalPluginEntity(final IFProEntity parent, final long pluginId, final String pluginName, final String pluginProducer, final int[] pluginVersion, final IResolvable resolver){
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
	@Override public IResolvable getResolver() {return resolver;}

	@Override
	public String toString() {
		return "EnternalPluginEntity [pluginName=" + pluginName + ", pluginProducer=" + pluginProducer + ", pluginVersion=" + pluginVersion + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pluginName == null) ? 0 : pluginName.hashCode());
		result = prime * result + ((pluginProducer == null) ? 0 : pluginProducer.hashCode());
		result = prime * result + ((pluginVersion == null) ? 0 : pluginVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		EnternalPluginEntity other = (EnternalPluginEntity) obj;
		if (pluginName == null) {
			if (other.pluginName != null) return false;
		} else if (!pluginName.equals(other.pluginName)) return false;
		if (pluginProducer == null) {
			if (other.pluginProducer != null) return false;
		} else if (!pluginProducer.equals(other.pluginProducer)) return false;
		if (pluginVersion == null) {
			if (other.pluginVersion != null) return false;
		} else if (!pluginVersion.equals(other.pluginVersion)) return false;
		return true;
	}
}
