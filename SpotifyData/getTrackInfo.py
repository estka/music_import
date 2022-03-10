import requests, json, os, random, time

CLIENT_ID = '5dc682cce0c14d75ace7a341f0024e40'
CLIENT_SECRET = 'fa686c0889da45c8b5f8d821c6b83a29'

AUTH_URL = 'https://accounts.spotify.com/api/token'

def get_access_token():
	# POST
	auth_response = requests.post(AUTH_URL, {
		'grant_type': 'client_credentials',
		'client_id': CLIENT_ID,
		'client_secret': CLIENT_SECRET,
		})

	# convert the response to JSON
	auth_response_data = auth_response.json()

	# save the access token
	access_token = auth_response_data['access_token']
	return access_token

class my_spotify_client:
	def __init__(self):
		self._access_token = None

	def get_access_token(self):
		if self._access_token is None:
			self._access_token = get_access_token()
		return self._access_token

	def get_tracks(self, ids):
		assert len(ids)<=50

		headers = {
		    'Authorization': f'Bearer {self.get_access_token()}'
		}

		url = 'https://api.spotify.com/v1/tracks'

		# params
		params = {
			'ids': ','.join([str(id) for id in ids]),
		}

		# actual GET request with proper header
		r = requests.get(url, params=params,  headers=headers)
		
		if r.status_code != 200:
			print(r.status_code)
			print(r.headers.get('retry-after'))
			print(json.dumps(r.json()))
			os._exit(1)
		return r.json()

def track_filename(track_id):
	return f'data/{track_id}.json'

def store_result(track_id, data):
	filename = track_filename(track_id)
	with open(filename, 'w+') as f:
		json.dump(data, f)

def store_results(result):
	cnt = 0
	for track in result['tracks']:
		store_result(track['id'], track)
		cnt += 1
	return cnt

# filter track_ids by checking if the output file exists
def missing_tracks(track_ids):
	ret = []
	for track_id in track_ids:
		if not os.path.exists(track_filename(track_id)):
			ret.append(track_id)
	return ret

def single_request(spotify_client, track_ids):
	missing_track_ids = missing_tracks(track_ids)
	# shuffle such that non-existsing tracks does not get packed together in the same request
	random.shuffle(missing_track_ids)

	print('missing tracks',len(missing_track_ids))
	if len(missing_track_ids) > 0:
		result = spotify_client.get_tracks(missing_track_ids[:50])
		stored = store_results(result)
		if stored > 0 :
			return True
	return False

def main():
	spotify_client = my_spotify_client()

	# load track ids from file
	with open('../../MusicTowerBlocks/first_track_per_album.txt','r') as f:
	    track_ids = [s.strip() for s in f.readlines()]
	
	while single_request(spotify_client, track_ids):
		time.sleep(5) #not exhaust simple rate limiting

if __name__ == '__main__':
    main()
