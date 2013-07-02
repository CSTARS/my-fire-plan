package gov.ca.ceres.myfireplan.client.intersect;

import java.util.HashMap;
import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.Error;
import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.callback.QueryTaskCallback;
import edu.ucdavis.cstars.client.event.ClickHandler;
import edu.ucdavis.cstars.client.event.MouseEvent;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.geometry.Point;
import edu.ucdavis.cstars.client.layers.LayerInfo;
import edu.ucdavis.cstars.client.tasks.FeatureSet;
import edu.ucdavis.cstars.client.tasks.Query;
import edu.ucdavis.cstars.client.tasks.QueryTask;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.config.LayerConfig;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;
import edu.ucdavis.gwt.gis.client.toolbar.Toolbar;
import edu.ucdavis.gwt.gis.client.toolbar.button.ToolbarItem;
import gov.ca.ceres.myfireplan.client.MyFirePlanConfig;

public class IntersectTool extends BootstrapModalLayout {
	
	private static IntersectToolUiBinder uiBinder = GWT.create(IntersectToolUiBinder.class);
	interface IntersectToolUiBinder extends UiBinder<Widget, IntersectTool> {}
	
	public final static IntersectTool INSTANCE = new IntersectTool();
	public final static IntersectMenuItem BUTTON = new IntersectMenuItem();


	private Widget panel;
	private MainMenuFooter footer = new MainMenuFooter();
	
	@UiField SimplePanel carouselRoot;
	@UiField CheckBox enable;
	@UiField CheckBox showLayer;

	private Graphic intersectGraphic = null;
	private MapServerDataLayer selectedLayer = null;
	private GetIntersectGeometry featureQuery = null;
	
	// layers that can be used for intersecting map layers
	private LinkedList<MapServerDataLayer> intersectLayers = new LinkedList<MapServerDataLayer>();
	// list of visible layers on the map
	private LinkedList<MapServerDataLayer> activeLayers = new LinkedList<MapServerDataLayer>();
	private HashMap<String, FindIntersectingGeometry> intersectQueries = new HashMap<String, FindIntersectingGeometry>();
	
	private boolean selectorInitialized = false;
	private JavaScriptObject selector = null;
	
