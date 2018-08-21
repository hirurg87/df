import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
import java.io.*;
import java.util.*;

public class BeatBox {
    JPanel mainPanel;
    ArrayList<JCheckBox> checkBoxeList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
                    "Hand Clap", "Hi Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
                    "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

    public static void main(String[] args) {
        new BeatBox().buildGui();
    }

    public void buildGui(){
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem loadMenuItem = new JMenuItem("Load");
        JMenuItem clearMenuItem = new JMenuItem("Clear");

        saveMenuItem.addActionListener(new SaveCfgListener());
        loadMenuItem.addActionListener(new LoadCfgListener());
        clearMenuItem.addActionListener(new ClearCfgListener());
        fileMenu.add(saveMenuItem);
        fileMenu.add(loadMenuItem);
        fileMenu.add(clearMenuItem);
        menuBar.add(fileMenu);


        checkBoxeList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton sendScheme = new JButton("Serialize it");
        sendScheme.addActionListener(new MySendListener());
        buttonBox.add(sendScheme);

        JButton readScheme = new JButton("Restore");
        readScheme.addActionListener(new MyReadInListener());
        buttonBox.add(readScheme);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(int i = 0; i < 16; i++){
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        theFrame.setJMenuBar(menuBar);

        theFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++){
           JCheckBox c = new JCheckBox();
           c.setSelected(false);
           checkBoxeList.add(c);
           mainPanel.add(c);
        }

        setUpMidi();

        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    public void setUpMidi(){
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }catch (Exception e) {e.printStackTrace();}
    }

    public void buildTrackAndStart(){
        int[] trackList = null;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++){
            trackList = new int[16];

            int key = instruments[i];

            for(int j = 0; j < 16; j++){
                JCheckBox jc = (JCheckBox) checkBoxeList.get(j + (16*i));
                if(jc.isSelected()){
                    trackList[j] = key;
                }else {
                    trackList[j] = 0;
                }
            }

            makeTracks(trackList);
            track.add(makeEvent(176,1,127,0,16));
        }

        track.add(makeEvent(192,9,1,0,15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        }catch (Exception e){ e.printStackTrace();}
    }

    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            sequencer.stop();
        }
    }

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * 1.03));
        }
    }

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (tempoFactor * .97));
        }
    }

    public class MySendListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            String patch = new String("CheckBox.ser");
            saveBeats(patch);

        }
    }

    private void saveBeats(String p){
        String path = p;
        boolean[] checkBoxState = new boolean[256];


        for (int i = 0; i < 256; i++){
            JCheckBox check = (JCheckBox) checkBoxeList.get(i);
            if(check.isSelected()){
                checkBoxState[i] = true;
            }
        }

        try {
            FileOutputStream fileStream = new FileOutputStream(new File(path));
            ObjectOutputStream os = new ObjectOutputStream(fileStream);
            os.writeObject(checkBoxState);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private class SaveCfgListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String patch = new String("CheckBox1.ser");
            saveBeats(patch);
        }
    }

    private class LoadCfgListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }

    private class ClearCfgListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {

            for(int i = 0; i < 256; i++){
                JCheckBox check = (JCheckBox) checkBoxeList.get(i);
                check.setSelected(false);

            }
        }
    }

    public class MyReadInListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            boolean[] checkBoxState = null;
            try {
                FileInputStream fileIn = new FileInputStream(new File("CheckBox.ser"));
                ObjectInputStream is = new ObjectInputStream(fileIn);
                checkBoxState = (boolean[]) is.readObject();
            }catch (Exception ex){
                ex.printStackTrace();
            }
            for(int i = 0; i < 256; i++){
                JCheckBox check = (JCheckBox) checkBoxeList.get(i);
                if(checkBoxState[i]){
                    check.setSelected(true);
                }else {
                    check.setSelected(false);
                }
        }

        sequencer.stop();
        buildTrackAndStart();
        }

    }

    public void makeTracks(int[] list){

        for (int i = 0; i < 16; i++ ){
            int key = list[i];

            if(key != 0){
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick){
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        }catch (Exception e){e.printStackTrace();}
        return event;
    }



}