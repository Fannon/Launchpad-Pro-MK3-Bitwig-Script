package io.github.fannon.novation.modes;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.*;
import io.github.fannon.novation.Utils;
import io.github.fannon.novation.internal.Session;
import io.github.fannon.novation.modes.session.ArrowPadLight;
import io.github.fannon.novation.modes.session.SessionPadLight;
import io.github.fannon.novation.surface.LaunchpadProMk3Pad;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;
import io.github.fannon.novation.surface.state.PadLightState;

import java.util.ArrayList;
import java.util.List;

public class SessionMode extends AbstractMode {
    private SessionSceneLight[] sceneLights = new SessionSceneLight[8];
    private HardwareActionBindable[] sceneLaunchActions = new HardwareActionBindable[8];
    private SessionPadLight[][] padLights = new SessionPadLight[8][8];
    private HardwareActionBindable[][] padActions = new HardwareActionBindable[8][8];
    private ArrowPadLight[] arrowLights = new ArrowPadLight[4];
    private HardwareBindable[] arrowActions;

    private class SessionSceneLight {
        private RangedValue mBPM;
        private BooleanValue mPulseSessionPads;
        private ColorValue mSceneColor;
        private BooleanValue mSceneExists;
        public SessionSceneLight(LaunchpadProMk3Surface surface, Scene scene, BooleanValue pulseSessionPads, RangedValue bpm) {
            mBPM = bpm;
            mPulseSessionPads = pulseSessionPads;
            mSceneColor = scene.color();
            mSceneExists = scene.exists();

            mSceneColor.addValueObserver((r, g, b) -> redraw(surface));
            mSceneExists.addValueObserver(e -> redraw(surface));
            mBPM.addValueObserver(b -> redraw(surface));
            mPulseSessionPads.addValueObserver(p -> redraw(surface));
        }

        public void draw(MultiStateHardwareLight sceneLight) {
            Color baseColor = mSceneColor.get();
            if(mSceneExists.get()) {
                if(mPulseSessionPads.get()) {
                    sceneLight.state().setValue(PadLightState.pulseLight(mBPM.getRaw(), Utils.toNovation(baseColor)));
                } else {
                    sceneLight.setColor(baseColor);
                }
            }
        }
    }

    public SessionMode(TrackBank bank, Transport transport, LaunchpadProMk3Surface surface, ControllerHost host, BooleanValue pulseSessionPads) {
        int[] ids = new int[]{89, 79, 69, 59, 49, 39, 29, 19};
        RangedValue bpm = transport.tempo().modulatedValue();

        // Set up scene buttons
        for(int i = 0; i < 8; i++) {
            Scene scene = bank.sceneBank().getItemAt(i);
            sceneLights[i] = new SessionSceneLight(surface, scene, pulseSessionPads, bpm);
            int finalI = i;
            sceneLaunchActions[i] = host.createAction(() -> {
                scene.launch();
                scene.selectInEditor();
            }, () -> "Press Scene " + finalI);
        }

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
        for(int scene = 0; scene < 8; scene++) {
            padActions[scene] = new HardwareActionBindable[8];
            padLights[scene] = new SessionPadLight[8];
            for(int trk = 0; trk < 8; trk++) {
                Track track = bank.getItemAt(trk);
                ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();
                ClipLauncherSlot slot = slotBank.getItemAt(scene);

                padLights[scene][trk] = new SessionPadLight(surface, slot, track, bpm, this::redraw, scene);
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
            arrowLights[i] = new ArrowPadLight(surface, arrowEnabled[i], this::redraw);
        }
    }

    @Override
    public List<HardwareBinding> onBind(LaunchpadProMk3Surface surface) {
        List<HardwareBinding> bindings = new ArrayList<>();
        for(int i = 0; i < 8; i++) {
            bindings.add(surface.scenes()[i].button().pressedAction().addBinding(sceneLaunchActions[i]));
        }
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                bindings.add(surface.notes()[i][j].button().pressedAction().addBinding(padActions[i][j]));
            }
        }
        LaunchpadProMk3Pad[] arrows = surface.arrows();
        for(int i = 0; i < 4; i++) {
            bindings.add(arrows[i].button().pressedAction().addBinding(arrowActions[i]));
        }
        return bindings;
    }

    @Override
    public void onDraw(LaunchpadProMk3Surface surface) {
        LaunchpadProMk3Pad[] scenes = surface.scenes();
        for(int i = 0; i < scenes.length; i++) {
            sceneLights[i].draw(scenes[i].light());
        }
        LaunchpadProMk3Pad[] arrows = surface.arrows();
        for(int i = 0; i < arrows.length; i++) {
            arrowLights[i].draw(surface.arrows()[i].light());
        }
        LaunchpadProMk3Pad[][] pads = surface.notes();
        for(int i = 0; i < pads.length; i++) {
            for(int j = 0; j < pads[i].length; j++) {
                padLights[i][j].draw(pads[i][j].light());
            }
        }
    }

    @Override
    public void finishedBind(Session session) {
        session.sendSysex("14 00 00");
        session.sendSysex("00 00");
    }
}
