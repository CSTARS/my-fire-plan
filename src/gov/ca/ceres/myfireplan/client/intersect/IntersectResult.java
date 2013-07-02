package gov.ca.ceres.myfireplan.client.intersect;

import java.util.HashMap;
import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.Graphic.Attributes;
import edu.ucdavis.cstars.client.tasks.FeatureSet;
import edu.ucdavis.gwt.gis.client.AppManager;
import edu.ucdavis.gwt.gis.client.layers.MapServerDataLayer;
import edu.ucdavis.gwt.gis.client.layout.modal.BootstrapModalLayout;
import edu.ucdavis.gwt.gis.client.layout.modal.MainMenuFooter;

public class IntersectResult extends BootstrapModalLayout {
	
	private static IntersectResultUiBinder uiBinder = GWT.create(IntersectResultUiBinder.class);
	interface IntersectResultUiBinder extends UiBinder<Widget, IntersectResult> {}
	
	public final static IntersectResult INSTANCE = new IntersectResult(); 

	private Widget panel;
	private MainMenuFooter footer = new MainMenuFooter();
	
	// store the current table data, good for when data is exported
	private JavaScriptObject currentTableData;
	
	@UiField FlowPanel selectedGeoAttrs;
	@UiField Element layerResultTable;
	@UiField Element layerResultHeader;
	
	public IntersectResult() {
		panel = uiBinder.createAndBindUi(this);
	}

	private native Element addLayerSelectWidget(Element ele, String selected, JavaScriptObject options, IntersectResult ir) /*-{
		var nav = $wnd.$('<ul class="nav nav-pills" style="font-size:16px;margin-bottom:10px;border-bottom:1px solid #ccc">'+
						'<li class="dropdown">'+
							'<a class="dropdown-toggle" data-toggle="dropdown" style="cursor:pointer" id="intersect-title-dd"  role="button" >'+selected+'<b class="caret"></b></a>'+
							'<ul class="dropdown-menu" role="menu" aria-labelledby="intersect-title-dd">'+
							'</ul>'+
						'</li>'+
					'</ul>');
		var menu = nav.find(".dropdown-menu");
		for( var i = 0; i < options.length; i++ ) {
			menu.append($wnd.$("<li role='presentation'><a role='menuitem' value='"+i+"'>"+options[i]+"</a></li>"));
		}
		menu.find('a').on('click', function() {
			nav.find('.dropdown-toggle').text($wnd.$(this).text());
			var index = parseInt($wnd.$(this).attr("value"));
			ir.@gov.ca.ceres.myfireplan.client.intersect.IntersectResult::onTitleChange(I)(index);
		});
		
		$wnd.$(ele).html("").append(nav);
		return nav[0];
	}-*/;

	// fired when a new result layer is selected
	private void onTitleChange(int index) {
		loading(false);
		IntersectTool.INSTANCE.getQuery(index);
	}
	
	public void loading(boolean clearAttrs) {
		if( clearAttrs ) {
			selectedGeoAttrs.clear();
			selectedGeoAttrs.add(new HTML("Finding geometry..."));
		}
		_clear(layerResultTable);
		layerResultTable.setInnerHTML("Loading...");
		if( !isVisible() ) show();
	}
	
	public void setLayerSelector(LinkedList<MapServerDataLayer> layers, String selected) {
		JsArrayString arr = JavaScriptObject.createArray().cast();
		for( int i = 0; i < layers.size(); i++ ) {
			_push(arr, layers.get(i).getLabel());
		}
		addLayerSelectWidget(layerResultHeader, selected, arr, this);
	}
	
	private native void _push(JavaScriptObject jso, String attr) /*-{
		jso.push(attr);
	}-*/;
	
	private native void _push(JavaScriptObject jso, JavaScriptObject attr) /*-{
		jso.push(attr);
	}-*/;
	
	private native void _put(JavaScriptObject jso, String attr, JavaScriptObject value) /*-{
		jso[attr] = value;
	}-*/;
	
