/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.musiccomposer;
import java.io.BufferedReader;
import java.io.IOException;
import org.jfugue.player.Player;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.lang.Thread;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;



/**
 *
 * @author bernardoleal
 */
public class MusicSingleton {
    
    private static MusicSingleton instance;
    private Player player;
    //private List<String> recordedChords;
    private boolean isRecording;
    private int tempo;
    private String instrument;
    private MainPage mainPage;
    private Thread recordingThread;
    private Thread playbackThread;
    private boolean isPaused;
    private boolean isStopped;
    private int currentChordIndex;
    //Used chat to remember how to use switch names format
    public static final String STATUS_PLAYING = "Playing";
    public static final String STATUS_PAUSED = "Paused";
    public static final String STATUS_IDLE = "Idle";
    public static final String STATUS_EPLAYING = "Edit Playing";
    
    private Map<Integer, Track> tracks; // Map to store multiple tracks
    private int currentTrackIndex; // Currently selected track
    
    
    private MusicSingleton() {
        this.player = new Player();
        //this.recordedChords = new ArrayList<>();
        this.tracks = new HashMap<>();
        this.isRecording = false;
        this.tempo = 120;
        this.currentChordIndex = -1;
        
        createTrack("Track 1", "Piano");
        this.currentTrackIndex = 0;
    }

     public static MusicSingleton getInstance() {
        if (instance == null) {
            synchronized (MusicSingleton.class) {
                if (instance == null) {
                    instance = new MusicSingleton();
                }
            }
        }
        return instance;
    }
     
    public void setMainPage(MainPage mainPage) {
        this.mainPage = mainPage;
    }
    
    public int createTrack(String name, String instrument) {
        int trackIndex = tracks.size();
        tracks.put(trackIndex, new Track(name, instrument));
        return trackIndex;
    }
    
    public void switchTrack(int trackIndex) {
        if (tracks.containsKey(trackIndex)) {
            this.currentTrackIndex = trackIndex;
            mainPage.updateTrackDisplay(getCurrentTrack());
        }
    }
    
    public Track getCurrentTrack() {
        return tracks.get(currentTrackIndex);
    }
    
    public Map<Integer, Track> getAllTracks() {
        return new HashMap<>(tracks);
    }
    
    public Track getTrack(int index) {
        return tracks.get(index);
    }
    
    public void deleteTrack(int trackIndex) {
        if (tracks.size() > 1) {
            tracks.remove(trackIndex);
            
            // If we deleted the current track, switch to another one
            if (currentTrackIndex == trackIndex) {
                currentTrackIndex = tracks.keySet().iterator().next(); //chat helped with the switch but It's not working correctly
            }
        } else {
            JOptionPane.showMessageDialog(mainPage, "Cannot delete the last track.");
        }
    }
    
    public void playChord(String chord) {
        Track currentTrack = getCurrentTrack();
        new Thread(() -> player.play("T" + currentTrack.getTempo() + " I[" + currentTrack.getInstrument() + "] " + chord)).start(); //crazy API syntax 
        
        if (isRecording) {
            currentTrack.addChord(chord);
            mainPage.updateTextArea(String.join(" ", currentTrack.getRecordedChords()));
        }
    }
    
    public void playRecordedChords() {
        
        if (playbackThread != null && playbackThread.isAlive()) {
            JOptionPane.showMessageDialog(mainPage, "Playback is already running.");
            return;
        }
        
        Track currentTrack = getCurrentTrack();
        
        if (currentTrack.getRecordedChords().isEmpty()) {
            JOptionPane.showMessageDialog(mainPage, "No chords recorded");
            return;
        }
        
        //chatGpt helped me on how to modify a JOptionPane to a custom element
        Object[] options = {"Listen", "Edit"};
        int choice = JOptionPane.showOptionDialog(
            mainPage,
            "How do you want to play this composition?",
            "Playback Mode", //title
            JOptionPane.YES_NO_OPTION, //Option type (ignored when custom options are used)
            JOptionPane.QUESTION_MESSAGE, //Message type (sets the icon and style)
            null, // Icon (null = default question icon)
            options, // The actual button labels shown
            options[0] // Default selected button ("Listen")
        );

        boolean isListeningMode = (choice == 0); // 0 = Listen, 1 = Edit
        
        isPaused = false;
        isStopped = false;
        
        //ternary is so much faster and better :)
        mainPage.playbackControlsState(isListeningMode ? STATUS_PLAYING : STATUS_EPLAYING);

        playbackThread = new Thread(() -> {
            try {
                List<String> chords = currentTrack.getRecordedChords();
                String trackInstrument = currentTrack.getInstrument();
                int trackTempo = currentTrack.getTempo();
                
                for (int i = 0; i < chords.size(); i++) {
                    String chord = chords.get(i);
                    
                    synchronized (this) {
                        while (isPaused) {
                            wait();
                        }
                        if (isStopped) {
                            break;
                        }
                    }

                    // Update current chord index
                    currentChordIndex = i;
                   
                    mainPage.highlightCurrentChord(i);
                    
                    
                    player.play("T" + trackTempo + " I[" + trackInstrument + "] " + chord);
                    
                    if (!isListeningMode) {
                        Thread.sleep(1000); //the time spent in each chord
                    }
                }
                
                mainPage.playbackControlsState(STATUS_IDLE);
                mainPage.clearChordHighlight();
                currentChordIndex = -1;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                
                if (!isPaused) {
                    mainPage.playbackControlsState(STATUS_IDLE);
                    mainPage.clearChordHighlight();
                }
            }
        });
        playbackThread.start();
    }
    
