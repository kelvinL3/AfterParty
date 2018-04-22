# receive song requests from app
# link with spotify
# when aaron's server request something from me, give a song to play

# BOSE API
# https://developer.bose.com/soundtouch-control-api/apis

# FLASK API
# http://flask.pocoo.org/docs/0.12/api/

# IP address of speaker  182.168.1.246 8090



class User():
    def __init__(self, user_id):
        # track id, strings
        self.user_id = user_id
        self.upvote = []
        self.downvote = []
    def add_upvote(self, song_ptr):
        self.upvote.append(song_ptr)
    def remove_upvote(self, song_ptr):
        if song_ptr in self.upvote:
            self.upvote.remove(song_ptr)
    def add_downvote(self, song_ptr):
        self.downvote.append(song_ptr)
    def remove_downvote(self, song_ptr):
        if song_ptr in self.upvote:
            self.downvote.remove(song_ptr)
    def __eq__(self, other):
        if isinstance(self, other.__class__):
            return self.user_id == other.user_id
        return False

class Song():
    def __init__(self, song_name, spotify_uri):
        self.song_name = song_name
        self.spotify_uri = spotify_uri
        self.num_upvote = 0
        self.num_downvote = 0
    def change_num_upvote(self, number):
        if self.num_upvote + number < 0:
            print("negative number of UPVOTE")
            self.num_upvote = 0
        else:
            self.num_upvote += number
    def change_num_downvote(self, number):
        if self.num_downvote + number < 0:
            print("negative number of DOWNVOTE")
            self.num_downvote = 0
        else:
            self.num_downvote += number
    def custom_comparator():
        def compare(song1, song2):
            if song1.num_upvote > song2.num_upvote:
                return -1
            elif song1.num_upvote > song2.num_upvote:
                return 1
            else:
                return 0
        return compare
    def __eq__(self, other):
        if isinstance(self, other.__class__):
            return self.spotify_uri == other.spotify_uri
        return False
    def __lt__(self, other):
        if isinstance(self, other.__class__):
            return self.num_upvote-self.num_downvote > other.num_upvote-other.num_downvote
        return False

class Playlist():
    def __init__(self):
        self.song_list = []
        # self.song_index = 0
    def add_song(self, song):
        self.song_list.append(song)
    def delete_user_likes_and_dislikes(self, user):
        for song_ptr in user.upvote:
            for song in playlist.song_list:
                if song is song_ptr:
                    song.change_num_upvote(-1)
        for song_ptr in user.downvote:
            for song in playlist.song_list:
                if song is song_ptr:
                    song.change_num_downvote(-1)
    # def send_next_song(self): # used in requests
    #     if len(self.song_list)==0:
    #         return None
    #     retval = self.song_list[0]
    #     d_song = self.song_list.pop(0)
    #     # for user in 
    #     return retval
    def preview(self, num):
        ans = self.song_list[:num]
        song_names = [song.song_name for song in ans]
        spotify_uris = [song.spotify_uri for song in ans]
        return song_names, spotify_uris

class Speaker():
    def __init__(self):
        self.playlist = Playlist()
        self.users = []
    def get_user_by_name(self, user_string):
        for user in self.users:
            if user.user_id == user_string:
                return user
        return None
    def send_next_song(self): # used in requests
        if len(self.playlist.song_list)==0:
            return None
        retval = self.playlist.song_list[0]
        d_song = self.playlist.song_list.pop(0)
        for user in self.users:
            user.remove_upvote(d_song)
            user.remove_downvote(d_song)
        return retval
# playlist = Playlist()

speakers = [Speaker() for i in range(2)]
# speaker = Speaker()

user_commands = ['login','logout','upvote','downvote']



from flask import Flask, request, jsonify
app = Flask(__name__)

@app.route('/')
def start():
    return jsonify(text='Hello. To use, "/user/<username>" if you are a user. \n' +
            '"/speaker/<action>" if you are Aaron Kau. \n ' +
            'For more help, "/help/user" or "/help/speaker".')

# help methods

@app.route('/help/user')
def help_speaker():
    return jsonify(url="/user/post",
                    details = "This should be a POST request",
                    json_structure="JSON should be 'user, action(valid_commands), song, speaker_num'",
                    example = "{" + 
                    "'user': 'kelvin'," + 
                    "'action': 'login'," + 
                    "'song_name': ''," + 
                    "'spotify_uri': ''," + 
                    "'speaker_num': 0" + 
                    "}",
                    valid_commands=user_commands)

