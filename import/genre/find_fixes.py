#!/usr/bin/env python3
import json

def read_json(filename):
	with open(filename, 'r') as f:
		return json.load(f)

def read_fixes(mapping, filename):
	genres = read_json(filename)
	for genre in genres:
		name = genre['name']
		matches = genre['matches']
		if type(matches) == list:
			for match in matches:
				if len(match) == 1:
					mapping[name] = match[0]

def main():
	mapping = {}
	read_fixes(mapping, 'fixes1.json')
	read_fixes(mapping, 'fixes2.json')
	with open('fixes.json', 'w+') as f:
		json.dump(mapping, f)
	
if __name__ == '__main__':
	main()
