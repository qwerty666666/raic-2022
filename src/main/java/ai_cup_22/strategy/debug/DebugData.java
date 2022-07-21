package ai_cup_22.strategy.debug;

import ai_cup_22.DebugInterface;
import ai_cup_22.strategy.debug.layers.DefaultLayer;
import ai_cup_22.strategy.debug.layers.DrawLayer;
import ai_cup_22.strategy.debug.layers.LootsLayer;
import ai_cup_22.strategy.debug.layers.MouseLayer;
import ai_cup_22.strategy.debug.layers.ObstaclesLayer;
import ai_cup_22.strategy.debug.layers.PositionsLayer;
import ai_cup_22.strategy.debug.layers.UnitsLayer;
import ai_cup_22.strategy.geometry.Position;
import ai_cup_22.strategy.utils.CollectionUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DebugData {
    public static final boolean isEnabled = "true".equals(System.getenv().get("debug"));
    private static DebugData instance;

    private Optional<Position> cursorPosition = Optional.empty();
    private Optional<Position> clickPosition = Optional.empty();

    private final ObstaclesLayer obstaclesLayer = new ObstaclesLayer();
    private final PositionsLayer positionsLayer = new PositionsLayer();
    private final DefaultLayer defaultLayer = new DefaultLayer();
    private final UnitsLayer unitsLayer = new UnitsLayer();
    private final LootsLayer lootsLayer = new LootsLayer();
    private final MouseLayer mouseLayer = new MouseLayer();

    private final Map<String, DrawLayer> layers = new HashMap<>() {{
        put("D", defaultLayer);
        put("L", lootsLayer);
        put("M", mouseLayer);
        put("I", positionsLayer);
        put("O", obstaclesLayer);
        put("U", unitsLayer);
    }};
    private Set<String> turnedOnLayers = new HashSet<>() {{
        add("D");
        add("M");
        add("U");
    }};

    public void setCursorPosition(Position cursorPosition) {
        this.cursorPosition = Optional.ofNullable(cursorPosition);
    }

    public Optional<Position> getCursorPosition() {
        return cursorPosition;
    }

    public Optional<Position> getClickPosition() {
        return clickPosition;
    }

    public DebugData setClickPosition(Position clickPosition) {
        this.clickPosition = Optional.ofNullable(clickPosition);
        return this;
    }

    public LootsLayer getLootsLayer() {
        return lootsLayer;
    }

    public ObstaclesLayer getObstaclesLayer() {
        return obstaclesLayer;
    }

    public PositionsLayer getPositionsLayer() {
        return positionsLayer;
    }

    public UnitsLayer getUnitsLayer() {
        return unitsLayer;
    }

    public DefaultLayer getDefaultLayer() {
        return defaultLayer;
    }

    public MouseLayer getMouseLayer() {
        return mouseLayer;
    }

    public void draw(DebugInterface debugInterface) {
        Set<String> pressedKeys = new HashSet<>(Arrays.asList(debugInterface.getState().getPressedKeys()));
        Set<String> pressedLayerKeys = pressedKeys.stream()
                .filter(layers::containsKey)
                .collect(Collectors.toSet());

//        if (pressedLayerKeys.isEmpty()) {
//            return;
//        }

//        if (pressedKeys.contains("LAlt")) {
//            Set<String> layersToTurnOff = pressedKeys.contains("LAlt") ? pressedLayerKeys : Collections.emptySet();
//
//            turnedOnLayers = CollectionUtils.diff(turnedOnLayers, layersToTurnOff);
//
//            debugInterface.clear();
//            turnedOnLayers.forEach(key -> layers.get(key).show(debugInterface));
//        } else {
//            Set<String> layersToTurnOn = CollectionUtils.diff(pressedLayerKeys, turnedOnLayers);
//
//            layersToTurnOn.forEach(key -> layers.get(key).show(debugInterface));
//
//            turnedOnLayers = CollectionUtils.union(turnedOnLayers, layersToTurnOn);
//        }

        if (pressedKeys.contains("LAlt")) {
            Set<String> layersToTurnOff = pressedLayerKeys;
            turnedOnLayers = CollectionUtils.diff(turnedOnLayers, layersToTurnOff);
        } else {
            Set<String> layersToTurnOn = CollectionUtils.diff(pressedLayerKeys, turnedOnLayers);
            turnedOnLayers = CollectionUtils.union(turnedOnLayers, layersToTurnOn);
        }


        if (pressedKeys.contains("Num1")) {
            unitsLayer.updateUnit(0);
        } else if (pressedKeys.contains("Num2")) {
            unitsLayer.updateUnit(1);
        } else if (pressedKeys.contains("Num3")) {
            unitsLayer.updateUnit(2);
        } else if (pressedKeys.contains("Num4")) {
            unitsLayer.updateUnit(null);
        }


        debugInterface.clear();
        turnedOnLayers.stream()
                .filter(key -> !key.equals("D"))
                .map(layers::get)
                .forEach(layer -> layer.show(debugInterface));
    }

    public static DebugData getInstance() {
        if (instance == null) {
            instance = new DebugData();
        }
        return instance;
    }
}
