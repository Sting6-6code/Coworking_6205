package datastructure;

import adt.SpaceADT;
import model.Space;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SpaceIndexDS
 *
 * 数据结构层，负责所有空间的：
 *   - building/floor 结构化索引
 *   - 类型索引
 *   - 状态索引
 *   - 高效过滤算法
 */
public class SpaceIndexDS implements SpaceADT {

    private final List<Space> allSpaces = new ArrayList<>();

    // building → floor → rooms
    private final Map<String, Map<String, List<Space>>> buildingFloorIndex = new HashMap<>();

    @Override
    public List<Space> getAllSpaces() {
        return allSpaces;
    }

    /** 调用 CSV loader 后，用此方法建立索引 */
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

        // 1）先按 building + floor 过滤
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
