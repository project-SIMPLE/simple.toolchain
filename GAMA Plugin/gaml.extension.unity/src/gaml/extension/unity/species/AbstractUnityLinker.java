/*******************************************************************************************************
 *
 * AbstractUnityLinker.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.species;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.xtext.util.Strings;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.root.PlatformAgent;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMap;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.matrix.GamaField;
import gama.core.util.matrix.GamaMatrix;
import gama.extension.serialize.gaml.SerialisationOperators;
import gama.gaml.descriptions.ConstantExpressionDescription;
import gama.gaml.operators.Cast;
import gama.gaml.operators.spatial.SpatialCreation;
import gama.gaml.operators.spatial.SpatialPunctal;
import gama.gaml.operators.spatial.SpatialQueries;
import gama.gaml.operators.spatial.SpatialTransformations;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.IStatement.WithArgs;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gaml.extension.unity.types.UnityProperties;
import gaml.extension.unity.types.UnityPropertiesType;

/**
 * The Class AbstractUnityLinker.
 */

@species (
		name = "abstract_unity_linker",
		skills = { "network" })
@vars ({ @variable (
		name = AbstractUnityLinker.CONNECT_TO_UNITY,
		type = IType.BOOL,
		init = "true",
		doc = { @doc ("Activate the unity connection; if activated, the model will wait for an connection from Unity to start") }),
		@variable (
				name = AbstractUnityLinker.READY_TO_MOVE_PLAYER,
				type = IType.LIST,
				init = "[]",
				doc = { @doc ("list of players that are readdy to have their position updated from Unity") }),

		@variable (
				name = AbstractUnityLinker.MIN_NUMBER_PLAYERS,
				type = IType.INT,
				init = "1",
				doc = { @doc ("Number of Unity players required to start the simulation") }),
		@variable (
				name = AbstractUnityLinker.MAX_NUMBER_PLAYERS,
				type = IType.INT,
				init = "1",
				doc = { @doc ("Maximal number of Unity players") }),
		@variable (
				name = AbstractUnityLinker.MIN_PLAYER_POSITION_UPDATE_DURATION,
				type = IType.FLOAT,
				init = "0.01",
				doc = { @doc ("Minimum delay between two transmissions of a player's position from Unity") }),
		
		@variable (
				name = AbstractUnityLinker.PRECISION,
				type = IType.INT,
				init = "10000",
				doc = { @doc ("Number of decimal for the data (location, rotation)") }),

		@variable (
				name = AbstractUnityLinker.UNITY_PROPERTIES,
				type = IType.LIST,
				of = UnityPropertiesType.UNITYPROPERTIESTYPE_ID,
				doc = { @doc ("List of background geometries to sent to Unity.") }),

		@variable (
				name = AbstractUnityLinker.BACKGROUND_GEOMETRIES,
				type = IType.MAP,
				doc = { @doc ("Map of background geometries to sent to Unity with the unity properties to use.") }),
		@variable (
				name = AbstractUnityLinker.ATTRIBUTES_TO_SEND,
				type = IType.MAP,
				doc = { @doc ("List of attributes to sent to Unity") }),
		@variable (
				name = AbstractUnityLinker.GEOMETRIES_TO_SEND,
				type = IType.MAP,
				doc = { @doc ("List of geometries to sent to Unity with the unity properties to use. It could be updated each simulation step") }),

		@variable (
				name = AbstractUnityLinker.GEOMETRIES_TO_KEEP,
				type = IType.LIST,
				doc = { @doc ("List of geometries to keep in Unity") }),
		@variable (
				name = AbstractUnityLinker.DO_SEND_WORLD,
				type = IType.BOOL,
				init = "true",
				doc = { @doc ("Has the agents has to be sent to unity?") }),

		@variable (
				name = AbstractUnityLinker.INITIALIZED,
				type = IType.BOOL,
				init = "false",
				doc = { @doc ("Has the world being initialized yet?") }),

		@variable (
				name = AbstractUnityLinker.PLAYER_SPECIES,
				type = IType.STRING,
				doc = { @doc ("Species of the player agent") }),
		@variable (
				name = AbstractUnityLinker.PLAYER_UNITY_PROPERTIES,
						type = IType.LIST,
						of = UnityPropertiesType.UNITYPROPERTIESTYPE_ID,
						
			//	type = UnityPropertiesType.UNITYPROPERTIESTYPE_ID,
				doc = { @doc ("Properties used to send the player agent geometry to Unity - if nil/empty, the player agents are not sent") }),

		@variable (
				name = AbstractUnityLinker.END_MESSAGE_SYMBOL,
				type = IType.STRING,
				init = "'|||'",
				doc = { @doc ("Symbol to be added at the end of the messages (only when the middleware is not used); it should be the same defined in Unity") }),

		@variable (
				name = AbstractUnityLinker.RECEIVE_INFORMATION,
				type = IType.BOOL,
				init = "false",
				doc = { @doc ("should GAMA receive information from Unity?") }),

		@variable (
				name = AbstractUnityLinker.MOVE_PLAYER_EVENT,
				type = IType.BOOL,
				init = "false",
				doc = { @doc ("Does the player agent moved from GAMA?") }),

		@variable (
				name = AbstractUnityLinker.MOVE_PLAYER_FROM_UNITY,
				type = IType.BOOL,
				init = "true",
				doc = { @doc ("Has the player to move in GAMA as it moves in Unity?") }),

		@variable (
				name = AbstractUnityLinker.USE_MIDDLEWARE,
				type = IType.BOOL,
				init = "true",
				doc = { @doc ("Use of the middleware to connect Unity and GAMA? Direct connection is only usable for 1 player game") }),

		@variable (
				name = AbstractUnityLinker.NEW_PLAYER_POSITION,
				type = IType.MAP,
				doc = { @doc ("The new poistion of the player to be sent to Unity - map with key: agent name, value: list of int [x,y]") }),
		@variable (
				name = AbstractUnityLinker.DISTANCE_PLAYER_SELECTION,
				type = IType.FLOAT,
				init = "2.0",
				doc = { @doc ("Maximal distance to select a player agent") }),
		@variable (
				name = AbstractUnityLinker.INIT_LOCATIONS,
				type = IType.LIST,
				of = IType.POINT,
				doc = { @doc ("Init locations of the player agents in the environment - this information will be sent to Unity to move the players accordingly") }),

		@variable (
				name = AbstractUnityLinker.THE_PLAYERS,
				type = IType.MAP,
				doc = { @doc ("Player agents indexes by their name") }), })
public class AbstractUnityLinker extends GamlAgent {

	/** The Constant PLAYER_SPECIES. */
	public static final String PLAYER_SPECIES = "player_species";

	/** The Constant PLAYER_UNITY_PROPERTIES. */
	public static final String PLAYER_UNITY_PROPERTIES = "player_unity_properties";

	/** The Constant MIN_NUMBER_PLAYERS. */
	public static final String MIN_NUMBER_PLAYERS = "min_num_players";

	/** The Constant MAX_NUMBER_PLAYERS. */
	public static final String MAX_NUMBER_PLAYERS = "max_num_players";
	
	public static final String MIN_PLAYER_POSITION_UPDATE_DURATION = "min_player_position_update_duration";

	/** The Constant CONNECT_TO_UNITY. */
	public static final String CONNECT_TO_UNITY = "connect_to_unity";

	/** The Constant PRECISION. */
	public static final String PRECISION = "precision";

	/** The Constant GEOMETRIES_TO_KEEP. */
	public static final String GEOMETRIES_TO_KEEP = "geometries_to_keep";

	/** The Constant ATTIBUTES_TO_SEND. */
	public static final String ATTRIBUTES_TO_SEND = "attributes_to_send";
	
	/** The Constant GEOMETRIES_TO_SEND. */
	public static final String GEOMETRIES_TO_SEND = "geometries_to_send";

	/** The Constant BACKGROUND_GEOMETRIES. */
	public static final String BACKGROUND_GEOMETRIES = "background_geometries";

	/** The Constant UNITY_PROPERTIES. */
	public static final String UNITY_PROPERTIES = "unity_properties";

	/** The Constant DO_SEND_WORLD. */
	public static final String DO_SEND_WORLD = "do_send_world";

	/** The Constant GROUND_DEPTH. */
	public static final String GROUND_DEPTH = "ground_depth";

	/** The Constant END_MESSAGE_SYMBOL. */
	public static final String END_MESSAGE_SYMBOL = "end_message_symbol";

	/** The Constant MOVE_PLAYER_EVENT. */
	public static final String MOVE_PLAYER_EVENT = "move_player_event";

	/** The Constant USE_MIDDLEWARE. */
	public static final String USE_MIDDLEWARE = "use_middleware";

	/** The Constant MOVE_PLAYER_FROM_UNITY. */
	public static final String MOVE_PLAYER_FROM_UNITY = "move_player_from_unity";

	/** The Constant INIT_LOCATIONS. */
	public static final String INIT_LOCATIONS = "init_locations";

	/** The Constant THE_PLAYERS. */
	public static final String THE_PLAYERS = "player_agents";

	/** The Constant NEW_PLAYER_POSITION. */
	public static final String NEW_PLAYER_POSITION = "new_player_position";

	/** The Constant DISTANCE_PLAYER_SELECTION. */
	public static final String DISTANCE_PLAYER_SELECTION = "distance_player_selection";

	/** The Constant INITIALIZED. */
	public static final String INITIALIZED = "initialized";

	/** The Constant RECEIVE_INFORMATION. */
	public static final String RECEIVE_INFORMATION = "receive_information";

	/** The Constant WAITING_MESSAGE. */
	public static final String WAITING_MESSAGE = "ready";

	/** The Constant READY_TO_MOVE_PLAYER. */
	public static final String READY_TO_MOVE_PLAYER = "ready_to_move_player";

	/** The Constant HEADING. */
	public static final String HEADING = "heading";

	/** The Constant ADD_TO_MAP. */
	public static final String ADD_TO_MAP = "add_to_map";

	/** The Constant LOC_TO_SEND. */
	public static final String LOC_TO_SEND = "loc_to_send";

	/** The Constant TO_MAP. */
	public static final String TO_MAP = "to_map";

	/** The Constant SPECIES_INDEX. */
	public static final String SPECIES_INDEX = "index";

	/** The Constant CONTENTS. */
	public static final String CONTENTS = "contents";

	/** The Constant ID. */
	public static final String ID = "id";

	/** The Constant CONTENT_MESSAGE. */
	public static final String CONTENT_MESSAGE = "contents";

