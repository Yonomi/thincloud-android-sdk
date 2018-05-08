package co.yonomi.thincloud.tcsdktestplatform.example.lights.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class State {
    private boolean power;
    private int hue;
    private int saturation;
    private int brightness;
    private int temperature;
}
