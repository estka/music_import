#!/usr/bin/env python3

from email import header
from os import read
import random

known_preferred = {
	'7icAbXym74dNFzYj2Ja7Yl': 600014, # correct spelling of Shakespeare
}

def readfile(filename):
	with open(filename, 'r') as f:
		header = f.readline().rstrip()
		rows = [s.rstrip().split('\t') for s in f.readlines()]
		return rows, header

def build_header_map(header):
	ret = {} #key: description, value: colum_index
	cols = header.split('\t')
	for i, col in enumerate(cols):
		ret[col] = i
	return ret

# match input file format and sorting
def write_output(header, rows, hmap, filename):
	sorted_rows = sorted(rows, key=lambda r: int(r[hmap['id']])) # increasing order of mtb_id
	with open(filename, 'w+') as f:
		f.write(f'{header}\n')
		hlen = len(header.split('\t'))
		for r in sorted_rows:
			tsv_data = '\t'.join(r)
			missing_tabs = hlen - len(r)
			extra_tabs = '\t' * missing_tabs # output equal number of columns for every row
			f.write(f'{tsv_data}{extra_tabs}\n')

# returns array of specified colums indexes
def get_cols(hmap, exclude=[], include=None):
	cols = []
	for name, i in hmap.items():
		if name not in exclude:
			if include is None or name in include:
				cols.append(i)
	return cols

# cut a row down to specified columns
# this quickly becomes expensive with many columns
def reduce_row(r, cols):
	ret = []
	r_len = len(r)
	for c in cols:
		if c < r_len: # avoid index out-of-range
			ret.append(r[c])
	return '\t'.join(ret)

# group rows by specified colums
def find_dups(rows, cols):
	all_groups = {}
	dup_groups = []
	dups = 0
	for r in rows:
		reduced_row = reduce_row(r, cols)
		grp = all_groups.get(reduced_row)
		if not grp:
			grp = []
			all_groups[reduced_row] = grp
		elif len(grp) == 1:
			dups += 1
			dup_groups.append(grp)
		if len(grp) > 0:
			dups += 1
		grp.append(r)
	return dups, dup_groups

# print total number of dublicates and number of distinct duplicate values
def find_dups_stats(rows, cols):
	dups, dup_groups = find_dups(rows, cols)
	return dups, len(dup_groups)

# generate a reduced set of rows without certain values
def filter_rows(rows, key, skip_values, comment=None):
	ret = []
	skipped = 0
	for r in rows:
		val = r[key]
		if val in skip_values:	
			skipped += 1
		else:
			ret.append(r)
	if comment is not None:
		print(f'filtered: {skipped} ({comment})')
	return ret

# lookup value for multiple colums at once
def row_cols(row, hmap, cols):
	return (row[hmap[c]] for c in cols)

# string representation of row_cols
def row_cols_str(row, hmap, cols):
	return '\t'.join(row_cols(row, hmap, cols))

# print information about duplicated rows
def debug_dups(rows, hmap, cols):
	dups, uniq_dups = find_dups(rows, cols)
	print(dups, len(uniq_dups))
	for grp in uniq_dups:
		for r in grp:
			print('grp\t' + row_cols_str(r, hmap, ['id','sp_uri','danceability','energy','key','loudness', 'mode', 'speechiness', 'acousticness', 'instrumentalness', 'liveness', 'valence', 'tempo', 'time_signature', 'duration_ms']))	
			#print('grp\t' + row_cols_str(r, hmap, ['id','x','y','z','sp_track_name']))	
			#x = r[hmap['x']]
			#z = r[hmap['z']]
		print()

# remove duplicates based on preference specified in global known_preferred
def dedup_preferred(rows, hmap):
	skip_ids = []
	for r in rows:
		sp_uri = r[hmap['sp_uri']]
		if sp_uri in known_preferred:
			mtb_id = r[hmap['id']]
			if known_preferred[sp_uri] != int(mtb_id):
				skip_ids.append(mtb_id)
	return filter_rows(rows, key=hmap['id'], skip_values=skip_ids, comment='preferred_id aka. Shakespeare')

