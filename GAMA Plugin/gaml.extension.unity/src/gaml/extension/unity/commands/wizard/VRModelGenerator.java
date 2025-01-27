/*******************************************************************************************************
 *
 * VRModelGenerator.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.commands.wizard;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.model.IModel;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.outputs.IOutput;
import gama.core.util.GamaColor;
import gama.gaml.species.ISpecies;
import one.util.streamex.StreamEx;

/**
 * The Class VRModelGenerator.
 */
public class VRModelGenerator {

	IModel model;

	Map<String, Player> players = new HashMap<>();
	Map<String, UnityProperties> properties = new HashMap<>(Map.of("default", new UnityProperties()));

	// private Map<String, Map<String, String>> definedProperties;
	Map<String, Species> species = new HashMap<>();
	Map<String, List<String>> displays = new HashMap<>();
	private List<String> displaysToHide = new ArrayList<>();
	private String mainDisplay;

	private final String existingModelFileName;

	private final String modelName;

	private final String newModelPath;
	
	private final String projectPath;

	private String experimentName;
	/** The minimum cycle duration. */
	private Double minimumCycleDuration = 0.1;
	Double perceptionRadius = getDefaultPlayerAgentsPerceptionRadius();
	Double minDistance = getDefaultPlayerAgentsMinDist();

	/** The min number player. */
	private int minNumberPlayer;

	/** The max num player. */
	private int maxNumPlayer;

	/** The has max number player. */
	private boolean hasMaxNumberPlayer;

	public VRModelGenerator(final IModel model) {
		this.model = model;
		buildInitialListOfSpecies();
		buildInitialListOfExperimentsAndDisplays();
		this.existingModelFileName = new File(model.getFilePath()).getName();
		this.modelName = model.getName() + "_VR";
		this.newModelPath = model.getFilePath().replace(".gaml", "-VR.gaml");
		
		

		this.projectPath = model.getProjectPath();

		
	}

	public class Player {
		GamaPoint location = getDefaultLocationInit();
		GamaColor color = getDefaultPlayerColor();
		Double size = getDefaultPlayerSize();
		String property;
	}

	public class UnityProperties {
		String tag = "";
		Double size = 1d;
		GamaColor color = getDefaultSpeciesColor();
		String material = "";
		Double buffer = 0d;
		String path = "Prefabs/Visual Prefabs/City/Vehicles/Car";
		Double height = 1d;
		Double rotationCoeff = 1d;
		Double offset = 0d;
		Double rotationOffset = 0d;
		boolean has_prefab;
		boolean collider;
		boolean follow;
		boolean interactable;
		boolean grabable;
	}

	public class Species {
		boolean keep = false;
		boolean dynamic = true;
		String property = "default";
		String when = "1";
	}

	void addPlayer(final int i) {
		players.put(getPlayerName(i), new Player());
	}

	void removePlayer(final int i) {
		players.remove(getPlayerName(i));
	}

	private void buildInitialListOfExperimentsAndDisplays() {
		for (IExperimentPlan ep : model.getExperiments()) {
			if (experimentName == null) { experimentName = ep.getName(); }
			List<String> itemsD = new ArrayList<>();
			for (IOutput d : ep.getOriginalSimulationOutputs()) {
				if (mainDisplay == null) { mainDisplay = d.getOriginalName(); }
				itemsD.add(d.getOriginalName());
			}
			displays.put(ep.getName(), itemsD);
		}
	}

	private Collection<String> buildInitialListOfSpecies() {
		Map<String, ISpecies> modelSpecies = model.getAllSpecies();
		Collection<String> result = new ArrayList<>(modelSpecies.keySet());
		result.remove(model.getName());
		for (String s : result) { species.put(s, new Species()); }
		return result;
	}

	UnityProperties getUnityProperties(final String name) {
		UnityProperties p = properties.get(name);
		return p;
		// return p == null ? properties.get("default") : p;
	}

	UnityProperties createUnityProperties(final String name) {
		UnityProperties result = new UnityProperties();
		properties.put(name, result);
		return result;
	}