	/** The Constant OUTPUT. */
	public static final String OUTPUT = "output";

	/** The Constant TYPE. */
	public static final String TYPE = "type";

	/** The Constant SERVER. */
	public static final String SERVER = "server";

	/** The current message. */
	private IMap currentMessage;

	/** The geometries to follow. */
	private IMap<String, IShape> geometriesToFollow;
	
	private IMap<String, UnityProperties> propertiesForPlayer;
	

	private IList<String> playersInitialized;

	/**
	 * Gets the distance selection.
	 *
	 * @param agent
	 *            the agent
	 * @return the distance selection
	 */
	@getter (AbstractUnityLinker.DISTANCE_PLAYER_SELECTION)
	public static Double getDistanceSelection(final IAgent agent) {
		return (Double) agent.getAttribute(DISTANCE_PLAYER_SELECTION);
	}

	/**
	 * Sets the distance selection.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.DISTANCE_PLAYER_SELECTION)
	public static void setDistanceSelection(final IAgent agent, final Double val) {
		agent.setAttribute(DISTANCE_PLAYER_SELECTION, val);
	}

	/**
	 * Gets the connect to unity.
	 *
	 * @param agent
	 *            the agent
	 * @return the connect to unity
	 */
	@getter (AbstractUnityLinker.CONNECT_TO_UNITY)
	public static Boolean getConnectToUnity(final IAgent agent) {
		return (Boolean) agent.getAttribute(CONNECT_TO_UNITY);
	}

	/**
	 * Sets the connect to unity.
	 *
	 * @param agent
	 *            the agent
	 * @param ctu
	 *            the ctu
	 */
	@setter (AbstractUnityLinker.CONNECT_TO_UNITY)
	public static void setConnectToUnity(final IAgent agent, final Boolean ctu) {
		agent.setAttribute(CONNECT_TO_UNITY, ctu);
	}

	/**
	 * Gets the use middleware.
	 *
	 * @param agent
	 *            the agent
	 * @return the use middleware
	 */
	@getter (AbstractUnityLinker.USE_MIDDLEWARE)
	public static Boolean getUseMiddleware(final IAgent agent) {
		return (Boolean) agent.getAttribute(USE_MIDDLEWARE);
	}

	/**
	 * Sets the use middleware.
	 *
	 * @param agent
	 *            the agent
	 * @param ctu
	 *            the ctu
	 */
	@setter (AbstractUnityLinker.USE_MIDDLEWARE)
	public static void setUseMiddleware(final IAgent agent, final Boolean ctu) {
		agent.setAttribute(USE_MIDDLEWARE, ctu);
	}

	/**
	 * Gets the min player.
	 *
	 * @param agent
	 *            the agent
	 * @return the min player
	 */
	@getter (AbstractUnityLinker.MIN_NUMBER_PLAYERS)
	public static Integer getMinPlayer(final IAgent agent) {
		return (Integer) agent.getAttribute(MIN_NUMBER_PLAYERS);
	}

	/**
	 * Sets the min player.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.MIN_NUMBER_PLAYERS)
	public static void setMinPlayer(final IAgent agent, final Integer val) {
		agent.setAttribute(MIN_NUMBER_PLAYERS, val);
	}

	/**
	 * Gets the max player.
	 *
	 * @param agent
	 *            the agent
	 * @return the max player
	 */
	@getter (AbstractUnityLinker.MAX_NUMBER_PLAYERS)
	public static Integer getMaxPlayer(final IAgent agent) {
		return (Integer) agent.getAttribute(MAX_NUMBER_PLAYERS);
	}

	/**
	 * Sets the max player.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.MAX_NUMBER_PLAYERS)
	public static void setMaxPlayer(final IAgent agent, final Integer val) {
		agent.setAttribute(MAX_NUMBER_PLAYERS, val);
	}
	
	
	@getter (AbstractUnityLinker.MIN_PLAYER_POSITION_UPDATE_DURATION)
	public static Double getMinPlayerPoisitionUpdateDuration(final IAgent agent) {
		return (Double) agent.getAttribute(MIN_PLAYER_POSITION_UPDATE_DURATION);
	}

	@setter (AbstractUnityLinker.MIN_PLAYER_POSITION_UPDATE_DURATION)
	public static void setMinPlayerPoisitionUpdateDuration(final IAgent agent, final Double val) {
		agent.setAttribute(MIN_PLAYER_POSITION_UPDATE_DURATION, val);
	}


	/**
	 * Gets the precision.
	 *
	 * @param agent
	 *            the agent
	 * @return the precision
	 */
	@getter (AbstractUnityLinker.PRECISION)
	public static Integer getPrecision(final IAgent agent) {
		return (Integer) agent.getAttribute(PRECISION);
	}

	/**
	 * Sets the precision.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.PRECISION)
	public static void setPrecision(final IAgent agent, final Integer val) {
		agent.setAttribute(PRECISION, val);
	}

	/**
	 * Gets the unity properties.
	 *
	 * @param agent
	 *            the agent
	 * @return the unity properties
	 */
	@getter (AbstractUnityLinker.UNITY_PROPERTIES)
	public static IList<UnityProperties> getUnityProperties(final IShape agent) {
		return (IList<UnityProperties>) agent.getAttribute(UNITY_PROPERTIES);
	}

	/**
	 * Sets the unity properties.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.UNITY_PROPERTIES)
	public static void setUnityProperties(final IAgent agent, final IList<UnityProperties> val) {
		agent.setAttribute(UNITY_PROPERTIES, val);
	}

	/**
	 * Gets the background geometries.
	 *
	 * @param agent
	 *            the agent
	 * @return the background geometries
	 */
	@getter (AbstractUnityLinker.BACKGROUND_GEOMETRIES)
	public static IMap<IShape, UnityProperties> getBackgroundGeometries(final IShape agent) {
		return (IMap<IShape, UnityProperties>) agent.getAttribute(BACKGROUND_GEOMETRIES);
	}

	/**
	 * Sets the background geometries.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.BACKGROUND_GEOMETRIES)
	public static void setBackgroundGeometries(final IAgent agent, final IMap<IShape, UnityProperties> val) {
		agent.setAttribute(BACKGROUND_GEOMETRIES, val);
	}

	
	/**
	 * Gets the attributes to send.
	 *
	 * @param agent
	 *            the agent
	 * @return the attributes to send
	 */
	@getter (AbstractUnityLinker.ATTRIBUTES_TO_SEND)
	public static IMap<IShape, IMap<String,Object>> getAttributesToSend(final IAgent agent) {
		return (IMap<IShape, IMap<String,Object>>) agent.getAttribute(ATTRIBUTES_TO_SEND);
	}

	/**
	 * Sets the attributes to send.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.ATTRIBUTES_TO_SEND)
	public static void setAttributesToSend(final IAgent agent, final IMap<IShape, IMap<String,Object>> val) {
		agent.setAttribute(ATTRIBUTES_TO_SEND, val);
	}
	
	
	/**
	 * Gets the geometries to send.
	 *
	 * @param agent
	 *            the agent
	 * @return the geometries to send
	 */
	@getter (AbstractUnityLinker.GEOMETRIES_TO_SEND)
	public static IMap<IShape, UnityProperties> getGeometriesToSend(final IAgent agent) {
		return (IMap<IShape, UnityProperties>) agent.getAttribute(GEOMETRIES_TO_SEND);
	}

	/**
	 * Sets the geometries to send.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.GEOMETRIES_TO_KEEP)
	public static void setGeometriesToKeep(final IAgent agent, final IList<IShape> val) {
		agent.setAttribute(GEOMETRIES_TO_KEEP, val);
	}
	
	/**
	 * Gets the geometries to send.
	 *
	 * @param agent
	 *            the agent
	 * @return the geometries to send
	 */
	@getter (AbstractUnityLinker.GEOMETRIES_TO_KEEP)
	public static IList<IShape> getGeometriesToKeep(final IAgent agent) {
		return (IList<IShape>) agent.getAttribute(GEOMETRIES_TO_KEEP);
	}

	/**
	 * Sets the geometries to send.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.GEOMETRIES_TO_SEND)
	public static void setGeometriesToSend(final IAgent agent, final IMap<IShape, UnityProperties> val) {
		agent.setAttribute(GEOMETRIES_TO_SEND, val);
	}

	/**
	 * Gets the end message symbol.
	 *
	 * @param agent
	 *            the agent
	 * @return the end message symbol
	 */
	@getter (AbstractUnityLinker.END_MESSAGE_SYMBOL)
	public static String getEndMessageSymbol(final IAgent agent) {
		return (String) agent.getAttribute(END_MESSAGE_SYMBOL);
	}

	/**
	 * Sets the end message symbol.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.END_MESSAGE_SYMBOL)
	public static void setEndMessageSymbol(final IAgent agent, final String val) {
		agent.setAttribute(END_MESSAGE_SYMBOL, val);
	}

	@getter (AbstractUnityLinker.PLAYER_UNITY_PROPERTIES)
	public static List<UnityProperties> getPlayerUnityProperties(final IAgent agent) {
	//public static UnityProperties getPlayerUnityProperties(final IAgent agent) {
	//	return (UnityProperties) agent.getAttribute(PLAYER_UNITY_PROPERTIES);
		return ((List<UnityProperties>) agent.getAttribute(PLAYER_UNITY_PROPERTIES));
	}

	@setter (AbstractUnityLinker.PLAYER_UNITY_PROPERTIES)
	//public static void setPlayerUnityProperties(final IAgent agent, final UnityProperties val) {
	public static void setPlayerUnityProperties(final IAgent agent, final List<UnityProperties> val) {
		agent.setAttribute(PLAYER_UNITY_PROPERTIES, val);
	}

	
	/**
	 * Gets the player species.
	 *
	 * @param agent
	 *            the agent
	 * @return the player species
	 */
	@getter (AbstractUnityLinker.PLAYER_SPECIES)
	public static String getPlayerSpecies(final IAgent agent) {
		return (String) agent.getAttribute(PLAYER_SPECIES);
	}

	/**
	 * Sets the player species.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.PLAYER_SPECIES)
	public static void setPlayerSpecies(final IAgent agent, final String val) {
		agent.setAttribute(PLAYER_SPECIES, val);
	}

	/**
	 * Gets the do send world.
	 *
	 * @param agent
	 *            the agent
	 * @return the do send world
	 */
	@getter (AbstractUnityLinker.DO_SEND_WORLD)
	public static Boolean getDoSendWorld(final IAgent agent) {
		return (Boolean) agent.getAttribute(DO_SEND_WORLD);
	}

