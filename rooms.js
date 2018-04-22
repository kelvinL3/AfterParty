var io;

var spotify = require("./spotify");

var users = {};
var roomCounts = [];

module.exports = {
	startSockets: function(http, numRooms, roomCountChanged, speakers) {

		//Create rooms.
		for (var i = 0 ; i < numRooms ; i++) {
			roomCounts = roomCounts.concat(0);
		}

		io = require('socket.io')(http);

		io.on('connection', function(socket){
			users[socket.id] = {
			  	room: -1
			};

			socket.on("HELLO", function(a) {
				console.log("RECEIVED: " + a);
			});


			socket.on('disconnect', function(){

			  	//If user leaves room, decrement room count.
			  	if (users[socket.id].room >= 0) {
			  		//POST LOGOUT user/post  user: socket.id, action: "logout", song: ""
			  		spotify.userPost({
			  			user: socket.id,
			  			action: "logout",
			  			"song_name": "",
			  			"spotify_uri": "",
			  			"speaker_num": users[socket.id].room
			  		});
			  		roomCounts[users[socket.id].room]--;
			  	}
			  	//Remove user from dictionary.
			  	delete users[socket.id];

			});

			socket.on('ENTER_ROOM', function(newRoom) {

				console.log("enter_room received: room " + newRoom);

				newRoom = roomNameToNumber(newRoom);

			  	const oldRoom = users[socket.id].room;
			  	if (oldRoom >= 0) {

			  		//POST LOGOUT user/post  user: socket.id, action: "logout", song: ""
			  		spotify.userPost({
			  			user: socket.id,
			  			action: "logout",
			  			"song_name": "",
			  			"spotify_uri": "",
			  			"speaker_num": oldRoom
			  		});

			  		roomCounts[oldRoom]--;
			  	} 

			  	//POST LOGIN user/post  user: socket.id, action: "login", song: "", speaker_num: newRoom
			  	spotify.userPost({
		  			user: socket.id,
		  			action: "login",
		  			"song_name": "",
		  			"spotify_uri": "",
		  			"speaker_num": newRoom
			  	});
			  	if (newRoom >= 0) {
			  		roomCounts[newRoom]++;

			  		//broadcast song name (when changes, and on connect)
					spotify.next3(newRoom, function(next3) {

						console.log("EMITTING SONG_INFO: current song: " + speakers.getCurrentSongName(users[socket.id].room) + ". nextSongs: " + JSON.stringify(next3) + ". CurrentStatus: " + speakers.getPlaybackState()[users[socket.id].room]);

				    	io.sockets.emit("SONG_INFO", {
							currentSong: speakers.getCurrentSongName(users[socket.id].room),
							nextSongs: next3.songs,
							nextUris: next3["spotify_uris"],
							currentStatus: speakers.getPlaybackState()[users[socket.id].room]
						});
				    });

				    //PLAY
				    speakers.play(newRoom);

			  	}
				users[socket.id].room = newRoom;
				roomCountChanged();

				console.log("RECEIVED ROOM CHANGE: " + newRoom);
				console.log(users);
				console.log(roomCounts);
			});

			socket.on('UPVOTE_SONG', function(song) {

				//POST user/post user: socket.id, action: "upvote", song: song, speaker_num: users[socket.id].room
				spotify.userPost({
		  			user: socket.id,
		  			action: "upvote",
					"song_name": song.name,
					"spotify_uri": song.uri,
		  			"speaker_num": users[socket.id].room
			  	}, function(ret) {
			  		spotify.next3(users[socket.id].room, function(next3) {

						console.log("EMITTING SONG_INFO: current song: " + speakers.getCurrentSongName(users[socket.id].room) + ". nextSongs: " + JSON.stringify(next3) + ". CurrentStatus: " + speakers.getPlaybackState()[users[socket.id].room]);

				    	io.sockets.emit("SONG_INFO", {
							currentSong: speakers.getCurrentSongName(users[socket.id].room),
							nextSongs: next3.songs,
							nextUris: next3["spotify_uris"],
							currentStatus: speakers.getPlaybackState()[users[socket.id].room]
						});
				    });
			  	});

			  	//speakers.playIfNothingsPlaying(users[socket.id].room);
			});

			socket.on('DOWNVOTE_SONG', function(song) {

				//POST user/post user: socket.id, action: "downvote", song: song, speaker_num: users[socket.id].room
				spotify.userPost({
		  			user: socket.id,
		  			action: "downvote",
					"song_name": song.name,
					"spotify_uri": song.uri,
		  			"speaker_num": users[socket.id].room
			  	}, function(ret) {
			  		spotify.next3(users[socket.id].room, function(next3) {

						console.log("EMITTING SONG_INFO: current song: " + speakers.getCurrentSongName(users[socket.id].room) + ". nextSongs: " + JSON.stringify(next3) + ". CurrentStatus: " + speakers.getPlaybackState()[users[socket.id].room]);

				    	io.sockets.emit("SONG_INFO", {
							currentSong: speakers.getCurrentSongName(users[socket.id].room),
							nextSongs: next3.songs,
							nextUris: next3["spotify_uris"],
							currentStatus: speakers.getPlaybackState()[users[socket.id].room]
						});
				    });
			  	});
			});

			socket.on('SKIP_SONG', function() {
				speakers.skipSong(users[socket.id].room);
			});

			socket.on('PLAY', function() {
				speakers.play(users[socket.id].room);
			});

			socket.on('PAUSE', function() {
				speakers.pause(users[socket.id].room);
			});

		});
	},

	getUsers: function() {
		return users;
	},

	getRoomCounts: function() {
		return roomCounts;
	},

	getSockets: function() {
		return io;
	}
}


function roomNameToNumber(newRoom) {
	if (newRoom == "") {
		return -1;
	} else if (newRoom == "hems03") {
		return 0;
	} else {
		return 1;
	}
}

