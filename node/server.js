var express = require('express');
var request = require('request');
var csv = require('csv');
var app = express();

var validUrls = require('./whitelist');

process.on('uncaughtException', function(err) {
    // handle the error safely
    console.log(err);
});

var allowCrossDomain = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Content-Length, X-Requested-With');
    
    console.log("Request Received, routing through cross-domain middleware");
    
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
  app.use(express.logger());
  app.use(express.methodOverride());
  app.use(express.session({secret: '1234567890QWERTY'}));
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
	
    console.log("Proxy request received: "+url);
    
	var body = req.body;
	var url = getProxyUrl(req);
    
	if( !body || url.length == "" ) {
		return res.send({error:true});
	}
	
	request.post(url, {form:body}, function (error, response, body) {
      console.log("Proxy response received: "+url);
	  if (!error && response.statusCode == 200) {
		  try {
			  return res.send(JSON.parse(body));
		  } catch (e) {
			  return res.send({error:true,message:"json format error"});
		  }
	  }
	  return res.send(error);
	});
});

// currently there is no way to let a non-loggedin user to access a public map via xhr...
// using this proxy for now
app.get('/loadPublicMap', function(req, res) {
	var url = req.query.url;
	
	if( !url ) return res.send({error:true,message:"no download link provided"});
	 console.log("Loading public map: "+url);
	
	var host = url.replace(/^https:\/\//,'').split("/")[0];
	if( !url.match(/.*docs.google\.com/ ) ) return res.send({error:true,message:"invalid link"});
	
	request.get(url, function (error, response, body) {
		console.log("Public map update: "+url);
		
	  if (!error && response.statusCode == 200) {
		  try {
			  return res.send(JSON.parse(body));
		  } catch (e) {
			  return res.send({error:true,message:"json format error"});
		  }
	  }
	  return res.send(error);
	  
	  
	});
	
});


function getProxyUrl(req) {
	var url = req.originalUrl.split("?");
	
	if( url.length == 1 ) return "";
	return url[1]
}


app.listen(4002);