	/**
	 * Sets the do send world.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.DO_SEND_WORLD)
	public static void setDoSendWorld(final IAgent agent, final Boolean val) {
		agent.setAttribute(DO_SEND_WORLD, val);
	}

	/**
	 * Gets the receive information.
	 *
	 * @param agent
	 *            the agent
	 * @return the receive information
	 */
	@getter (AbstractUnityLinker.RECEIVE_INFORMATION)
	public static Boolean getReceiveInformation(final IAgent agent) {
		return (Boolean) agent.getAttribute(RECEIVE_INFORMATION);
	}

	/**
	 * Sets the receive information.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.RECEIVE_INFORMATION)
	public static void setReceiveInformation(final IAgent agent, final Boolean val) {
		agent.setAttribute(RECEIVE_INFORMATION, val);
	}

	/**
	 * Gets the initialized.
	 *
	 * @param agent
	 *            the agent
	 * @return the initialized
	 */
	@getter (AbstractUnityLinker.INITIALIZED)
	public static Boolean getInitialized(final IAgent agent) {
		return (Boolean) agent.getAttribute(INITIALIZED);
	}

	/**
	 * Sets the initialized.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.INITIALIZED)
	public static void setInitialized(final IAgent agent, final Boolean val) {
		agent.setAttribute(INITIALIZED, val);
	}

	/**
	 * Gets the move player event.
	 *
	 * @param agent
	 *            the agent
	 * @return the move player event
	 */
	@getter (AbstractUnityLinker.MOVE_PLAYER_EVENT)
	public static Boolean getMovePlayerEvent(final IAgent agent) {
		return (Boolean) agent.getAttribute(MOVE_PLAYER_EVENT);
	}

	/**
	 * Sets the move player event.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.MOVE_PLAYER_EVENT)
	public static void setMovePlayerEvent(final IAgent agent, final Boolean val) {
		agent.setAttribute(MOVE_PLAYER_EVENT, val);
	}

	/**
	 * Gets the move player from unity.
	 *
	 * @param agent
	 *            the agent
	 * @return the move player from unity
	 */
	@getter (AbstractUnityLinker.MOVE_PLAYER_FROM_UNITY)
	public static Boolean getMovePlayerFromUnity(final IAgent agent) {
		return (Boolean) agent.getAttribute(MOVE_PLAYER_FROM_UNITY);
	}

	/**
	 * Sets the move player from unity.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.MOVE_PLAYER_FROM_UNITY)
	public static void setMovePlayerFromUnity(final IAgent agent, final Boolean val) {
		agent.setAttribute(MOVE_PLAYER_FROM_UNITY, val);
	}

	/**
	 * Gets the player location init.
	 *
	 * @param agent
	 *            the agent
	 * @return the player location init
	 */
	@getter (AbstractUnityLinker.INIT_LOCATIONS)
	public static IList<GamaPoint> getPlayerLocationInit(final IAgent agent) {
		return (IList<GamaPoint>) agent.getAttribute(INIT_LOCATIONS);
	}

	/**
	 * Sets the player location init.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.INIT_LOCATIONS)
	public static void setPlayerLocationInit(final IAgent agent, final IList val) {
		agent.setAttribute(INIT_LOCATIONS, val);
	}

	/**
	 * Gets the ready to move players.
	 *
	 * @param agent
	 *            the agent
	 * @return the ready to move players
	 */
	@getter (AbstractUnityLinker.READY_TO_MOVE_PLAYER)
	public static IList<IAgent> getReadyToMovePlayers(final IAgent agent) {
		return (IList<IAgent>) agent.getAttribute(READY_TO_MOVE_PLAYER);
	}

	/**
	 * Sets the ready to move players.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.READY_TO_MOVE_PLAYER)
	public static void setReadyToMovePlayers(final IAgent agent, final IList val) {
		agent.setAttribute(READY_TO_MOVE_PLAYER, val);
	}

	/**
	 * Gets the players.
	 *
	 * @param agent
	 *            the agent
	 * @return the players
	 */
	@getter (AbstractUnityLinker.THE_PLAYERS)
	public static IMap<String, IAgent> getPlayers(final IAgent agent) {
		return (IMap<String, IAgent>) agent.getAttribute(THE_PLAYERS);
	}

	/**
	 * Sets the players.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.THE_PLAYERS)
	public static void setPlayers(final IAgent agent, final IMap<String, IAgent> val) {
		agent.setAttribute(THE_PLAYERS, val);
	}

	/**
	 * Gets the new player position.
	 *
	 * @param agent
	 *            the agent
	 * @return the new player position
	 */
	@getter (AbstractUnityLinker.NEW_PLAYER_POSITION)
	public static IMap<String, IList<Integer>> getNewPlayerPosition(final IAgent agent) {
		return (IMap<String, IList<Integer>>) agent.getAttribute(NEW_PLAYER_POSITION);
	}

	/**
	 * Sets the new player position.
	 *
	 * @param agent
	 *            the agent
	 * @param val
	 *            the val
	 */
	@setter (AbstractUnityLinker.NEW_PLAYER_POSITION)
	public static void setNewPlayerPosition(final IAgent agent, final IMap<String, IList<Integer>> val) {
		agent.setAttribute(NEW_PLAYER_POSITION, val);
	}

	/**
	 * Instantiates a new abstract unity linker.
	 *
	 * @param s
	 *            the s
	 * @param index
	 *            the index
	 */
	public AbstractUnityLinker(final IPopulation<? extends IAgent> s, final int index) {
		super(s, index);

	}

	@Override
	public void dispose() {

		super.dispose();
	}

	/**
	 * Do action no arg.
	 *
	 * @param scope
	 *            the scope
	 * @param actionName
	 *            the action name
	 * @return the object
	 */
	private Object doActionNoArg(final IScope scope, final String actionName) {
		WithArgs act = getAgent().getSpecies().getAction(actionName);
		return act.executeOn(scope);
	}

	/**
	 * Do action 1 arg.
	 *
	 * @param scope
	 *            the scope
	 * @param actionName
	 *            the action name
	 * @param argName
	 *            the arg name
	 * @param ArgVal
	 *            the arg val
	 * @return the object
	 */
	private Object doAction1Arg(final IScope scope, final String actionName, final String argName,
			final Object ArgVal) {
		Arguments args = new Arguments();
		args.put(argName, ConstantExpressionDescription.createNoCache(ArgVal));
		WithArgs act = getAgent().getSpecies().getAction(actionName);
		act.setRuntimeArgs(scope, args);
		return act.executeOn(scope);
	}

	/**
	 * Do action 2 arg.
	 *
	 * @param scope
	 *            the scope
	 * @param actionName
	 *            the action name
	 * @param argName1
	 *            the arg name 1
	 * @param ArgVal1
	 *            the arg val 1
	 * @param argName2
	 *            the arg name 2
	 * @param ArgVal2
	 *            the arg val 2
	 * @return the object
	 */
	private Object doAction2Arg(final IScope scope, final String actionName, final String argName1,
			final Object ArgVal1, final String argName2, final Object ArgVal2) {
		Arguments args = new Arguments();
		args.put(argName1, ConstantExpressionDescription.createNoCache(ArgVal1));
		args.put(argName2, ConstantExpressionDescription.createNoCache(ArgVal2));
		WithArgs act = getAgent().getSpecies().getAction(actionName);
		act.setRuntimeArgs(scope, args);
		return act.executeOn(scope);
	}

	/**
	 * Do action 3 arg.
	 *
	 * @param scope
	 *            the scope
	 * @param actionName
	 *            the action name
	 * @param argName1
	 *            the arg name 1
	 * @param ArgVal1
	 *            the arg val 1
	 * @param argName2
	 *            the arg name 2
	 * @param ArgVal2
	 *            the arg val 2
	 * @param argName3
	 *            the arg name 3
	 * @param ArgVal3
	 *            the arg val 3
	 * @return the object
	 */
	private Object doAction3Arg(final IScope scope, final String actionName, final String argName1,
			final Object ArgVal1, final String argName2, final Object ArgVal2, final String argName3,
			final Object ArgVal3) {
		Arguments args = new Arguments();
		args.put(argName1, ConstantExpressionDescription.createNoCache(ArgVal1));
		args.put(argName2, ConstantExpressionDescription.createNoCache(ArgVal2));
		args.put(argName3, ConstantExpressionDescription.createNoCache(ArgVal3));
		WithArgs act = getAgent().getSpecies().getAction(actionName);
		act.setRuntimeArgs(scope, args);
		return act.executeOn(scope);
	}
	private Object doAction4Arg(final IScope scope, final String actionName, final String argName1,
			final Object ArgVal1, final String argName2, final Object ArgVal2, final String argName3,
			final Object ArgVal3, final String argName4, final Object ArgVal4) {
		Arguments args = new Arguments();
		args.put(argName1, ConstantExpressionDescription.createNoCache(ArgVal1));
		args.put(argName2, ConstantExpressionDescription.createNoCache(ArgVal2));
		args.put(argName3, ConstantExpressionDescription.createNoCache(ArgVal3));
		args.put(argName4, ConstantExpressionDescription.createNoCache(ArgVal4));
		WithArgs act = getAgent().getSpecies().getAction(actionName);
		act.setRuntimeArgs(scope, args);
		return act.executeOn(scope);
	}

	@Override
	public Object _init_(final IScope scope) {
		Object init = super._init_(scope);
		startSimulation(scope);
		return init;
	}

	/**
	 * Interaction with player.
	 *
	 * @param scope
	 *            the scope
	 * @param ag
	 *            the ag
	 */
	private void interactionWithPlayer(final IScope scope, final IAgent ag) {
		GamaPoint pt = scope.getGui().getMouseLocationInModel();
		if (pt != null) {
			IList<IAgent> ags = GamaListFactory.create();
			for (String playerName : getPlayers(ag).keySet()) {
				IAgent a = getPlayers(ag).get(playerName);
				if (a == null) {
					getPlayers(ag).remove(playerName);
					continue;
				}
				if (a.euclidianDistanceTo(pt) <= getDistanceSelection(ag)) { ags.add(a); }
			}
			if (ags.isEmpty()) {
				Optional<IAgent> selected = getPlayers(ag).getValues().stream()
						.filter(a -> (Boolean) a.getAttribute("selected")).findFirst();
				if (selected.isPresent()) {
					IAgent pl = selected.get();
					pt.z = pl.getLocation().z;
					doAction2Arg(scope, "move_player", "player",pl , "loc", pt); }
			} else {
				IAgent player = (IAgent) SpatialQueries.closest_to(scope, ags, pt);

				player.setAttribute(AbstractUnityPlayer.SELECTED,
						!((Boolean) player.getAttribute(AbstractUnityPlayer.SELECTED)));
			}

		}
	}

