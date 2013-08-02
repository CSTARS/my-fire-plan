package gov.ca.ceres.myfireplan.client;

import edu.ucdavis.gwt.gis.client.GisClient;
import edu.ucdavis.gwt.gis.client.GisClient.GisClientLoadHandler;
import edu.ucdavis.gwt.gis.client.layers.DataLayer;
import edu.ucdavis.gwt.gis.client.layout.LayerMenuCreateHandler;
import edu.ucdavis.gwt.gis.client.layout.LayerMenuItem;
import edu.ucdavis.gwt.gis.client.toolbar.button.AddFeaturesButton;
import edu.ucdavis.gwt.gis.client.toolbar.menu.AddLayerMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.BasemapMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.ExportMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.HelpMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.QueryMenu;
import edu.ucdavis.gwt.gis.client.toolbar.menu.SaveLoadMenu;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MyFirePlan implements EntryPoint {

	private GisClient mapClient = null;
	private AddLayerMenu addLayerMenu = null;

	
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
		
		mapClient.load(new GisClientLoadHandler(){
			@Override
			public void onLoad() {
				onClientReady();
			}
		});

		
	}
	

		
	public void onClientReady() {

		mapClient.getToolbar().addToolbarMenu(new BasemapMenu());

		QueryMenu queryMenu = new QueryMenu();
		mapClient.getToolbar().addToolbarMenu(queryMenu);

		addLayerMenu = new AddLayerMenu();
		mapClient.getToolbar().addToolbarMenu(addLayerMenu);
		addLayerMenu.addItem(new AddFeaturesButton());
		mapClient.getToolbar().addToolbarMenu(new SaveLoadMenu());
		mapClient.getToolbar().addToolbarMenu(new ExportMenu());
		mapClient.getToolbar().addToolbarMenu(new HelpMenu());

		if( GisClient.isIE7() || GisClient.isIE8() ) {
			mapClient.getRootPanel().getElement().getStyle().setProperty("border", "1px solid #aaaaaa");
		}

	}

	private class BrowseMenuItem extends LayerMenuItem {
		
		@Override
		public String getIcon() {
			return "<i class='icon-cloud'></i>";
		}
		@Override
		public String getText() {
			return "&nbsp;Browse ArcGIS Server";
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
