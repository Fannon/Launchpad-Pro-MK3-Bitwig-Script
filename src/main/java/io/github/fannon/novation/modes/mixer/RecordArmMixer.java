package io.github.fannon.novation.modes.mixer;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.*;
import io.github.fannon.novation.Mode;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;
import io.github.fannon.novation.surface.NoteButton;
import io.github.fannon.novation.surface.state.PadLightState;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RecordArmMixer extends AbstractSessionMixerMode {
    private ArmRowPadLight[] mArmPads = new ArmRowPadLight[8];
    private HardwareActionBindable[] mArmAction = new HardwareActionBindable[8];

    private class ArmRowPadLight {
        private BooleanValue mArm;
        private BooleanValue mHasNoteInput;
        private BooleanValue mHasAudioInput;
        private BooleanValue mExists;
        public ArmRowPadLight(LaunchpadProMk3Surface surface, Track track) {
            mArm = track.arm();
            mExists = track.exists();
            mHasAudioInput = track.sourceSelector().hasAudioInputSelected();
            mHasNoteInput = track.sourceSelector().hasNoteInputSelected();

            mArm.addValueObserver(s -> redraw(surface));
            mExists.addValueObserver(e -> redraw(surface));
            mHasNoteInput.addValueObserver(n -> redraw(surface));
            mHasAudioInput.addValueObserver(a -> redraw(surface));
        }

        public void draw(MultiStateHardwareLight light) {
            if(mExists.get()) {
                if(mArm.get()) {
                    light.state().setValue(PadLightState.solidLight(120));
                } else if (mHasNoteInput.get() || mHasAudioInput.get()) {
                    light.state().setValue(PadLightState.solidLight(121));
                } else {
                    light.setColor(Color.nullColor());
                }
            } else {
                light.setColor(Color.nullColor());
            }
        }
    }

    public RecordArmMixer(AtomicReference<Mode> mixerMode, ControllerHost host, Transport transport,
                          LaunchpadProMk3Surface surface, TrackBank bank) {
        super(mixerMode, host, transport, surface, bank, Mode.MIXER_ARM, 120);

        for(int i = 0; i < 8; i++) {
            Track track = bank.getItemAt(i);

            mArmPads[i] = new ArmRowPadLight(surface, track);
            mArmAction[i] = track.arm().toggleAction();
        }
    }

    @Override
    public void onDraw(LaunchpadProMk3Surface surface) {
        super.onDraw(surface);

        drawMixerModeIndicator(surface, 7);

        NoteButton[] finalRow = getFinalRow(surface);
        for(int i = 0; i < finalRow.length; i++) {
            mArmPads[i].draw(finalRow[i].light());
        }
    }

    @Override
    public List<HardwareBinding> onBind(LaunchpadProMk3Surface surface) {
        List<HardwareBinding> list = super.onBind(surface);

        // Bind the final row of pads
        NoteButton[] finalRow = getFinalRow(surface);
        for(int i = 0; i < finalRow.length; i++) {
            NoteButton pad = finalRow[i];
            list.add(pad.button().pressedAction().setBinding(mArmAction[i]));
        }

        return list;
    }
}
