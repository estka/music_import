import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.siegmar.fastcsv.reader.CsvReader;

public class MtbImporter {
    private List<Track> MtbTracks;
    private Map<String, List<Track>> MtbByUri;
    private List<String> MtbGenres;

    private Map<String, List<Album>> DRalbums; 
    private Map<Integer, Album> inputDRalbumByReleaseIDs;
    private Map<String, Set<Integer>> DRartists;
    private Map<Integer, Artist> artistByIDs;

    //MTB hierarchies
    private AlphabeticHierarchy trackHierarchy;
    private AlphabeticHierarchy albumHierarchy;
    private AlphabeticHierarchy artistHierarchy;
    private YearHierarchy decadeHierarchy;
    private DurationHierarchy durationHierarchy;
    private DurationHierarchy durationHierarchyLight;
    //DR hierarchies
    private CountryHierarchy countryHierarchy;
    private DefaultHierarchy<String> genderHierarchy;
    private DefaultHierarchy<String> mediaHierarchy;
    private DefaultHierarchy<String> formatHierarchy;
    private AlphabeticHierarchy labelHierarchy;

    public void readMusicTowerBlocks(String filepath, String filename){
        MtbTracks = new ArrayList<>();
        MtbByUri = new HashMap<>();
        MtbGenres = new ArrayList<>();

        Set<String> artists = new HashSet<>();
        Set<String> albums = new HashSet<>();

        Path path = FileSystems.getDefault().getPath(filepath, filename);
        try (CsvReader csv = CsvReader.builder().fieldSeparator('\t').quoteCharacter('\"').build(path);) {
            var iter = csv.iterator();
            List<String> columns = iter.next().getFields();
            System.out.println(columns);

            while (iter.hasNext()) {
                List<String> currTrack = iter.next().getFields();
                //System.out.println(currTrack);
                if (currTrack.size()!=19) {System.out.println("ODD SIZE");}

                Track track = new Track(currTrack);

                if (!MtbByUri.containsKey(currTrack.get(1))){
                    MtbByUri.put(currTrack.get(1), new ArrayList<>());
                }
                else{
                    System.out.println("Duplicate sp_uri !!");
                    throw new AssertionError("input contains sp_uri duplicate"); //assert no dup sp_uri in input
                }

                MtbTracks.add(track);
                MtbByUri.get(currTrack.get(1)).add(track);

                for (String genre : currTrack.subList(16, 19)) { //[beginIdx;[endIdx
                    if (genre.length()>1){ // 1 letter genres not valid
                        MtbGenres.add(genre);
                    }
                }
                MtbGenres.addAll(currTrack.subList(16, 18));
            }
            csv.close();
        } 
        catch(IOException e) {
            System.out.println("Problem");
            e.printStackTrace();
        }
        System.out.println("# tracks found & read: "+MtbTracks.size());

        MtbTracks.forEach(track -> track.getArtist_names().forEach(thisartist -> artists.add(thisartist))); //{MtbArtists.put(track, thisartist); }
        MtbTracks.forEach(track -> albums.add(track.getAlbum_name())); //{MtbAlbums.put(track, track.getAlbum_name()); }
        System.out.println("# present artists: "+ artists.size()); //MtbArtists.size()+"\t"+
        System.out.println("# present albums: "+ albums.size()); //MtbAlbums.size()+"\t"+
        Set<String> distinctGenres = new HashSet<>(MtbGenres);//MtbGenres.stream().distinct().collect(Collectors.toList());
        System.err.println("# distinct genres: "+ distinctGenres.size());
        //writeListToFile("genres.txt", distinctGenres);
    }

