const app = require('express')();
const http = require('http').Server(app);
const bodyParser = require('body-parser');

const rooms = require('./rooms');
const speakers = require('./speakers');


const bozeIPs = ["192.168.1.246", "192.168.1.136"];
const sourceAccs = ["1229288988", "shashank135sharma"];

app.use( bodyParser.json() );       // to support JSON-encoded bodies
app.use(bodyParser.urlencoded({     // to support URL-encoded bodies
  extended: true
})); 

app.get('/', function(req, res){
  res.send(`
  	<script src="/socket.io/socket.io.js"></script>
	<script>
  	var socket = io();
  	socket.emit('test');
  	socket.emit('test', 'test123', 2, 3);
	</script>
	`);
});

http.listen(3000, function(){
  console.log('listening on *:3000');
});

//speakers.playSong(bozeIPs[0], sourceAccs[0], "spotify:album:4Wv5UAieM1LDEYVq5WmqDd", "TEST");
//speakers.playSong(bozeIPs[1], sourceAccs[1], "spotify:album:4Wv5UAieM1LDEYVq5WmqDd", "TEST");

rooms.startSockets(http, bozeIPs.length, roomCountChanged, speakers);
speakers.listenForSongChanges(bozeIPs[0], sourceAccs[0], 0);
speakers.listenForSongChanges(bozeIPs[1], sourceAccs[1], 1);
speakers.setSockets(rooms.getSockets());

speakers.updateVolume(bozeIPs[0], 0);
speakers.updateVolume(bozeIPs[1], 0);

speakers.pause(0);
speakers.pause(1);

speakers.getSpotify().requestNextSong(0, bozeIPs[0], sourceAccs[0], speakers.playSong);
speakers.getSpotify().requestNextSong(1, bozeIPs[1], sourceAccs[1], speakers.playSong);

function roomCountChanged() {
	for (var i = 0 ; i < rooms.getRoomCounts().length ; i++) {
		speakers.updateVolume(bozeIPs[i], rooms.getRoomCounts()[i]);
	}
}
