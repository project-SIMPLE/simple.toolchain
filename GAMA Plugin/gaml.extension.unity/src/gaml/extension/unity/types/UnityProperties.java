/*******************************************************************************************************
 *
 * UnityProperties.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.types;

import java.util.Map;

import gama.annotations.doc;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.misc.IValue;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;


@vars ({ @variable (
		name = "id",
		type = IType.STRING,
		doc = @doc ("The id of the Unity properties")),
	@variable (
		name = "aspect",
		type = UnityAspectType.UNITYASPECTTYPE_ID,
		doc = @doc ("The aspect associated to the Unity properties")),
	@variable (
			name = "interaction",
			type = UnityInteractionType.UNITYINTERACTIONTYPE_ID,
			doc = @doc ("The interaction associated to the Unity properties")),
		@variable (
				name = "tag",
				type = IType.STRING,
				doc = @doc ("the tag associated to the Unity properties"))
					
		 })  
public class UnityProperties implements IValue { 

	private String id;
	private UnityAspect aspect;
	private UnityInteraction interaction;
	private String tag;
	private boolean toFollow;
	



	public UnityProperties(String id,  String tag, UnityAspect aspect, UnityInteraction interaction, boolean toFollow) {
		super();
		this.id = id;
		this.aspect = aspect;
		this.tag = tag;
		this.interaction = interaction;
		this.toFollow = toFollow;
	}

	public String getId() {
		return id;
	}

	public UnityAspect getAspect() {
		return aspect;
	}

	public String getTag() {
		return tag;
	}
	public UnityInteraction getInteraction() {
		return interaction;
	}

	

	public boolean isToFollow() {
		return toFollow;
	}

	@Override
	public String toString() {
		return id + " - " + aspect + " - " + interaction + " - " + tag ;
		
	}

	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		return toString() ;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = GamaMapFactory.create();
		map.put("id", id);
		map.put("tag", tag);
		map.putAll(aspect.toMap());
		map.putAll(interaction.toMap());
		map.put("toFollow", toFollow);
		
		
		return map;
	}

	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		return null;
	}

	@Override
	public IType<?> getGamlType() {
		{
			return Types.get(UnityPropertiesType.UNITYPROPERTIESTYPE_ID);
		}
	}

	@Override
	public IJsonValue serializeToJson(IJson json) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