@app.route('/help/speaker')
def help_user():
    return jsonify(url1="/speaker/getnextsong/<speaker_num>",
                    url2="/speaker/preview/<speaker_num>",
                    details = "These should be a GET request")

# user methods
@app.route('/user/post', methods=['POST'])
def method():
    # login/<name>
    json_data = request.get_json(force=True)
    if json_data is None:
        return jsonify(error="must be POST request with user, action, song")
    print(json_data)
    
    error = None
    if 'user' not in json_data: 
        error = "user missing"
    if 'action' not in json_data:
        error = "action missing"
    if 'song_name' not in json_data:
        error = "song_name missing"
    if 'spotify_uri' not in json_data:
        error = "spotify_uri missing"
    if 'speaker_num' not in json_data:
        error = "speaker number missing"
    if error is not None:
        return jsonify(error = "not in right format",
                        detail = error)
    
    user_string = json_data.get('user')
    action_type = json_data.get('action')
    if action_type not in user_commands:
        return jsonify(error = "action_type is not valid")
    song_name = json_data.get('song_name')
    spotify_uri = json_data.get('spotify_uri')
    s_num = json_data.get('speaker_num')
    if s_num >= 2:
        return jsonify(error = "speaker_num is too high, can be 0 or 1")
    if action_type != "login" and User(user_string) not in speakers[s_num].users:
        return jsonify(error = "user name not logged in or registered")
    
    
    if action_type not in user_commands:
        return jsonify(error="action is not in user_commands")
    action = user_commands.index(action_type)
    if action==0: # login
        user = User(user_string)
        if user in speakers[s_num].users:
            return jsonify(error="user already here, login twice")
        speakers[s_num].users.append(user)
    elif action==1: # logout
        speakers[s_num].playlist.delete_user_likes_and_dislikes(User(user_string))
        us = User(user_string)
        if us in speakers[s_num].users:
            speakers[s_num].users.remove(us)
    elif action==2 or action==3: # upvote & downvote
        # assume user is already here
        user = speakers[s_num].get_user_by_name(user_string)
        song = Song(song_name, spotify_uri)

        index = None
        try:   
            index = speakers[s_num].playlist.song_list.index(song)
            song = speakers[s_num].playlist.song_list[index]
        except ValueError: # if not in song_list
            speakers[s_num].playlist.add_song(song)
        
        if action==2: # upvote
            if song not in user.upvote:
                user.add_upvote(song)
                song.change_num_upvote(1)
                # if song was downvoted before, remove
                if song in user.downvote:
                    user.remove_downvote(song)
                    song.change_num_downvote(-1)
                # return jsonify(what="dsfa", song=song.num_upvote)
        elif action==3: # downvote
            if song not in user.downvote:
                user.add_downvote(song)
                song.change_num_downvote(1)
                # if song was upvoted before, remove
                if song in user.upvote:
                    user.remove_upvote(song)
                    song.change_num_upvote(-1)
    speakers[s_num].playlist.song_list.sort()
    
    # just for testing
    temp1 = [u.user_id for u in speakers[s_num].users]
    # temp11 = []
    # try:
    #     temp11 = [song.song_name for song in speakers[s_num].users[0]]
    # except ValueError:
    #     pass
    temp2 = [s.song_name for s in speakers[s_num].playlist.song_list]
    temp3 = [s.num_upvote for s in speakers[s_num].playlist.song_list]
    temp4 = [s.num_downvote for s in speakers[s_num].playlist.song_list]
    # must return something
    return jsonify(error="",
                    users=temp1,
                    # usersLiked=temp11,
                    songs=temp2,
                    songsDownvote=temp4,
                    songsUpvote=temp3)

# speaker methods
@app.route('/speaker/getnextsong/<speaker_num>', methods=['GET'])
def get_next_song(speaker_num):
    speaker_num = int(speaker_num)
    # song = speakers[speaker_num].playlist.send_next_song()
    song = speakers[speaker_num].send_next_song()
    if song is None:
        return jsonify(song = "", spotify_uri="", error = "No song")
    return jsonify(song = song.song_name, spotify_uri=song.spotify_uri, error="")
    
@app.route('/speaker/preview/<speaker_num>', methods=['GET'])
def preview(speaker_num):
    speaker_num = int(speaker_num)
    if speaker_num < 0:
        return jsonify(error="speaker_num is less than 0!")
    songs,spotify_uris = speakers[speaker_num].playlist.preview(3)
    if len(songs) == 0:
        return jsonify(songs = [], spotify_uris=[], error = "")
    return jsonify(songs = songs, spotify_uris = spotify_uris, error="")


if __name__ == '__main__':
    app.run() # debug=True
