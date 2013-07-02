package gov.ca.ceres.maps.client;

import edu.ucdavis.gwt.gis.client.CompatibilityTester;
import edu.ucdavis.gwt.gis.client.DataManager;
import edu.ucdavis.gwt.gis.client.Debugger;
import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.GisClient.GisClientLoadHandler;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;
import edu.ucdavis.gwt.gis.client.layers.DataLayer.DataLayerLoadHandler;
import edu.ucdavis.gwt.gis.client.layout.LayerMenuCreateHandler;
import edu.ucdavis.gwt.gis.client.layout.LayerMenuItem;
import edu.ucdavis.gwt.gis.client.toolbar.button.AddFeaturesButton;
import edu.ucdavis.gwt.gis.client.toolbar.menu.AddLayerMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.BasemapMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.ExportMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.HelpMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.QueryMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.SaveLoadMenu;
import edu.ucdavis.gwt.gis.client.query.QueryToolSettings;
import edu.ucdavis.gwt.gis.client.query.RenderPopupHandler;
import edu.ucdavis.gwt.gis.client.resources.GadgetResources;
import gov.ca.ceres.maps.client.albumSearch.AlbumSearchWidget;
import gov.ca.ceres.maps.client.imagery.DownloadPopupContent;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.Graphic.Attributes;
import edu.ucdavis.cstars.client.layers.LayerInfo;
import edu.ucdavis.cstars.client.virtualearth.VETiledLayer;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Search implements EntryPoint {

	private GisClient mapClient = null;
	//private CeicSearch search = null;
	private AlbumSearchWidget search = new AlbumSearchWidget();
	private AddLayerMenu addLayerMenu = null;
	
	private String cbaseUrl = "http://ec2-50-18-14-244.us-west-1.compute.amazonaws.com/cgi-bin/cbase.py";
	
	public void onModuleLoad() {
		
		/*GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){
			@Override
			public void onUncaughtException(Throwable e) {
				String stackTrace = "";
				for( int i = 0; i < e.getStackTrace().length; i++ ) {
					StackTraceElement ele = e.getStackTrace()[i];
					stackTrace += ele.toString()+"<br />";
				}
				Debugger.INSTANCE.catchException(e, "Search", "Uncaught exception in CalAtlasMaps<br /><b>Trace</b><br>"+stackTrace);
			}
		});*/
		
		injectMobileMetaTag();
		
		CompatibilityTester.showDetails(false);
		GisClient.setLayerMenuCreateHandler(new LayerMenuCreateHandler(){
			@Override
			public LayerMenuItem onCreate(VerticalPanel menu, DataLayer dl) {
					BrowseMenuItem item = new BrowseMenuItem();
					item.init(dl);
					menu.add(item);
					return item;
			}
		});
		

		mapClient = new GisClient();
		
		String searchText = Window.Location.getParameter("search");
		if( searchText != null ) {
			search.show();
			search.search(searchText);
		}
		
		mapClient.load(new GisClientLoadHandler(){
			@Override
			public void onLoad() {
				onClientReady();
			}
		});

	}
	

		
	public void onClientReady() {

		mapClient.getToolbar().addToolbarMenu(new BasemapMenu());
		mapClient.getToolbar().addToolbarMenu(new QueryMenu());
		
		addLayerMenu = new AddLayerMenu();
		mapClient.getToolbar().addToolbarMenu(addLayerMenu);
		addLayerMenu.addItem(search.getIcon());	
		addLayerMenu.addItem(new AddFeaturesButton());
		mapClient.getToolbar().addToolbarMenu(new SaveLoadMenu());
		mapClient.getToolbar().addToolbarMenu(new ExportMenu());
		mapClient.getToolbar().addToolbarMenu(new HelpMenu());
		
		mapClient.expandLegends(true);
		

		
		String label = Window.Location.getParameter("download");
		if( label != null ) {
			if( DataManager.INSTANCE.getDataLayers().size() > 0 ) {
				DataLayer dl = DataManager.INSTANCE.getDataLayers().get(0);
				dl.setLabel(label);
				dl.addLoadHandler(new DataLayerLoadHandler(){
					@Override
					public void onDataLoaded(DataLayer dataLayer) {
						 ((MapServerDataLayer) dataLayer).zoomToLayerExtent();
						setupDownloadTool(dataLayer);
					}
				});
			}
		}
		
		if( GisClient.isIE7() || GisClient.isIE8() ) {
			mapClient.getRootPanel().getElement().getStyle().setProperty("border", "1px solid #aaaaaa");
		}
		
	}
	
	private void setupDownloadTool(DataLayer dataLayer) {
		QueryToolSettings.INSTANCE.selectServiceFromUrl(dataLayer.getUrl());
		DataManager.INSTANCE.getQueryTool().setRenderHandler(
			new RenderPopupHandler(){
				@Override
				public Widget createContent(String title, LayerInfo info, Graphic g) {
					DownloadPopupContent content = new DownloadPopupContent();
					content.setTitle("Download - "+title+" Products");
					
					content.setName(info.getName());
					
					Attributes attrs = g.getAttributes();
					JsArrayString keys = attrs.getKeys();
					for( int i = 0; i < keys.length(); i++ ) {
						content.addAttribute(keys.get(i), attrs.getStringForced(keys.get(i)));
					}
					
					return content;
				}
			}
		);
	}
	
	private class BrowseMenuItem extends LayerMenuItem {
		
		@Override
		public Image getIcon() {
			return new Image(GadgetResources.INSTANCE.server());
		}
		@Override
		public String getText() {
			return "Browse ArcGIS Server";
		}
		@Override
		public void onClick(DataLayer dataLayer) {
			addLayerMenu.getServiceAdder().browseServer(dataLayer.getUrl());
		}
	}
	
	
	private native void injectMobileMetaTag() /*-{
		try {
			var head = $wnd.document.getElementsByTagName('head')[0];
			
			var meta = $wnd.document.createElement('meta');
			meta.setAttribute('name', 'viewport');
			meta.setAttribute('content', 'width=device-width, initial-scale=1.0, maximum-scale=1.0; user-scalable=0;');
			head.appendChild(meta);
			
			meta = $wnd.document.createElement('meta');
			meta.setAttribute('name', 'viewport');
			meta.setAttribute('content', 'width=device-width');
			head.appendChild(meta);
			
			meta = $wnd.document.createElement('meta');
			meta.setAttribute('name', 'HandheldFriendly');
			meta.setAttribute('content', 'True');
			head.appendChild(meta);
		} catch (e) {}
	}-*/;

}
