/*******************************************************************************************************
 *
 * VRExperiment.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.species;

import java.util.ArrayList;
import java.util.List;

import gama.annotations.doc;
import gama.annotations.experiment;
import gama.annotations.getter;
import gama.annotations.setter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.api.compilation.descriptions.IDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.core.experiment.ExperimentAgent;
import gaml.compiler.descriptions.StatementWithChildrenDescription;
/**
 * The Class VRExperiment.
 */
@experiment ("unity")
@vars ({ @variable (
		name = VRExperiment.UNITY_LINKER_SPECIES,
		type = IType.STRING,
		doc = { @doc ("Species of the unity linker agent") }),
		@variable (
				name = VRExperiment.UNITY_LINKER,
				type = IType.AGENT,
				doc = { @doc ("unity linker agent") }),
		@variable (
				name = VRExperiment.DISPLAYS_TO_HIDE,
				type = IType.LIST,
				doc = { @doc ("Displays that will not be display in the experiment") }) })
@doc ("Experiments design for models with a connection with Unity")
public class VRExperiment extends ExperimentAgent {

	/** The Constant DISPLAYS_TO_HIDE. */
	public static final String DISPLAYS_TO_HIDE = "displays_to_hide";

	/** The Constant UNITY_LINKER_SPECIES. */
	public static final String UNITY_LINKER_SPECIES = "unity_linker_species";

	/** The Constant UNITY_LINKER. */
	public static final String UNITY_LINKER = "unity_linker";

	/** The unity linker. */
	private IAgent unityLinker = null;

	/**
	 * Instantiates a new VR experiment.
	 *
	 * @param s
	 *            the s
	 * @param index
	 *            the index
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public VRExperiment(final IPopulation<? extends IAgent> s, final int index) throws GamaRuntimeException {
		super(s, index);
	}

	/**
	 * Gets the unity linker.
	 *
	 * @param agent
	 *            the agent
	 * @return the unity linker
	 */
	@getter (VRExperiment.UNITY_LINKER)
	public static String getUnityLinker(final IAgent agent) {
		return (String) agent.getAttribute(UNITY_LINKER);
	}

	/**
	 * Sets the unity linker.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (VRExperiment.UNITY_LINKER)
	public static void setUnityLinker(final IAgent agent, final IAgent val) {
		agent.setAttribute(UNITY_LINKER, val);
	}

	/**
	 * Gets the unity linker species.
	 *
	 * @param agent
	 *            the agent
	 * @return the unity linker species
	 */
	@getter (VRExperiment.UNITY_LINKER_SPECIES)
	public static String getUnityLinkerSpecies(final IAgent agent) {
		return (String) agent.getAttribute(UNITY_LINKER_SPECIES);
	}

	/**
	 * Sets the unity linker species.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (VRExperiment.UNITY_LINKER_SPECIES)
	public static void setUnityLinkerSpecies(final IAgent agent, final String val) {
		agent.setAttribute(UNITY_LINKER_SPECIES, val);
	}

	/**
	 * Gets the displays to hide.
	 *
	 * @param agent
	 *            the agent
	 * @return the displays to hide
	 */
	@getter (DISPLAYS_TO_HIDE)
	public static IList<String> getDisplaysToHide(final IAgent agent) {
		return (IList<String>) agent.getAttribute(DISPLAYS_TO_HIDE);
	}

	/**
	 * Sets the displays to hide.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (DISPLAYS_TO_HIDE)
	public static void setDisplaysToHide(final IAgent agent, final IList<String> val) {
		agent.setAttribute(DISPLAYS_TO_HIDE, val);
	}

	/**
	 * Inits the.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 */
	@Override
	public void _init_(final IScope scope) {
		IList<String> dispToHide = getDisplaysToHide(getAgent());
		if (dispToHide != null) {
			List<IDescription> toRemove = new ArrayList<>();
			final IDescription des = ((ISymbol) this.getSpecies().getOriginalSimulationOutputs()).getDescription();
			for (IDescription dd : des.getOwnChildren()) {
				if (dispToHide.contains(dd.getName())) { toRemove.add(dd); }
			}
			((StatementWithChildrenDescription) des).getChildren().removeAll(toRemove);
		}
		 super._init_(scope); 

		ISpecies sp = Cast.asSpecies(scope, getUnityLinkerSpecies(getAgent()));
		IAgent ul = sp.getPopulation(scope).createAgentAtIndex(scope.getSimulation().getScope(), 0, GamaMapFactory.create(),
				false, true);
		setUnityLinker(ul);


	}
 
	/**
	 * Gets the unity linker.
	 *
	 * @return the unity linker
	 */
	public IAgent getUnityLinker() { return unityLinker; }

	/**
	 * Sets the unity linker.
	 *
	 * @param unityLinker
	 *            the new unity linker
	 */
	public void setUnityLinker(final IAgent unityLinker) { this.unityLinker = unityLinker; }

}
