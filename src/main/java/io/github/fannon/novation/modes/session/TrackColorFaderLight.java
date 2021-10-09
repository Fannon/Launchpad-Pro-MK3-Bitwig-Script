package io.github.fannon.novation.modes.session;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.*;
import io.github.fannon.novation.surface.LaunchpadProMk3Surface;
import io.github.fannon.novation.surface.state.FaderLightState;

import java.util.function.Consumer;

public class TrackColorFaderLight {
    private BooleanValue mValid;
    private ColorValue mColor;

    public TrackColorFaderLight(LaunchpadProMk3Surface surface, Track track, Consumer<LaunchpadProMk3Surface> redraw) {
        mValid = track.exists();
        mColor = track.color();

        mValid.addValueObserver(v -> redraw.accept(surface));
        mColor.addValueObserver((r, g, b) -> redraw.accept(surface));
    }

    public TrackColorFaderLight(LaunchpadProMk3Surface surface, Send send, Consumer<LaunchpadProMk3Surface> redraw) {
        mValid = send.exists();
        mColor = send.sendChannelColor();

        mValid.addValueObserver(v -> redraw.accept(surface));
        mColor.addValueObserver((r, g, b) -> redraw.accept(surface));
    }

    public void draw(MultiStateHardwareLight faderLight) {
        if(mValid.get()) {
            Color color = mColor.get();

            if(color.getRed() + color.getBlue() + color.getGreen() == 0.0) {
                faderLight.state().setValue(new FaderLightState((byte)1));
            } else {
                faderLight.setColor(mColor.get());
            }
        } else {
            faderLight.setColor(Color.nullColor());
        }
    }
}
