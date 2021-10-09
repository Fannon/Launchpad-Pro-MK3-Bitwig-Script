package io.github.fannon.novation.modes.session;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.BooleanValue;
import com.bitwig.extension.controller.api.MultiStateHardwareLight;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;
import io.github.fannon.novation.surface.state.PadLightState;

import java.util.function.Consumer;

public class ArrowPadLight {
    private BooleanValue mIsValid;
    private int mColor;
    public ArrowPadLight(LaunchpadProMk3Surface surface, BooleanValue isValid, int color, Consumer<LaunchpadProMk3Surface> redraw) {
        mIsValid = isValid;
        mColor = color;

        mIsValid.addValueObserver(v -> redraw.accept(surface));
    }

    public ArrowPadLight(LaunchpadProMk3Surface surface, BooleanValue isValid, Consumer<LaunchpadProMk3Surface> redraw) {
        mIsValid = isValid;
        mColor = 84;

        mIsValid.addValueObserver(v -> redraw.accept(surface));
    }

    public void draw(MultiStateHardwareLight arrowLight) {
        if(mIsValid.get()) {
            arrowLight.state().setValue(PadLightState.solidLight(mColor));
        } else {
            arrowLight.setColor(Color.nullColor());
        }
    }
}