    public void playMultiRecordedChords() {
        // Check if we have any tracks with chords
        boolean hasChords = false;
        Map<Integer, Track> tracksToPlay = new HashMap<>();
        
        for (Map.Entry<Integer, Track> entry : tracks.entrySet()) {
            Track track = entry.getValue();
            if (!track.getRecordedChords().isEmpty() && !track.isMuted()) {
                hasChords = true;
                tracksToPlay.put(entry.getKey(), track);
            }
        }
        
        if (!hasChords) {
            JOptionPane.showMessageDialog(mainPage, "No chords recorded in any track");
            return;
        }

        if (playbackThread != null && playbackThread.isAlive()) {
            JOptionPane.showMessageDialog(mainPage, "Playback is already running.");
            return;
        }
        
        isPaused = false;
        isStopped = false;
        
        mainPage.playbackControlsState(STATUS_PLAYING);

        playbackThread = new Thread(() -> {
            try {
                // I found out that JFugue's Player class wasn't designed to have multiple instances playing simultaneously
                // In JFugue, we can use different voice channels (V0, V1, etc.) for different tracks (low key took ages to discover it)
                StringBuilder masterPattern = new StringBuilder();

                int voiceNum = 0;
                for (Track track : tracksToPlay.values()) {
                    if (track.getRecordedChords().isEmpty() || track.isMuted()) {
                        continue;
                    }

                    // Add this track as a separate voice in the master pattern
                    masterPattern.append("V").append(voiceNum).append(" ");
                    masterPattern.append("T").append(track.getTempo()).append(" ");
                    masterPattern.append("I[").append(track.getInstrument()).append("] ");

                    for (String chord : track.getRecordedChords()) {
                        masterPattern.append(chord).append(" ");
                    }

                    masterPattern.append(" ");
                    voiceNum++;
                }

                // Create a single player for the master pattern
                if (masterPattern.length() > 0) {
                    final String finalPattern = masterPattern.toString();

                    // Play the combined pattern
                    try {
                        Player player = new Player();
                        player.play(finalPattern);
                        mainPage.playbackControlsState(STATUS_IDLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mainPage.playbackControlsState(STATUS_IDLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainPage.playbackControlsState(STATUS_IDLE);
            }
        });

        playbackThread.start();
    }
    
    public synchronized void pausePlayback() {
        if (playbackThread != null && playbackThread.isAlive()) {
            isPaused = true;
            mainPage.playbackControlsState(STATUS_PAUSED);
            
        }
    }

    public synchronized void resumePlayback() {
        if (isPaused) {
            isPaused = false;
            mainPage.playbackControlsState(STATUS_PLAYING);
            notifyAll();
        }
    }

    public synchronized void stopPlayback() {
        if (playbackThread != null && playbackThread.isAlive()) {
            isStopped = true;
            isPaused = false;
            mainPage.playbackControlsState(STATUS_IDLE);
            mainPage.clearChordHighlight();
            currentChordIndex = -1;
            playbackThread.interrupt();
            notifyAll();
        }
    }
    
    public void startRecording() {
        isRecording = !isRecording;
        if (isRecording) {
            getCurrentTrack().clearChords();
            startRecordingThread();
            JOptionPane.showMessageDialog(mainPage,"🎤 Recording started!");
        } else {
            stopRecordingThread();
            JOptionPane.showMessageDialog(mainPage,"🛑 Recording stopped!");
        }
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    
    private void startRecordingThread() {
        recordingThread = new Thread(() -> {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    Track currentTrack = getCurrentTrack();
                    mainPage.updateTextArea(String.join("", currentTrack.getRecordedChords()));
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        recordingThread.start();
    }
    
    public List<String> getRecordedChords() {
        return getCurrentTrack().getRecordedChords();
    }
    
    private void stopRecordingThread() {
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
    }
    
    public Thread getPlaybackThread() {
        return playbackThread;
    }
    
    public boolean isPlaybackActive() {
        return playbackThread != null && playbackThread.isAlive();
    }
    
    public boolean isPlaybackPaused() {
        return isPaused;
    }
    
    public int getCurrentChordIndex() {
        return currentChordIndex;
    }
    
    public void setTempo() {
        String newTempo = JOptionPane.showInputDialog("Enter new tempo:", getCurrentTrack().getTempo());
        try {
            int newTempoValue = Integer.parseInt(newTempo); //just double checking
            getCurrentTrack().setTempo(newTempoValue);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainPage, "Invalid tempo input!");
        }
    }
    
    public void setInstrument(String instrument) {
        getCurrentTrack().setInstrument(instrument);
    }

    public String getInstrument() {
        return getCurrentTrack().getInstrument();
    }

    public int getTempo() {
        return getCurrentTrack().getTempo();
    }
    
    public void saveComposition(String composition) {
        // Get the authentication singleton to access user folder
        AuthenticationSingleton auth = AuthenticationSingleton.getInstance();

        if (!auth.isLoggedIn()) {
            JOptionPane.showMessageDialog(mainPage, "You must be logged in to save compositions.", 
                    "Login Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String fileName = JOptionPane.showInputDialog(mainPage, 
                "Enter a name for your composition:", 
                "Save Composition", 
                JOptionPane.QUESTION_MESSAGE);

        if (fileName == null || fileName.trim().isEmpty()) {
            return; 
        }

        if (!fileName.toLowerCase().endsWith(".txt")) {
            fileName += ".txt";
        }

        try {
            StringBuilder content = new StringBuilder();
            content.append("# Music Composition\n");
            content.append("# Number of Tracks: ").append(tracks.size()).append("\n\n");

            // Save each track with nice markers suggestions that Chat gave me
            for (Map.Entry<Integer, Track> entry : tracks.entrySet()) {
                int trackIndex = entry.getKey();
                Track track = entry.getValue();

                // Use clear section markers for each track
                content.append("## TRACK_START ").append(trackIndex).append("\n");
                content.append("NAME: ").append(track.getName()).append("\n");
                content.append("INSTRUMENT: ").append(track.getInstrument()).append("\n");
                content.append("TEMPO: ").append(track.getTempo()).append("\n");
                content.append("MUTED: ").append(track.isMuted()).append("\n");
                content.append("CHORDS: ").append(String.join(" ", track.getRecordedChords())).append("\n");
                content.append("## TRACK_END ").append(trackIndex).append("\n\n");
            }

            boolean saved = auth.storeUserData(fileName, content.toString());

            if (saved) {
                JOptionPane.showMessageDialog(mainPage, 
                        "Composition saved successfully as " + fileName + " in your account.", 
                        "Save Successful", 
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainPage, 
                        "Error saving composition to your account folder.", 
                        "Save Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPage, 
                    "Error saving composition: " + e.getMessage(), 
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //The loadComposition for more than 1 track was heavily influenced by AI
    //The multi tracking destroyed me so most of the structure for the method was based on Chat (between line 495 and 621)
    //I even commented to help the understanding but this method in specific didn't came from my mind 
    public void loadComposition() {
        AuthenticationSingleton auth = AuthenticationSingleton.getInstance();

        if (!auth.isLoggedIn()) {
            JOptionPane.showMessageDialog(mainPage, "You must be logged in to load compositions.", 
                    "Login Required", JOptionPane.WARNING_MESSAGE);
        }

        String[] files = auth.listUserFiles();

        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(mainPage, "No saved compositions found.", 
                    "No Files", JOptionPane.INFORMATION_MESSAGE);
        }

        // Filter for txt files
        List<String> txtFiles = new ArrayList<>();
        for (String file : files) {
            if (file.toLowerCase().endsWith(".txt")) {
                txtFiles.add(file);
            }
        }

        if (txtFiles.isEmpty()) {
            JOptionPane.showMessageDialog(mainPage, "No saved compositions found.", 
                    "No Files", JOptionPane.INFORMATION_MESSAGE);
        }

        String selectedFile = (String) JOptionPane.showInputDialog(
                mainPage,
                "Select a composition to load:",
                "Load Composition",
                JOptionPane.QUESTION_MESSAGE,
                null,
                txtFiles.toArray(),
                txtFiles.get(0));


        String composition = auth.retrieveUserData(selectedFile);

        if (composition != null) {
            try {
                // Clear existing tracks before loading
                tracks.clear();

                BufferedReader reader = new BufferedReader(new java.io.StringReader(composition));
                String line;

                boolean insideTrack = false;
                int currentTrackIndex = -1;
                String trackName = "";
                String trackInstrument = "Piano";
                int trackTempo = 120;
                boolean trackMuted = false;
                List<String> trackChords = new ArrayList<>();

                while ((line = reader.readLine()) != null) {

                    // Check for track section markers
                    if (line.startsWith("## TRACK_START")) {
                        insideTrack = true;
                        trackName = "";
                        trackInstrument = "Piano";
                        trackTempo = 120;
                        trackMuted = false;
                        trackChords.clear();

                        // Extract track index if available
                        try {
                            String indexStr = line.substring("## TRACK_START".length()).trim();
                            currentTrackIndex = Integer.parseInt(indexStr);
                        } catch (Exception e) {
                            currentTrackIndex = tracks.size(); // Use next available index
                        }
                    }

                    if (line.startsWith("## TRACK_END")) {
                        if (insideTrack) {
                            // Create and add the track
                            Track track = new Track(trackName, trackInstrument);
                            track.setTempo(trackTempo);
                            track.setMuted(trackMuted);

                            // Add all chords to the track
                            for (String chord : trackChords) {
                                if (!chord.isEmpty()) {
                                    track.addChord(chord);
                                }
                            }

                            // Add track to tracks map
                            tracks.put(currentTrackIndex, track);
                            insideTrack = false;
                        }
                    }

                    // Parse track properties
                    if (insideTrack) {
                        if (line.startsWith("NAME:")) {
                            trackName = line.substring("NAME:".length()).trim();
                            if (trackName.isEmpty()) {
                                trackName = "Track " + (currentTrackIndex + 1);
                            }
                        } else if (line.startsWith("INSTRUMENT:")) {
                            trackInstrument = line.substring("INSTRUMENT:".length()).trim();
                            if (trackInstrument.isEmpty()) {
                                trackInstrument = "Piano";
                            }
                        } else if (line.startsWith("TEMPO:")) {
                            try {
                                trackTempo = Integer.parseInt(line.substring("TEMPO:".length()).trim());
                            } catch (NumberFormatException e) {
                                trackTempo = 120; 
                            }
                        } else if (line.startsWith("MUTED:")) {
                            trackMuted = Boolean.parseBoolean(line.substring("MUTED:".length()).trim());
                        } else if (line.startsWith("CHORDS:")) {
                            String chordsLine = line.substring("CHORDS:".length()).trim();
                            if (!chordsLine.isEmpty()) {
                                String[] chordsArray = chordsLine.split("\\s+");
                                trackChords.addAll(Arrays.asList(chordsArray));
                            }
                        }
                    } else {
                        // Handle old format files or unknown format
                        // Try to extract chords as a fallback
                        if (!line.startsWith("#") && !line.contains(":")) {
                            String[] oldFormatChords = line.trim().split("\\s+");
                            if (oldFormatChords.length > 0) {
                                if (tracks.isEmpty()) {
                                    // Create a default track for old format files
                                    Track defaultTrack = new Track("Imported Track", "Piano");
                                    for (String chord : oldFormatChords) {
                                        if (!chord.isEmpty()) {
                                            defaultTrack.addChord(chord);
                                        }
                                    }
                                    tracks.put(0, defaultTrack);
                                }
                            }
                        }
                    }
                }

                // Handle case where file ends while still inside a track
                if (insideTrack) {
                    Track track = new Track(trackName, trackInstrument);
                    track.setTempo(trackTempo);
                    track.setMuted(trackMuted);

                    for (String chord : trackChords) {
                        if (!chord.isEmpty()) {
                            track.addChord(chord);
                        }
                    }

                    tracks.put(currentTrackIndex, track);
                }

                // If no tracks were loaded, create a default one
                if (tracks.isEmpty()) {
                    createTrack("Track 1", "Piano");
                }

                // Set current track to first one
                currentTrackIndex = tracks.keySet().iterator().next();

                mainPage.refreshTrackSelector();
                mainPage.updateTrackDisplay(getCurrentTrack());

                JOptionPane.showMessageDialog(mainPage, 
                        "Composition loaded successfully.", 
                        "Load Successful", 
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                // If there's an error parsing, create a default track
                tracks.clear();
                createTrack("Track 1", "Piano");
                currentTrackIndex = 0;

                JOptionPane.showMessageDialog(mainPage, 
                        "Error parsing composition file. Created a default track.", 
                        "Parse Error", 
                        JOptionPane.WARNING_MESSAGE);

            }
        } else {
            JOptionPane.showMessageDialog(mainPage, 
                    "Error loading composition.", 
                    "Load Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void muteTrack(int trackIndex) {
        Track track = tracks.get(trackIndex);
        if (track != null) {
            track.toggleMute();
        }
    }
    
    public void renameTrack(int trackIndex, String newName) {
        Track track = tracks.get(trackIndex);
        if (track != null) {
            track.setName(newName);
        }
    }
    
    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }
}
