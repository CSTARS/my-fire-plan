var express = require('express');
var request = require('request');
var csv = require('csv');
var app = express();

var allowCrossDomain = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Content-Length, X-Requested-With');
    
    // make sure it's a valid proxy url

    // intercept OPTIONS method
    if ('OPTIONS' == req.method) {
      res.send(200);
    } else {
      next();
    }
};

app.configure(function () {
  app.use(allowCrossDomain);
  app.use(express.bodyParser());
  app.use(express.methodOverride());
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
	var body = req.body;
	var url = req.originalUrl.split("?");
	
	if( !body || url.length == 1 ) {
		return res.send({error:true});
	} else {
		url = url[1];
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

app.listen(3000);