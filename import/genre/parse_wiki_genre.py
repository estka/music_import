#!/usr/bin/env python3.8
import re, json, random
import strsimpy

def read_file(filename):
	with open(filename, 'r') as f:
		return [s.strip() for s in f.readlines()]

def normalize_levels(tree):
	top_level = min([e['level'] for e in tree])
	ret = []
	for e in tree:
		ret.append({
			'name': e['name'],
			'level': e['level'] - top_level,
		})
	return ret

def parse_first_stage(lines):
	ret = []
	ctx_lines = None
	for line in lines:
		header = re.match('^(=+)([^=]+)\\1$', line)
		if header:
			#print(len(header.group(1)), header.group(2))
			ctx = { 
				'name' : header.group(2),
				'level' : len(header.group(1)),
				'lines' : [],
				}
			ctx_lines = ctx['lines']
			ret.append(ctx)
		else:
			if ctx_lines is not None:
				ctx_lines.append(line)
	return ret

def normalize_str(s):
	s = re.sub('\\[\\[(?:[^|\\]]+[|])?([^\\]]+)\\]\\]', '\\1', s)
	s = re.sub('}$', '', s)
	return s

def parse_list(lines):
	depth = 0
	ret = []
	for line in lines:
		if re.match('^{{',line):
			continue
		m = re.match('^(\\*+) *(.+)$', line)
		if m:
			#print(len(m.group(1))+depth, normalize_str(m.group(2)))
			ret.append({
				'name': normalize_str(m.group(2)),
				'level': depth + len(m.group(1)),
			})

		subhdr = re.match("^('+)([^']+?):?\\1$", line)
		if subhdr:
			depth=3-len(subhdr.group(1))
			ret.append({
				'name': normalize_str(subhdr.group(2)),
				'level': depth,
			})
			#print(line, subhdr.group(2))
	return ret

def parse_second_stage(headers):
	ret = []
	i = 0
	for header in headers:
		depth = header['level']
		ret.append({
			'name': header['name'],
			'level': depth,
		})
		items = parse_list(header['lines'])		
		for item in items:
			ret.append({
				'name': item['name'],
				'level': depth + item['level'],
			})
	return ret

def new_node(name):
	return {
		'name': name,
		'children': [],
	}

def build_tree(tree):
	ret = new_node('genre')
	levels = {0:ret} 
	for e in tree:
		name = e.get('mtb_name', e['name'])
		n = new_node(name)
		lvl = e['level']

		parent = levels[lvl] 
		parent['children'].append(n)
		levels[lvl+1] = n #store ref for future iterations
	return ret

def rate_match_trigram(genre, name):
	from strsimpy.ngram import NGram
	trigram = NGram(3)
	return trigram.distance(genre, name)

def rate_match_sift4(genre, name):
	from strsimpy import SIFT4
	return SIFT4().distance(genre, name)
	
def rate_match_stupid(genre, name):
	return int(genre!=name) # 0 = match

def rate_match_levenshtein(genre, name):
	from strsimpy.levenshtein import Levenshtein
	dist = Levenshtein().distance(genre, name)
	return dist

def rate_match_random(genre, name):
	return random.random()

#https://github.com/luozhouyang/python-string-similarity

#rate_match = rate_match_stupid
#rate_match = rate_match_trigram
rate_match = rate_match_sift4
#rate_match = rate_match_levenshtein

all_fixes = None
def load_fixes(filename):
	global all_fixes
	with open(filename, 'r') as f:
		all_fixes = json.load(f)


def match_genre(genre, tree):
	#print(genre)
	fix = all_fixes.get(genre)
	if fix:
		return fix
		
	prospects = []
	for n in tree:
		normalized_name = re.sub('[- ()]', '', n['name']).lower()
		score = rate_match(genre.lower(), normalized_name)
		prospects.append((score, n['name']))
	prospects.sort(key=lambda v: v[0], reverse=False)
	#for n in range(0,10):
	#	p = prospects[n]
	#	print(f'n\t{p[0]:.4f}\t{p[1]}')
	#print()
	#for n in range(len(prospects)-10,len(prospects)):
	#	p = prospects[n]
	#	print(f'n\t{p[0]:.4f}\t{p[1]}')
	first_prospects = prospects[0]
	if first_prospects[0]==0:
		return first_prospects[1]
	#else:
	#	return False
	return [prospects[n] for n in range(0,10)]

def match_genres(genres, tree):
	genres = genres.copy()
	random.shuffle(genres)
	ret = []
	for g in genres:
		matches = match_genre(g, tree)
		ret.append({
			'name':g,
			'matches':matches,
		})
	return ret

def make_matches_map(genres):
	ret = {}
	for genre in genres:
		mtb_name = genre['name']
		match = genre['matches']
		if type(match) == str:
			ret[match] = mtb_name
	return ret

def store_matches_in_tree(tree, mapping):
	for idx, n in enumerate(tree):
		mtb_name = mapping.get(n['name'])
		if mtb_name:
			n['mtb_name'] = mtb_name
			make_visible(tree, idx)

def make_visible(tree, idx):
	last_level = 1000
	for i in range(idx, -1, -1):
		n = tree[i]
		lvl = n['level']
		if lvl < last_level:
			last_level = lvl
			n['visible'] = True
		if lvl == 0:
			return
	
def filter_visible_tree(tree):
	ret = []
	for n in tree:
		if n.get('visible', False):
			ret.append(n)
	return ret

def load_genres(filename):
	genres = []
	with open(filename, 'r') as f:
		f.readline() #throw away header line
		for line in f:
			genres.append(line.strip())
	return genres

def main():
	#load translation map for genre names to match MTB
	load_fixes('fixes.json')

	#read wikipedia tree
	lines = read_file('fromWikiGenres.txt')
	headers = parse_first_stage(lines)
	tree = parse_second_stage(headers)
	tree = normalize_levels(tree)

	#load genres datafile
	genres = load_genres('MTBgenres.txt')
	#matches is output of matching wiki genre names with MTB genre names
	matches = match_genres(genres, tree)
	with open('matches.json', 'w+') as f:
		json.dump(matches, f)

	#apply matches to tree
	mapping = make_matches_map(matches)
	store_matches_in_tree(tree, mapping)

	with open('debug_tree.json', 'w+') as f:
		json.dump(tree, f)

	#only output MTB coupled parts of the tree
	filtered_tree = filter_visible_tree(tree)
	nested_tree = build_tree(filtered_tree)
	with open('tree.json', 'w+') as f:
		json.dump(nested_tree, f)
	

if __name__ == '__main__':
	main()
