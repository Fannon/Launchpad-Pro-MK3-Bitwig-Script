package io.github.fannon.novation.modes;

import com.bitwig.extension.controller.api.HardwareBinding;
import io.github.fannon.novation.Mode;
import io.github.fannon.novation.ModeMachine;
import io.github.fannon.novation.internal.Session;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMode {
    protected ModeMachine mModeMachine;
    private Mode mTarget;

    public final void onInit(ModeMachine machine, Mode target) {
        mModeMachine = machine;
        mTarget = target;
    }

    protected final void redraw(LaunchpadProMk3Surface surface) {
        if(mModeMachine.mode() == mTarget) {
            mModeMachine.redraw(surface);
        }
    }

    public abstract List<HardwareBinding> onBind(LaunchpadProMk3Surface surface);
    public void onDraw(LaunchpadProMk3Surface surface) {}
    public List<String> processSysex(byte[] sysex) { return new ArrayList<>(); }
    public void finishedBind(Session session) {}
}
