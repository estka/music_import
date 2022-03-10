import json, os

def track_filename(track_id):
	return f'data/{track_id}.json'

def read_json(filename):
	with open(filename, 'r') as f:
		return json.load(f)

def read_file(filename):
	with open(filename, 'r') as f:
		return [s.strip() for s in f.readlines()]

def write_json(filename, data):
	with open(filename, 'w+') as f:
		json.dump(data, f)

def write_file(filename, data, header):
	with open(filename, 'w+') as f:
		f.write(header)
		f.write(data)

albums={}
album_by_track={}

def handle_track(track):
	t_album = track['album']
	album = albums.get(t_album['id'])
	if album is None:
		album = {
			'tracks':[],
			'album': t_album,
		}
		albums[t_album['id']]=album
	album_by_track[track['id']]=album
	#imgs = track['album']['images']
	#print(imgs)
	#if len(imgs) != 3:
	#	print(imgs)

def add_tracks(all_tracks):
	album = None
	for track_id in all_tracks:
		if track_id in album_by_track:
			album = album_by_track[track_id] 
		else:
			album_by_track[track_id] = album #add track for java output
		assert album is not None
		if track_id not in album['tracks']:
			album['tracks'].append(track_id)	

def write_file_to_img_net_classifier():
	ret = []
	album_cnt = 0
	for album_id, data in albums.items():
		album_cnt += 1
		imgs = data['album']['images']
		track_ids = [t_id for t_id in data['tracks']]
		ret.append({
			'album_id': album_id,
			'track_ids': track_ids,
			'images': imgs,
		})
	print('albums', album_cnt)
	write_json('input_to_image_classifier.json', ret)

def write_file_to_java_import(all_tracks):
	lines = []
	odd_lines = []
	cnt_missing_img = 0
	cnt_one_img=0
	for track_id in all_tracks:
		album = album_by_track[track_id]['album']
		if not 'images' in album:
			print(track_id, album)
		imgs = album['images']
		if len(imgs) == 0:
			cnt_missing_img += 1
			continue
		if len(imgs) == 1:
			cnt_one_img += 1
		img = imgs[0] #try 1st img
		if img['width'] != 300:
			img = imgs[1] #or 2nd img 
			#img = imgs[2] #or thumbnail 
			#assert img['width'] == 64
		assert img['width'] == 300
		img_url = img['url']
		#if(img_url.startswith('https://i.scdn.co/image/ab67616d00001e02')):
		if(img_url.startswith('https://i.scdn.co/image/ab67616d0000')):
			cut_url = img_url[-24:]
		else:
			cut_url = img_url[24:]
			odd_lines.append(f'{track_id}\t{img_url}')	
		lines.append(f'{track_id}\t{cut_url}')
	odd_out = '\n'.join(odd_lines)+'\n'
	out = '\n'.join(lines)+'\n'
	write_file('spotify_album_cover_artwork_ref.txt', out, 'URI\timg_path_reference\n')
	#write_file('spotify_album_cover_artwork_odd_paths.txt', odd_out, 'URI\todd_path\n')
	print('count tracks without images', cnt_missing_img)
	print('count tracks with one image', cnt_one_img)

def process(track_id):
	filename = track_filename(track_id)
	if os.path.exists(filename):
		handle_track(read_json(filename))

def main():
	# load track ids from file
	track_ids = read_file('../../MusicTowerBlocks/first_track_per_album.txt')
	#print(len(track_ids))
	all_tracks = read_file('../../MusicTowerBlocks/all_tracks_by_album.txt')
	#print(len(all_tracks))
	for track_id in track_ids:
		process(track_id)
	add_tracks(all_tracks)
	
	# write outputs 
	write_file_to_img_net_classifier()
	write_file_to_java_import(all_tracks)

if __name__ == '__main__':
    main()
