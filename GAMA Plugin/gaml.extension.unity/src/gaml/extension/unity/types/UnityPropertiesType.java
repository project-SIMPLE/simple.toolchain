/*******************************************************************************************************
 *
 * UnityPropertiesType.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.runtime.scope.IScope; 


/**
 * The Class UnityPropertiesType.
 */
   
@type (
		name = "unity_property",
		id = UnityPropertiesType.UNITYPROPERTIESTYPE_ID,
		wraps = { UnityProperties.class },
		concept = { IConcept.TYPE, "Unity" })
@doc ("a type representing a set of properties for the geometry/agent to send to Unity")
public class UnityPropertiesType extends GamaType<UnityProperties> {

	/** The Constant id. */
	public final static int UNITYPROPERTIESTYPE_ID = IType.BEGINNING_OF_CUSTOM_TYPES + 123163525;
 
	public UnityPropertiesType(ITypesManager typesManager) {
		super(typesManager);
	}

	
	@Override 
	public boolean canCastToConst() {
		return true; 
	} 

	@Override
	@doc ("cast an object into a unity_properties if it is an instance of a unity_properties")
	public UnityProperties cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof UnityProperties) return (UnityProperties) obj;
		return null;
	}

	@Override
	public UnityProperties getDefault() { return null; }

}
