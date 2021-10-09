package io.github.fannon.novation.modes.mixer;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.*;
import io.github.fannon.novation.Mode;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;
import io.github.fannon.novation.surface.NoteButton;
import io.github.fannon.novation.surface.state.PadLightState;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class StopClipMixer extends AbstractSessionMixerMode {
    private StopRowPadLight[] mStopPads = new StopRowPadLight[8];
    private HardwareActionBindable[] mStopAction = new HardwareActionBindable[8];

    private class StopRowPadLight {
        private BooleanValue mStop;
        private BooleanValue mExists;
        public StopRowPadLight(LaunchpadProMk3Surface surface, Track track) {
            mStop = track.isStopped();
            mExists = track.exists();

            mStop.addValueObserver(s -> redraw(surface));
            mExists.addValueObserver(e -> redraw(surface));
        }

        public void draw(MultiStateHardwareLight light) {
            if(mExists.get()) {
                if(mStop.get()) {
                    light.state().setValue(PadLightState.solidLight(7));
                } else {
                    light.state().setValue(PadLightState.solidLight(5));
                }
            } else {
                light.setColor(Color.nullColor());
            }
        }
    }

    public StopClipMixer(AtomicReference<Mode> mixerMode, ControllerHost host, Transport transport,
                         LaunchpadProMk3Surface surface, TrackBank bank) {
        super(mixerMode, host, transport, surface, bank, Mode.MIXER_STOP, 5);

        for(int i = 0; i < 8; i++) {
            Track track = bank.getItemAt(i);
            mStopPads[i] = new StopRowPadLight(surface, track);
            mStopAction[i] = track.stopAction();
        }
    }

    @Override
    public void onDraw(LaunchpadProMk3Surface surface) {
        super.onDraw(surface);

        drawMixerModeIndicator(surface, 4);

        NoteButton[] finalRow = getFinalRow(surface);
        for(int i = 0; i < finalRow.length; i++) {
            mStopPads[i].draw(finalRow[i].light());
        }
    }

    @Override
    public List<HardwareBinding> onBind(LaunchpadProMk3Surface surface) {
        List<HardwareBinding> list = super.onBind(surface);

        NoteButton[] finalRow = getFinalRow(surface);
        for(int i = 0; i < finalRow.length; i++) {
            NoteButton pad = finalRow[i];
            list.add(pad.button().pressedAction().addBinding(mStopAction[i]));
        }

        return list;
    }
}