	@Override
	public boolean doStep(final IScope scope) {
		if (super.doStep(scope)) {
			IAgent ag = getAgent();
			// setInitialized(ag, true);
			if (getConnectToUnity(ag)) {
				if (/* getInitialized(ag) && */getMovePlayerEvent(ag) && !getPlayers(ag).isEmpty()) {
					setMovePlayerEvent(ag, false);
					interactionWithPlayer(scope, ag);
				}
				// if(getInitialized(ag)) {
				if (getDoSendWorld(ag) && !getPlayers(ag).isEmpty())  { doActionNoArg(scope, "send_world"); }

				// }

				if (currentMessage != null && !currentMessage.isEmpty()) { sendCurrentMessage(scope); }

			}
			return true;
		}
		return false;
	}

	/**
	 * Send current message.
	 *
	 * @param scope
	 *            the scope
	 */
	private void sendCurrentMessage(final IScope scope) {
		PlatformAgent pa = GAMA.getPlatformAgent();
		String mes = "";
		if (getUseMiddleware(getAgent())) {
			
			mes = SerialisationOperators.toJson(scope, currentMessage, false);
			
			pa.sendMessage(scope, ConstantExpressionDescription.createNoCache(mes));
		} else {
			Iterator<IMap> it =
					(Iterator<IMap>) ((IList<IMap>) currentMessage.get(CONTENTS)).iterable(scope).iterator();
			while (it.hasNext()) {
				IMap v = it.next();
				Object c = v.get(CONTENT_MESSAGE);
				mes += SerialisationOperators.toJson(scope, c, false) + "|||";
			}
			if (!mes.isBlank() && !"{}".equals(mes)) {
				try {
					pa.sendMessage(scope, ConstantExpressionDescription.createNoCache(mes));
				} catch (WebsocketNotConnectedException e) {
					if (!getUseMiddleware(getAgent())) {
						getPlayers(pa).get(0).dispose();
						getPlayers(pa).clear();
					}
				}
			}
		}
		currentMessage.clear();

	}

	
	@action ( 
			name = "send_current_message",
			doc = { @doc (
					value = "send the current message to the Unity Client") })
	public void primSentCurrentMessage(final IScope scope) throws GamaRuntimeException {
		sendCurrentMessage(scope);
	}
		
	/**
	 * Adds the to current message.
	 *
	 * @param scope
	 *            the scope
	 * @param recipients
	 *            the recipients
	 * @param content
	 *            the content
	 */
	private void addToCurrentMessage(final IScope scope, final IList<String> recipients, final Object content) {

		if (currentMessage == null) {

			currentMessage = GamaMapFactory.synchronizedMap(GamaMapFactory.create());
		}
		if (currentMessage.isEmpty()) {
			currentMessage.put(CONTENTS, GamaListFactory.create());
			currentMessage.put(TYPE, OUTPUT);
		}
		IMap newMessage = GamaMapFactory.create();
		newMessage.put(ID, recipients);
		newMessage.put(CONTENT_MESSAGE, content);
		((IList) currentMessage.get(CONTENTS)).add(newMessage);
	}

