#!/bin/bash

sort_tracks_by_album(){
	cut -f2,6,7 |
		tail -n +2 |
		sort --field-separator "$(printf "\t")" -k2 -k3

}

tracks_per_album(){
	sort_tracks_by_album |
		uniq --skip-fields 1 |
		cut -f1
}

tracks_in_album(){
	sort_tracks_by_album |
		cut -f1
}

#tracks_per_album < full_table_27_aug.csv > first_track_per_album.txt
#tracks_in_album < full_table_27_aug.csv > all_tracks_by_album.txt
tracks_per_album < cleaned_data.tsv > first_track_per_album.txt
tracks_in_album < cleaned_data.tsv > all_tracks_by_album.txt
