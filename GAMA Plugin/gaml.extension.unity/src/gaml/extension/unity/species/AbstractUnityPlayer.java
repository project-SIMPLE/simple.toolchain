/*******************************************************************************************************
 *
 * AbstractUnityPlayer.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.species;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.gaml.operators.spatial.SpatialCreation;
import gama.gaml.operators.spatial.SpatialOperators;
import gama.gaml.operators.spatial.SpatialTransformations;
import gama.gaml.types.IType;

/**
 * The Class AbstractUnityPlayer.
 */

@species(name = "abstract_unity_player")
@vars({ @variable(name = IKeyword.HEADING, type = IType.FLOAT,
	doc = { @doc ("rotation to apply for the display of the agent in GAMA")}),
	@variable(name = IKeyword.COLOR, type = IType.COLOR,
			doc = { @doc ("color of the agent for the display in GAMA")}),
	@variable(name = AbstractUnityPlayer.TO_DISPLAY, type = IType.BOOL,
			doc = { @doc ("display or not the agent in GAMA")}),
	@variable(name = AbstractUnityPlayer.SELECTED, type = IType.BOOL,
	doc = { @doc ("is the agent selected")}),
	@variable(name = AbstractUnityPlayer.ZOFFSET, type = IType.FLOAT,init = "2.0",
	doc = { @doc ("offset along the z-axis for the the display of the agent in GAMA")}),

	@variable(name = AbstractUnityPlayer.CONE_DISTANCE, type = IType.FLOAT,
			doc = { @doc ("distance of the cone for the display of the agent in GAMA")}),
	@variable(name = AbstractUnityPlayer.CONE_AMPLITUDE, type = IType.FLOAT,
			doc = { @doc ("amplitude of the cone for the display of the agent in GAMA")}),
	@variable(name = AbstractUnityPlayer.PLAYER_SIZE, type = IType.FLOAT, init = "3.0", 
			doc = { @doc ("Size of the player for the display of the agent in GAMA")}), 
	@variable(name = AbstractUnityPlayer.PLAYER_ROTATION, type = IType.FLOAT, init = "90.0", 
	doc = { @doc ("Rotation (angle in degrees) to add to the player for the display of the agent in GAMA")}), 
	@variable(name = AbstractUnityPlayer.PLAYER_AGENTS_PERCEPTION_RADIUS, type = IType.FLOAT, init = "0.0", 
	doc = { @doc ("Allow to reduce the quantity of information sent to Unity - only the agents at a certain distance are sent")}), 
	@variable(name = AbstractUnityPlayer.PLAYER_AGENTS_MIN_DIST, type = IType.FLOAT, init = "0.0", 
			doc = { @doc ("Allow to not send to Unity agents that are to close (i.e. overlapping) ")})})
public class AbstractUnityPlayer extends GamlAgent{ 
	
	public static final String ACTION_CONE = "player_perception_cone";
	
	public static final String TO_DISPLAY = "to_display";
	public static final String SELECTED = "selected";
	public static final String CONE_DISTANCE = "cone_distance"; 
	public static final String CONE_AMPLITUDE = "cone_amplitude";
	public static final String PLAYER_AGENTS_PERCEPTION_RADIUS = "player_agents_perception_radius";
	public static final String PLAYER_AGENTS_MIN_DIST = "player_agents_min_dist";
	public static final String PLAYER_SIZE = "player_size";
	public static final String PLAYER_ROTATION = "player_rotation";
	public static final String ZOFFSET = "z_offset"; 
	
	
	public AbstractUnityPlayer(IPopulation<? extends IAgent> s, int index) {
		super(s, index);
	} 
	
	@getter (SELECTED)
	public static Boolean getSelected(final IAgent agent) {
		return (Boolean) agent.getAttribute(SELECTED);
	}
	@setter(SELECTED)
	public static void setSelected(final IAgent agent, final Boolean val) {
		agent.setAttribute(SELECTED, val);
	}
	@getter (TO_DISPLAY)
	public static Boolean getToDisplay(final IAgent agent) {
		return (Boolean) agent.getAttribute(TO_DISPLAY);
	}
	@setter(TO_DISPLAY)
	public static void setToDisplay(final IAgent agent, final Boolean val) {
		agent.setAttribute(TO_DISPLAY, val);
	}
	@getter (IKeyword.HEADING)
	public static Double getHeading(final IAgent agent) {
		return (Double) agent.getAttribute(IKeyword.HEADING);
	}
	@setter(IKeyword.HEADING)
	public static void setHeading(final IAgent agent, final Double val) {
		agent.setAttribute(IKeyword.HEADING, val);
	}
	
