/*******************************************************************************************************
 *
 * UnityInteraction.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
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
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.misc.IValue;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The Class BDIPlan.
 */
/**
 *
 */
@vars ({ @variable (
		name = "has_collider",
		type = IType.BOOL,
		doc = @doc ("has the geometry a collider")),
		@variable (
				name = "constraints",
				type = IType.LIST,
				of = IType.BOOL,
				doc = @doc ("Constraints for the movement of the geometry - [Freeze x position, Freeze y position, Freeze Z position, Freeze x rotation, Freeze y rotation, Freeze Z rotation]")),
		@variable (
				name = "is_interactable",
				type = IType.BOOL,
				doc = @doc ("is the geometry interactable")),
		@variable (
				name = "is_grabable",
				type = IType.BOOL,
				doc = @doc ("is the geometry grabable (interaction with Ray interactor otherwise"))

})
public class UnityInteraction implements IValue {

	/** The collider. */
	private final boolean collider;

	/** The interactable. */
	private final boolean interactable;

	/** The grabable. */
	private final boolean grabable;

	/** The constraints. */
	private final IList<Boolean> constraints;

	/**
	 * Instantiates a new unity interaction.
	 *
	 * @param collider
	 *            the collider
	 * @param interactable
	 *            the interactable
	 * @param grabable
	 *            the grabable
	 * @param constraints
	 *            the constraints
	 */
	public UnityInteraction(final boolean collider, final boolean interactable, final boolean grabable, final IList<Boolean> constraints) {
		this.collider = collider;
		this.interactable = interactable;
		this.grabable = grabable;
		this.constraints = constraints;
	}

	/**
	 * Checks if is collider.
	 *
	 * @return true, if is collider
	 */
	public boolean isCollider() { return collider; }

	/**
	 * Checks if is interactable.
	 *
	 * @return true, if is interactable
	 */
	public boolean isInteractable() { return interactable; }

	/**
	 * Checks if is grabable.
	 *
	 * @return true, if is grabable
	 */
	public boolean isGrabable() { return grabable; }

	/**
	 * Gets the constraints.
	 *
	 * @return the constraints
	 */
	public IList<Boolean> getConstraints() { return constraints; }

	/**
	 * To map.
	 *
	 * @return the map
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = GamaMapFactory.create();
		map.put("constraints", constraints);
		map.put("isInteractable", interactable);
		map.put("isGrabable", grabable);
		map.put("hasCollider", collider);
		return map;
	}

	@Override
	public String toString() {
		return (collider ? "- has_collider" : "") + (interactable ? "- is_interactable" : "")
				+ (grabable ? "- is_grabable" : "") + " - " + constraints;
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return toString();
	}

	@Override
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		return null;
	}

	@Override
	public IType<?> getGamlType() { return Types.get(UnityInteractionType.UNITYINTERACTIONTYPE_ID); }

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		// TODO Auto-generated method stub
		return null;
	}

}
