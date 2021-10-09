package io.github.fannon.novation.modes.mixer;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.*;
import io.github.fannon.novation.Mode;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;
import io.github.fannon.novation.surface.NoteButton;
import io.github.fannon.novation.surface.state.PadLightState;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MuteMixer extends AbstractSessionMixerMode {
    private MuteRowPadLight[] mMutePads = new MuteRowPadLight[8];
    private HardwareActionBindable[] mMuteAction = new HardwareActionBindable[8];

    private class MuteRowPadLight {
        private BooleanValue mMute;
        private BooleanValue mExists;
        public MuteRowPadLight(LaunchpadProMk3Surface surface, Track track) {
            mMute = track.mute();
            mExists = track.exists();

            mMute.addValueObserver(s -> redraw(surface));
            mExists.addValueObserver(e -> redraw(surface));
        }

        public void draw(MultiStateHardwareLight light) {
            if(mExists.get()) {
                if(mMute.get()) {
                    light.state().setValue(PadLightState.solidLight(9));
                } else {
                    light.state().setValue(PadLightState.solidLight(11));
                }
            } else {
                light.setColor(Color.nullColor());
            }
        }
    }

    public MuteMixer(AtomicReference<Mode> mixerMode, ControllerHost host, Transport transport,
                     LaunchpadProMk3Surface surface, TrackBank bank) {
        super(mixerMode, host, transport, surface, bank, Mode.MIXER_MUTE, 9);

        for(int i = 0; i < 8; i++) {
            Track track = bank.getItemAt(i);
            mMutePads[i] = new MuteRowPadLight(surface, track);
            mMuteAction[i] = track.mute().toggleAction();
        }
    }

    @Override
    public void onDraw(LaunchpadProMk3Surface surface) {
        super.onDraw(surface);

        drawMixerModeIndicator(surface, 5);

        NoteButton[] finalRow = getFinalRow(surface);
        for(int i = 0; i < finalRow.length; i++) {
            mMutePads[i].draw(finalRow[i].light());
        }
    }

    @Override
    public List<HardwareBinding> onBind(LaunchpadProMk3Surface surface) {
        List<HardwareBinding> list = super.onBind(surface);

        NoteButton[] finalRow = getFinalRow(surface);
        for(int i = 0; i < finalRow.length; i++) {
            NoteButton pad = finalRow[i];
            list.add(pad.button().pressedAction().addBinding(mMuteAction[i]));
        }

        return list;
    }
}