	public void showIntersectingGeo(Graphic selectedGeo) {
		selectedGeoAttrs.clear();
		JsArrayString keys = selectedGeo.getAttributes().getKeys();
		for( int i = 0; i < keys.length(); i++) {
			selectedGeoAttrs.add(new HTML("<b>"+keys.get(i)+":</b> "+selectedGeo.getAttributes().getStringForced(keys.get(i))));
		}
	}
	
	public void showResult(Graphic selectedGeo, LinkedList<FeatureSet> result) {
		showIntersectingGeo(selectedGeo);
		
		_clear(layerResultTable);
		currentTableData = JavaScriptObject.createObject();
		
		if( result.size() == 0 ) {
			layerResultTable.setInnerHTML("No interesting geometries found.");
		} else {
			for( FeatureSet fs: result ) {
				addTable(fs.getDisplayFieldName(), fs.getFeatures());
			}
		}
		
		if( !isVisible() ) show();
	}
	
	private void addTable(String title, JsArray<Graphic> features) {
		// create export array
		JavaScriptObject arr = JavaScriptObject.createArray();
		
		// get attribute list
		LinkedList<String> attrs = new LinkedList<String>();
		for( int i = 0; i < features.length(); i++ ) {
			JsArrayString keys = features.get(i).getAttributes().getKeys();
			for( int j = 0; j < keys.length(); j++ ) {
				if( !attrs.contains(keys.get(j)) ) attrs.add(keys.get(j));
			}
		}
		
		String table = "<table class='table table-striped'><tr>";
		JavaScriptObject row = JavaScriptObject.createArray();
		for( int i = 0; i < attrs.size(); i++ ) {
			table += "<th>"+attrs.get(i)+"</th>";
			_push(row, attrs.get(i));
		}
		_push(arr, row);
		table += "</tr>";
		
		for( int i = 0; i < features.length(); i++ ) {
			table += "<tr>";
			Attributes aList = features.get(i).getAttributes();
			row = JavaScriptObject.createArray();
			for( int j = 0; j < attrs.size(); j++ ) {
				if( aList.hasKey(attrs.get(j)) ) {
					table += "<td>"+aList.getStringForced(attrs.get(j))+"</td>";
					_push(row, aList.getStringForced(attrs.get(j)));
				} else {
					table += "<td>&nbsp;</td>";
					_push(row, "");
				}
				
			}
			_push(arr, row);
			table += "</tr>";
		}
		
		_put(currentTableData, title, arr);
		_setTable(layerResultTable, title, table, this, AppManager.INSTANCE.getConfig().getProxy());
	}
	
	private native void _clear(Element ele) /*-{
		$wnd.$(ele).html("");
	}-*/;
	
	private native void _setTable(Element ele, String title, String table, IntersectResult ir, String exportUrl) /*-{
		var exportBtn = $wnd.$('<div style="height:45px">'+title+' <i class="icon-download-alt pull-right btn" value="'+title+'" style="font-size:28px" ></i></div>');
		exportBtn.find('i').on('click', function(){
			var title = $wnd.$(this).attr("value");
			var tables = ir.@gov.ca.ceres.myfireplan.client.intersect.IntersectResult::currentTableData;
			if( tables[title] ) {
				
				var form = $wnd.$('<form name="input" style="display:none" target="_blank" action="'+exportUrl.replace("/proxy","/export")+'" method="post">' +
									'<input type="text" name="title">'+
									'<input type="text" name="data">'+
								  '</form>');
				
				form.find("input[name=title]").val(title);
				form.find("input[name=data]").val(JSON.stringify(tables[title]));
				form.submit();
				
				// TODO: not attaching to DOM for now... do we need to for good old IE?
				//setTimeout(function(){
				//	form.remove();	
				//},10000);
			}
		});
		
		$wnd.$(ele).append(exportBtn);
		$wnd.$(ele).append($wnd.$(table));
	}-*/;
	
	@Override
	public String getTitle() {
		return "Intersection Result";
	}

	@Override
	public Widget getBody() {
		return panel;
	}

	@Override
	public Widget getFooter() {
		return footer;
	}
	
}