    // public void addTimestamps(String filepath, String filename){
    //     Path path = FileSystems.getDefault().getPath(filepath, filename);
    //     try (CsvReader csv = CsvReader.builder().fieldSeparator('\t').quoteCharacter('\"').build(path);) {
    //         var iter = csv.iterator();
    //         List<String> columns = iter.next().getFields();
    //         System.out.println(columns);
    //         // [sp_uri, timestamp]
    //         while (iter.hasNext()) {
    //             List<String> currLine = iter.next().getFields();
    //             //System.out.println(currLine);
    //             if (currLine.size()!=2) {System.out.println("ODD SIZE");}
    //             if (!MtbByUri.containsKey(currLine.get(0))) {System.out.println("key uri is missing: "+currLine.get(0));}
    //             if (MtbByUri.get(currLine.get(0)).size()>1) {throw new AssertionError("input contains sp_uri duplicate");}
                
    //             MtbByUri.get(currLine.get(0)).get(0).setTimestamp(currLine.get(1)); 
    //         }
    //         csv.close();
    //     } 
    //     catch(IOException e) {
    //         System.out.println("Problem");
    //         e.printStackTrace();
    //     }
    // }

    // public void addSpotifyAlbumCoverArtwork(String filepath, String filename){
    //     Path path = FileSystems.getDefault().getPath(filepath, filename);
    //     try (CsvReader csv = CsvReader.builder().fieldSeparator('\t').quoteCharacter('\"').build(path);) {
    //         var iter = csv.iterator();
    //         List<String> columns = iter.next().getFields();
    //         System.out.println(columns);
    //         // [sp_uri, sp_img]
    //         while (iter.hasNext()) {
    //             List<String> currLine = iter.next().getFields();
    //             //System.out.println(currLine);
    //             if (currLine.size()!=2) {System.out.println("ODD SIZE");}
    //             if (!MtbByUri.containsKey(currLine.get(0))) {System.out.println("key uri is missing: "+currLine.get(0));}
    //             if (MtbByUri.get(currLine.get(0)).size()>1) {throw new AssertionError("input contains sp_uri duplicate");}
                
    //             MtbByUri.get(currLine.get(0)).get(0).setSpotifyImage(currLine.get(1)); 
    //         }
    //         csv.close();
    //     } 
    //     catch(IOException e) {
    //         System.out.println("Problem");
    //         e.printStackTrace();
    //     }
    // }

    // public void addSemanticLabels(String filepath, String filename){
    //     //Set<String> allTags = new HashSet<>();
    //     Path path = FileSystems.getDefault().getPath(filepath, filename);
    //     try (CsvReader csv = CsvReader.builder().fieldSeparator('\t').quoteCharacter('\"').build(path);) {
    //         var iter = csv.iterator();
    //         List<String> columns = iter.next().getFields();
    //         System.out.println(columns);
    //         // [sp_uri, [labels]]
    //         while (iter.hasNext()) {
    //             List<String> currLine = iter.next().getFields();
    //             //System.out.println(currLine);
    //             if (currLine.size()!=2) {System.out.println("ODD SIZE");}
    //             if (!MtbByUri.containsKey(currLine.get(0))) {System.out.println("key uri is missing: "+currLine.get(0));}
    //             if (MtbByUri.get(currLine.get(0)).size()>1) {throw new AssertionError("input contains sp_uri duplicate");}
                
    //             MtbByUri.get(currLine.get(0)).get(0).addSemanticLabels(Arrays.asList(currLine.get(1).split(",")));
    //             //allTags.addAll(Arrays.asList(currLine.get(1).split(",")));
    //         }
    //         csv.close();
    //     } 
    //     catch(IOException e) {
    //         System.out.println("Problem");
    //         e.printStackTrace();
    //     }
    //     //System.out.println("all tags "+allTags.size());
    // }

