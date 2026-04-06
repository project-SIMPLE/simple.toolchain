/*******************************************************************************************************
 *
 * UnityInteractionType.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
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
 * The Class UnityInteractionType.
 */ 

@type (
		name = "unity_interaction",
		id = UnityInteractionType.UNITYINTERACTIONTYPE_ID,
		wraps = { UnityInteraction.class },
		concept = { IConcept.TYPE, "Unity" })
@doc ("a type representing a set of properties concerning the interaction for the geometry/agent to send to Unity")
public class UnityInteractionType extends GamaType<UnityInteraction> {

	public UnityInteractionType(ITypesManager typesManager) {
		super(typesManager);
	}

	/** The Constant id. */
	public final static int UNITYINTERACTIONTYPE_ID = IType.BEGINNING_OF_CUSTOM_TYPES + 383736;

	@Override
	public boolean canCastToConst() {
		return true;
	} 

	@Override
	@doc ("cast an object into a unity_interaction if it is an instance of a unity_interaction")
	public UnityInteraction cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof UnityInteraction) return (UnityInteraction) obj;
		return null;
	}

	@Override
	public UnityInteraction getDefault() { return null; }

}
