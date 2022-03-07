#!/bin/bash

sql_cmd(){
	local outputfile="$1"
	cat <<SQL
\copy (select t.name from tagsets s join alphanumerical_tags t on t.tagset_id =  s.id where s.name = 'genre' and lower(t.name) = t.name order by 1) to '${outputfile}.tmpxxx' (format CSV);
SQL
}

psql_cmd(){
	local username="$1"
	local db="$2"
	psql -q "$db" "$username"
}

bail(){
	echo "$@" >&2
	exit 1
}

main(){
	if [ $# -ne 2 ] ; then
		bail "$0 <username> <database> - write genres to file"
	fi
	local username="$1"
	local database="$2"
	local outputfile="genres.txt"
	if [ -z "$username" ] ; then
		bail "username cannot be empty"
	fi
	if [ -z "$database" ] ; then
		bail "database cannot be empty"
	fi
	if [ -z "$outputfile" ] ; then
		bail "filename cannot be empty"
	fi
	if [ -e "$outputfile" ] ; then
		bail "file already exists"
	fi
	
	sql_cmd "$outputfile" | psql_cmd "$username" "$database"
	(
		sql_cmd "$outputfile" | sed -e 's/\.tmpxxx//g'
		cat "$outputfile.tmpxxx"
	) > "$outputfile"
	rm "$outputfile.tmpxxx"
}

main "$@"
	