    public void addAudioFeatures(String filepath, String filename){
        Set<Integer> t_sig = new HashSet<>();
        Set<Integer> key = new HashSet<>();
        List<Float> bpm = new ArrayList<>();
        List<Float> loud = new ArrayList<>();
        int dance = 0, extratracks = 0;
        boolean print = true;
        Path path = FileSystems.getDefault().getPath(filepath, filename);
        try (CsvReader csv = CsvReader.builder().fieldSeparator('\t').quoteCharacter('\"').build(path);) {
            var iter = csv.iterator();
            List<String> columns = iter.next().getFields();
            System.out.println(columns);
            System.out.println(columns.subList(2, 14));
            // [id, sp_uri, danceability, energy, key, loudness, mode, speechiness, acousticness, instrumentalness, liveness, valence, tempo, time_signature, duration_ms]
            while (iter.hasNext()) {
                List<String> currLine = iter.next().getFields();
                // if (print) {System.out.println(currLine);}
                // cnt++;
                // if (cnt==10) {print = false;}
                if (currLine.size()!=15) {System.out.println("ODD SIZE");System.out.println(currLine);}
                if (!MtbByUri.containsKey(currLine.get(1))) {
                    //if (first) {System.out.println("key uri is missing: "+currLine.get(0)+" "+currLine.get(1));} 
                    extratracks++; 
                    continue;
                    //if (extratracks==10) {print = false;}
                }
                if(currLine.get(2).equals("")){ //14 (18) tracks with no audio features
                    dance++;
                    continue; // if danceability is missing the other audio features are also missing
                }
                if(Float.valueOf(currLine.get(12))==0){ //* if bpm is 0 , indicates strange values
                    //System.out.println(currLine);
                }
                key.add((int) Math.round(Float.valueOf(currLine.get(4))));
                loud.add(Float.valueOf(currLine.get(5)));
                //loud.add((int) Math.round(Float.valueOf(currLine.get(5))));
                bpm.add(Float.valueOf(currLine.get(12)));
                t_sig.add((int) Math.round(Float.valueOf(currLine.get(13))));

                MtbByUri.get(currLine.get(1)).get(0).addAudioFeatures(currLine.subList(2,14));
            }
            csv.close();
            System.out.println("extra tracks with audio features: " + extratracks);
            Collections.sort(bpm); Collections.sort(loud);
            System.out.println("bpm\tmin: "+bpm.get(0)+"\tmax: "+bpm.get(bpm.size()-1));
            System.out.println("loud\tmin: "+loud.get(0)+"\tmax: "+loud.get(loud.size()-1));
            System.out.println(dance);
            System.out.print("time signature ");
            t_sig.forEach(x -> System.out.print(x+" "));
            System.out.println();
            System.out.print("key ");
            key.forEach(x -> System.out.print(x+" "));
            System.out.println();
        } 
        catch(IOException e) {
            System.out.println("Problem");
            e.printStackTrace();
        }
        //System.out.println("all tags "+allTags.size());
    }

    //addTagsFromFile(...., (track, line) => track.add())
    public void addTagsFromFile(String filepath, String filename, BiConsumer<Track,String> fun ){
        Path path = FileSystems.getDefault().getPath(filepath, filename);
        try (CsvReader csv = CsvReader.builder().fieldSeparator('\t').quoteCharacter('\"').build(path);) {
            var iter = csv.iterator();
            List<String> columns = iter.next().getFields();
            System.out.println(columns);
            while (iter.hasNext()) {
                List<String> currLine = iter.next().getFields();
                // Safety checks 
                if (currLine.size()!=2) {System.out.println("ODD SIZE");}
                if (!MtbByUri.containsKey(currLine.get(0))) {System.out.println("key uri is missing: "+currLine.get(0));}
                if (MtbByUri.get(currLine.get(0)).size()>1) {throw new AssertionError("input contains sp_uri duplicate");}
                
                Track track = MtbByUri.get(currLine.get(0)).get(0);
                
                fun.accept(track, currLine.get(1));
            }
            csv.close();
        } 
        catch(IOException e) {
            System.out.println("Problem. Hmm,, how many tabs in file?");
            e.printStackTrace();
        }
    }

