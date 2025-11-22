package datastructure;

import adt.SpaceADT;
import model.Space;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SpaceIndexDS
 *
 * Data structure layer, responsible for all spaces:
 *   - building/floor structured index
 *   - Type index
 *   - Status index
 *   - Efficient filtering algorithm
 */
public class SpaceIndexDS implements SpaceADT {

    private final List<Space> allSpaces = new ArrayList<>();

    // building → floor → rooms
    private final Map<String, Map<String, List<Space>>> buildingFloorIndex = new HashMap<>();

    @Override
    public List<Space> getAllSpaces() {
        return allSpaces;
    }

    /** Call CSV loader, then use this method to build index */
    public void buildIndex(List<Space> spaces) {
        allSpaces.clear();
        allSpaces.addAll(spaces);

        buildingFloorIndex.clear();

        for (Space s : spaces) {
            buildingFloorIndex
                    .computeIfAbsent(s.getBuilding(), b -> new HashMap<>())
                    .computeIfAbsent(s.getFloor(), f -> new ArrayList<>())
                    .add(s);
        }
    }

    @Override
    public List<Space> getBuildings() {
        return allSpaces;
    }

    @Override
    public List<String> getFloors(String building) {
        return new ArrayList<>(buildingFloorIndex
                .getOrDefault(building, Map.of())
                .keySet());
    }

    @Override
    public List<Space> getSpacesByBuildingFloor(String building, String floor) {
        return buildingFloorIndex
                .getOrDefault(building, Map.of())
                .getOrDefault(floor, List.of());
    }

    @Override
    public List<Space> filterSpaces(String building, String floor,
                                    String type, int minCap, boolean onlyAvail) {

        // 1) Filter by building + floor first
        List<Space> list = getSpacesByBuildingFloor(building, floor);

        return list.stream()
                .filter(s ->
                        (type == null || type.equals("All") ||
                                s.getType().equalsIgnoreCase(type))
                                && s.getCapacity() >= minCap
                                && (!onlyAvail || s.isAvailable())
                )
                .collect(Collectors.toList());
    }
    public Map<String, Map<String, List<Space>>> getBuildingFloorIndex() {
        return buildingFloorIndex;
    }
}