	/**
	 * Prim sent message.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action ( 
			name = "send_message",
			args = { @arg (
					name = "players",
					type = IType.LIST,
					doc = @doc ("Players to send the geometries to")),
					@arg (
							name = "mes",
							type = IType.MAP,
							doc = @doc ("Map to send")) },
			doc = { @doc (
					value = "send a message to the Unity Client") })
	public void primSentMessage(final IScope scope) throws GamaRuntimeException {
		IAgent ag = getAgent();
		IList<IAgent> players = (IList) scope.getListArg("players");
		IMap mes = (IMap) scope.getArg("mes", IType.MAP);

		IMap message = GamaMapFactory.create();
		message.put(CONTENTS, GamaListFactory.create());
		message.put(TYPE, OUTPUT);

		IMap newMessage = GamaMapFactory.create();

		IList<String> recipients = GamaListFactory.create();
		for (IAgent a : players) { recipients.add(a.getName()); }
		newMessage.put(ID, recipients);
		newMessage.put(CONTENT_MESSAGE, mes);
		((IList) message.get(CONTENTS)).add(newMessage);

		PlatformAgent pa = GAMA.getPlatformAgent();
		String mesStr = SerialisationOperators.toJson(scope, message, false);
		pa.sendMessage(scope, ConstantExpressionDescription.createNoCache(mesStr));

	}
	
	@action (
			name = "end_of_game",
			args = { @arg (
					name = "mes",
					type = IType.STRING,
					doc = @doc ("Message to display for the Unity client"))},
			doc = { @doc (
					value = "put an end to the game and restart the game") })
	public void primEndOfGame(final IScope scope) {
		currentMessage.clear();
		String message = scope.getStringArg("mes");
		IAgent ag = getAgent();
		IMap<String, Object> toSend = GamaMapFactory.create();
		Set<String> players = getPlayers(ag).keySet();

		for (String playerName : players) {
			IAgent player = getPlayers(ag).get(playerName);
			if (player == null) {
				getPlayers(ag).remove(playerName);
				continue;
			}
		}
		if (!getPlayers(ag).isEmpty()) {
			toSend.put("endOfGame", message);
			addToCurrentMessage(scope, getPlayers(ag).getKeys(), toSend);
			sendCurrentMessage(scope);
		}

		GAMA.pauseFrontmostExperiment(true);
		GAMA.reloadFrontmostExperiment(true);

	}

	/**
	 * Prim sent world.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "send_world",
			doc = { @doc (
					value = "send the current state of the world to the Unity clients") })
	public void primSentWorld(final IScope scope) throws GamaRuntimeException {
		IAgent ag = getAgent();
		IMap<String, Object> toSend = GamaMapFactory.create();
		Set<String> players = getPlayers(ag).keySet();

		for (String playerName : players) {
			if (playersInitialized == null || !playersInitialized.contains(playerName)) continue;
			IAgent player = getPlayers(ag).get(playerName);
			if (player == null) {
				getPlayers(ag).remove(playerName);
				continue;
			}
			IList<IShape> geomsKeep = getGeometriesToKeep(ag);
			
			
			
			IMap<IShape, UnityProperties> geoms = getGeometriesToSend(ag);
						
				
				if (propertiesForPlayer !=null && !propertiesForPlayer.isEmpty() && getPlayers(ag).size() > 1) {
					if (geoms == null) {
						geoms = GamaMapFactory.create();
					} else {
						geoms = geoms.copy(scope);
					}
					for (IAgent p : getPlayers(ag).getValues()) {
						if (!p.getName().equals(playerName)) {
							UnityProperties up = propertiesForPlayer.get(p.getName());
							if (up != null) {
								geoms.put(p, up);
								
							}
						}
					}
				}
				if (geoms != null) {
					boolean filterDist = player
							.getAttribute(AbstractUnityPlayer.PLAYER_AGENTS_PERCEPTION_RADIUS) != null
							&& (Double) player.getAttribute(AbstractUnityPlayer.PLAYER_AGENTS_PERCEPTION_RADIUS) > 0;

					boolean filterProx = player.getAttribute(AbstractUnityPlayer.PLAYER_AGENTS_MIN_DIST) != null
							&& (Double) player.getAttribute(AbstractUnityPlayer.PLAYER_AGENTS_MIN_DIST) > 0;

					if (filterDist || filterProx) {
						IList<IShape> geomsPl = GamaListFactory.create();
						geomsPl.addAll(geoms.keySet());
						if (filterDist) {
							geomsPl = (IList<IShape>) doAction1Arg(scope, "filter_distance", "geometries", geomsPl);
						}
						if (filterProx) {
							geomsPl = (IList<IShape>) doAction1Arg(scope, "filter_overlapping", "geometries", geomsPl);
						}
						geoms = GamaMapFactory.create();
						IMap<IShape, UnityProperties> geoms2 = getGeometriesToSend(ag);

						for (IShape s : geomsPl) { geoms.put(s, geoms2.get(s)); }
					}

					doAction4Arg(scope, "send_geometries", "player", player, "geoms", geoms, "update_position",
							getNewPlayerPosition(ag) !=null	&& getNewPlayerPosition(ag).get(player.getName()) !=null&&!getNewPlayerPosition(ag).get(player.getName()).isEmpty(), "is_init", false);

				}
				if (geomsKeep != null && !geomsKeep.isEmpty()) {
					if (geoms == null) 
						geoms = GamaMapFactory.create();
					for(IShape s : geomsKeep)
						geoms.put(s, null);
				}
			
			if (! toSend.isEmpty())
				addToCurrentMessage(scope, buildPlayerListfor1Player(scope, player), toSend);

			doAction1Arg(scope, "after_sending_world", "map_to_send", toSend);
		}
		if (getGeometriesToSend(ag) != null)
			getGeometriesToSend(ag).clear();
		if (getGeometriesToKeep(ag) != null)
			getGeometriesToKeep(ag).clear();
		if (getAttributesToSend(ag) != null)
			getAttributesToSend(ag).clear();
		
	}
	
	@action (
			name = "send_teleport_area",
			args = { @arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("Player to which the message will be sent")),
					@arg (
							name = "id",
							type = IType.STRING,
							doc = @doc ("id of the teleoprtation area")),
				@arg (
							name = "geoms",
							type = IType.LIST,
							doc = @doc ("list of geometries of the walls")) },
			doc = { @doc (
					value = "send a geometry to be used a teleportation area. "
							+ "If a telepoprtation with the same id already existed,"
							+ "update its geometry. It the send geometry is null or empty, destroy this teleportation area") })
	public void primSentTeleportationArea(final IScope scope) throws GamaRuntimeException {
		IAgent ag = getAgent();
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		String id = scope.getStringArg("id");
		
		IMap<String, Object> toSend = GamaMapFactory.create();
		IList<Integer> posT = GamaListFactory.create(Types.INT);
		int precision = getPrecision(ag);

		IList<IShape> geoms = (IList<IShape>) scope.getArg("geoms", IType.LIST);
		
		List<Integer> yOffset = new ArrayList<>();
		List pointsGeom = new ArrayList<>();

		for (IShape g : geoms) {
			pointsGeom.add(doAction1Arg(scope, "message_geometry_shape", "geom", g));
			yOffset.add((int)(g.getLocation().z * precision));
		}
		toSend.put("teleportId", id);
		toSend.put("offsetYGeom", yOffset);
		toSend.put("height", (int)(0.1 * precision));
		
		toSend.put("pointsGeom", pointsGeom);
		addToCurrentMessage(scope, buildPlayerListfor1Player(scope, player), toSend);
	}
	
	@action (
			name = "build_invisible_walls",
			args = { @arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("Player to which the message will be sent")),
					@arg (
							name = "id",
							type = IType.STRING,
							doc = @doc ("id of the walls")),
					@arg (
							name = "height",
							type = IType.FLOAT,
							doc = @doc ("height of the walls")),
					@arg (
							name = "wall_width",
							type = IType.FLOAT,
							doc = @doc ("width of the walls")),
					@arg (
							name = "geoms",
							type = IType.LIST,
							doc = @doc ("list of geometries of the walls")) },
			doc = { @doc (
					value = "send a list of geometry to be used to build walls at the given height. "
							+ "If walls with the same id already existed,"
							+ "destroy them before creating the new walls") })
	public void primBuildInvisibleWalls(final IScope scope) throws GamaRuntimeException {
		IAgent ag = getAgent();
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		IList<IShape> geoms = (IList<IShape>) scope.getArg("geoms", IType.LIST);
		IMap<String, Object> toSend = GamaMapFactory.create();
		IList<Integer> posT = GamaListFactory.create(Types.INT);
		int precision = getPrecision(ag);
		String id = scope.getStringArg("id");
		
		List<Integer> yOffset = new ArrayList<>();
		List pointsGeom = new ArrayList<>();
		double wallWidth = scope.getFloatArg("wall_width");
		
		for (IShape g : geoms) {
			for (int i = 0 ; i < g.getPoints().length(scope) - 1; i++) {
				IList<IShape> pts = GamaListFactory.create();
				pts.add( g.getPoints().get(i));
				pts.add( g.getPoints().get(((i+1) == g.getPoints().length(scope)) ? 0 : i+1 ));
				IShape l = SpatialCreation.line(scope, pts);
				if (l.getPerimeter() > 0.0) {
					l = SpatialTransformations.enlarged_by(scope, l, wallWidth);
					
					pointsGeom.add(doAction1Arg(scope, "message_geometry_shape", "geom", l));
					yOffset.add((int)(g.getLocation().z * precision));
				}
				
			}
			
		}
		toSend.put("wallId", id);
		
		toSend.put("offsetYGeom", yOffset);
		
		double height = scope.getFloatArg("height");
		toSend.put("height", (int)(height * precision));
		
		
		toSend.put("pointsGeom", pointsGeom);
	
		addToCurrentMessage(scope, buildPlayerListfor1Player(scope, player), toSend);


	}

	@action (
			name = "enable_player_movement",
			args = { @arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("Player to which to enable/disable the movement")),
					@arg (
							name = "enable",
							type = IType.BOOL,
							doc = @doc ("Enable (true) or disable (false) the movement of the player"))},
			doc = { @doc (
					value = "Enable (true) or disable (false) the movement (teleportation, continuous move) of a player") })
	public void primAllowPlayerMovement(final IScope scope) throws GamaRuntimeException {
		IAgent ag = getAgent();
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		Boolean enable = scope.getBoolArg("enable");
		IMap<String, Object> toSend = GamaMapFactory.create();
		toSend.put("enableMove", enable);
		addToCurrentMessage(scope, buildPlayerListfor1Player(scope, player), toSend);	
	}
		


	private String getName(IShape g, UnityProperties up) {
		String name = g instanceof IAgent ? ((IAgent) g).getName() : (String) g.getAttribute("name");
		if (name == null || name.isBlank()) { 
			if (up == null) 
				return "geometry";
			name = up.getId() +"_" + (int)g.getLocation().x +"_" + (int)g.getLocation().y ;
		}
		return name;
	}
	/**
	 * Prim sent geometries.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "send_geometries",
			args = { @arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("Player to which the message will be sent")),
					@arg (
							name = "update_position",
							type = IType.BOOL,
							doc = @doc ("Has the player to be sent to Unity?")),
					@arg (
							name = "is_init",
							type = IType.BOOL,
							doc = @doc ("send the geometries for the initialization?")),
					@arg (
							name = "geoms",
							type = IType.MAP,
							doc = @doc ("Map of geometry to send (geometry::unity_property)")) 
		},
			doc = { @doc (
					value = "send the background geometries to the Unity client") })
	public void primSentGeometries(final IScope scope) throws GamaRuntimeException {
		IAgent ag = getAgent();
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		Boolean isInit = scope.getBoolArg("is_init");
		Boolean updatePos = scope.getBoolArg("update_position");
		IMap<IShape, UnityProperties> geoms = (IMap<IShape, UnityProperties>) scope.getArg("geoms", IType.MAP);
		
		IMap<String, Object> toSend = GamaMapFactory.create();
		IList<Integer> posT = GamaListFactory.create(Types.INT);
		int precision = getPrecision(ag);

		List<String> names = new ArrayList<>();
		List<String> namesToKeep = new ArrayList<>();
		
		List<String> propertyID = new ArrayList<>();
		List<Integer> yOffset = new ArrayList<>();
		
		List pointsLoc = new ArrayList<>();
		List pointsGeom = new ArrayList<>();
		
		List<Map<String, Object>> atts = new ArrayList<>();
		IMap<IShape, IMap<String,Object>> attributes = getAttributesToSend(ag);
		
		for (IShape g : geoms.keySet()) {
			UnityProperties up = geoms.get(g);
			String name = getName(g, up);
			
			if (up == null) {
				namesToKeep.add(name);
				continue;
			}
				
			names.add(name);
			IMap<String,Object> obj = attributes.get(g);
			atts.add(obj == null ? GamaMapFactory.create(): obj);
			
			propertyID.add(up.getId());
			boolean hp = up.getAspect().isPrefabAspect();
			if (hp) {
				pointsLoc.add(doAction1Arg(scope, "message_geometry_loc", "geom", g));
			} else {
				pointsGeom.add(doAction1Arg(scope, "message_geometry_shape", "geom", g));
				yOffset.add((int)(g.getLocation().z * precision));
			}
		}
		toSend.put("pointsLoc", pointsLoc);
		toSend.put("isInit", isInit);
		toSend.put("offsetYGeom", yOffset);
		
		toSend.put("names", names);
		toSend.put("keepNames", namesToKeep);
		
		toSend.put("propertyID", propertyID);
		toSend.put("pointsGeom", pointsGeom);
		
		toSend.put("attributes", atts);
		if (updatePos) {
			List<Integer> pos = new ArrayList<>(getNewPlayerPosition(ag).get(player.getName()));
			
			toSend.put("position", pos);
			getNewPlayerPosition(ag).get(player.getName()).clear();
		}
		doAction1Arg(scope, "add_to_send_world", "map_to_send", toSend);
		
		addToCurrentMessage(scope, buildPlayerListfor1Player(scope, player), toSend);

		doAction1Arg(scope, "after_sending_geometries", "player", player);

	}

	/**
	 * Prim message geoms shape.
	 *
	 * @param scope
	 *            the scope
	 * @return the i map
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "message_geometry_shape",
			args = { @arg (
					name = "geom",
					type = IType.GEOMETRY,
					doc = @doc ("Geometry to send to Unity")) },

			doc = { @doc (
					value = "Action called by the send_world action that returns the message to send to Unity") })
	public IMap primMessageGeomsShape(final IScope scope) throws GamaRuntimeException {
		IList<Integer> vals = GamaListFactory.create();
		IShape geom = (IShape) scope.getArg("geom", IType.GEOMETRY);
		if (geom == null) return  GamaMapFactory.create();
		int precision = getPrecision(getAgent());

		for (GamaPoint pt : geom.getPoints()) {
			vals.add((int) (pt.x * precision));
			vals.add((int) (pt.y * precision));
			// vals.add((int)(pt.z * precision));

		}
		IMap<String, Object> map = GamaMapFactory.create();
		map.put("c", vals);
		Arguments args = new Arguments();
		args.put("map", ConstantExpressionDescription.createNoCache(map));
		args.put("geom", ConstantExpressionDescription.createNoCache(geom));
		WithArgs actATM = getAgent().getSpecies().getAction(ADD_TO_MAP);

		actATM.setRuntimeArgs(scope, args);
		actATM.executeOn(scope);

		return map;
	}

	/**
	 * Prim message geoms.
	 *
	 * @param scope
	 *            the scope
	 * @return the i map
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "message_geometry_loc",
			args = { @arg (
					name = "geom",
					type = IType.GEOMETRY,
					doc = @doc ("Geometry to send to Unity")) },

			doc = { @doc (
					value = "Action called by the send_world action that returns the message to send to Unity") })
	public IMap primMessageGeoms(final IScope scope) throws GamaRuntimeException {
		IList<Integer> vals = GamaListFactory.create();
		IShape geom = (IShape) scope.getArg("geom", IType.GEOMETRY);
		int precision = getPrecision(getAgent());
		vals.add((int) (geom.getLocation().x * precision));
		vals.add((int) (geom.getLocation().y * precision));
		vals.add((int) (geom.getLocation().z * precision));
		Double hd = (Double) geom.getAttribute("heading");
		if (hd == null) { hd = 0.0; }
		vals.add((int) (hd * precision));
		IMap<String, Object> map = GamaMapFactory.create();
		map.put("c", vals);

		Arguments args = new Arguments();
		args.put("map", ConstantExpressionDescription.createNoCache(map));
		args.put("geom", ConstantExpressionDescription.createNoCache(geom));
		WithArgs actATM = getAgent().getSpecies().getAction(ADD_TO_MAP);

		actATM.setRuntimeArgs(scope, args);
		actATM.executeOn(scope);

		return map;
	}
	
	
	
	
	@action (
			name = "update_terrain",
			args = {@arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("player to send the information to"))
					  ,@arg (
					name = "id",
					type = IType.STRING,
					doc = @doc ("id of to terrain to update")),
					  @arg (
							name = "field",
							type = IType.FIELD,
							optional = true,
						doc = @doc ("Field to send to Unity")),
					  @arg (
					name = "matrix",
					type = IType.MATRIX,
					optional = true,
					doc = @doc ("Matrix to send to Unity")),
					  @arg (
								name = "max_value",
								type = IType.FLOAT,
								optional = true,
								doc = @doc ("max possible value for the grid (if missing, use the max value in the grid/matrix)")),
					 
					  @arg (
								name = "size_x",
								type = IType.FLOAT,
								optional = true,
								doc = @doc ("x-size of the terrain in Unity")),
					  @arg (
								name = "size_y",
								type = IType.FLOAT,
								optional = true,
								doc = @doc ("y-size of the terrain in Unity")),
					 @arg (
								name = "resolution",
								type = IType.INT,
								doc = @doc ("Resolution of the terrain in Unity"))},
			doc = { @doc (
					value = "send a matrix/cell/field to update a Terrain in Unity") })
	public void primUpdateTerrain(final IScope scope) throws GamaRuntimeException {
		
		GamaMap<String, Object> toSend = (GamaMap<String, Object>) GamaMapFactory.create();
		IAgent ag = getAgent();
		GamaMatrix matrix = (GamaMatrix) scope.getArg("matrix", IType.MATRIX);
		GamaField field = (GamaField) scope.getArg("field", IType.FIELD);
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		if (matrix == null && field == null) return;
		int numCols = 0;
		int numRows = 0;
		
		String id = scope.getStringArg("id");
		Double maxV = scope.getFloatArg("max_value");
		
		Double size = scope.getFloatArg("resolution");
		Double sizeX = 1.0 ; 
		Double sizeY = 1.0;
		if (field != null) {
			numCols = field.numCols;
			numRows = field.numRows;
			GamaPoint s= field.getCellSize(scope);
			sizeX = numCols * s.x; 
			sizeY = numRows * s.y; 
		} else  {
			numCols = matrix.numCols;
			numRows = matrix.numRows;
			sizeX = scope.getFloatArg("size_x");
			sizeY = scope.getFloatArg("size_y");
		
		}
		double coeffX = numCols / size;
		double coeffY = numRows / size;
		boolean simpleCase = ((size == numCols) && (size == numRows))  ;
		int valMax = maxV != null ? ((int) maxV.doubleValue()) : -1 * Integer.MAX_VALUE;
		List<Map<String,List<Integer>>> mat = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Map<String,List<Integer>> row = new Hashtable<>();
			List<Integer> v = new ArrayList<>();
			row.put("h", v);
			for (int j = 0; j < size; j++) {
				int dX = j;
				int dY = i;
				if (! simpleCase) {
					dX = (int)(j * coeffX);
					dY = (int)(i * coeffY);
				}
				int iV  = 0;
				if (field != null) {
					iV = Cast.asInt(scope, field.get(scope, dX, dY));
				} else  {
					iV = Cast.asInt(scope, matrix.get(scope, dX, dY));
				}
				if (iV > valMax) {
					valMax = iV;
				}
				v.add(iV);
			}
			mat.add(row);
		}
		toSend.put("rows", mat);
		toSend.put("valMax", valMax);
		toSend.put("sizeX", sizeX);
		toSend.put("sizeY", sizeY);
		
		toSend.put("id", id);
		
		addToCurrentMessage(scope, buildPlayerListfor1Player(scope, player), toSend);
		
		if (currentMessage != null && !currentMessage.isEmpty()) { sendCurrentMessage(scope); }

	}

	@action (
			name = "set_terrain_values",
			args = {@arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("player to send the information to"))
					  ,@arg (
					name = "id",
					type = IType.STRING,
					doc = @doc ("id of to terrain to update")),
					  @arg (
							name = "field",
							type = IType.FIELD,
							optional = true,
						doc = @doc ("Field to send to Unity")),
					  @arg (
					name = "matrix",
					type = IType.MATRIX,
					optional = true,
					doc = @doc ("Matrix to send to Unity")),
					  @arg (
								name = "index_x",
								type = IType.INT,
								optional = false,
								doc = @doc ("index-x (column) of the matrix/field")),
					  @arg (
								name = "index_y",
								type = IType.INT,
								optional = false,
								doc = @doc ("index-y (row) of the matrix/field"))},
			doc = { @doc (
					value = "send a sub-part of a Terrain in Unity at a given index") })
	public void primSetTerrainValues(final IScope scope) throws GamaRuntimeException {
		
		GamaMap<String, Object> toSend = (GamaMap<String, Object>) GamaMapFactory.create();
		IAgent ag = getAgent();
		GamaMatrix matrix = (GamaMatrix) scope.getArg("matrix", IType.MATRIX);
		GamaField field = (GamaField) scope.getArg("field", IType.FIELD);
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		if (matrix == null && field == null) return;
		
		String id = scope.getStringArg("id");
		
		Integer indexX  = scope.getIntArg("index_x");
		Integer indexY  = scope.getIntArg("index_y");
		int num_rows = matrix != null ? matrix.numRows : field.numRows;
		int num_cols = matrix != null ? matrix.numCols : field.numCols;
		int valMax = -1 * Integer.MAX_VALUE;
		List<Map<String,List<Integer>>> mat = new ArrayList<>();
		for (int i = 0; i < num_cols; i++) {
			Map<String,List<Integer>> row = new Hashtable<>();
			List<Integer> v = new ArrayList<>();
			row.put("h", v);
			for (int j = 0; j < num_rows; j++) {
				int dX = j;
				int dY = i;
				
				int iV  = 0;
				if (field != null) {
					iV = Cast.asInt(scope, field.get(scope, dX, dY));
				} else  {
					iV = Cast.asInt(scope, matrix.get(scope, dX, dY));
				}
				if (iV > valMax) {
					valMax = iV;
				}
				v.add(iV);
			}
			mat.add(row);
		}
		toSend.put("indexX", indexX);
		toSend.put("indexY", indexY);
	
		toSend.put("rows", mat);
		toSend.put("valMax", valMax);
		
		toSend.put("id", id);
		
		addToCurrentMessage(scope, buildPlayerListfor1Player(scope, player), toSend);
		
		if (currentMessage != null && !currentMessage.isEmpty()) { sendCurrentMessage(scope); }

	}


	/**
	 * Prim sent init data.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "send_init_data",
			args = { @arg (
					name = "id",
					type = IType.STRING,
					doc = @doc ("if of the player to send the geometries to")) },
			doc = { @doc (
					value = "Wait for the connection of a unity client and send the paramters to the client") })
	public void primSentInitData(final IScope scope) throws GamaRuntimeException {
		
		IAgent ag = getAgent();
		String playerId = scope.getStringArg("id");
		IAgent player = getPlayers(ag).get(playerId);
		if (player == null) return;
		
		doAction1Arg(scope, "send_parameters", "player", player);

		
		doAction1Arg(scope, "send_unity_propetries", "player", player);
		doAction1Arg(scope, "send_player_position", "player", player);

		doAction4Arg(scope, "send_geometries", "player", player, "geoms", getBackgroundGeometries(ag),
				"update_position", true, "is_init", true);

		//doActionNoArg(scope, "send_world");
		
		if (currentMessage != null && !currentMessage.isEmpty()) { 
			
			sendCurrentMessage(scope); }

		startSimulation(scope);
		if (playersInitialized == null) playersInitialized = GamaListFactory.create();
		playersInitialized.add(playerId);
	}

	/**
	 * Prim after sending world.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "after_sending_world",
			args = { @arg (
					name = "map_to_send",
					type = IType.MAP,
					doc = @doc ("data already sent to the client")) },
			doc = { @doc (
					value = "Action trigger just after sending the world to Unity ") })
	public void primAfterSendingWorld(final IScope scope) throws GamaRuntimeException {

	}

	/**
	 * Prim after sending geometries.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "after_sending_geometries",
			args = { @arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("Player to which the message will be sent")) },

			doc = { @doc (
					value = "Action trigger just after sending the background geometries to a Unity client ") })
	public void primAfterSendingGeometries(final IScope scope) throws GamaRuntimeException {

	}

	/**
	 * Prim create init player.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "create_init_player",
			args = { @arg (
					name = "id",
					type = IType.STRING,
					doc = @doc ("name of the player agent")) },

			doc = { @doc (
					value = "Create and init a new unity player agent") })
	public void primCreateInitPlayer(final IScope scope) throws GamaRuntimeException {
		setUseMiddleware(getAgent(), false);
		String id = scope.getStringArg("id");
		doAction1Arg(scope, "create_player", "id", id);
		doAction1Arg(scope, "send_init_data", "id", id);

	}

	/**
	 * Prim init player.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "create_player",
			args = { @arg (
					name = "id",
					type = IType.STRING,
					doc = @doc ("name of the player agent")) },

			doc = { @doc (
					value = "Create a new unity player agent") })
	public void primInitPlayer(final IScope scope) throws GamaRuntimeException {
		IMap<String, IAgent> players = getPlayers(getAgent());
		for(String id : new ArrayList<String>(players.keySet())) {
			if (players.get(id).dead()) {
				players.remove(id);
			}
		}
		IAgent ag = getAgent();
		String id = scope.getStringArg("id");

		ISpecies sp = Cast.asSpecies(scope, getPlayerSpecies(ag));
		if ((sp == null) || (getMaxPlayer(ag) >= 0 && getPlayers(ag).length(scope) >= getMaxPlayer(ag))) return;
		if (getPlayers(ag).containsKey(id) && getPlayers(ag).get(id) != null) return;

		Map<String, Object> init = GamaMapFactory.create();
		if (getPlayerLocationInit(ag).size() <= players.length(scope)) {
			getPlayerLocationInit(ag).add(SpatialPunctal.any_location_in(scope, scope.getSimulation()));
		}
		init.put(IKeyword.LOCATION, getPlayerLocationInit(ag).get(players.length(scope)));
		init.put(IKeyword.NAME, id);

		IAgent player = sp.getPopulation(scope).createAgentAt(scope, getPlayers(ag).length(scope), init, false, true);
		getPlayers(getAgent()).put(id, player);
		
		if (propertiesForPlayer == null)
			propertiesForPlayer = GamaMapFactory.concurrentMap();
		List<UnityProperties>  props = getPlayerUnityProperties(ag);
		if (props != null && !props.isEmpty()) {
			if (getPlayers(ag).size() > props.size()) {
				int index = scope.getRandom().between(0, props.size()-1);
				propertiesForPlayer.put(id, props.get(index));
			} else {
				propertiesForPlayer.put(id, props.get(getPlayers(ag).size() - 1));
			}
		}
	}

	/**
	 * Prim add background geometries.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "add_background_geometries",
			args = { @arg (
					name = "geometries",
					type = IType.CONTAINER,
					doc = @doc ("list of geometries add as backrgound geometries")),
					@arg (
							name = "property",
							type = UnityPropertiesType.UNITYPROPERTIESTYPE_ID,
							doc = @doc ("the unity properties to attach this list of geometries")) },

			doc = { @doc (
					value = "Action called by the send_world action that returns the sub-list of geometries to send to Unity from a given list of geometries according to a max distance to the player") })
	public void primAddBackgroundGeometries(final IScope scope) throws GamaRuntimeException {
		IList<IShape> geometries = Cast.asList(scope, scope.getListArg("geometries"));
		IAgent ag = getAgent();
		Map gb = getBackgroundGeometries(ag);
		UnityProperties property = (UnityProperties) scope.getArg("property",UnityPropertiesType.UNITYPROPERTIESTYPE_ID);
		if (geometriesToFollow == null) { geometriesToFollow = GamaMapFactory.create(); }
		for (IShape s : geometries) {
			gb.put(s, property);
			String name = s instanceof IAgent ? ((IAgent) s).getName() : (String) s.getAttribute("name");
			if (!geometriesToFollow.containsKey(name)) { geometriesToFollow.put(name, s); }
		}
	}

	/**
	 * Prim add geometries to send.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "add_geometries_to_send",
			args = { @arg (
					name = "geometries",
					type = IType.CONTAINER,
					doc = @doc ("list of geometries to send to Unity")),
					@arg (
							name = "property",
							type = UnityPropertiesType.UNITYPROPERTIESTYPE_ID,
							doc = @doc ("the unity properties to attach this list of geometries")) ,
			@arg (
					name = "attributes",
					optional = true,
					type = IType.MAP,
					doc = @doc ("the attributes to send with the geometry - list of map")) },

			doc = { @doc (
					value = "Action allows to define the list of geometries to send to Unity") })
	public void primAddGeometriesToSend(final IScope scope) throws GamaRuntimeException {
		IList<IShape> geometries = Cast.asList(scope, scope.getListArg("geometries"));
		IAgent ag = getAgent();
		Map gts = getGeometriesToSend(ag);
		Map gas = getAttributesToSend(ag);
		Map<String, IList> attributes = scope.hasArg("attributes") ? Cast.asMap(scope, scope.getArg("attributes", IType.MAP), false): null;
		UnityProperties property = (UnityProperties) scope.getArg("property", UnityPropertiesType.UNITYPROPERTIESTYPE_ID);
		if (geometriesToFollow == null) { geometriesToFollow = GamaMapFactory.create(); }
		int cpt = 0;
		for (IShape s : geometries) {
			gts.put(s, property);
			if (attributes != null && !attributes.isEmpty()) {
				IMap<String, Object> vals = GamaMapFactory.create();
				for (String att : attributes.keySet()) {
					vals.put(att, attributes.get(att).get(cpt));
					cpt++;	
				}
				gas.put(s, vals);
				
			}
			String name = s instanceof IAgent ? ((IAgent) s).getName() : (String) s.getAttribute("name");
			if (!geometriesToFollow.containsKey(name)) { geometriesToFollow.put(name, s); }
		}
	}
	
	@action (
			name = "add_geometries_to_keep",
			args = { @arg (
					name = "geometries",
					type = IType.CONTAINER,
					doc = @doc ("list of geometries to keep in Unity"))},

			doc = { @doc (
					value = "Action allows to define the list of geometries to keep in Unity (and not sent)") })
	public void primAddGeometriesToKeep(final IScope scope) throws GamaRuntimeException {
		IList<IShape> geometries = Cast.asList(scope, scope.getListArg("geometries"));
		IAgent ag = getAgent();
		Map gts = getGeometriesToSend(ag);
		UnityProperties property = (UnityProperties) scope.getArg("property", UnityPropertiesType.UNITYPROPERTIESTYPE_ID);
		if (geometriesToFollow == null) { geometriesToFollow = GamaMapFactory.create(); }
		for (IShape s : geometries) {
			gts.put(s, property);
			String name = s instanceof IAgent ? ((IAgent) s).getName() : (String) s.getAttribute("name");
			if (!geometriesToFollow.containsKey(name)) { geometriesToFollow.put(name, s); }
		}
	}
	
	@action (
			name = "update_animation",
			args = {@arg (
					name = "players",
					type = IType.LIST,
					doc = @doc ("player to send the information to")),
					@arg (
					name = "geometries",
					type = IType.CONTAINER,
					doc = @doc ("list of geometries for which to update the animation")),
					@arg (
							name = "triggers",
							type = IType.LIST,
							doc = @doc ("parameter to send - value to change of the animator's variables")),
					@arg (
							name = "parameters",
							type = IType.MAP,
							doc = @doc ("parameter to send - value to change of the animator's variables")) },

			doc = { @doc (
					value = "Action called by the send_world action that returns the sub-list of geometries to send to Unity from a given list of geometries according to a max distance to the player") })
	public void primUpdateAnimation(final IScope scope) throws GamaRuntimeException {
		IList<IShape> geometries = Cast.asList(scope, scope.getListArg("geometries"));
		IAgent ag = getAgent();
		IList<IAgent> players = Cast.asList(scope, scope.getListArg("players"));
		IMap<String, Object> parameters = Cast.asMap(scope, scope.getArg("parameters", IType.MAP), false);
		if (players == null || players.isEmpty()|| geometries == null || geometries.isEmpty())
			return;
		GamaMap<String, Object> toSend = (GamaMap<String, Object>) GamaMapFactory.create();
		IList<String> triggers = scope.getListArg("triggers");
		List<String> names = new ArrayList<>();
		
		for (IShape s : geometries) {
			names.add(getName(s,null));
		}
		
		List<Map<String, Object>> parameterToSend = new ArrayList<>();
		for (String pId : parameters.keySet()) {
			Object v = parameters.get(pId);
			GamaMap<String, Object> param = (GamaMap<String, Object>) GamaMapFactory.create();
			
			if (v instanceof Double) {
				Float f = Double.valueOf((Double)v).floatValue();
				param.put("key", pId);
				param.put("floatVal", f);
				param.put("type", "float");
			} else if (v instanceof Integer){
				param.put("key", pId);
				param.put("intVal", v);				
				param.put("type", "int");
			} else if (v instanceof Boolean){
				param.put("key", pId);
				param.put("boolVal", v);
				param.put("type", "bool");
			}
			parameterToSend.add(param);
		}
		
		IList<String> plStr = GamaListFactory.create();
		for (IAgent p: players) plStr.add(p.getName());
		
		
		toSend.put("triggers", new ArrayList<String>(triggers));
		toSend.put("names", names);
		toSend.put("parameters", parameterToSend);
		addToCurrentMessage(scope,plStr, toSend);
		
		if (currentMessage != null && !currentMessage.isEmpty()) { sendCurrentMessage(scope); }
	}

	/**
	 * Prim move geoms followed.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "move_geoms_followed",
			args = { @arg (
					name = "ids",
					type = IType.STRING,
					doc = @doc ("ids of the geometries to move")),
					@arg (
							name = "points",
							type = IType.STRING,
							doc = @doc ("points of the geometries to move")),
					@arg (
							name = "sep",
							type = IType.STRING,
							doc = @doc ("separator used to tokenized the id and position")) },

			doc = { @doc (
					value = "Action called by the Unity Client to move agents") })
	public void primMoveGeomsFollowed(final IScope scope) throws GamaRuntimeException {
		IAgent ag = scope.getAgent();
		String sep = scope.getStringArg("sep");
		String ids = scope.getStringArg("ids");
		String points = scope.getStringArg("points");

		if (sep == null || ids == null || points == null || sep.isBlank() || ids.isBlank() || points.isBlank()) return;
		List<String> idsStr = Strings.split(ids, sep);
		int precision = getPrecision(ag);
		int nb = idsStr.size();
		List<String> ptsStr = Strings.split(points, sep);
		int cpt = 0;
		for (int i = 0; i < nb; i++) {
			String id = idsStr.get(i);
			if (id.isBlank()) { continue; }
			IShape geom = geometriesToFollow.get(id);
			if (geom != null) {
				double x = (0.0 + Integer.valueOf(ptsStr.get(cpt))) / precision;
				double y = (0.0 + Integer.valueOf(ptsStr.get(cpt + 1))) / precision;
				double z = (0.0 + Integer.valueOf(ptsStr.get(cpt + 2))) / precision;
				geom.setLocation(new GamaPoint(x, y, z));

			}
			cpt = cpt + 3;
		}
	}

	/**
	 * Prim filter distance.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "filter_distance",
			args = { @arg (
					name = "geometries",
					type = IType.LIST,
					doc = @doc ("list of geometries to filter")),
					@arg (
							name = "player",
							type = IType.AGENT,
							doc = @doc ("the player agent")) },

			doc = { @doc (
					value = "Action called by the send_world action that returns the sub-list of geometries to send to Unity from a given list of geometries according to a max distance to the player") })
	public IList<IShape> primFilterDistance(final IScope scope) throws GamaRuntimeException {
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		IList<IShape> geoms = GamaListFactory.create();
		geoms.addAll((IList<IShape>) scope.getArg("geometries", IType.GEOMETRY));

		Double dist = (Double) player.getAttribute(AbstractUnityPlayer.PLAYER_AGENTS_PERCEPTION_RADIUS);
		return (IList<IShape>) SpatialQueries.overlapping(scope, geoms,
				SpatialTransformations.enlarged_by(scope, player, dist));
	}

	/**
	 * Prim filter overlapping.
	 *
	 * @param scope
	 *            the scope
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "filter_overlapping",
			args = { @arg (
					name = "geometries",
					type = IType.LIST,
					doc = @doc ("list of geometries to filter")),
					@arg (
							name = "player",
							type = IType.AGENT,
							doc = @doc ("the player agent")) },

			doc = { @doc (
					value = "Action called by the send_world action that returns the sub-list of geometries to send to Unity from a given list of geometries according to a min proximity to the other geometries to send") })
	public IList<IShape> primFilterOverlapping(final IScope scope) throws GamaRuntimeException {
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		IList<IShape> geoms = GamaListFactory.create();
		geoms.addAll((IList<IShape>) scope.getArg("geometries", IType.GEOMETRY));

		IList<IShape> toRemove = GamaListFactory.create();
		for (IShape g : geoms) {
			if (!toRemove.contains(g)) {
				Double dist = (Double) player.getAttribute(AbstractUnityPlayer.PLAYER_AGENTS_MIN_DIST);
				toRemove.addAll(
						SpatialQueries.overlapping(scope, geoms, SpatialTransformations.enlarged_by(scope, player, dist)));
			}
		}
		geoms.removeAll(toRemove);
		return geoms;
	}

	/**
	 * Prim new player loc.
	 *
	 * @param scope
	 *            the scope
	 * @return the gama point
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "new_player_location",
			args = { @arg (
					name = "loc",
					type = IType.POINT,
					doc = @doc ("Location of the player agent")),

					@arg (
							name = "player",
							type = IType.AGENT,
							doc = @doc ("the player agent")) },

			doc = { @doc (
					value = "Action called by the move_player action that returns the location to send to Unity from a given player location") })
	public GamaPoint primNewPlayerLoc(final IScope scope) throws GamaRuntimeException {
		return (GamaPoint) scope.getArg("loc", IType.POINT);
	}

	/**
	 * Prim move player.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "move_player",
			args = { @arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("the player agent to move")),

					@arg (
							name = "loc",
							type = IType.POINT,
							doc = @doc ("Location of the player agent")) },
			doc = { @doc (
					value = "move the player agent") })
	public void primMovePlayer(final IScope scope) throws GamaRuntimeException {
		IAgent ag = getAgent();
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		GamaPoint loc = (GamaPoint) scope.getArg("loc", IType.POINT);
		getReadyToMovePlayers(ag).remove(player);
		loc = (GamaPoint) doAction2Arg(scope, "new_player_location", "player", player, "loc", loc);
		player.setLocation(loc);
		doAction1Arg(scope, "send_player_position", "player", player);

	}

	/**
	 * Prim move player from unity.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "move_player_external",
			args = { @arg (
					name = "id",
					type = IType.STRING,
					doc = @doc ("id of the player agent to move")),

					@arg (
							name = "x",
							type = IType.INT,
							doc = @doc ("x Location of the player agent")),
					@arg (
							name = "y",
							type = IType.INT,
							doc = @doc ("y Location of the player agent")),
					@arg (
							name = "z",
							type = IType.INT,
							doc = @doc ("z Location of the player agent")),
					@arg (
							name = "angle",
							type = IType.INT,
							doc = @doc ("angle of the player agent")) },
			doc = { @doc (
					value = "move the player agent ") })
	public void primMovePlayerFromUnity(final IScope scope) throws GamaRuntimeException {

		IAgent ag = getAgent();
		IAgent thePlayer = getPlayers(ag).get(scope.getStringArg("id"));
		if (thePlayer == null) return;
		Integer x = scope.getIntArg("x");
		Integer y = scope.getIntArg("y");
		Integer z = scope.getIntArg("z");
		Integer angle = scope.getIntArg("angle");
		int precision = getPrecision(ag);
		Double rot = (Double) thePlayer.getAttribute("player_rotation");
		if (getReadyToMovePlayers(ag) != null && getReadyToMovePlayers(ag).contains(thePlayer)) {
			if (rot != null) { thePlayer.setAttribute("heading", angle.floatValue() / precision + rot); }
			if (x != null && y != null) {
				thePlayer.setLocation(new GamaPoint(x.floatValue() / precision, y.floatValue() / precision,
						z.floatValue() / precision));
			}
			thePlayer.setAttribute("to_display", true);
		}

	}

	

	/**
	 * Prim ping GAMA.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "ping_GAMA",
			args = { @arg (
					name = "id",
					type = IType.STRING,
					doc = @doc ("Player agent that try to ping GAMA")) },
			doc = { @doc (
					value = "Ping GAMA to test the connection") })
	public void primPingGAMA(final IScope scope) throws GamaRuntimeException {
		IAgent ag = getAgent();
		IAgent thePlayer = getPlayers(ag).get(scope.getStringArg("id"));
		if (thePlayer == null) return;
		PlatformAgent pa = GAMA.getPlatformAgent();
		pa.sendMessage(scope, ConstantExpressionDescription.createNoCache("pong"));
	}

	/**
	 * Prim player position updated.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "player_position_updated",
			args = { @arg (
					name = "id",
					type = IType.STRING,
					doc = @doc ("Player agent of which the position has been updated")) },
			doc = { @doc (
					value = "reactivate the reception of player position") })
	public void primPlayerPositionUpdated(final IScope scope) throws GamaRuntimeException {
		IAgent ag = getAgent();
		IAgent thePlayer = getPlayers(ag).get(scope.getStringArg("id"));
		if (thePlayer == null) return;
		getNewPlayerPosition(ag).put(thePlayer.getName(), GamaListFactory.create());
		if (!getReadyToMovePlayers(ag).contains(thePlayer.getName())) { getReadyToMovePlayers(ag).add(thePlayer); }

	}

	/**
	 * Prim send player position.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "send_player_position",
			args = { @arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("Player agent of which to send the position")) },
			doc = { @doc (
					value = "send the new position of the player to Unity (used to teleport the player from GAMA) ") })
	public void primSendPlayerPosition(final IScope scope) throws GamaRuntimeException {
		IAgent ag = getAgent();
		if (!getConnectToUnity(ag)) return;
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		int precision = getPrecision(ag);
		IList<Integer> pos = GamaListFactory.create();
		pos.add((int) (player.getLocation().x * precision));
		pos.add((int) (player.getLocation().y * precision));
		pos.add((int) (player.getLocation().z * precision));
		getNewPlayerPosition(ag).put(player.getName(), pos);
	}

	/**
	 * Start simulation.
	 *
	 * @param scope
	 *            the scope
	 */
	private void startSimulation(final IScope scope) {
		if (getPlayers(getAgent()).size() >= getMinPlayer(getAgent())) { 
			for (IAgent player : getPlayers(getAgent()).values()) {
				doAction2Arg(scope, "enable_player_movement", "player", player, "enable", true);
			}
			scope.getSimulation().resume(scope); 
		}
	}

