package service;

import datastructure.SpaceIndexDS;
import model.Space;
import util.BookingDataUtil;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SpaceService {

    private static final String FILE = "data/spaces.csv";

    private final SpaceIndexDS index = new SpaceIndexDS();
    
    private final List<Space> cachedSpaces = new ArrayList<>();

    public void loadSpacesFromCSV() {
        List<Space> list = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(FILE), StandardCharsets.UTF_8)) {
            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] p = line.split(",");
                if (p.length < 9) continue;

                Space s = new Space(
                        p[0], p[1], p[2], p[3], p[4],
                        p[5], Integer.parseInt(p[6]),
                        p[7], Double.parseDouble(p[8]),
                        0,0,0,0
                );
                list.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        cachedSpaces.clear();
        cachedSpaces.addAll(list);
        index.buildIndex(list);
    }

    public SpaceIndexDS getIndex() {
        return index;
    }
    
    // =============================================
    // Public Functionality (Available to both Admin and User)
    // =============================================

    /** Get all spaces (for drawing overview / user browsing) */
    public List<Space> getAllSpaces() {
        return index.getAllSpaces();
    }

    /** Get building-floor structure (for hierarchical rendering) */
    public Map<String, Map<String, List<Space>>> getBuildingFloorMap() {
        return index.getBuildingFloorIndex();
    }

    /** Get current cached list (for saving modifications or adding spaces) */
    public List<Space> getCachedSpaces() {
        return cachedSpaces;
    }

    public void addSpace(Space s) {
        List<Space> existing = new ArrayList<>();

        // ⚠️ Step 1: Read old data
        try (BufferedReader br = Files.newBufferedReader(Paths.get(FILE), StandardCharsets.UTF_8)) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split(",");
                if (p.length < 9) continue;

                existing.add(new Space(
                        p[0], p[1], p[2], p[3], p[4],
                        p[5], Integer.parseInt(p[6]),
                        p[7], Double.parseDouble(p[8]),
                        0, 0, 0, 0
                ));
            }
        } catch (IOException e) {
            System.err.println("Failed to read existing spaces: " + e.getMessage());
        }

        // ⚠️ Step 2: Add new space
        existing.add(s);

        // ⚠️ Step 3: Rewrite file (not APPEND)
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(FILE), StandardCharsets.UTF_8)) {
            bw.write("id,name,floor,spaceId,type,building,seats,status,creditsPerHour\n");
            for (Space sp : existing) {
                bw.write(String.join(",",
                        sp.getId(), sp.getName(), sp.getFloor(), sp.getSpaceId(),
                        sp.getType(), sp.getBuilding(),
                        String.valueOf(sp.getCapacity()),
                        sp.getStatus(),
                        String.valueOf(sp.getCreditsPerHour())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing spaces.csv: " + e.getMessage());
        }

        // ⚠️ Step 4: Update cache and index
        cachedSpaces.clear();
        cachedSpaces.addAll(existing);
        index.buildIndex(existing);
    }

    /** Booking count per building (For Admin Statistics) */
    public Map<String, Long> getBookingCountByBuilding() {
        Map<String, Long> result = new HashMap<>();
        for (Space s : index.getAllSpaces()) {
            long count = BookingDataUtil.getBookingsBySpaceId(s.getSpaceId()).size();
            result.merge(s.getBuilding(), count, Long::sum);
        }
        return result;
    }

    /** Total revenue per building (For Admin Statistics) */
    public Map<String, Double> getRevenueByBuilding() {
        Map<String, Double> result = new HashMap<>();
        for (Space s : index.getAllSpaces()) {
            double revenue = BookingDataUtil.getBookingsBySpaceId(s.getSpaceId()).stream()
                    .mapToDouble(b ->
                            s.getCreditsPerHour() *
                                    (b.getEndTime().toSecondOfDay() - b.getStartTime().toSecondOfDay()) / 3600.0
                    ).sum();
            result.merge(s.getBuilding(), revenue, Double::sum);
        }
        return result;
    }

    /** Save spaces to CSV (Used after Admin adds/modifies) */
    public void saveSpacesToCSV(List<Space> spaces) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(FILE), StandardCharsets.UTF_8)) {
            bw.write("id,name,floor,spaceId,type,building,seats,status,creditsPerHour\n");
            for (Space sp : spaces) {
                bw.write(String.join(",",
                        sp.getId(), sp.getName(), sp.getFloor(), sp.getSpaceId(),
                        sp.getType(), sp.getBuilding(),
                        String.valueOf(sp.getCapacity()),
                        sp.getStatus(),
                        String.valueOf(sp.getCreditsPerHour())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving spaces.csv: " + e.getMessage());
        }
    }

    /** Filter spaces by condition (User search / filter function) */
    public List<Space> filterSpaces(String building, String floor, String type, int minCapacity, boolean onlyAvailable) {
        return index.filterSpaces(building, floor, type, minCapacity, onlyAvailable);
    }
}