	@getter (IKeyword.COLOR)
	public static GamaColor getColor(final IAgent agent) {
		return (GamaColor) agent.getAttribute(IKeyword.COLOR);
	}
	@setter(IKeyword.COLOR)
	public static void setColor(final IAgent agent, final GamaColor val) {
		agent.setAttribute(IKeyword.COLOR, val);
	}
	
	@getter (ZOFFSET)
	public static Double getZOffset(final IAgent agent) {
		return (Double) agent.getAttribute(ZOFFSET);
	}
	@setter(ZOFFSET)
	public static void setZOffset(final IAgent agent, final Double val) {
		agent.setAttribute(ZOFFSET, val);
	}
	
	@getter (CONE_DISTANCE)
	public static Double getConeDistance(final IAgent agent) {
		return (Double) agent.getAttribute(CONE_DISTANCE);
	}
	@setter(CONE_DISTANCE)
	public static void setConeDistance(final IAgent agent, final Double val) {
		agent.setAttribute(CONE_DISTANCE, val);
	}
	
	@getter (PLAYER_ROTATION)
	public static Double getPlayerRotation(final IAgent agent) {
		return (Double) agent.getAttribute(PLAYER_ROTATION);
	}
	@setter(PLAYER_ROTATION)
	public static void setPlayerRotation(final IAgent agent, final Double val) {
		agent.setAttribute(PLAYER_ROTATION, val);
	}	
	
	@getter (PLAYER_SIZE)
	public static Double getPlayerSize(final IAgent agent) {
		return (Double) agent.getAttribute(PLAYER_SIZE);
	}
	@setter(PLAYER_SIZE)
	public static void setPlayerSize(final IAgent agent, final Double val) {
		agent.setAttribute(PLAYER_SIZE, val);
	}	
	
	@getter (CONE_AMPLITUDE)
	public static Double getConeAmplitude(final IAgent agent) {
		return (Double) agent.getAttribute(CONE_AMPLITUDE);
	}
	@setter(CONE_AMPLITUDE)
	public static void setConeAmplitude(final IAgent agent, final Double val) {
		agent.setAttribute(CONE_AMPLITUDE, val);
	}	
	
	@getter (PLAYER_AGENTS_PERCEPTION_RADIUS)
	public static Double getPlayerAgentPerceptionRadius(final IAgent agent) {
		return (Double) agent.getAttribute(PLAYER_AGENTS_PERCEPTION_RADIUS);
	}
	@setter(PLAYER_AGENTS_PERCEPTION_RADIUS)
	public static void setPlayerAgentPerceptionRadius(final IAgent agent, final Double val) {
		agent.setAttribute(PLAYER_AGENTS_PERCEPTION_RADIUS, val);
	}
		
	@getter (PLAYER_AGENTS_MIN_DIST)
	public static Double getPlayerAgentMinDist(final IAgent agent) {
		return (Double) agent.getAttribute(PLAYER_AGENTS_MIN_DIST);
	}
	@setter(PLAYER_AGENTS_MIN_DIST)
	public static void setPlayerAgentMinDist(final IAgent agent, final Double val) {
		agent.setAttribute(PLAYER_AGENTS_MIN_DIST, val);
	}
	
	@action (
			name = ACTION_CONE,
			doc = { @doc (
					value = "Wait for the connection of a unity client and send the paramters to the client")})
	public IShape primGetCone(final IScope scope) throws GamaRuntimeException {
		return getCone(scope, getAgent());
	}
	
	private static IShape getCone(IScope scope, IAgent agent) {
		Double rotation = getHeading(agent) * -1 ;
		Double cone_amplitude = getConeAmplitude(agent);
		IShape g = SpatialCreation.cone(scope, (int)(rotation - cone_amplitude/2),(int)(rotation + cone_amplitude/2));
		g = SpatialOperators.inter(scope, g, SpatialCreation.circle(scope, getConeDistance(agent)));
		g = SpatialTransformations.translated_by(scope, g, new GamaPoint(0,0,getZOffset(agent)));
		return g;
		
	}
	

}
