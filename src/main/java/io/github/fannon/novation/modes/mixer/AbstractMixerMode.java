package io.github.fannon.novation.modes.mixer;

import com.bitwig.extension.controller.api.*;
import io.github.fannon.novation.Mode;
import io.github.fannon.novation.internal.Session;
import io.github.fannon.novation.modes.AbstractMode;
import io.github.fannon.novation.surface.LaunchpadProMk3Pad;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;
import io.github.fannon.novation.surface.state.PadLightState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractMixerMode extends AbstractMode {
    protected RangedValue mBPM;
    protected AtomicReference<Mode> mMixerMode;
    private final Mode mTargetMode;

    private static final Mode[] scenemodes = new Mode[] {
            Mode.MIXER_VOLUME,
            Mode.MIXER_PAN,
            Mode.MIXER_SEND,
            Mode.MIXER_CONTROLS,
            Mode.MIXER_STOP,
            Mode.MIXER_MUTE,
            Mode.MIXER_SOLO,
            Mode.MIXER_ARM
    };
    private HardwareActionBindable[] sceneActions = new HardwareActionBindable[8];
    protected int mModeColor;

    public AbstractMixerMode(AtomicReference<Mode> mixerMode, ControllerHost host,
                             Transport transport, LaunchpadProMk3Surface lSurf, Mode targetMode, int modeColor) {
        mBPM = transport.tempo().modulatedValue();
        mMixerMode = mixerMode;
        mTargetMode = targetMode;

        mBPM.markInterested();
        mModeColor = modeColor;

        for(int i = 0; i < 8; i++) {
            final int j = i;
            sceneActions[i] = host.createAction(() -> mModeMachine.setMode(lSurf, scenemodes[j]), () -> "Set mode to " + scenemodes[j].toString());
        }
    }

    public void drawMixerModeIndicator(LaunchpadProMk3Surface surface, int padIndex) {
        for(int i = 0; i < 8; i++) {
            LaunchpadProMk3Pad scene = surface.scenes()[i];
            if(i == padIndex) {
                scene.light().state().setValue(PadLightState.pulseLight(mBPM.getRaw(), mModeColor));
            } else {
                scene.light().state().setValue(PadLightState.solidLight(1));
            }
        }
    }

    @Override
    public List<HardwareBinding> onBind(LaunchpadProMk3Surface surface) {
        List<HardwareBinding> bindings = new ArrayList<>();

        for(int i = 0; i < 8; i++) {
            LaunchpadProMk3Pad scene = surface.scenes()[i];
            bindings.add(scene.button().pressedAction().addBinding(sceneActions[i]));
        }

        return bindings;
    }

    @Override
    public void finishedBind(Session session) {
        session.sendSysex("14 6C 02");
        mMixerMode.set(mTargetMode);
    }
}
