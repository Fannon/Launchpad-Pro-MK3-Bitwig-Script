package io.github.fannon.novation.modes.mixer;

import com.bitwig.extension.controller.api.*;
import io.github.fannon.novation.Mode;
import io.github.fannon.novation.internal.Session;
import io.github.fannon.novation.modes.session.ArrowPadLight;
import io.github.fannon.novation.modes.session.SessionPadLight;
import io.github.fannon.novation.surface.LaunchpadProMk3Pad;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;
import io.github.fannon.novation.surface.NoteButton;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractSessionMixerMode extends AbstractMixerMode {
    private SessionPadLight[][] padLights = new SessionPadLight[7][8];
    private HardwareActionBindable[][] padActions = new HardwareActionBindable[7][8];
    private ArrowPadLight[] arrowLights = new ArrowPadLight[4];
    private HardwareBindable[] arrowActions;

    public AbstractSessionMixerMode(AtomicReference<Mode> mixerMode, ControllerHost host,
                                    Transport transport, LaunchpadProMk3Surface surface, TrackBank bank, Mode targetMode, int modeColor) {
        super(mixerMode, host, transport, surface, targetMode, modeColor);

        // Setup pad lights and buttons
        /*
        The indicies map the pad out as
        0,0.......0,7
        1,0.......1,7
        .          .
        .          .
        .          .
        7,0.......7,7

        since we want scenes to go down, we simply mark the indicies as (scene, track)
         */
        for(int scene = 0; scene < 7; scene++) {
            padActions[scene] = new HardwareActionBindable[8];
            padLights[scene] = new SessionPadLight[8];
            for(int trk = 0; trk < 8; trk++) {
                Track track = bank.getItemAt(trk);
                ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();
                ClipLauncherSlot slot = slotBank.getItemAt(scene);

                padLights[scene][trk] = new SessionPadLight(surface, slot, track, mBPM, this::redraw, scene);
                padActions[scene][trk] = slot.launchAction();
            }
        }

        arrowActions = new HardwareActionBindable[] {
                bank.sceneBank().scrollBackwardsAction(),
                bank.sceneBank().scrollForwardsAction(),
                bank.scrollBackwardsAction(),
                bank.scrollForwardsAction()
        };

        BooleanValue[] arrowEnabled = new BooleanValue[]{
                bank.sceneBank().canScrollBackwards(),
                bank.sceneBank().canScrollForwards(),
                bank.canScrollBackwards(),
                bank.canScrollForwards()
        };

        LaunchpadProMk3Pad[] arrows = surface.arrows();
        for(int i = 0; i < arrows.length; i++) {
            arrowLights[i] = new ArrowPadLight(surface, arrowEnabled[i], mModeColor, this::redraw);
        }
    }

    protected final NoteButton[] getFinalRow(LaunchpadProMk3Surface surface) {
        return surface.notes()[7];
    }

    @Override
    public void onDraw(LaunchpadProMk3Surface surface) {
        super.onDraw(surface);

        LaunchpadProMk3Pad[] arrows = surface.arrows();
        for(int i = 0; i < arrows.length; i++) {
            arrowLights[i].draw(arrows[i].light());
        }

        LaunchpadProMk3Pad[][] pads = surface.notes();
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 8; j++) {
                padLights[i][j].draw(pads[i][j].light());
            }
        }
    }

    @Override
    public List<HardwareBinding> onBind(LaunchpadProMk3Surface surface) {
        List<HardwareBinding> bindings = super.onBind(surface);

        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 8; j++) {
                NoteButton button = surface.notes()[i][j];
                bindings.add(button.button().pressedAction().addBinding(padActions[i][j]));
            }
        }
        LaunchpadProMk3Pad[] arrows = new LaunchpadProMk3Pad[]{surface.up(), surface.down(), surface.left(), surface.right()};
        for(int i = 0; i < 4; i++) {
            bindings.add(arrows[i].button().pressedAction().addBinding(arrowActions[i]));
        }

        return bindings;
    }

    @Override
    public void finishedBind(Session session) {
        super.finishedBind(session);
        session.sendSysex("00 00");
    }
}