	Species getSpecies(final String name) {
		return species.get(name);
	}

	Player getPlayer(final String name) {
		return players.get(name);
	}

	/** The species to send. */
	// private Map<String, Map<String, String>> speciesToSend;

	// private Map<String, Map<String, String>> speciesToSendStatic;

	// private Map<String, Map<String, String>> speciesToSendDynamic;

	// private List<String> playerProperties = null;

	/** The location init. */
	// private GamaPoint locationInit = new GamaPoint(50, 50, 0);

	/** The player agents perception radius. */
	// private Double playerAgentsPerceptionRadius = 0.0;

	/** The player agents min dist. */
	// private Double playerAgentsMinDist = 0.0;

	/** The player size. */
	// private Double playerSize = 1.0;

	/**
	 * Builds the VR model.
	 *
	 * @return the string
	 */
	public void buildAndSaveVRModel() {
		StringBuilder modelVR = new StringBuilder("model ").append(modelName).append("\n\n");
		modelVR.append("import \"").append(existingModelFileName).append("\"\n\n");
		modelVR.append(UnityLinkerSpecies()).append("\n\n");
		modelVR.append(playerStr()).append("\n\n");
		modelVR.append(experimentStr());

		try (FileWriter fw = new FileWriter(newModelPath)) {
			fw.write(modelVR.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		Path directoryP = Paths.get(projectPath);
		Path file = Paths.get(newModelPath);
		
		Path relativeP = directoryP.relativize(file);
		
		
		StringBuilder settings = new StringBuilder("{\n").append("\"type\": \"json_settings\",\n");
		settings.append("\"name\": \"" ).append(modelName).append("\",\n");
		settings.append("\"splashscreen\": \"./models/snapshots/snapshot.png\",\n");
		settings.append("\"model_file_path\": \"./" ).append(relativeP.toString()).append("\",\n");
		settings.append("\"experiment_name\": \"vr_xp\",\n");
		settings.append("\"minimal_players\": \"" ).append(minNumberPlayer).append("\",\n");
		settings.append("\"maximal_players\": \"" ).append(maxNumPlayer).append("\",\n");
		settings.append("\"selected_monitoring\": \"gama_screen\"\n}");
		
		try (FileWriter fw = new FileWriter(projectPath + "/settings.json")) {
			fw.write(settings.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Experiment str.
	 *
	 * @return the string
	 */
	public String experimentStr() {
		StringBuilder modelExp =
				new StringBuilder("experiment vr_xp ").append(experimentName != null ? "parent:" + experimentName : "")
						.append(" autorun: false type: unity {\n");
		modelExp.append("\tfloat minimum_cycle_duration <- ").append(minimumCycleDuration).append(";\n");
		modelExp.append("\tstring unity_linker_species <- string(unity_linker);\n");
		StringBuilder disToHide = new StringBuilder();
		boolean first = true;
		if (displaysToHide != null) {
			for (String d : displaysToHide) {
				disToHide.append(first ? "" : ",").append("\"").append(d).append("\"");
				first = false;
			}
		}

		modelExp.append("\tlist<string> displays_to_hide <- [").append(disToHide.toString()).append("];\n");
		modelExp.append("\tfloat t_ref;\n\n");

		modelExp.append("\taction create_player(string id) {\n");
		modelExp.append("\t\task unity_linker {\n");
		modelExp.append("\t\t\tdo create_player(id);\n");
		modelExp.append("\t\t}\n");
		modelExp.append("\t}\n\n");

		modelExp.append("\taction remove_player(string id_input) {\n");
		modelExp.append("\t\tif (not empty(unity_player)) {\n");

		modelExp.append("\t\t\task first(unity_player where (each.name = id_input)) {\n");
		modelExp.append("\t\t\t\tdo die;\n");
		modelExp.append("\t\t\t}\n");
		modelExp.append("\t\t}\n");
		modelExp.append("\t}\n\n");

		modelExp.append("\toutput {\n");
		if (mainDisplay != null) {

			modelExp.append("\t\t display ").append(mainDisplay).append("_VR parent:").append(mainDisplay)
					.append("{\n");
			modelExp.append("\t\t\t species unity_player;\n");
			modelExp.append("\t\t\t event #mouse_down{\n");

			modelExp.append("\t\t\t\t float t <- gama.machine_time;\n");
			modelExp.append("\t\t\t\t if (t - t_ref) > 500 {\n");

			modelExp.append("\t\t\t\t\t ask unity_linker {\n");
			modelExp.append("\t\t\t\t\t\t move_player_event <- true;\n");
			modelExp.append("\t\t\t\t\t }\n");
			modelExp.append("\t\t\t\t\t t_ref <- t;\n");
			modelExp.append("\t\t\t\t }\n");

			modelExp.append("\t\t\t }\n");
			modelExp.append("\t\t }\n");

			modelExp.append("\t}\n");

			modelExp.append("}\n");
		}

		return modelExp.toString();

	}

	/**
	 * Player str.
	 *
	 * @return the string
	 */
	public String playerStr() {
		StringBuilder modelPlayer = new StringBuilder("species unity_player parent: abstract_unity_player{\n");
		Player p = maxNumPlayer <= 0 ? new Player() : this.getPlayer(getPlayerName(0));
		if (perceptionRadius != null && perceptionRadius > 0.0) {
			modelPlayer.append("\tfloat player_agents_perception_radius <- ").append(perceptionRadius).append(";\n");
		}
		if (minDistance != null && minDistance > 0.0) {
			modelPlayer.append("\tfloat player_agents_min_dist <- ").append(minDistance).append(";\n");
		}
		modelPlayer.append("\tfloat player_size <- ").append(p.size).append(";\n");
		modelPlayer.append("\trgb color <- ").append(p.color.serializeToGaml(true)).append(";\n");
		modelPlayer.append("\tfloat cone_distance <- 10.0 * player_size;\n");
		modelPlayer.append("\tfloat cone_amplitude <- 90.0;\n");
		modelPlayer.append("\tfloat player_rotation <- 90.0;\n");
		modelPlayer.append("\tbool to_display <- true;\n");
		modelPlayer.append("\tfloat z_offset <- 2.0;\n");

		modelPlayer.append("\taspect default {\n");
		modelPlayer.append("\t\tif to_display {\n");
		modelPlayer.append("\t\t\tif selected {\n");
		modelPlayer
				.append("\t\t\t\t draw circle(player_size) at: location + {0, 0, z_offset} color: rgb(#blue, 0.5);\n");

		modelPlayer.append("\t\t\t}\n");

		modelPlayer.append("\t\t\tdraw circle(player_size/2.0) at: location + {0, 0, z_offset} color: color ;\n");
		modelPlayer.append("\t\t\tdraw player_perception_cone() color: rgb(color, 0.5);");
		modelPlayer.append("\n\t\t}");
		modelPlayer.append("\n\t}");
		modelPlayer.append("\n}");

		return modelPlayer.toString();
	}

	/**
	 * Unity linker species.
	 *
	 * @return the string
	 */
	public String UnityLinkerSpecies() {
		StringBuilder modelUnityLinker = new StringBuilder("species unity_linker parent: abstract_unity_linker {\n");
		modelUnityLinker.append("\tstring player_species <- string(unity_player);\n");

		if (hasMaxNumberPlayer) {
			modelUnityLinker.append("\tint max_num_players  <- ").append(maxNumPlayer).append(";\n");
		} else {
			modelUnityLinker.append("\tint max_num_players  <- -1;\n");
		}
		if (minNumberPlayer > 0) {
			modelUnityLinker.append("\tint min_num_players  <- ").append(minNumberPlayer).append(";\n");
		}
		for (String p : properties.keySet()) {
			modelUnityLinker.append("\tunity_property ").append("up_" + p).append(";\n");
		}

		StringBuilder locationInitStr = new StringBuilder();
		if (maxNumPlayer <= 0) {
			locationInitStr.append("").append(getDefaultLocationInit());
		} else {
			boolean first = true;
			for (int i = 0; i < maxNumPlayer; i++) {
				locationInitStr.append(first ? "" : ",").append(getPlayer(getPlayerName(i)).location);
				first = false;
			}
		}

		modelUnityLinker.append("\tlist<point> init_locations <- define_init_locations();\n");
		modelUnityLinker.append("\n\tlist<point> define_init_locations {\n");
		modelUnityLinker.append("\t\treturn [").append(locationInitStr.toString()).append("];\n");
		modelUnityLinker.append("\t}\n\n");
		Map<String, Species> speciesToSendStatic = null;
		Map<String, Species> speciesToSendDynamic = null;
		if (!species.isEmpty()) {
			speciesToSendStatic = new HashMap<>();
			speciesToSendDynamic = new HashMap<>();
			for (String sp : species.keySet()) {
				Species data = species.get(sp);
				if (data.keep) {
					if (!data.dynamic) {
						speciesToSendStatic.put(sp, data);
					} else {
						speciesToSendDynamic.put(sp, data);
					}
				}
			}
		}
		if (speciesToSendStatic != null && !speciesToSendStatic.isEmpty() || !properties.isEmpty()) {
			modelUnityLinker.append("\n\tinit {");
			if (!properties.isEmpty()) { modelUnityLinker.append("\n\t\tdo define_properties;"); }
			Collection<String> playerProperties =
					StreamEx.ofValues(players).map(each -> each.property).toImmutableList();
			if (playerProperties != null && !playerProperties.isEmpty()) {
				StringBuilder pp = new StringBuilder();
				boolean first = true;
				for (String prop : playerProperties) {

					pp.append(first ? "" : ",");
					if (prop != null) {
						pp.append("up_").append(prop);
					} else {
						pp.append("nil");
					}
					first = false;
				}
				modelUnityLinker.append("\n\t\tplayer_unity_properties <- [" + pp.append("];").toString());
			}
			if (speciesToSendStatic != null && !speciesToSendStatic.isEmpty()) {
				for (String sp : speciesToSendStatic.keySet()) {
					Species data = speciesToSendStatic.get(sp);
					Double buffer = properties.get(data.property).buffer;
					String geom = sp;
					
					if (buffer != null && buffer != 0 ) {
						
						String idG = "g_" + sp;
						geom = idG;
						
						modelUnityLinker.append("\n\t\tlist<geometry> " + idG+ " <- "+ sp +" collect (each.shape + " + buffer+ ");");
						
						modelUnityLinker.append("\n\t\tloop i from: 0 to: length(" + sp+ ") - 1 {");
						modelUnityLinker.append("\n\t\t\t"+ idG+ "[i].attributes[\"name\"] <- " + sp +"[i].name"); 
						modelUnityLinker.append("\n\t\t}");
					}
						 
					
					modelUnityLinker.append("\t\tdo add_background_geometries(" + geom + ",up_" + data.property + ");");
				}
			}

			modelUnityLinker.append("\n\t}");

		}

		if (!properties.isEmpty()) {
			modelUnityLinker.append("\n\taction define_properties {");
			int cpt = 0;
			for (String p : properties.keySet()) {
				UnityProperties data = properties.get(p);
				String idA = p + "_aspect";
				modelUnityLinker.append("\n\t\tunity_aspect ").append(idA + " <- ");
				if (data.has_prefab) {
					modelUnityLinker.append("prefab_aspect(\"" + data.path + "\"," + data.size + "," + data.offset + ","
							+ data.rotationCoeff + "," + data.rotationOffset + ",precision);");
				} else {
					String color = data.color.serializeToGaml(true);
					String material = data.material;
					if (material != null && !material.isBlank()) {
						modelUnityLinker.append(
								"geometry_aspect(" + data.height + ",\"" + material + "\"," + color + ",precision);");
					} else {
						modelUnityLinker.append("geometry_aspect(" + data.height + "," + color + ",precision);");
					}
				}
				String interaction = "";
				if (!data.collider) {
					interaction = "#no_interaction";
				} else if (!data.interactable) {
					interaction = "new_geometry_interaction(true, false,false,[])";
				} else if (!data.grabable) {
					interaction = "#ray_interactable";
				} else {
					interaction = "#grabable";
				}

				modelUnityLinker.append("\n\t\t");
				modelUnityLinker.append("up_" + p).append(" <- ").append("geometry_properties(\"").append(p + "\",\"")
						.append(data.tag + "\",").append(idA + ",").append(interaction + ",")
						.append(data.follow + ");");
				modelUnityLinker.append("\n\t\tunity_properties << ").append("up_" + p).append(";");
				if (cpt < properties.size()) { modelUnityLinker.append("\n\n"); }
				cpt++;
			}
			modelUnityLinker.append("\n\t}");
		}
		if (speciesToSendDynamic != null && !speciesToSendDynamic.isEmpty()) {
			modelUnityLinker.append("\n\treflex send_geometries {");
			for (String sp : speciesToSendDynamic.keySet()) {
				Species data = speciesToSendDynamic.get(sp);
				String when = data.when;
				boolean addC = false;
				if (when != null && !when.isBlank() && !"1".equals(when)) {
					addC = true;
					modelUnityLinker.append("\n\t\tif every(" + when + "){");
				}
				modelUnityLinker.append("\n\t\t" + (addC ? "\t" : ""));
				Double buffer = properties.get(data.property).buffer;
				String geom = sp + (buffer != null && buffer != 0.0 ? " collect (each.shape + " + buffer + ")" : "");
				modelUnityLinker.append("do add_geometries_to_send(" + geom + ",up_" + data.property + ");");
				if (addC) { modelUnityLinker.append("\n\t\t}"); }
			}
			modelUnityLinker.append("\n\t}");
		}

		modelUnityLinker.append("\n}");
		return modelUnityLinker.toString();
	}

	/**
	 * Gets the player color.
	 *
	 * @return the player color
	 */
	public GamaColor getDefaultPlayerColor() { return GamaColor.get(Color.red); }

	public GamaColor getDefaultSpeciesColor() { return GamaColor.get("gray"); }

	/**
	 * Gets the location init.
	 *
	 * @return the location init
	 */
	public GamaPoint getDefaultLocationInit() { return new GamaPoint(50, 50, 0); }

	/**
	 * Sets the location init.
	 *
	 * @param locationInit
	 *            the new location init
	 */
	// public void setLocationInit(final GamaPoint locationInit) { this.locationInit = locationInit; }

	/**
	 * Gets the player agents perception radius.
	 *
	 * @return the player agents perception radius
	 */
	public Double getDefaultPlayerAgentsPerceptionRadius() { return 0d; }

	/**
	 * Sets the player agents perception radius.
	 *
	 * @param playerAgentsPerceptionRadius
	 *            the new player agents perception radius
	 */
	// public void setPlayerAgentsPerceptionRadius(final Double playerAgentsPerceptionRadius) {
	// this.playerAgentsPerceptionRadius = playerAgentsPerceptionRadius;
	// }

	/**
	 * Gets the player agents min dist.
	 *
	 * @return the player agents min dist
	 */
	public Double getDefaultPlayerAgentsMinDist() { return 0d; }

	/**
	 * Sets the player agents min dist.
	 *
	 * @param playerAgentsMinDist
	 *            the new player agents min dist
	 */
	// public void setPlayerAgentsMinDist(final Double playerAgentsMinDist) {
	// this.playerAgentsMinDist = playerAgentsMinDist;
	// }

	/**
	 * Gets the player size.
	 *
	 * @return the player size
	 */
	public Double getDefaultPlayerSize() { return 1d; }

	/**
	 * Sets the player size.
	 *
	 * @param playerSize
	 *            the new player size
	 */
	// public void setPlayerSize(final Double playerSize) { this.playerSize = playerSize; }

	/**
	 * Gets the minimum cycle duration.
	 *
	 * @return the minimum cycle duration
	 */
	public Double getMinimumCycleDuration() { return minimumCycleDuration; }

	/**
	 * Sets the minimum cycle duration.
	 *
	 * @param minimumCycleDuration
	 *            the new minimum cycle duration
	 */
	public void setMinimumCycleDuration(final Double minimumCycleDuration) {
		this.minimumCycleDuration = minimumCycleDuration;
	}

	/**
	 * Gets the displays to hide.
	 *
	 * @return the displays to hide
	 */
	public List<String> getDisplaysToHide() { return displaysToHide; }

	/**
	 * Sets the displays to hide.
	 *
	 * @param displaysToHide
	 *            the new displays to hide
	 */
	public void setDisplaysToHide(final List<String> displaysToHide) { this.displaysToHide = displaysToHide; }

	/**
	 * Gets the main display.
	 *
	 * @return the main display
	 */
	public String getMainDisplay() { return mainDisplay; }

	/**
	 * Sets the main display.
	 *
	 * @param mainDisplay
	 *            the new main display
	 */
	public void setMainDisplay(final String mainDisplay) { this.mainDisplay = mainDisplay; }

	/**
	 * Gets the experiment name.
	 *
	 * @return the experiment name
	 */
	public String getExperimentName() { return experimentName; }

	/**
	 * Sets the experiment name.
	 *
	 * @param experimentName
	 *            the new experiment name
	 */
	public void setExperimentName(final String experimentName) { this.experimentName = experimentName; }

	/**
	 * Gets the min num player.
	 *
	 * @return the min num player
	 */
	public int getMin_num_player() { return minNumberPlayer; }

	/**
	 * Sets the min num player.
	 *
	 * @param min_num_player
	 *            the new min num player
	 */
	public void setMin_num_player(final int min_num_player) { this.minNumberPlayer = min_num_player; }

	/**
	 * Gets the max num player.
	 *
	 * @return the max num player
	 */
	public int getMax_num_player() { return maxNumPlayer; }

	/**
	 * Sets the max num player.
	 *
	 * @param max_num_player
	 *            the new max num player
	 */
	public void setMax_num_player(final int max_num_player) { this.maxNumPlayer = max_num_player; }

	/**
	 * Checks for max num player.
	 *
	 * @return true, if successful
	 */
	public boolean has_max_num_player() {
		return hasMaxNumberPlayer;
	}

	/**
	 * Sets the checks for max num player.
	 *
	 * @param has_max_num_player
	 *            the new checks for max num player
	 */
	public void setHas_max_num_player(final boolean has_max_num_player) {
		this.hasMaxNumberPlayer = has_max_num_player;
	}
	//
	// public Map<String, Map<String, String>> getSpeciesToSend() {
	// if (speciesToSend == null) { speciesToSend = new Hashtable<>(); }
	// return speciesToSend;
	// }
	//
	// public void setSpeciesToSend(final Map<String, Map<String, String>> speciesToSend) {
	// this.speciesToSend = speciesToSend;
	// }
	//
	// public Map<String, Map<String, String>> getDefinedProperties() {
	// if (definedProperties == null) { definedProperties = new Hashtable<>(); }
	// return definedProperties;
	// }
	//
	// public void setDefinedProperties(final Map<String, Map<String, String>> definedProperties) {
	// this.definedProperties = definedProperties;
	// }
	//
	// public List<String> getPlayerProperties() { return playerProperties; }
	//
	// public void setPlayerProperties(final List<String> playerProperties) { this.playerProperties = playerProperties;
	// }

	public String getPlayerName(final int i) {
		return "Player" + i;
	}

	List<UnityPropertiesEditor> propertiesEditors = new ArrayList<>();

	public void removePropertiesListener(final UnityPropertiesEditor unityPropertiesEditor) {
		propertiesEditors.remove(unityPropertiesEditor);
	}

	public void addPropertiesListener(final UnityPropertiesEditor unityPropertiesEditor) {
		propertiesEditors.add(unityPropertiesEditor);
	}

	public void notifyPropertiesChanged() {
		for (UnityPropertiesEditor ed : propertiesEditors) { ed.propertiesChanged(); }
	}
}
