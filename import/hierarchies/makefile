all:	mtb_hierarchies.json mtb_dr_hierarchies.json

JSON=comma json-start json-end
MTB=track_name.json album_mtb.json artist_mtb.json decade.json
MTB+=duration-light.json genre_hierarchy.json semantic_combined_tree.json $(JSON)
DR=$(MTB) countries.json gender.json media.json format.json label.json

mtb_hierarchies.json: $(MTB)
	cat json-start track_name.json comma album_mtb.json comma artist_mtb.json comma decade.json comma duration-light.json comma genre_hierarchy.json comma semantic_combined_tree.json json-end > $@

mtb_dr_hierarchies.json: $(DR)
	cat json-start track_name.json comma album_mtb.json comma artist_mtb.json comma decade.json comma duration-light.json comma genre_hierarchy.json comma semantic_combined_tree.json comma countries.json comma gender.json comma media.json comma format.json comma label.json json-end > $@
