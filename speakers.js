var request = require('request');
const WebSocket = require('ws');
const spotify = require('./spotify');

var currentSongName = ["", ""];


var io;


var playbackState = ["paused", "paused"];

const bozeIPs = ["192.168.1.246", "192.168.1.136"];
const sourceAccs = ["1229288988", "shashank135sharma"];
var stuffPlaying = [false, false];

module.exports = {
	playSong: function(pos, ip, sourceAcc, songString, songName) {

		console.log("play song called from room : " + pos);

		const address = "http://" + ip + ":8090/select";
		const req = '<ContentItem source="SPOTIFY" type="uri" location="'+songString+'" sourceAccount="'+sourceAcc+'"></ContentItem>';

		request({
		    url: address,
		    method: "POST",
		    headers: {
		        "content-type": "text/xml"  
		    },
		    body: req
		}, function (error, response, body){
		    console.log(body);

		    //BROADCAST TO ALL THAT SONG CHANGED
		    spotify.next3(pos, function(next3) {

				console.log("EMITTING SONG_INFO: current song: " + songName + ". nextSongs: " + JSON.stringify(next3));

		    	io.sockets.emit("SONG_INFO", {
					currentSong: songName,
					nextSongs: next3.songs,
					nextUris: next3["spotify_uris"],
					currentStatus: "playing"
				});

				playbackState[pos] = "playing";
		    });

			currentSongName[pos] = songName;

		});

	},

	updateVolume: function(ip, numPeople) {
		const newVolume = calculateVolume(numPeople);
		const address = "http://" + ip + ":8090/volume";
		const req = '<volume>'+newVolume+'</volume>';
		
		request({
		    url: address,
		    method: "POST",
		    headers: {
		        "content-type": "text/xml"  
		    },
		    body: req
		}, function (error, response, body){
		    console.log(response.body);
		});

	},

	listenForSongChanges: function(bozeIP, sourceAcc, position) {
		var bozeListener = new WebSocket("ws://"+bozeIP+":8080", "gabbo");

		bozeListener.onmessage = function (event) {

			if (event.data.includes("</time>")) {
				const end = event.data.indexOf("</time>");
				const time = event.data.substring(end - 2, end);
				console.log("Current Time: " + time);

				if (time > 30) {
					spotify.requestNextSong(position, bozeIP, sourceAcc, module.exports.playSong);
				}
			}
			
		};
	},

	setSockets: function(sockets) {
		io = sockets;
	},

	getCurrentSongName: function(pos){
		return currentSongName[pos];
	},

	playIfNothingsPlaying: function(room) {

		return;

		if (stuffPlaying[room]) {
			return;
		}

		console.log('i am going to try to play the next song');

		spotify.requestNextSong(room, bozeIPs[room],sourceAccs[room], module.exports.playSong);
		stuffPlaying[room] = true;
	},

	skipSong: function(room) {
		spotify.requestNextSong(room, bozeIPs[room],sourceAccs[room], module.exports.playSong);
		stuffPlaying[room] = true;
	},

	play: function(room) {
		const address = "http://" + bozeIPs[room] + ":8090/key";
		const req = '<key state="press" sender="Gabbo">PLAY</key>';
		
		request({
		    url: address,
		    method: "POST",
		    headers: {
		        "content-type": "text/xml"  
		    },
		    body: req
		}, function (error, response, body){
		    //console.log(response.body);

			spotify.next3(room, function(next3) {

				console.log("EMITTING SONG_INFO: current song: " + currentSongName[room] + ". nextSongs: " + JSON.stringify(next3));

		    	io.sockets.emit("SONG_INFO", {
					currentSong: currentSongName[room],
					nextSongs: next3.songs,
					nextUris: next3["spotify_uris"],
					currentStatus: "playing"
				});

				playbackState[room] = "playing";
		    });

		});
	},

	pause: function(room) {
		const address = "http://" + bozeIPs[room] + ":8090/key";
		const req = '<key state="press" sender="Gabbo">PAUSE</key>';
		
		request({
		    url: address,
		    method: "POST",
		    headers: {
		        "content-type": "text/xml"  
		    },
		    body: req
		}, function (error, response, body){
		    spotify.next3(room, function(next3) {

				console.log("EMITTING SONG_INFO: current song: " + currentSongName[room] + ". nextSongs: " + JSON.stringify(next3));

		    	io.sockets.emit("SONG_INFO", {
					currentSong: currentSongName[room],
					nextSongs: next3.songs,
					nextUris: next3["spotify_uris"],
					currentStatus: "paused"
				});

				playbackState[room] = "paused";
		    });
		});
	},

	getSpotify() {
		return spotify;
	},

	getPlaybackState() {
		return playbackState;
	}
}

function calculateVolume(numPeople) {
	if (numPeople <= 0) {
		return 0; 
	} else if (numPeople <= 1) {
		return 50;
	} else if (numPeople <= 2) {
		return 60;
	} else if (numPeople <= 3) {
		return 70;
	} else if (numPeople <= 4) {
		return 80;
	} else {
		return 80;
	}
}