# remove duplicates that only have different y-values, keeping lower y value
def dedup_different_y(rows, hmap):
	dups, uniq_dups = find_dups(rows, get_cols(hmap, exclude=['id','y']))
	skip_ids = []
	for grp in uniq_dups:
		sorted_grp = sorted(grp, key=lambda r: int(r[hmap['y']]))
		for r in sorted_grp[1:]:
			skip_ids.append(r[hmap['id']]) # remove row
	return filter_rows(rows, key=hmap['id'], skip_values=skip_ids, comment='different_y')

# deterministicly eliminate duplicates by taking the first occurance
def dedup_take_first(rows, hmap):
	dups, uniq_dups = find_dups(rows, get_cols(hmap, include=['sp_uri']))
	skip_ids = []
	for grp in uniq_dups:
		sorted_grp = sorted(grp, key=lambda r: int(r[hmap['id']]))
		for r in sorted_grp[1:]:
			skip_ids.append(r[hmap['id']])
	return filter_rows(rows, key=hmap['id'], skip_values=skip_ids, comment='take first')

# remove duplicates randomly
def dedup_randomly(rows, hmap):
	dups, uniq_dups = find_dups(rows, get_cols(hmap, include=['sp_uri']))
	skip_ids = []
	for grp in uniq_dups:
		random.shuffle(grp)
		for r in grp[1:]:
			skip_ids.append(r[hmap['id']])
	return filter_rows(rows, key=hmap['id'], skip_values=skip_ids, comment='randomly')

# collapse building and fill gaps from vacated floors
def reassign_y(rows, hmap):
	sorted_rows = sorted(rows, key=lambda r: (int(r[hmap['x']]), int(r[hmap['z']]), int(r[hmap['y']])))

	last_x, last_z, last_y, new_y = -1, -1, -1, -1
	has_gap = False
	for r in sorted_rows:
		(x, y, z) = row_cols(r, hmap, ['x','y','z'])
		if x != last_x:
			last_x = x
			last_z = -1 # new x value, so also trigger new z detection
		if z != last_z:
			last_z = z
			last_y = -1
			has_gap = False
		if int(y) != last_y + 1:
			if not has_gap:
				new_y = last_y # find floor offset
			has_gap = True
		if has_gap:
			new_y += 1
			r[hmap['y']] = str(new_y) #assign new floor in building
		last_y = int(y)
	return rows

def main():
	rows, header = readfile('full_table_27_aug.csv')
	#rows, header = readfile('cleaned_data_intermediate.tsv') # to reduce process time
	hmap = build_header_map(header) # map column indexes to header fields

	print(f'got {len(rows)} rows')

	# assert mtb_id is unique
	assert find_dups_stats(rows, get_cols(hmap, include=['id'])) == (0,0)

	#print(find_dups_stats(rows, get_cols(hmap, exclude=['id','y'])))
	rows = dedup_different_y(rows, hmap)
	#print(find_dups_stats(rows, get_cols(hmap, exclude=['id','y'])))

	rows = dedup_preferred(rows, hmap) # remove shakespeare's dup

	#rows = dedup_take_first(rows, hmap) # remove dups by retaining the lowest id entry
	rows = dedup_randomly(rows, hmap)
	#print(find_dups_stats(rows, get_cols(hmap, include=['sp_uri'])))

	write_output(header, rows, hmap, 'cleaned_data_intermediate.tsv') # useful for diff against full_table
	rows = reassign_y(rows, hmap) # renumber building floors

	write_output(header, rows, hmap, 'cleaned_data.tsv') # cleaned result with 410007 tracks
	print(f'wrote {len(rows)} rows')
 
	# --------------------------------
	# Do it again for audio features
	rows, header = readfile('mtb_artist_track_spotify_features.txt')
	print(header)
	hmap = build_header_map(header) # map column indexes to header fields

	print(f'got {len(rows)} rows')
	assert find_dups_stats(rows, get_cols(hmap, include=['id'])) == (0,0)
	#debug_dups(rows, hmap, get_cols(hmap, exclude=['id']))

	print(find_dups_stats(rows, get_cols(hmap, exclude=['id'])))
	rows = dedup_randomly(rows, hmap) # we use random, but could also have used take_first
	print(find_dups_stats(rows, get_cols(hmap, exclude=['id'])))
 
	write_output(header, rows, hmap, 'mtb_artist_track_cleaned_spotify_features.tsv') # cleaned result with 462046 tracks
	print(f'wrote {len(rows)} rows')
	return
 

if __name__ == '__main__':
	main()
