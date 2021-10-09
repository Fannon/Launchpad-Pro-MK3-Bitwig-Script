package io.github.fannon.novation.modes.mixer;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Transport;
import io.github.fannon.novation.Mode;
import io.github.fannon.novation.internal.Session;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Switches the Launchpad to fader mode, when all bindings are complete
 */
public abstract class AbstractFaderMixerMode extends AbstractMixerMode {

    public AbstractFaderMixerMode(AtomicReference<Mode> mixerMode, ControllerHost host,
                                  Transport transport, LaunchpadProMk3Surface lSurf, Mode targetMode, int modeColor) {
        super(mixerMode, host, transport, lSurf, targetMode, modeColor);
    }

    @Override
    public void finishedBind(Session session) {
        super.finishedBind(session);
        session.sendSysex("00 0D");
    }
}
