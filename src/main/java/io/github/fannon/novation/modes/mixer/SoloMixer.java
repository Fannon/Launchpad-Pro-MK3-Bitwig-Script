package io.github.fannon.novation.modes.mixer;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.*;
import io.github.fannon.novation.Mode;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;
import io.github.fannon.novation.surface.NoteButton;
import io.github.fannon.novation.surface.state.PadLightState;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SoloMixer extends AbstractSessionMixerMode {
    private SoloRowPadLight[] mSoloPads = new SoloRowPadLight[8];
    private HardwareActionBindable[] mSoloAction = new HardwareActionBindable[8];

    private class SoloRowPadLight {
        private BooleanValue mSolo;
        private BooleanValue mExists;
        public SoloRowPadLight(LaunchpadProMk3Surface surface, Track track) {
            mSolo = track.solo();
            mExists = track.exists();

            mSolo.addValueObserver(s -> redraw(surface));
            mExists.addValueObserver(e -> redraw(surface));
        }

        public void draw(MultiStateHardwareLight light) {
            if(mExists.get()) {
                if(mSolo.get()) {
                    light.state().setValue(PadLightState.solidLight(124));
                } else {
                    light.state().setValue(PadLightState.solidLight(125));
                }
            } else {
                light.setColor(Color.nullColor());
            }
        }
    }

    public SoloMixer(AtomicReference<Mode> mixerMode, ControllerHost host, Transport transport,
                     LaunchpadProMk3Surface surface, TrackBank bank) {
        super(mixerMode, host, transport, surface, bank, Mode.MIXER_SOLO, 124);

        for(int i = 0; i < 8; i++) {
            Track track = bank.getItemAt(i);
            mSoloPads[i] = new SoloRowPadLight(surface, track);
            mSoloAction[i] = track.solo().toggleAction();
        }
    }

    @Override
    public void onDraw(LaunchpadProMk3Surface surface) {
        super.onDraw(surface);

        drawMixerModeIndicator(surface, 6);

        NoteButton[] finalRow = getFinalRow(surface);
        for(int i = 0; i < finalRow.length; i++) {
            mSoloPads[i].draw(finalRow[i].light());
        }
    }

    @Override
    public List<HardwareBinding> onBind(LaunchpadProMk3Surface surface) {
        List<HardwareBinding> list = super.onBind(surface);

        NoteButton[] finalRow = getFinalRow(surface);
        for(int i = 0; i < finalRow.length; i++) {
            NoteButton pad = finalRow[i];
            list.add(pad.button().pressedAction().addBinding(mSoloAction[i]));
        }

        return list;
    }
}
