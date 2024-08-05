/*******************************************************************************************************
 *
 * UnityAspect.java, in gaml.extension.unity, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.extension.unity.types;

import java.awt.Color;
import java.util.Map;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.GamaMapFactory;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;

/**
 * The Class UnityAspect.
 */

@vars ({ @variable (
		name = "prefab",
		type = IType.STRING,
		doc = @doc ("The prefab used to display the agent/geometry")),
	@variable (
				name = "size",
				type = IType.FLOAT,
				doc = @doc ("The size (scale) of the prefab used to display the geometry")),
	 @variable (
				name = "rotation_coeff",
				type = IType.FLOAT,
				doc = @doc ("Prefab: the rotation coefficient applied to the rotation along the y axe of the prefab used to display the agent/geometry")),
	 @variable (
				name = "rotation_offset",
				type = IType.FLOAT,
				doc = @doc ("Prefab: The rotation offset applied to the rotation along the y axe of the prefab used to display the agent/geometry")),
	 @variable (
				name = "y_offset",
				type = IType.FLOAT,
				doc = @doc ("Prefab: The offset translation along the y axe applied to the prefab used to display the agent/geometry")),
	@variable (
		name = "height",
		type = IType.FLOAT,
		doc = @doc ("Geometry: height of the geometry displayed in Unity (extrusion along the y-axe); if the height is 0, the geometry is considered as 2D")),
	@variable (
			name = "material",
			type = IType.STRING,
			doc = @doc ("The material used to display the agent/geometry")),
		 @variable (
		name = "color",
		type = IType.COLOR,
		doc = @doc ("Geometry: color of the geometry displayed in Unity"))


})
public class UnityAspect implements IValue {

	private String prefab;
	private String material;
	private double size;
	private double rotation_coeff;
	private double rotation_offset;
	private double y_offset;
	
	private double height;
	private GamaColor color;
	
	private boolean prefabAspect;
	
	private int precision;
	
	
	
	public UnityAspect(double height, GamaColor color, int precision) {
		super();
		this.precision =  precision;
		this.height = height;
		this.color = color;
		this.prefabAspect = false;
	}
	
	public UnityAspect(double height, String material,GamaColor color, int precision) {
		super();
		this.precision =  precision;
		this.height = height;
		this.material = material;
		this.color = color;
		this.prefabAspect = false;
	}

	public UnityAspect(String prefab, double size, double rotation_coeff, double rotation_offset, double y_offset, int precision) {
		super();
		this.precision =  precision;
		this.prefab = prefab;
		this.size = size;
		this.rotation_coeff = rotation_coeff;
		this.rotation_offset = rotation_offset;
		this.y_offset = y_offset;
		this.color =  GamaColor.get(Color.gray);
		this.prefabAspect = true;
	}

	
	
	
	
	public boolean isPrefabAspect() {
		return prefabAspect;
	}

	@getter ("prefab")
	public String getPrefab() {
		return prefab;
	}
	
	@getter ("material")
	public String getMaterial() {
		return material;
	}

	@getter ("size")
	public double getSize() {
		return size;
	}

	@getter ("rotation_coeff")
	public double getRotation_coeff() {
		return rotation_coeff;
	}

	@getter ("rotation_offset")
	public double getRotation_offset() {
		return rotation_offset;
	}

	@getter ("y_offset")
	public double getY_offset() {
		return y_offset;
	}

	@getter ("height")
	public double getHeight() {
		return height;
	}

	@getter ("color")
	public GamaColor getColor() {
		return color;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = GamaMapFactory.create();
		map.put("hasPrefab", prefabAspect);
		
		if (getPrefab() != null && !getPrefab().isBlank()) {
			map.put("prefab", prefab);
			map.put("size",(int) (size * precision));
			map.put("rotationCoeff", (int) (rotation_coeff * precision));
			map.put("rotationOffset", (int) (rotation_offset * precision));
			map.put("yOffset", (int) (y_offset * precision));
		} else {
			map.put("height", (int) (height * precision)); 
			map.put("is3D", height != 0.0); 
			map.put("red", color.red());
			map.put("green", color.green());
			map.put("blue", color.blue());
			map.put("alpha", color.alpha());
			map.put("material", material);
		}
		return map;
	}

	@Override
	public String toString() {
		if (prefab != null && !prefab.isBlank()) {
			return prefab + " - " + size + " - " + rotation_coeff + " - " + rotation_offset + " - " + y_offset;
		}
		
		return "geometry - " + height + " - "+ color;
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
