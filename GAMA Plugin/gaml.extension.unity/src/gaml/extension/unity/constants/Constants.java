/*******************************************************************************************************
 *
 * Constants.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.constants;

import gama.annotations.precompiler.GamlAnnotations.constant;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IConstantCategory;
import gaml.extension.unity.types.UnityInteraction;

/**
 * The Interface Constants.
 */
public interface Constants {

	/** The no int. */
	@constant (
			value = "no_interaction",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT, "Unity" },
			doc = @doc ("unity_interaction with no interaction")) UnityInteraction noInt =
					new UnityInteraction(false, false, false, null);
	
	/** The collider. */
	@constant (
			value = "collider",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT, "Unity" },
			doc = @doc ("unity_interaction with just a collider")) UnityInteraction collider =
					new UnityInteraction(true, false, false, null);

	/** The grabable. */
	@constant (
			value = "grabable",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT, "Unity" },
			doc = @doc ("unity_interaction with a grabable interaction")) UnityInteraction grabable =
					new UnityInteraction(true, true, true, null);

	/** The ray inter. */
	@constant (
			value = "ray_interactable",
			category = { IConstantCategory.CONSTANT },
			concept = { IConcept.CONSTANT, "Unity" },
			doc = @doc ("unity_interaction with a ray interaction")) UnityInteraction rayInter =
					new UnityInteraction(true, true, false, null);

}
