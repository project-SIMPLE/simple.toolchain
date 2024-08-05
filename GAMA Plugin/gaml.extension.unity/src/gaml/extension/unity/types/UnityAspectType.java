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

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.types.GamaType;
import gama.gaml.types.IType;

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

	/** The Constant id. */
	public final static int UNITYASPECTTYPE_ID = IType.AVAILABLE_TYPES + 352583;
 
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