    public void writeListToFile(String filename, List<String> thislist){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
            //thislist =  thislist.stream().distinct().collect(Collectors.toList());
            for (String elem : thislist) {
                writer.write(elem+"\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String removeParentheses(String inputText){
        if (inputText.contains("(")){
            //System.out.println(inputText);
            int idx = inputText.indexOf("(");
            inputText = inputText.substring(0, idx);
            //inputText.replace("\\s+", "");
            inputText.trim();
        }
        return inputText;
    }

    public void readDR(String filepath, String filename){
        inputDRalbumByReleaseIDs = new HashMap<>();
        artistByIDs = new HashMap<>();
        DRalbums = new HashMap<>();
        DRartists = new HashMap<>();
        Path path = FileSystems.getDefault().getPath(filepath, filename);
        try (CsvReader csv = CsvReader.builder().fieldSeparator(';').quoteCharacter('\"').build(path);) {
            var iter = csv.iterator();
            List<String> columns = iter.next().getFields();
            System.out.println(columns);

            int cnt = 1;
            while (iter.hasNext()) {
                cnt ++;
                List<String> currRelease = iter.next().getFields();
                //System.out.println(currRelease+"\nline: "+cnt);
                if(currRelease.size()!=13){
                    System.out.println("ODD SIZE");
                }
                //currRelease.subList(0, 1) //find right index

                Integer releaseId = Integer.valueOf(currRelease.get(0));

                Album thisAlbum;

                if (!inputDRalbumByReleaseIDs.containsKey(releaseId)){
                    thisAlbum = new Album(currRelease);
                    inputDRalbumByReleaseIDs.put(releaseId, thisAlbum);

                    String title_low = thisAlbum.getTitle().toLowerCase();
                    title_low = removeParentheses(title_low); //cutof () in title for better matching
    
                    //add album
                    if (!DRalbums.containsKey(title_low)){
                        DRalbums.put(title_low, new ArrayList<>() );
                    }
                    DRalbums.get(title_low).add(thisAlbum);

                    for (Artist artist : thisAlbum.getArtists()){
                        //add artist
                        String name = artist.getName();
                        if (!DRartists.containsKey(name)){
                            DRartists.put(name, new HashSet<>() );
                        }
                        DRartists.get(name).add(artist.getID());
                    }
                }
                else{
                    thisAlbum = inputDRalbumByReleaseIDs.get(releaseId);
                }
                List<String> img = currRelease.subList(10, 12);
                inputDRalbumByReleaseIDs.get(releaseId).addImage(img);

                //System.out.println(thisAlbum.toString(false));

            }

            csv.close();
        } 
        catch(IOException e) {
            System.out.println("Problem");
            e.printStackTrace();
        }
        // DRalbums.forEach((title,album) -> { 
        //     for (Artist artist : album.getArtists()) {
        //         if (DRartists.containsKey(thisAlbum.getTitle())){
        //             System.out.println("DUBLICATE");
        //         }
        //         DRartists.put(artist.getName(), artist);
        //     }
        // });
        //System.out.println("# Releases found & read: "+inputDRalbums.size());
        System.out.println("# distinct releaseIDs:\t"+inputDRalbumByReleaseIDs.keySet().size());
        int albumCnt = 0, missingArtists = 0;
        Set<Integer> DRartistsbyID = new HashSet<>();
        Set<String> DRartistsbyName = new HashSet<>();
        for (String title : DRalbums.keySet()){
            albumCnt += DRalbums.get(title).size(); // sum all list items...
            for (Album album : DRalbums.get(title)){
                missingArtists += album.getMissingCounter();
                album.getArtists().forEach(artist -> {
                    DRartistsbyID.add(artist.getID());
                    DRartistsbyName.add(artist.getName());
                    artistByIDs.put(artist.getID(), artist);
                });
            }
        }
        System.out.println("# Albums found & read:\t"+albumCnt);
        //DRalbums.forEach((k,v) -> System.out.println(k));
        System.out.println("# Artist present by id:\t"+ DRartistsbyID.size());
        System.out.println("# Artist by name:\t"+ DRartistsbyName.size());
        System.out.println("#artist:\t"+ DRartists.keySet().size());
        System.out.println("# Artist missing:\t"+ missingArtists);
    }

    public void createDrHierarchies(){
        countryHierarchy = new CountryHierarchy("dr_artist_country");
        genderHierarchy = new DefaultHierarchy<>("dr_artist_gender");
        mediaHierarchy = new DefaultHierarchy<>("dr_release_media");
        formatHierarchy = new DefaultHierarchy<>("dr_release_format");
        labelHierarchy = new AlphabeticHierarchy("dr_release_label");
        
        for (Track track : MtbTracks){
        //for (Integer releaseID : inputDRalbumByReleaseIDs.keySet()) {
            //Album album = inputDRalbumByReleaseIDs.get(releaseID);
            Album album = track.getDRalbum();
            List<String> countries = new ArrayList<>();
            List<String> gender = new ArrayList<>();

            if(album!=null){
                //add to hierarchy
                mediaHierarchy.addToTagSet(album.getMedia());
                formatHierarchy.addToTagSet(album.getFormat());
                album.getLabels().forEach(label -> labelHierarchy.addToTagSet(label));

                List<Artist> artists = album.getArtists(); //try album artist
                artists.forEach(artist -> {
                    countries.add(artist.getCountry());
                    gender.add(artist.getGender());
                });

            } else {
                List<Artist> artists = track.getDRartists(); // augmented mtb artist
                artists.forEach(artist -> {
                    countries.add(artist.getCountry());
                    gender.add(artist.getGender());
                });
            }

            // if artist tags, add to hierarchy
            if(countries.size()>0){
                for (String c : countries) {
                    countryHierarchy.addToTagSet(c);
                }            
            }
            if(gender.size()>0){
                for (String g : gender) {
                    genderHierarchy.addToTagSet(g);
                }
            }
        }
    }

    public void createMtbHierarchies(){
        trackHierarchy = new AlphabeticHierarchy("sp_track_name");
        albumHierarchy = new AlphabeticHierarchy("sp_album_name");
        artistHierarchy = new AlphabeticHierarchy("sp_artist_infos");
        decadeHierarchy = new YearHierarchy("year");
        durationHierarchy = new DurationHierarchy("sp_track_duration");
        durationHierarchyLight = new DurationHierarchy("sp_track_duration");
        String now = MtbTracks.get(0).getNow();
        int yearcnt = 0, lightDurationCnt = 0;
        for (Track track : MtbTracks) {
            String timestamp = track.getTimestamp();
            if(!timestamp.equals(now)){
                //add to hierarchy
                decadeHierarchy.addToTagSet(track.getYear());
                yearcnt ++;
            } else {
                decadeHierarchy.addToTagSet("missing");
            }
            durationHierarchy.addToTagSet(track.getDuration());
            if(track.getDuration()<600){ //only tracks less than 10 minutes
                durationHierarchyLight.addToTagSet(track.getDuration());
                lightDurationCnt ++;
            }
            //trackHierarchy.addToTagSet(track.getTrackName());
            //albumHierarchy.addToTagSet(track.getAlbum_name());
            //track.getArtist_names().forEach(artist -> artistHierarchy.addToTagSet(artist));
        }
        //writeListToFile("difficultTracks.txt", trackHierarchy.getDifficult());
        // writeListToFile("difficultAlbums.txt", albumHierarchy.getDifficult());
        // writeListToFile("difficultArtist.txt", artistHierarchy.getDifficult());
        System.out.println("year tags: "+yearcnt);
        System.out.println("duration < 10 min: "+ lightDurationCnt);
    }
    
    public void augmentMtbAlbums(){
        int matchCnt = 0, notFound = 0; Set<Album> foundAlbums = new HashSet<>();
        //*On the Sunday of Life (Remaster) MTB, On the Sunday of Life DR
        for (Track track : MtbTracks) {
            String trackAlbum = removeParentheses(track.getAlbum_name().toLowerCase());
            if (DRalbums.containsKey(trackAlbum)){
                for (Album album : DRalbums.get(trackAlbum)) {
                    
                    //if one album -> true
                    /*List<String> mtbArtist = track.getArtist_names();
                    Set<String> drList = album.getArtists().stream().map(Artist::getName).collect(Collectors.toSet()); 
                    */
                    //.map(str -> str.replace("\\s+", ""))
                    Set<String> mtbArtistSet = track.getArtist_names().stream().map(String::toLowerCase).collect(Collectors.toSet());
                    List<String> drList = album.getArtists().stream().map(Artist::getName).map(String::toLowerCase).collect(Collectors.toList());
                    Boolean foundAllArtist = true;
                    Boolean foundOneArtist = false;
                    //System.out.println("MTB\t"+track.getAlbum_name() +"\t"+ track.getArtist_names());
                    for (String trackArtist : mtbArtistSet){
                        Boolean found = false;
                        for (String drArtist : drList){
                            if(drArtist.equals(trackArtist) ){
                                found = true;
                                foundOneArtist = true;
                            }
                            if (!drArtist.equals(trackArtist) && trackArtist.contains(drArtist)){
                                found = true;
                                foundOneArtist = true;                             
                            }
                            if (!drArtist.equals(trackArtist) && drArtist.contains(trackArtist)){
                                found = true;
                                foundOneArtist = true; 
                            }
                            else{
                                //not found
                                //System.out.println("DR \t"+album.getTitle() +"\t"+ album.getArtistsNames());
                            }
                        }
                        if (!found){
                            foundAllArtist = false;
                        }
                    }
                    //if (foundAllArtist){ //strict
                    if (foundOneArtist){ //loosening
                        //System.out.println("images "+album.countImages());
                        track.setDRalbum(album); 
                        foundAlbums.add(album);
                    }
                    else{
                        //System.out.println("MTB\t"+track.getAlbum_name() +"\t"+ track.getArtist_names());
                        //System.out.println("DR \t"+album.getTitle() +"\t"+ album.getArtistsNames());
                    }
                    /*
                    //*intersection
                    long result = mtbArtist.stream()
                    .distinct()
                    .filter(drList::contains) //contains på set er O(1)
                    .count();
                    if(result==mtbArtist.size()){
                        track.setDRalbum(album); 
                    }
                    */

                } 
            }
            if (track.getDRalbum()==null) {
                notFound++;
            } else {
                matchCnt++;
            }
        }

        System.out.println("total album (track) match: "+matchCnt+"\t\tnot found: "+notFound);
        System.out.println("distinct found albums "+foundAlbums.size());
    }

    public void augmentMtbArtist(){
        Set<String> reginaFinnish = new HashSet<>(Arrays.asList("Oi miten suuria voimia!", "Puutarhatrilogia", "Katso Maisemaa", "Soita Mulle","Näinä mustina iltoina", "Minua ollaan vastassa", "Olisitko sittenkin halunnut palata")).stream()
            .map(String::toLowerCase).collect(Collectors.toSet());

        int cnt2 = 0, cnt3 = 0, multiCnt = 0, notfoundCnt= 0;
        int matchCnt = 0, notFound = 0; Set<Artist> foundArtists = new HashSet<>();
        Set<Integer> multipleMatch = new HashSet<>();

        for (Track track : MtbTracks) {
            for(String name : track.getArtist_names()){
                boolean found = false;
                if (DRartists.containsKey(name)){
                    List<Integer> ids = new ArrayList<>(DRartists.get(name));
                    Artist artist = null;
                    if(ids.size()==1){
                        artist = artistByIDs.get(ids.get(0));
                    }
                    if(ids.size()>1){
                        multipleMatch.addAll(ids); multiCnt++;

                        //assign random artist from match 
                        // Random random = new Random();
                        // int randomint =  random.nextInt(ids.size());//range [0 - ids.size[
                        // artist = artistByIDs.get(ids.get(randomint));

                        // track.addDRartist(artist);
                        // foundArtists.add(artist);
                        // System.out.println(name+"\n"+ids);


                        List<Artist> found_artist = new ArrayList<>();
                        for(Integer id : ids){
                            Artist artist_id = artistByIDs.get(id);
                            if(!artist_id.getCountry().equals("Ukendt") && 
                                !artist_id.getGender().equals("mangler")){
                                found_artist.add(artist_id);
                            }
                        }
                        if(found_artist.size()==1){
                            // System.out.println(track.toString(false));
                            // System.out.println(found_artist.get(0));
                            // System.out.println();
                            artist = found_artist.get(0);
                        }
                        //System.out.println();
                    }
                    if (artist != null){
                        track.addDRartist(artist);
                        foundArtists.add(artist);
                    }
                }
                else{
                    //System.out.println("NOT FOUND "+name);
                }
            }
            if (track.getDRartists().size()==0) {
                notFound++;
            } else {
                matchCnt++;
            }
        }
        System.out.println("multiple artist matches: "+multipleMatch.size()+ "\tcnt: "+multiCnt);
        System.out.println("total artist (track) match: "+matchCnt+"\t\tnot found: "+notFound);
        System.out.println("distinct found artist "+foundArtists.size());
    }

    public void writeToFile(Boolean augmented, String filename){
        String description = filename.split("\\.")[0];
        System.out.println("Started writing tags into "+description+" file.");
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
                writer.write("# Format: FileName:TagSet:Tag:TagSet:Tag:(...)\n");
                for (Track track : MtbTracks){
                    writer.write(track.toString(augmented));
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void writeHierarchies(String path){
        System.out.println("Started writing hierarchies file.");

        writeHierarchy(path+"countries.json", countryHierarchy);
        writeHierarchy(path+"gender.json", genderHierarchy);
        writeHierarchy(path+"media.json", mediaHierarchy);
        writeHierarchy(path+"format.json", formatHierarchy);
        writeHierarchy(path+"label.json", labelHierarchy);
        // writeHierarchy(path+"track_name.json", trackHierarchy);
        // writeHierarchy(path+"album_mtb.json", albumHierarchy);
        // writeHierarchy(path+"artist_mtb.json", artistHierarchy);
        // writeHierarchy(path+"decade.json", decadeHierarchy);
        // writeHierarchy(path+"duration.json", durationHierarchy);
        // writeHierarchy(path+"duration-light.json", durationHierarchyLight);
    }

    public <T> void writeHierarchy(String filename, TagHierarchy<T> hierarchy) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
            //writer.write("{\"name\": \"root\", \"children\": [");
            writer.write(hierarchy.toString());
            //writer.write("]}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO ny fil m MTB (5 linier) og DR (10 linier)
    // case exact match, delvist match og ikke et match. (Lower case match)

    public static void main(String[] args) {
        
        MtbImporter MtbImp = new MtbImporter();

        //MtbImp.readMusicTowerBlocks("../", "full_table_27_aug.csv"); //*436064 rows
        MtbImp.readMusicTowerBlocks("../","cleaned_data.tsv"); //*410007 rows
        MtbImp.addTagsFromFile("../", "EmoMTB-dates.tsv", 
            (track, line) -> track.setTimestamp(line));
        MtbImp.addTagsFromFile("../","spotify_album_cover_artwork_reference.txt",
            (track, line) -> track.setSpotifyImage(line));
        MtbImp.addTagsFromFile("../", "spotify_semantic_labels.tsv",
            (track, line) -> track.addSemanticLabels(Arrays.asList(line.split(","))));
        //audio features not represented in interface in this version. enum (DOW) and slider filter opportunies ahead
        //MtbImp.addAudioFeatures("../", "mtb_artist_track_cleaned_spotify_features.tsv"); //*462045 rows, 52038 extra tracks
        MtbImp.readDR("../", "dr_releases.csv");        
        MtbImp.augmentMtbAlbums();
        MtbImp.augmentMtbArtist();
        MtbImp.writeToFile(false, "MTB_imageTags.csv");
        MtbImp.writeToFile(true, "MTB+DRimageTags.csv");
        // MtbImp.createDrHierarchies();
        // MtbImp.createMtbHierarchies();
        // MtbImp.writeHierarchies("hierarchies/");
    }
}