	protected IntersectTool() {
		panel = uiBinder.createAndBindUi(this);
		
		// get layers from config
		MyFirePlanConfig config = (MyFirePlanConfig) AppManager.INSTANCE.getConfig();
		JsArray<LayerConfig> configLayers = config.getIntersectLayers();
		for( int i = 0; i < configLayers.length(); i++ ) {
			intersectLayers.add(new MapServerDataLayer(configLayers.get(i)));
		}

		createSelector(carouselRoot.getElement(), configLayers);
		
		enable.addClickHandler(new com.google.gwt.event.dom.client.ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( enable.getValue() ) {
					selectedLayer = intersectLayers.get(getActiveSlide(selector));
					if( showLayer.getValue() ) selectedLayer.setVisible(true);
				} else {
					selectedLayer.setVisible(false);
					selectedLayer = null;
				}
			}
		});
		
		showLayer.addClickHandler(new com.google.gwt.event.dom.client.ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				if( showLayer.getValue() && enable.getValue() && selectedLayer != null ) {
					selectedLayer.setVisible(true);
				} else if ( selectedLayer != null ) {
					selectedLayer.setVisible(false);
				}
			}
		});
		
		
		AppManager.INSTANCE.getMap().addClickHandler(new ClickHandler(){
			@Override
			public void onClick(MouseEvent event) {
				if( selectedLayer == null ) return;
				IntersectTool.this.onClick(event.getMapPoint());
			}
		});
	}
	
	private native void debug(JavaScriptObject jso) /*-{
		console.log(jso);
	}-*/;
	
	@Override
	protected void onShow() {
		if( !selectorInitialized ) {
			selectorInitialized = true;
			_initSelector(this);
		}
	}

	
	private native void createSelector(Element ele, JsArray<LayerConfig> layers) /*-{
		ele = $wnd.$(ele).html("");
		var layout = '<table id="IntersectTool-Slider" cellpadding="0" cellspacing="0" width="100%">'+
						'<tr><td align="left" valign="middle"><i class="icon-circle-arrow-left" style="font-size:36px;cursor:pointer"></i></td>'+
						'<td align="center"><div class="swiper-container">'+
							'<div class="swiper-wrapper"></div>'+
					 	'</div></td>'+
					 	'<td align="right" valign="middle"><i class="icon-circle-arrow-right" style="font-size:36px;cursor:pointer"></i></td><tr></table>';
		var selector = $wnd.$(layout);

		console.log(layers);
		for( var i = 0; i < layers.length; i++ ) {
			selector.find(".swiper-wrapper").append(
				$wnd.$('<div class="swiper-slide" style="width:250px;height:275px"><div id="IntersectTool-select-'+i+'" ></div>'+
						'<div style="text-align:center;style="font-size:18px"">'+layers[i].label+'</div></div>')
			);
		}

		for( var i = 0; i < layers.length; i++ ) {
			selector.find("#IntersectTool-select-"+i).esriPreview({
				url : layers[i].url,
				height : 250,
				width  : 250
			});
		}
		ele.append(selector);
		
	}-*/;
	
	private void onSlideChange() {
		if( enable.getValue() && showLayer.getValue() ) {
			selectedLayer.setVisible(false);
			selectedLayer = intersectLayers.get(getActiveSlide(selector));
			selectedLayer.setVisible(true);
		}
	}
	
	private native void _initSelector(IntersectTool it) /*-{
		setTimeout(function() {
			var mySwiper = new $wnd.Swiper(".swiper-container",{
				mode:'horizontal',
				loop: true,
				onSlideChangeEnd: function(){
					it.@gov.ca.ceres.myfireplan.client.intersect.IntersectTool::onSlideChange()();
				}
			});
			
			it.@gov.ca.ceres.myfireplan.client.intersect.IntersectTool::selector = mySwiper;
			
			$wnd.$("#IntersectTool-Slider .icon-circle-arrow-left").on('click', function() {
				mySwiper.swipePrev();
			});
			
			$wnd.$("#IntersectTool-Slider .icon-circle-arrow-right").on('click', function() {
				mySwiper.swipeNext();
			});
		}, 500);
	}-*/;
	
	private native int getActiveSlide(JavaScriptObject mySwiper) /*-{
		if( !mySwiper ) return 0;
		return mySwiper.activeSlide;
	}-*/;
	
	// find the geometry that was clicked on.  Then run the first intersection 
	// query and show popup
	private void onClick(Point p) {
		if( featureQuery != null ) featureQuery.cancel();
		
		activeLayers.clear();
		LinkedList<DataLayer> layers = AppManager.INSTANCE.getDataLayers();
		for( DataLayer dl: layers ) {
			if( dl.isVisible() && dl instanceof MapServerDataLayer ) {
				activeLayers.add((MapServerDataLayer) dl); 
			}
		}
		
		if( activeLayers.size() == 0 ) {
			Window.alert("You have to layers turned on.  Turn on the layers you wish to find the interects of.");
			return;
		}
		
		IntersectResult.INSTANCE.loading(true);
	
		featureQuery = new GetIntersectGeometry(new GetIntersectGeometryCallback(){
			@Override
			public void onGeometryReady(Graphic g) {
				intersectGraphic = g;
				IntersectResult.INSTANCE.showIntersectingGeo(intersectGraphic);
				query();
			}
		});
		featureQuery.getIntersectGeometry(p);
	}
	
	// run a fresh intersect query on the first layer
	// this clears the 'cache' 
	private void query() {
		for( String key: intersectQueries.keySet() ) {
			intersectQueries.get(key).cancel();
		}
		intersectQueries.clear();

		IntersectResult.INSTANCE.setLayerSelector(activeLayers, activeLayers.get(0).getLabel());
		getQuery(0);
	}
	
	// this should only be called by the IntersectResult as someone changes the layer
	public void getQuery(int index) {
		if( index > activeLayers.size() ) return;
		
		MapServerDataLayer dl = activeLayers.get(index);
		
		if( intersectQueries.containsKey(dl.getUrl()) ) {
			intersectQueries.get(dl.getUrl()).fireCallback();
		} else {
			intersectQueries.put(activeLayers.get(index).getUrl(), 
				new FindIntersectingGeometry(activeLayers.get(0),
						new FindIntersectingGeometryCallback(){
								@Override
								public void onGeometryReady(LinkedList<FeatureSet> fs) {
									IntersectResult.INSTANCE.showResult(intersectGraphic, fs);
								}
						}
				)
			);
			intersectQueries.get(activeLayers.get(index).getUrl()).query(intersectGraphic.getGeometry());
		}
	}
	

	@Override
	public String getTitle() {
		return "Intersect Tool";
	}

	@Override
	public Widget getBody() {
		return panel;
	}

	@Override
	public Widget getFooter() {
		return footer;
	}
	
	private interface FindIntersectingGeometryCallback {
		public void onGeometryReady(LinkedList<FeatureSet> fs);
	}
	
	private class FindIntersectingGeometry {
		private boolean cancel = false;
		private FindIntersectingGeometryCallback callback;
		private LinkedList<FeatureSet> featureSets = new LinkedList<FeatureSet>();
		private int responses = 0;
		private MapServerDataLayer dl;

		public FindIntersectingGeometry(MapServerDataLayer dl, FindIntersectingGeometryCallback callback) {
			this.callback = callback;
			this.dl = dl;
		}
	
		private void query(Geometry intersect) {
			for( int i = 0; i < dl.getLayersInfo().length(); i++ ) {
				LayerInfo info = dl.getLayersInfo().get(i);
				 
				QueryTask queryTask = QueryTask.create(dl.getUrl()+"/"+info.getId());
				Query q = Query.create();
				q.setGeometry(intersect);
				q.setOutFields(new String[] {"*"});

				makeRequest(q, queryTask, info.getName());
				
			}
			
		}
		
		private void makeRequest(Query q, QueryTask queryTask, final String name) {
			queryTask.execute(q, new QueryTaskCallback(){
				@Override
				public void onComplete(FeatureSet featureSet) {
					responses++;
					featureSets.add(featureSet);
					featureSet.setDisplayFieldName(name);
					
					if( responses == dl.getLayersInfo().length() && !cancel) {
						callback.onGeometryReady(featureSets);
					}

				}

				@Override
				public void onError(Error error) {
					responses++;
					if( responses == dl.getLayersInfo().length() && !cancel ) {
						callback.onGeometryReady(featureSets);
					}
				}
			});
		}
		
		public void cancel() {
			this.cancel = true;
		}
		
		public void fireCallback() {
			callback.onGeometryReady(featureSets);
		}

	}
	
	private interface GetIntersectGeometryCallback {
		public void onGeometryReady(Graphic g);
	}
	
	private class GetIntersectGeometry {
		private boolean cancel = false;
		private GetIntersectGeometryCallback callback;
		
		
		public GetIntersectGeometry(GetIntersectGeometryCallback callback) {
			this.callback = callback;
		}
	
		private void getIntersectGeometry(Point point) {
			double scale = Extent.getScale(AppManager.INSTANCE.getMap());
			// 5px buffer
			double screenScale = scale*0.000254*5;		
			Extent queryBox = Extent.create(
					point.getX()-screenScale, 
					point.getY()-screenScale, 
					screenScale+point.getX(), 
					screenScale+point.getY(), 
					AppManager.INSTANCE.getMap().getSpatialReference()
			);
			getIntersectGeometry(queryBox);
		}
		
		private void getIntersectGeometry(Extent point) {

			for( int i = 0; i < selectedLayer.getLayersInfo().length(); i++ ) {
				 LayerInfo info = selectedLayer.getLayersInfo().get(i);
				 
				 QueryTask queryTask = QueryTask.create(selectedLayer.getUrl()+"/"+info.getId());
					Query q = Query.create();
					q.setGeometry(point);
					q.setReturnGeometry(true);
					q.setOutSpatialReference(AppManager.INSTANCE.getMap().getSpatialReference());
					
					q.setOutFields(new String[] {"*"});

					queryTask.execute(q, new QueryTaskCallback(){
						@Override
						public void onComplete(FeatureSet featureSet) {
							if( cancel || featureSet == null ) return;
							
							// only grab the first geometry returned
							if( featureSet.getFeatures().length() > 0 ) {
								callback.onGeometryReady(featureSet.getFeatures().get(0));
								cancel();
							}
						}

						@Override
						public void onError(Error error) {}
					});
			}
			
		}
		
		public void cancel() {
			this.cancel = true;
		}

	}
	
	private static class IntersectMenuItem extends ToolbarItem {
		
		@Override
		public String getIcon() {
			return "<i class=\"icon-ellipsis-horizontal\"></i>";
		}
		
		@Override
		public String getText() {
			return "&nbsp;Intersect Tool";
		}

		@Override
		public void onClick() {
			IntersectTool.INSTANCE.show();
		}
		
		@Override
		public void onAdd(Toolbar toolbar) {}
	}

}
