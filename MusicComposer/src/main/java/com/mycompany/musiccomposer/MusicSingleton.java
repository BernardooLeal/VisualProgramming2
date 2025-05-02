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
import java.util.HashMap;
import java.util.Map;



/**
 *
 * @author bernardoleal
 */
public class MusicSingleton {
    
    private static MusicSingleton instance;
    private Player player;
    private List<String> recordedChords;
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
        this.recordedChords = new ArrayList<>();
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
        new Thread(() -> player.play("T" + tempo + " I[" + currentTrack.getInstrument() + "] " + chord)).start(); //crazy API syntax 
        
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
                    
                    // highlight current chord
                    mainPage.highlightCurrentChord(i);
                    
                    
                    player.play("T" + tempo + " I[" + trackInstrument + "] " + chord);
                    
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
        for (Track track : tracks.values()) {
            if (!track.getRecordedChords().isEmpty()) {
                hasChords = true;
                break;
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
            // Multi-track playback, used chat for appends
            StringBuilder pattern = new StringBuilder();
            pattern.append("T").append(tempo).append(" ");

            int trackCount = 0;
            for (Map.Entry<Integer, Track> entry : tracks.entrySet()) {
                Track track = entry.getValue();
                if (track.getRecordedChords().isEmpty() || track.isMuted()) {
                    continue;
                }

                pattern.append("V").append(trackCount).append(" ");
                pattern.append("I[").append(track.getInstrument()).append("] ");
                for (String chord : track.getRecordedChords()) {
                    pattern.append(chord).append(" ");
                }
                pattern.append(" ");
                trackCount++;
            }

            if (trackCount > 0) {
                Player localPlayer = new Player();
                localPlayer.play(pattern.toString());
            }

            mainPage.playbackControlsState(STATUS_IDLE);
            currentChordIndex = -1;
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
                    mainPage.updateTextArea(String.join("", recordedChords));
                    
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
    
    public void setTempo(int tempo) {
        String newTempo = JOptionPane.showInputDialog("Enter new tempo:");
        try {
            tempo = Integer.parseInt(newTempo);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainPage,"Invalid tempo input!");
        }
    }
    
    public void setInstrument(String instrument) {
        getCurrentTrack().setInstrument(instrument);
    }

    public String getInstrument() {
        return getCurrentTrack().getInstrument();
    }

    public int getTempo() {
        return tempo;
    }
    
    // Either the loading doesn't load more than 1 track, or it is saving wrong
    public void saveComposition(String composition) {
        if (composition == null || composition.isEmpty()) {
            JOptionPane.showMessageDialog(mainPage, "No composition to save.");
            return;
        }
        
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
            return; // User canceled or entered empty name
        }
        
        if (!fileName.toLowerCase().endsWith(".txt")) {
            fileName += ".txt";
        }
        
        try {
            StringBuilder content = new StringBuilder();
            content.append("# Music Composition\n");
            content.append("# Tempo: ").append(tempo).append("\n\n");
            
            // Save each track separately
            for (Map.Entry<Integer, Track> entry : tracks.entrySet()) {
                Track track = entry.getValue();
                content.append("# Track: ").append(track.getName()).append("\n");
                content.append("# Instrument: ").append(track.getInstrument()).append("\n");
                content.append("# Muted: ").append(track.isMuted()).append("\n");
                content.append(String.join(" ", track.getRecordedChords())).append("\n\n");
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
    
    //not working when there are more than 1 track / or the problem is in the saving. Layout on the track area was giving by chat
    public String loadComposition() {
        AuthenticationSingleton auth = AuthenticationSingleton.getInstance();

        if (!auth.isLoggedIn()) {
            JOptionPane.showMessageDialog(mainPage, "You must be logged in to load compositions.", 
                    "Login Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        String[] files = auth.listUserFiles();

        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(mainPage, "No saved compositions found.", 
                    "No Files", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        //if for some reason the files are not txt
        List<String> txtFiles = new ArrayList<>();
        for (String file : files) {
            if (file.toLowerCase().endsWith(".txt")) {
                txtFiles.add(file);
            }
        }

        if (txtFiles.isEmpty()) {
            JOptionPane.showMessageDialog(mainPage, "No saved compositions found.", 
                    "No Files", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        String selectedFile = (String) JOptionPane.showInputDialog(
                mainPage,
                "Select a composition to load:",
                "Load Composition",
                JOptionPane.QUESTION_MESSAGE,
                null,
                txtFiles.toArray(),
                txtFiles.get(0));

        if (selectedFile == null) {
            return null; // User canceled
        }

        String composition = auth.retrieveUserData(selectedFile);

        if (composition != null) {
            JOptionPane.showMessageDialog(mainPage, 
                    "Composition loaded successfully.", 
                    "Load Successful", 
                    JOptionPane.INFORMATION_MESSAGE);

            tracks.clear();

            //track
            try {
                    BufferedReader reader = new BufferedReader(new java.io.StringReader(composition));
                    String line;
                    StringBuilder trackContent = new StringBuilder();
                    String trackName = "Track 1";
                    String trackInstrument = "Piano";
                    boolean trackMuted = false;
                    int trackIndex = 0;

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("# Tempo:")) {
                            int tempoValue = Integer.parseInt(line.substring("# Tempo:".length()).trim());
                            this.tempo = tempoValue;
                        } else if (line.startsWith("# Track:")) {
                            // If we have a previous track to save
                            if (trackContent.length() > 0) {
                                Track track = new Track(trackName, trackInstrument);
                                track.setMuted(trackMuted);

                                // Add chords to track
                                String[] chordsArray = trackContent.toString().trim().split("\\s+");
                                for (String chord : chordsArray) {
                                    if (!chord.isEmpty()) {
                                        track.addChord(chord);
                                    }
                                }

                                tracks.put(trackIndex, track);
                                trackIndex++;
                                trackContent = new StringBuilder();
                            }

                            trackName = line.substring("# Track:".length()).trim();
                        } else if (line.startsWith("# Instrument:")) {
                            trackInstrument = line.substring("# Instrument:".length()).trim();
                            if (trackInstrument.isEmpty()) {
                                trackInstrument = "Piano";
                            }
                        } else if (line.startsWith("# Muted:")) {
                            trackMuted = Boolean.parseBoolean(line.substring("# Muted:".length()).trim());
                        } else if (!line.isEmpty() && !line.startsWith("#")) {
                            trackContent.append(line).append(" ");
                        }
                    }

                    // Add the last track if there is one
                    if (trackContent.length() > 0) {
                        Track track = new Track(trackName, trackInstrument);
                        track.setMuted(trackMuted);

                        // Add chords to track
                        String[] chordsArray = trackContent.toString().trim().split("\\s+");
                        for (String chord : chordsArray) {
                            if (!chord.isEmpty()) {
                                track.addChord(chord);
                            }
                        }

                        tracks.put(trackIndex, track);
                    }

                    // If no tracks were loaded, create a default one
                    if (tracks.isEmpty()) {
                        createTrack("Track 1", "Piano");
                    }

                    // Set current track to first one
                    currentTrackIndex = 0;

                    mainPage.updateTrackDisplay(getCurrentTrack());
                    mainPage.updateTextArea(String.join(" ", getCurrentTrack().getRecordedChords()));
                    mainPage.instrumentSelection(getCurrentTrack().getInstrument());

                    return composition;

                } catch (IOException e) {
                    // If there's an error parsing, create a default track
                    tracks.clear();
                    createTrack("Track 1", "Piano");
                    currentTrackIndex = 0;

                    JOptionPane.showMessageDialog(mainPage, 
                            "Error parsing composition file. Created a default track.", 
                            "Parse Error", 
                            JOptionPane.WARNING_MESSAGE);

                    return composition;
                }
            } else {
                JOptionPane.showMessageDialog(mainPage, 
                        "Error loading composition.", 
                        "Load Error", 
                        JOptionPane.ERROR_MESSAGE);
                return null;
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
