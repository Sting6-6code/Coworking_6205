package adt;

import model.Space;
import java.util.List;

public interface SpaceADT {

    List<Space> getAllSpaces();

    List<Space> getBuildings();

    List<String> getFloors(String building);

    List<Space> getSpacesByBuildingFloor(String building, String floor);

    List<Space> filterSpaces(
            String building,
            String floor,
            String type,
            int minCapacity,
            boolean onlyAvailable
    );
}
