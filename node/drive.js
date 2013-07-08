var request = require('request');
var passport = require('passport');
var googleapis = require('googleapis'),
	OAuth2Client = googleapis.OAuth2Client;
var oauth2Client =
    new OAuth2Client("277357850264.apps.googleusercontent.com", "U_wQqlVSt32nqFiydz2BBnW3", "http://localhost:3000/google/callback");
var app = null;
var users = {};
var driveClient = null;

var MAPS_FOLDER = "MyFirePlan-SavedMaps";
var MIME_TYPE = "application/vnd.gwt-gis.map";

exports.configure = function(express_app) {
	app = express_app;
	
	app.use(passport.initialize());
	app.use(passport.session());
}

exports.init = function() {
	app.get('/google/callback', function(req, res){
		oauth2Client.getToken(req.query.code, function(err, tokens) {
			// add current time to token, so we can know when it expires
			tokens.timestamp = new Date().getTime();
			users[req.cookies["connect.sid"]] = tokens;
			res.send({success:true});
		});
	});
	
	app.get('/google/login', function(req, res){
		var url = oauth2Client.generateAuthUrl({
		  scope: 'https://www.googleapis.com/auth/drive'
		});
		res.redirect(url);
	});
	
	app.get('/rest/listMaps', function(req, res){
		
		if( !req.cookies["connect.sid"] || !users[req.cookies["connect.sid"]] ) {
			return console.log({error:true,message:"You are not logged in"});
		}
		
		listMaps(req, res);
   });
	
   app.get('/rest/getMap', function(req, res){
	   var url = "https://www.googleapis.com/drive/v2/files/"+req.query.id;
	   console.log(url);
	   
	   request({
	    	  url: url,
	    	  headers: {
	    		  'Authorization' : 'Bearer '+users[req.cookies["connect.sid"]].access_token
	    	  },
	    	  method: 'GET'
	    }, function (err, resp, body) {
		  if (!err && res.statusCode == 200) {
			  getFile(req, JSON.parse(body).webContentLink, function(content){
				  res.send(content);
			  });
		  } else {
			res.send(err);
		  }
	    });
   });
	
   app.get('/rest/saveMap', function(req, res){
	   getMapsFolder(req, function(err, id){
			if( err ) return res.send(err);
			
			console.log("adding to folder: "+id);
			
		    var boundary = '-------314159265358979323846';
		    var delimiter = "\r\n--" + boundary + "\r\n";
		    var close_delim = "\r\n--" + boundary + "--";
		   
		    //var map = req.body.map;
		  	//var title = req.body.title;
		    var map = {
		    		title : "this is a test",
		    		center : {
		    			lat : 22,
		    			lng : 101.1
		    		},
		    		description : "here it is"
		    }
		    var title = "This is a test map";
		    
		  	var url = 'https://www.googleapis.com/upload/drive/v2/files';
		    
		    var metadata = {
		    	title: title,
		        mimeType: MIME_TYPE,
		        parents:[{id: id}]
		    }
		 
		    var base64Data = new Buffer(JSON.stringify(map)).toString('base64');
		    var multipartRequestBody =
		        delimiter +
		        'Content-Type: application/json\r\n\r\n' +
		        JSON.stringify(metadata) +
		        delimiter +
		        'Content-Type: ' + metadata.mimeType + '\r\n' +
		        'Content-Transfer-Encoding: base64\r\n' +
		        '\r\n' +
		        base64Data +
		        close_delim;
		    
		    console.log(multipartRequestBody);
		  
		    request({
		    	  url: url,
		    	  headers: {
		    		  'content-type' : 'multipart/mixed; boundary="' + boundary + '"',
		    		  'Authorization' : 'Bearer '+users[req.cookies["connect.sid"]].access_token
		    	  },
		    	  method: 'POST',
		    	  body: multipartRequestBody
		    }, function (err, resp, body) {
			  if (!err && res.statusCode == 200) {
			    console.log(body);
			  } else {
				console.log(err);
			  }
			  res.send(body);
		    });
	    

		});
   });
}

function getFile(req, url, callback) {
	console.log(url+" ***");
   request({
  	  url: url,
  	  headers: {
  		  'Authorization' : 'Bearer '+users[req.cookies["connect.sid"]].access_token
  	  },
  	  method: 'GET'
	}, function (err, resp, body) {
		console.log(err);
		console.log(body);
	  if (!err && resp.statusCode == 200) {
	    callback(null, body);
	  } else {
		callback(err);
	  }
  });
}

function getMapsFolder(req, callback) {
	var url = 'https://www.googleapis.com/drive/v2/files';
    var params = {
            access_token: users[req.cookies["connect.sid"]].access_token,
            q : "title = '"+MAPS_FOLDER+"' and mimeType = 'application/vnd.google-apps.folder'"
    };

    // get the maps folder
    request.get({url:url, qs:params}, function(err, resp, body) {
    	// Handle any errors that may occur
    	if (err) return callback(err);
    	
    	var list = JSON.parse(body);
    	if( list.error ) return callback(list.error);
    	
    	if( list.items.length == 0 ) {
    		createMapsFolder(req, callback);
    	} else {
    		callback(null, list.items[0].id);
    	}
    });
}


function createMapsFolder(req, callback) {
	var url = 'https://www.googleapis.com/drive/v2/files';
    
    var body = {
    		title: MAPS_FOLDER,
            mimeType: 'application/vnd.google-apps.folder'
    }

    // get the maps folder
    request({
    	  url: url,
    	  headers: {
    		  'content-type' : 'application/json',
    		  'Authorization' : 'Bearer '+users[req.cookies["connect.sid"]].access_token
    	  },
    	  method: 'POST',
    	  body: JSON.stringify(body)
    }, function (err, res, body) {
	  if (!err && res.statusCode == 200) {
	    callback(null, JSON.parse(body).id);
	  } else {
		callback(err);
	  }
    });
}

function listMaps(req, res) {
	var url = 'https://www.googleapis.com/drive/v2/files';
    var params = {
         access_token: users[req.cookies["connect.sid"]].access_token,
         q: "mimeType = '"+MIME_TYPE+"'"
    };

    request.get({url:url, qs:params}, function(err, resp, body) {
    	// Handle any errors that may occur
    	if (err) return res.send(error);
    	
    	var list = JSON.parse(body);
    	if( list.error ) return res.send(list);
    	
    	res.send({maps:list.items});
    });
}


passport.serializeUser(function(user, done) {
  done(null, user.id);
});

passport.deserializeUser(function(id, done) {
	if( users[id] ) done(null, users[id]);
	else done({error:true,message:"user not logged in"});
});


