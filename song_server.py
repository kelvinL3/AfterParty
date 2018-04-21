# receive song requests from app
# link with spotify
# when aaron's server request something from me, give a song to play


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
        self.upvote.remove(song_ptr)
    def add_downvote(self, song_ptr):
        self.downvote.append(song_ptr)
    def remove_downvote(self, song_ptr):
        self.downvote.remove(song_ptr)

class Song():
    def __init__(self, song_string):
        this.song_string = song_string
        this.num_upvote = 0
        this.num_downvote = 0
    def change_num_upvote(self, number):
        if this.num_upvote + number < 0:
            print("negative number of UPVOTE")
            this.num_upvote = 0
        else:
            this.num_upvote += number
    def change_num_downvote(self, number):
        if this.num_downvote + number < 0:
            print("negative number of DOWNVOTE")
            this.num_downvote = 0
        else:
            this.num_downvote += number
    def custom_comparator():
        def compare(song1, song2):
            if song1.num_upvote > song2.num_upvote:
                return -1
            elif song1.num_upvote > song2.num_upvote:
                return 1
            else:
                return 0
        return compare

class Playlist():
    def __init__(self):
        self.song_list = []
        self.song_index = 0
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
    def sort_update(self):
        self.song_list.sort(cmp = Song.custom_comparator())
    def send_next_song(self): # used in requests
        if self.song_index == len(self.song_list)-1:
            return None
        self.song_index += 1
        return self.song_list[self.song_index-1]



users = []
playlist = Playlist()

user_commands = ['login','logout','upvote','downvote']

# TODO: server stuff
# TODO: add user
# TODO: user for those 4 methods

from flask import Flask, request
app = Flask(__name__)

@app.route('/')
def start():
    return "Hello. To use, '"/user/<username>"' if you're a user. \n " +
            "'"/speaker/<action>"' if you're Aaron Kau. \n " +
            "For more help, '"/help/user"'" or '"/help/speaker"'."

# help methods

@app.route('/help/user')
def help_speaker():
    return jsonify(help1="/user/post",
                    help2="JSON should be 'user, action, song'",
                    help2=user_commands)

@app.route('/help/speaker')
def help_user():
    return jsonify(help="/speaker/getnextsong")

# user methods
@app.route('/user/post', methods=['POST'])
def method():
    # login/<name>
    json_data = request.get_json()

    user_string = json_data.get('user')
    action_type = json_data.get('action')
    if action_type not in user_commands:
        return jsonify(error = "action_type is not valid")
    if action_type != "login" and name not in users:
        return jsonify(error = "user name not logged in or registered")
    song_string = json_data.get('song')

    action = commaction_type.index(action_type)
    if action==0: # login
        users.append(User(user_string))
    elif action==1: # logout
        playlist.delete_user_likes_and_dislikes(User(user_string))
        users.remove(User(user_string))
    elif action==2: # upvote
        # assume user is already here
        user = users.index(User(user_string))
        song = Song(song_string)

        index = playlist.song_list.index(song)
        if index == -1:
            playlist.add_song(song)
        else:
            song = playlist.song_list[index]
        song.change_num_upvote(1)
        user.add_upvote(song)
        # if song was downvoted before, remove
        if song in user.downvote:
            user.remove_downvote(song)
            song.change_num_downvote(-1)

    elif action==3: # downvote
        # assume user is already here
        user = users.index(User(user_string))
        song = Song(song_string)

        index = playlist.song_list.index(song)
        if index == -1:
            playlist.add_song(song)
        else:
            song = playlist.song_list[index]
        song.change_num_downvote(1)
        user.add_downvote(song)
        # if song was upvoted before, remove
        if song in user.upvote:
            user.remove_upvote(song)
            song.change_num_upvote(-1)


# speaker methods
@app.route('/speaker/getnextsong', methods=['GET'])
def get_next_song():
    s = playlist.send_next_song()
    e = "No Error"
    if s is None:
        e = "No song"
    return jsonify(song = s, error = e)




# @app.route('/', methods=['GET', 'POST'])
# def index():
#     name = None
#     if request.method == 'POST' and 'name' in request.form:
#         name = request.form['name']
#     return render_template('index.html', name=name)
#
if __name__ == '__main__':
    app.run(debug=True)




# @app.route('/')
# def index():
#     return "<h1>Hello, World!</h1>"
#
# @app.route('/user/<name>')
# def user(name):
# 	return '<h1>Hello, {0}!</h1>'.format(name)
#
# if __name__ == '__main__':
#     app.run(debug=True)
