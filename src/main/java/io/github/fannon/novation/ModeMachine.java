package io.github.fannon.novation;

import com.bitwig.extension.controller.api.HardwareBinding;
import io.github.fannon.novation.internal.Session;
import io.github.fannon.novation.modes.AbstractMode;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeMachine {
    private Map<Mode, AbstractMode> mModes;
    private Mode mMode;
    private AbstractMode mModus;
    private List<HardwareBinding> mBindings;
    private Session mSession;

    public ModeMachine(Session session) {
        mModes = new HashMap<>();
        mBindings = new ArrayList<>();
        mMode = Mode.UNKNOWN;
        mSession = session;
    }

    public Mode mode() { return mMode; }

    public void register(Mode mode, AbstractMode am) {
        am.onInit(this, mode);
        mModes.put(mode, am);
    }

    public void setMode(LaunchpadProMk3Surface surface, Mode mode) {
        for(HardwareBinding binding : mBindings) {
            binding.removeBinding();
        }
        mMode = mode;
        if(!mModes.containsKey(mode)) throw new RuntimeException("Invalid mode state: " + mode);
        surface.clear();
        mModus = mModes.get(mode);
        mBindings = mModus.onBind(surface);
        mModus.finishedBind(mSession);
        redraw(surface);
    }

    public void redraw(LaunchpadProMk3Surface surface) {
        mModus.onDraw(surface);
    }

    public void sendSysex(byte[] message) {
        List<String> responses = mModes.get(mMode).processSysex(message);
        for(String response : responses) {
            mSession.sendSysex(response);
        }
    }
}
