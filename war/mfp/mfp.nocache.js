function mfp(){
  var $wnd_0 = window, $doc_0 = document, $stats = $wnd_0.__gwtStatsEvent?function(a){
    return $wnd_0.__gwtStatsEvent(a);
  }
  :null, $sessionId_0 = $wnd_0.__gwtStatsSessionId?$wnd_0.__gwtStatsSessionId:null, gwtOnLoad, bodyDone, base = '', metaProps = {}, values = [], providers = [], answers = [], softPermutationId = 0, onLoadErrorFunc, propertyErrorFunc;
  $stats && $stats({moduleName:'mfp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'begin'});
  if (!$wnd_0.__gwt_stylesLoaded) {
    $wnd_0.__gwt_stylesLoaded = {};
  }
  if (!$wnd_0.__gwt_scriptsLoaded) {
    $wnd_0.__gwt_scriptsLoaded = {};
  }
  function isHostedMode(){
    var result = false;
    try {
      var query = $wnd_0.location.search;
      return (query.indexOf('gwt.codesvr=') != -1 || (query.indexOf('gwt.hosted=') != -1 || $wnd_0.external && $wnd_0.external.gwtOnLoad)) && query.indexOf('gwt.hybrid') == -1;
    }
     catch (e) {
    }
    isHostedMode = function(){
      return result;
    }
    ;
    return result;
  }

  function maybeStartModule(){
    if (gwtOnLoad && bodyDone) {
      gwtOnLoad(onLoadErrorFunc, 'mfp', base, softPermutationId);
      $stats && $stats({moduleName:'mfp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date).getTime(), type:'end'});
    }
  }

  function computeScriptBase(){
    function getDirectoryOfFile(path){
      var hashIndex = path.lastIndexOf('#');
      if (hashIndex == -1) {
        hashIndex = path.length;
      }
      var queryIndex = path.indexOf('?');
      if (queryIndex == -1) {
        queryIndex = path.length;
      }
      var slashIndex = path.lastIndexOf('/', Math.min(queryIndex, hashIndex));
      return slashIndex >= 0?path.substring(0, slashIndex + 1):'';
    }

    function ensureAbsoluteUrl(url){
      if (url.match(/^\w+:\/\//)) {
      }
       else {
        var img = $doc_0.createElement('img');
        img.src = url + 'clear.cache.gif';
        url = getDirectoryOfFile(img.src);
      }
      return url;
    }

    function tryMetaTag(){
      var metaVal = __gwt_getMetaProperty('baseUrl');
      if (metaVal != null) {
        return metaVal;
      }
      return '';
    }

    function tryNocacheJsTag(){
      var scriptTags = $doc_0.getElementsByTagName('script');
      for (var i = 0; i < scriptTags.length; ++i) {
        if (scriptTags[i].src.indexOf('mfp.nocache.js') != -1) {
          return getDirectoryOfFile(scriptTags[i].src);
        }
      }
      return '';
    }

    function tryMarkerScript(){
      var thisScript;
      if (typeof isBodyLoaded == 'undefined' || !isBodyLoaded()) {
        var markerId = '__gwt_marker_mfp';
        var markerScript;
        $doc_0.write('<script id="' + markerId + '"><\/script>');
        markerScript = $doc_0.getElementById(markerId);
        thisScript = markerScript && markerScript.previousSibling;
        while (thisScript && thisScript.tagName != 'SCRIPT') {
          thisScript = thisScript.previousSibling;
        }
        if (markerScript) {
          markerScript.parentNode.removeChild(markerScript);
        }
        if (thisScript && thisScript.src) {
          return getDirectoryOfFile(thisScript.src);
        }
      }
      return '';
    }

    function tryBaseTag(){
      var baseElements = $doc_0.getElementsByTagName('base');
      if (baseElements.length > 0) {
        return baseElements[baseElements.length - 1].href;
      }
      return '';
    }

    function isLocationOk(){
      var loc = $doc_0.location;
      return loc.href == loc.protocol + '//' + loc.host + loc.pathname + loc.search + loc.hash;
    }

    var tempBase = tryMetaTag();
    if (tempBase == '') {
      tempBase = tryNocacheJsTag();
    }
    if (tempBase == '') {
      tempBase = tryMarkerScript();
    }
    if (tempBase == '') {
      tempBase = tryBaseTag();
    }
    if (tempBase == '' && isLocationOk()) {
      tempBase = getDirectoryOfFile($doc_0.location.href);
    }
    tempBase = ensureAbsoluteUrl(tempBase);
    base = tempBase;
    return tempBase;
  }

  function processMetas(){
    var metas = document.getElementsByTagName('meta');
    for (var i = 0, n = metas.length; i < n; ++i) {
      var meta = metas[i], name_0 = meta.getAttribute('name'), content_0;
      if (name_0) {
        name_0 = name_0.replace('mfp::', '');
        if (name_0.indexOf('::') >= 0) {
          continue;
        }
        if (name_0 == 'gwt:property') {
          content_0 = meta.getAttribute('content');
          if (content_0) {
            var value, eq = content_0.indexOf('=');
            if (eq >= 0) {
              name_0 = content_0.substring(0, eq);
              value = content_0.substring(eq + 1);
            }
             else {
              name_0 = content_0;
              value = '';
            }
            metaProps[name_0] = value;
          }
        }
         else if (name_0 == 'gwt:onPropertyErrorFn') {
          content_0 = meta.getAttribute('content');
          if (content_0) {
            try {
              propertyErrorFunc = eval(content_0);
            }
             catch (e) {
              alert('Bad handler "' + content_0 + '" for "gwt:onPropertyErrorFn"');
            }
          }
        }
         else if (name_0 == 'gwt:onLoadErrorFn') {
          content_0 = meta.getAttribute('content');
          if (content_0) {
            try {
              onLoadErrorFunc = eval(content_0);
            }
             catch (e) {
              alert('Bad handler "' + content_0 + '" for "gwt:onLoadErrorFn"');
            }
          }
        }
      }
    }
  }

  function __gwt_getMetaProperty(name_0){
    var value = metaProps[name_0];
    return value == null?null:value;
  }

  function unflattenKeylistIntoAnswers(propValArray, value){
    var answer = answers;
    for (var i = 0, n = propValArray.length - 1; i < n; ++i) {
      answer = answer[propValArray[i]] || (answer[propValArray[i]] = []);
    }
    answer[propValArray[n]] = value;
  }

  function computePropValue(propName){
    var value = providers[propName](), allowedValuesMap = values[propName];
    if (value in allowedValuesMap) {
      return value;
    }
    var allowedValuesList = [];
    for (var k in allowedValuesMap) {
      allowedValuesList[allowedValuesMap[k]] = k;
    }
    if (propertyErrorFunc) {
      propertyErrorFunc(propName, allowedValuesList, value);
    }
    throw null;
  }

  providers['user.agent'] = function(){
    var ua = navigator.userAgent.toLowerCase();
    var makeVersion = function(result){
      return parseInt(result[1]) * 1000 + parseInt(result[2]);
    }
    ;
    if (function(){
      return ua.indexOf('opera') != -1;
    }
    ())
      return 'opera';
    if (function(){
      return ua.indexOf('webkit') != -1;
    }
    ())
      return 'safari';
    if (function(){
      return ua.indexOf('msie') != -1 && $doc_0.documentMode >= 9;
    }
    ())
      return 'ie9';
    if (function(){
      return ua.indexOf('msie') != -1 && $doc_0.documentMode >= 8;
    }
    ())
      return 'ie8';
    if (function(){
      var result = /msie ([0-9]+)\.([0-9]+)/.exec(ua);
      if (result && result.length == 3)
        return makeVersion(result) >= 6000;
    }
    ())
      return 'ie6';
    if (function(){
      return ua.indexOf('gecko') != -1;
    }
    ())
      return 'gecko1_8';
    return 'unknown';
  }
  ;
  values['user.agent'] = {gecko1_8:0, ie6:1, ie8:2, ie9:3, opera:4, safari:5};
  mfp.onScriptLoad = function(gwtOnLoadFunc){
    mfp.onScriptLoad = null;
    gwtOnLoad = gwtOnLoadFunc;
    maybeStartModule();
  }
  ;
  if (isHostedMode()) {
    alert('Cross-site hosted mode not yet implemented. See issue ' + 'http://code.google.com/p/google-web-toolkit/issues/detail?id=2079');
    return;
  }
  processMetas();
  computeScriptBase();
  $stats && $stats({moduleName:'mfp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'selectingPermutation'});
  var strongName;
  try {
    unflattenKeylistIntoAnswers(['safari'], '3381A3E1D06C7896615C2EB9DE85D5D1');
    unflattenKeylistIntoAnswers(['ie6'], '7CD60C7A4593CA15E12F68DF64A0D95E');
    unflattenKeylistIntoAnswers(['ie8'], '8571E999606120183F797DADA8BB69ED');
    unflattenKeylistIntoAnswers(['gecko1_8'], '949F09DEA28DBDE3334F849547C4E9C0');
    unflattenKeylistIntoAnswers(['ie9'], 'A37C77D8BEC6D602928F2784E28CBC7B');
    unflattenKeylistIntoAnswers(['opera'], 'F9E64DC1B1BF3491018B591B08569A72');
    strongName = answers[computePropValue('user.agent')];
    var idx = strongName.indexOf(':');
    if (idx != -1) {
      softPermutationId = Number(strongName.substring(idx + 1));
      strongName = strongName.substring(0, idx);
    }
  }
   catch (e) {
    return;
  }
  var onBodyDoneTimerId;
  function onBodyDone(){
    if (!bodyDone) {
      bodyDone = true;
      if (!__gwt_stylesLoaded['http://serverapi.arcgisonline.com/jsapi/arcgis/3.4/js/dojo/dijit/themes/claro/claro.css']) {
        var l = $doc_0.createElement('link');
        __gwt_stylesLoaded['http://serverapi.arcgisonline.com/jsapi/arcgis/3.4/js/dojo/dijit/themes/claro/claro.css'] = l;
        l.setAttribute('rel', 'stylesheet');
        l.setAttribute('href', 'http://serverapi.arcgisonline.com/jsapi/arcgis/3.4/js/dojo/dijit/themes/claro/claro.css');
        $doc_0.getElementsByTagName('head')[0].appendChild(l);
      }
      if (!__gwt_stylesLoaded['http://serverapi.arcgisonline.com/jsapi/arcgis/3.4/js/esri/css/esri.css']) {
        var l = $doc_0.createElement('link');
        __gwt_stylesLoaded['http://serverapi.arcgisonline.com/jsapi/arcgis/3.4/js/esri/css/esri.css'] = l;
        l.setAttribute('rel', 'stylesheet');
        l.setAttribute('href', 'http://serverapi.arcgisonline.com/jsapi/arcgis/3.4/js/esri/css/esri.css');
        $doc_0.getElementsByTagName('head')[0].appendChild(l);
      }
      if (!__gwt_stylesLoaded['GwtGisClient.css']) {
        var l = $doc_0.createElement('link');
        __gwt_stylesLoaded['GwtGisClient.css'] = l;
        l.setAttribute('rel', 'stylesheet');
        l.setAttribute('href', base + 'GwtGisClient.css');
        $doc_0.getElementsByTagName('head')[0].appendChild(l);
      }
      if (!__gwt_stylesLoaded['css/bootstrap.min.css']) {
        var l = $doc_0.createElement('link');
        __gwt_stylesLoaded['css/bootstrap.min.css'] = l;
        l.setAttribute('rel', 'stylesheet');
        l.setAttribute('href', base + 'css/bootstrap.min.css');
        $doc_0.getElementsByTagName('head')[0].appendChild(l);
      }
      if (!__gwt_stylesLoaded['css/bootstrap-responsive.min.css']) {
        var l = $doc_0.createElement('link');
        __gwt_stylesLoaded['css/bootstrap-responsive.min.css'] = l;
        l.setAttribute('rel', 'stylesheet');
        l.setAttribute('href', base + 'css/bootstrap-responsive.min.css');
        $doc_0.getElementsByTagName('head')[0].appendChild(l);
      }
      if (!__gwt_stylesLoaded['css/font-awesome.css']) {
        var l = $doc_0.createElement('link');
        __gwt_stylesLoaded['css/font-awesome.css'] = l;
        l.setAttribute('rel', 'stylesheet');
        l.setAttribute('href', base + 'css/font-awesome.css');
        $doc_0.getElementsByTagName('head')[0].appendChild(l);
      }
      if (!__gwt_stylesLoaded['MyFirePlan.css']) {
        var l = $doc_0.createElement('link');
        __gwt_stylesLoaded['MyFirePlan.css'] = l;
        l.setAttribute('rel', 'stylesheet');
        l.setAttribute('href', base + 'MyFirePlan.css');
        $doc_0.getElementsByTagName('head')[0].appendChild(l);
      }
      if (!__gwt_stylesLoaded['idangerous.swiper.css']) {
        var l = $doc_0.createElement('link');
        __gwt_stylesLoaded['idangerous.swiper.css'] = l;
        l.setAttribute('rel', 'stylesheet');
        l.setAttribute('href', base + 'idangerous.swiper.css');
        $doc_0.getElementsByTagName('head')[0].appendChild(l);
      }
      maybeStartModule();
      if ($doc_0.removeEventListener) {
        $doc_0.removeEventListener('DOMContentLoaded', onBodyDone, false);
      }
      if (onBodyDoneTimerId) {
        clearInterval(onBodyDoneTimerId);
      }
    }
  }

  if ($doc_0.addEventListener) {
    $doc_0.addEventListener('DOMContentLoaded', function(){
      onBodyDone();
    }
    , false);
  }
  var onBodyDoneTimerId = setInterval(function(){
    if (/loaded|complete/.test($doc_0.readyState)) {
      onBodyDone();
    }
  }
  , 50);
  $stats && $stats({moduleName:'mfp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'bootstrap', millis:(new Date).getTime(), type:'end'});
  $stats && $stats({moduleName:'mfp', sessionId:$sessionId_0, subSystem:'startup', evtGroup:'loadExternalRefs', millis:(new Date).getTime(), type:'begin'});
  if (!__gwt_scriptsLoaded['js/esri_3_4_patch.js']) {
    __gwt_scriptsLoaded['js/esri_3_4_patch.js'] = true;
    document.write('<script language="javascript" src="' + base + 'js/esri_3_4_patch.js"><\/script>');
  }
  if (!__gwt_scriptsLoaded['http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js']) {
    __gwt_scriptsLoaded['http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'] = true;
    document.write('<script language="javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"><\/script>');
  }
  if (!__gwt_scriptsLoaded['https://apis.google.com/js/client.js?onload=ginit']) {
    __gwt_scriptsLoaded['https://apis.google.com/js/client.js?onload=ginit'] = true;
    document.write('<script language="javascript" src="https://apis.google.com/js/client.js?onload=ginit"><\/script>');
  }
  if (!__gwt_scriptsLoaded['js/bootstrap.min.js']) {
    __gwt_scriptsLoaded['js/bootstrap.min.js'] = true;
    document.write('<script language="javascript" src="' + base + 'js/bootstrap.min.js"><\/script>');
  }
  if (!__gwt_scriptsLoaded['idangerous.swiper-1.9.min.js']) {
    __gwt_scriptsLoaded['idangerous.swiper-1.9.min.js'] = true;
    document.write('<script language="javascript" src="' + base + 'idangerous.swiper-1.9.min.js"><\/script>');
  }
  if (!__gwt_scriptsLoaded['jquery.esriPreview.js']) {
    __gwt_scriptsLoaded['jquery.esriPreview.js'] = true;
    document.write('<script language="javascript" src="' + base + 'jquery.esriPreview.js"><\/script>');
  }
  var compiledScriptTag = '"<script src=\\"' + base + strongName + '.cache.js\\"><\/scr" + "ipt>"';
  $doc_0.write('<scr' + 'ipt><!-' + '-\n' + 'window.__gwtStatsEvent && window.__gwtStatsEvent({' + 'moduleName:"mfp", sessionId:window.__gwtStatsSessionId, subSystem:"startup",' + 'evtGroup: "loadExternalRefs", millis:(new Date()).getTime(),' + 'type: "end"});' + 'window.__gwtStatsEvent && window.__gwtStatsEvent({' + 'moduleName:"mfp", sessionId:window.__gwtStatsSessionId, subSystem:"startup",' + 'evtGroup: "moduleStartup", millis:(new Date()).getTime(),' + 'type: "moduleRequested"});' + 'document.write(' + compiledScriptTag + ');' + '\n-' + '-><\/scr' + 'ipt>');
}

mfp();
