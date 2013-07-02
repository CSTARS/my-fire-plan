package gov.ca.ceres.myfireplan.client;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

import edu.ucdavis.gwt.gis.client.config.GadgetConfig;
import edu.ucdavis.gwt.gis.client.config.LayerConfig;

public class MyFirePlanConfig extends GadgetConfig {
	
	protected MyFirePlanConfig() {}
	
	public final native JsArray<LayerConfig> getIntersectLayers() /*-{
		if( this.intersectLayers ) return this.intersectLayers;
		return [];
	}-*/;

}
