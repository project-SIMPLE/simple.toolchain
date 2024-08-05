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

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;

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

	private boolean collider;
	private boolean interactable;
	private boolean grabable;
	private IList<Boolean> constraints;
	
	public UnityInteraction( boolean collider,
			boolean interactable, boolean grabable, IList<Boolean> constraints) {
		super();
		this.collider = collider;
		this.interactable = interactable;
		this.grabable = grabable;
		this.constraints = constraints;
	}
	

	
	public boolean isCollider() {
		return collider;
	}

	public boolean isInteractable() {
		return interactable;
	}

	public boolean isGrabable() {
		return grabable;
	}
	public IList<Boolean> getConstraints() {
		return constraints;
	}
	

	public Map<String, Object> toMap() {
		Map<String, Object> map = GamaMapFactory.create();
		map.put("constraints", constraints);
		map.put("isInteractable",interactable);
		map.put("isGrabable", grabable);
		map.put("hasCollider", collider);
		return map;
	}

	@Override
	public String toString() {
		return (collider ? "- has_collider" : "") +  (interactable ? "- is_interactable" : "") + (grabable ? "- is_grabable" : "") + " - "+constraints; 
	}

	
	@Override
	public String stringValue(IScope scope) throws GamaRuntimeException {
		return toString();
	}


	@Override
	public IValue copy(IScope scope) throws GamaRuntimeException {
		return null;
	}

	@Override
	public JsonValue serializeToJson(Json json) {
		// TODO Auto-generated method stub
		return null;
	}

}
