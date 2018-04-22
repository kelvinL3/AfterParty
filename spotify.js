var request = require('request');

const ngrokLink = "http://3ee1f2ac.ngrok.io/";

module.exports = {
	requestNextSong: function(id, ip, sourceAcc, playSong) {


		console.log("Requesting song for room: " + id);

		var options = {
		    url: ngrokLink + 'speaker/getnextsong/' + id,
		    port: 80,
		    method: 'GET',
		    json:true
		}
		request(options, function(error, response, body){
		    if(error) console.log(error);
		    else if (body.error) {
				console.log("requestNextSong ERROR THAT'S KELVIN'S FAULT: " + body.error);
		    } else {
		    	const nextSong = body["spotify_uri"];
		    	const songName = body.song;
		        playSong(id, ip, sourceAcc, nextSong, songName);
		    }
		});

	},

	userPost: function(obj, cb) {

		console.log("CALLING USERPOST WITH OBJ: " + JSON.stringify(obj));

		request.post(
		    ngrokLink + 'user/post',
		    { json: obj },
		    function (error, response, body) {
		        if (error || response.statusCode != 200) {
		            console.log("File Spotify.js userPost error: " + body);
		        } else if (body.error) {
		        	console.log("userPost ERROR THAT'S KELVIN'S FAULT: " + body.error);
		        } else {
		        	if (cb) {
		        		cb(body);
		        	}
		        }
		    }
		);
	},

	next3: function(id, cb) {

		console.log("Calling next3 with id: " + id);

		var options = {
		    url: ngrokLink + 'speaker/preview/' + id,
		    port: 80,
		    method: 'GET',
		    json:true
		}
		request(options, function(error, response, body){
		    if(error || !body) console.log(error);
		    else if (body.error) {
				console.log("next3 ERROR THAT'S KELVIN'S FAULT: " + body.error);
		    } else {
		    	cb(body);
		    }
		});
	}
}