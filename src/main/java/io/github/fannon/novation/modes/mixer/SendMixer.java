package io.github.fannon.novation.modes.mixer;

import com.bitwig.extension.controller.api.*;
import io.github.fannon.novation.Mode;
import io.github.fannon.novation.modes.session.ArrowPadLight;
import io.github.fannon.novation.modes.session.TrackColorFaderLight;
import io.github.fannon.novation.surface.Fader;
import io.github.fannon.novation.surface.LaunchpadProMk3Pad;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SendMixer extends AbstractFaderMixerMode {
    private TrackColorFaderLight[] faderLights = new TrackColorFaderLight[8];
    private Parameter[] sends = new Parameter[8];

    private ArrowPadLight trackForwardLight;
    private ArrowPadLight trackBackwardLight;
    private HardwareActionBindable trackForwardAction;
    private HardwareActionBindable trackBackwardAction;

    public SendMixer(AtomicReference<Mode> mixerMode, ControllerHost host, Transport transport,
                     LaunchpadProMk3Surface surface, CursorTrack track) {
        super(mixerMode, host, transport, surface, Mode.MIXER_SEND, 82);

        SendBank bank = track.sendBank();

        for(int i = 0; i < 8; i++) {
            Send send = bank.getItemAt(i);
            faderLights[i] = new TrackColorFaderLight(surface, send, this::redraw);
            sends[i] = send;
        }

        trackForwardLight = new ArrowPadLight(surface, bank.canScrollForwards(), mModeColor, this::redraw);
        trackBackwardLight = new ArrowPadLight(surface, bank.canScrollBackwards(), mModeColor, this::redraw);
        trackForwardAction = bank.scrollForwardsAction();
        trackBackwardAction = bank.scrollBackwardsAction();
    }

    private LaunchpadProMk3Pad getBack(LaunchpadProMk3Surface surface) { return surface.left(); }
    private LaunchpadProMk3Pad getForward(LaunchpadProMk3Surface surface) { return surface.right(); }

    @Override
    public void onDraw(LaunchpadProMk3Surface surface) {
        super.onDraw(surface);

        drawMixerModeIndicator(surface, 2);

        Fader[] faders = surface.faders();
        for(int i = 0; i < faders.length; i++) {
            faderLights[i].draw(faders[i].light());
        }

        LaunchpadProMk3Pad back = getBack(surface);
        LaunchpadProMk3Pad frwd = getForward(surface);
        trackBackwardLight.draw(back.light());
        trackForwardLight.draw(frwd.light());
    }

    @Override
    public List<HardwareBinding> onBind(LaunchpadProMk3Surface surface) {
        List<HardwareBinding> list = super.onBind(surface);

        // Enable faders
        surface.setupFaders(true, false, 37);

        LaunchpadProMk3Pad back = getBack(surface);
        LaunchpadProMk3Pad frwd = getForward(surface);
        list.add(back.button().pressedAction().addBinding(trackBackwardAction));
        list.add(frwd.button().pressedAction().addBinding(trackForwardAction));

        for(int i = 0; i < 8; i++) {
            Fader sendFader = surface.faders()[i];
            list.add(sends[i].addBinding(sendFader.fader()));
        }

        return list;
    }
}