	/**
	 * Prim add to sent parameter.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "add_to_send_parameter",
			args = { @arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("Player to which send the information")),
					@arg (
					name = "map_to_send",
					type = IType.MAP,
					doc = @doc ("data already sent to the client")) },
			doc = { @doc (
					value = "add values to the parameters sent to the Unity Client") })
	public void primAddToSentParameter(final IScope scope) throws GamaRuntimeException {

	}
	
	@action (
			name = "add_to_send_world",
			args = { @arg (
					name = "map_to_send",
					type = IType.MAP,
					doc = @doc ("data already sent to the client")) },
			doc = { @doc (
					value = "add values to the world sent to the Unity Client") })
	public void primAddToSentWorld(final IScope scope) throws GamaRuntimeException {

	} 

	/**
	 * Prim send parameters.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "send_parameters",
			args = { @arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("Player to which the message will be sent")) },

			doc = { @doc (
					value = "Send the parameter to Unity to intialized the connection") })
	public void primSendParameters(final IScope scope) throws GamaRuntimeException {
		GamaMap<String, Object> toSend = (GamaMap<String, Object>) GamaMapFactory.create();
		IAgent ag = getAgent();
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		int precision = getPrecision(ag);
		toSend.put(PRECISION, precision);
		IList<Integer> worldT = GamaListFactory.create(Types.INT);
		worldT.add((int) (scope.getSimulation().getGeometricEnvelope().getWidth() * precision));
		worldT.add((int) (scope.getSimulation().getGeometricEnvelope().getHeight() * precision));
		
		toSend.put("world", worldT);
		toSend.put("minPlayerUpdateDuration", getMinPlayerPoisitionUpdateDuration(ag) * precision);
		doAction2Arg(scope, "add_to_send_parameter", "player", player, "map_to_send", toSend);
		addToCurrentMessage(scope, buildPlayerListfor1Player(scope, player), toSend);
		doAction2Arg(scope, "enable_player_movement", "player", player, "enable", false);
	}

	/**
	 * Prim send unity properties.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "send_unity_propetries",
			args = { @arg (
					name = "player",
					type = IType.AGENT,
					doc = @doc ("Player to which the message will be sent")) },

			doc = { @doc (
					value = "Send the Unity properties to intialize the possible properties of geometries") })
	public void primSendUnityProperties(final IScope scope) throws GamaRuntimeException {
		GamaMap<String, Object> toSend = (GamaMap<String, Object>) GamaMapFactory.create();
		IAgent ag = getAgent();
		IAgent player = (IAgent) scope.getArg("player", IType.AGENT);
		List<UnityProperties> props = getUnityProperties(ag);
		if (props.isEmpty()) return;
		List<Map> propMap = new ArrayList<>();
		for (UnityProperties p : props) { propMap.add(p.toMap()); }
		toSend.put("properties", propMap);
		addToCurrentMessage(scope, buildPlayerListfor1Player(scope, player), toSend);
	}

	/**
	 * Builds the player listfor 1 player.
	 *
	 * @param scope
	 *            the scope
	 * @param player
	 *            the player
	 * @return the i list
	 */
	private IList buildPlayerListfor1Player(final IScope scope, final IAgent player) {
		IList players = GamaListFactory.create();
		players.add(player.getName());
		return players;
	}

