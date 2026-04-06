/*******************************************************************************************************
 *
 * UnityAspectType.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
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
import gama.api.runtime.scope.IScope;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;

/**
 * The Class UnityAspectType.
 */
@type ( 
		name = "unity_aspect",
		id = UnityAspectType.UNITYASPECTTYPE_ID,
		wraps = { UnityAspect.class },
		concept = { IConcept.TYPE, "Unity" })
@doc ("a type representing the way a geometry will be displayed in Unity")
public class UnityAspectType extends GamaType<UnityAspect> {
 
	public UnityAspectType(ITypesManager typesManager) {
		super(typesManager);
	}

	/** The Constant id. */
	public final static int UNITYASPECTTYPE_ID = IType.BEGINNING_OF_CUSTOM_TYPES + 352583;
 
	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("cast an object into a GeometryToSend if it is an instance of a GeometryToSend")
	public UnityAspect cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof UnityAspect) return (UnityAspect) obj;
		return null;
	} 
 
	@Override
	public UnityAspect getDefault() { return null; }

}
