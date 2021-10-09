package io.github.fannon.novation.surface;

import com.bitwig.extension.controller.api.*;
import io.github.fannon.novation.Utils;
import io.github.fannon.novation.internal.ChannelType;
import io.github.fannon.novation.internal.Session;

import java.util.Arrays;

public class LaunchpadProMk3Surface {
    private CCButton mUpArrow;
    private CCButton mDownArrow;
    private CCButton mLeftArrow;
    private CCButton mRightArrow;
    private CCButton mSessionButton;
    private CCButton mNoteButton;
    private CCButton mCustomButton;
    private CCButton mRecordButton;
    private CCButton mNovationButton;
    private AbsoluteHardwareControl mChannelPressure;
    private Session mSession;
    private HardwareSurface mSurface;

    private CCButton[] mSceneButtons;

    private NoteButton[][] mNoteButtons;

    private Fader[] mFaders;

    //private int[] mVolumeFaderCCs = new int[]{21, 22, 23, 24, 25, 26, 27, 28};
    //private int[] mFaderCCs = new int[]{45, 46, 47, 48, 49, 50, 51, 52};

    public LaunchpadProMk3Pad up() { return mUpArrow; }
    public LaunchpadProMk3Pad down() { return mDownArrow; }
    public LaunchpadProMk3Pad left() { return  mLeftArrow; }
    public LaunchpadProMk3Pad right() { return mRightArrow; }
    public LaunchpadProMk3Pad[] arrows() { return new LaunchpadProMk3Pad[] {mUpArrow, mDownArrow, mLeftArrow, mRightArrow}; }

    public LaunchpadProMk3Pad session() { return mSessionButton; }
    public LaunchpadProMk3Pad note() { return mNoteButton; }
    public LaunchpadProMk3Pad custom() { return mCustomButton; }

    public LaunchpadProMk3Pad record() { return mRecordButton; }
    public LaunchpadProMk3Pad novation() { return mNovationButton; }

    public LaunchpadProMk3Pad[] scenes() { return mSceneButtons; }
    public NoteButton[][] notes() { return mNoteButtons; }
    public AbsoluteHardwareControl channelPressure() { return mChannelPressure; }

    public Fader[] faders() { return mFaders; }

    public LaunchpadProMk3Surface(ControllerHost host, Session session, HardwareSurface surface) {
        mUpArrow = new CCButton(session, surface, "Up", 91, 13, 13);
        mDownArrow = new CCButton(session, surface, "Down", 92, 13 + 23, 13);
        mLeftArrow = new CCButton(session, surface, "Left", 93, 13 + 23*2, 13);
        mRightArrow = new CCButton(session, surface, "Right", 94, 13 + 23*3, 13);
        mSessionButton = new CCButton(session, surface, "Session", 95, 13 + 23*4, 13);
        mNoteButton = new CCButton(session, surface, "Note", 96, 13 + 23*5, 13);
        mCustomButton = new CCButton(session, surface, "Custom", 97, 13+23*6, 13);
        mRecordButton = new CCButton(session, surface, "Record", 98, 13 + 23*7, 13);
        mNovationButton = new CCButton(session, surface, "N", 99, 13 + 23 * 8, 13);
        mSceneButtons = new CCButton[8];

        mChannelPressure = surface.createAbsoluteHardwareKnob("Pressure");
        MidiIn in = session.midiIn(ChannelType.DAW);
        AbsoluteHardwareValueMatcher onDrumChannelPressure = in.createAbsoluteValueMatcher("status == 0xD8", "data1", 7);
        mChannelPressure.setAdjustValueMatcher(onDrumChannelPressure);

        mSession = session;
        mSurface = surface;

        for(int i = 0; i < 8; i++) {
            mSceneButtons[i] = new CCButton(session, surface, "S" + (i+1), (8 - i) * 10 + 9, 13 + 23 * 8, 13 + 23 * (1 + i));
        }

        mNoteButtons = new NoteButton[8][8];
        int[] row_offsets = new int[]{80, 70, 60, 50, 40, 30, 20, 10};
        int[] drum_pad_notes = new int[] {
            64, 65, 66, 67, 96, 97, 98, 99,
            60, 61, 62, 63, 92, 93, 94, 95,
            56, 57, 58, 59, 88, 89, 90, 91,
            52, 53, 54, 55, 84, 85, 86, 87,
            48, 49, 50, 51, 80, 81, 82, 83,
            44, 45, 46, 47, 76, 77, 78, 79,
            40, 41, 42, 43, 72, 73, 74, 75,
            36, 37, 38, 39, 68, 69, 70, 71
        };
        for(int row = 0; row < 8; row++) {
            mNoteButtons[row] = new NoteButton[8];
            for(int col = 0; col < 8; col++) {
                mNoteButtons[row][col] = new NoteButton(host, session, surface, "" + row + "," + col, row_offsets[row] + col + 1, drum_pad_notes[row * 8 + col], 13 + (col * 23), 13 + 23 + (row * 23));
            }
        }

        mFaders = new Fader[8];
        for(int i = 0; i < 8; i++) {
            mFaders[i] = new Fader(session, surface, "FV" + i,0, i * 24);
        }
    }

    public void setupFaders(boolean vertical, boolean bipolar, int baseCC) {
        boolean[] bipolars = new boolean[8];
        Arrays.fill(bipolars, bipolar);
        setupFaders(vertical, bipolars, baseCC);
    }

    public void setupFaders(boolean vertical, boolean[] bipolar, int baseCC) {
        StringBuilder sysexString = new StringBuilder();
        sysexString.append("01 00");
        if(vertical) {
            sysexString.append("00 ");
        } else {
            sysexString.append("01 ");
        }
//        assert colors.length == 8;
        for(int i = 0; i < 8; i++) {
            int cc = baseCC + i;
            sysexString.append(Utils.toHexString((byte)i));
            if(bipolar[i]) {
                sysexString.append("01");
            } else {
                sysexString.append("00");
            }
            sysexString.append(Utils.toHexString((byte)cc));
//            sysexString.append(Utils.toHexString((byte)colors[i]));
            sysexString.append("00 ");
        }
        mSurface.invalidateHardwareOutputState();
        mSession.sendSysex(sysexString.toString());

        for(int i = 0; i < 8; i++) {
            mFaders[i].setId(baseCC + i);
        }
    }

    /**
     * Clears all color states for the surface.
     */
    public void clear() {
        mUpArrow.resetColor();
        mDownArrow.resetColor();
        mLeftArrow.resetColor();
        mRightArrow.resetColor();

        for(LaunchpadProMk3Pad scenePad : mSceneButtons) {
            scenePad.resetColor();
        }

        for(LaunchpadProMk3Pad[] noteRow : mNoteButtons) {
            for(LaunchpadProMk3Pad note : noteRow) {
                note.resetColor();
            }
        }

        for(Fader fader : mFaders) {
            fader.resetColor();
        }
    }
}