	/**
	 * Prim loc to send.
	 *
	 * @param scope
	 *            the scope
	 * @return the gama point
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = LOC_TO_SEND,
			doc = @doc (
					returns = "the location to send to Unity"))
	public GamaPoint primLocToSend(final IScope scope) throws GamaRuntimeException {
		return scope.getAgent().getLocation();

	}

	/**
	 * Prim add to map.
	 *
	 * @param scope
	 *            the scope
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = ADD_TO_MAP,
			args = { @arg (
					name = "map",
					type = IType.MAP,
					doc = @doc ("map of data to send to Unity")),
					@arg (
							name = "geom",
							type = IType.GEOMETRY,
							doc = @doc ("Geometry to send to Unity")) },
			doc = @doc (
					returns = "other elements than the location to add to the data sent to Unity"))
	public void primAddToMap(final IScope scope) throws GamaRuntimeException {

	}

	/**
	 * Gets the index species.
	 *
	 * @param agent
	 *            the agent
	 * @return the index species
	 */
	public Integer getIndexSpecies(final IAgent agent) {
		return (Integer) agent.getAttribute(SPECIES_INDEX);
	}

	/**
	 * Gets the heading.
	 *
	 * @param agent
	 *            the agent
	 * @return the heading
	 */
	public Double getHeading(final IAgent agent) {
		if (agent.hasAttribute(HEADING)) return (Double) agent.getAttribute(HEADING);
		return 0.0;
	}

}
