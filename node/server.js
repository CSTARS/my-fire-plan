var express = require('express');
var request = require('request');
var csv = require('csv');
var app = express();
var drive = require('./drive');

var validUrls = require('./whitelist');

var allowCrossDomain = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Content-Length, X-Requested-With');
    
    // intercept OPTIONS method
    if ('OPTIONS' == req.method) {
    	
	    // make sure it's a valid proxy url
	    var url = getProxyUrl(req).replace(/.*:\/\//,'').replace(/\/.*/,'');
	    if( validUrls.list.indexOf(url) == -1 ) {
	    	console.log("Invalid proxy: "+url);
	    	return res.send(403)
	    }
    	
      res.send(200);
    } else {
      next();
    }
};

app.configure(function () {
  app.use(allowCrossDomain);
  app.use(express.bodyParser());
  app.use(express.cookieParser());
  app.use(express.methodOverride());
  app.use(express.session({secret: '1234567890QWERTY'}));
  drive.configure(app);
  app.use(app.router);
});

app.post('/export', function(req,res){
	var body = req.body;
	res.header('Content-Disposition', 'attachment; filename='+body.title+'.csv');
	
	csv()
	.from( JSON.parse(body.data) )
	.to( function(data){
	  res.send(data);
	});
});

app.post('/proxy', function(req, res){	
    // make sure it's a valid proxy url
    var url = getProxyUrl(req).replace(/.*:\/\//,'').replace(/\/.*/,'');
    if( validUrls.list.indexOf(url) == -1 ) {
    	console.log("Invalid proxy: "+url);
    	return res.send(403)
    }
	
	var body = req.body;
	var url = getProxyUrl(req);
    
	if( !body || url.length == "" ) {
		return res.send({error:true});
	}
	
	request.post(url, {form:body}, function (error, response, body) {
	  if (!error && response.statusCode == 200) {
		  try {
			  res.send(JSON.parse(body));
		  } catch (e) {
			  res.send({error:true,message:"json format error"});
		  }
	  } else {
		res.send(error);
	  }
	  
	});
});

drive.init();

function getProxyUrl(req) {
	var url = req.originalUrl.split("?");
	
	if( url.length == 1 ) return "";
	return url[1]
}


app.listen(3002);