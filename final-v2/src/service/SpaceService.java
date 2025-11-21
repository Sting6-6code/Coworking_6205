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
    // 公共功能（Admin + User 都可使用）
    // =============================================

    /** 获取所有空间（用于绘制总览/用户浏览） */
    public List<Space> getAllSpaces() {
        return index.getAllSpaces();
    }

    /** 获取建筑-楼层结构（用于层级渲染） */
    public Map<String, Map<String, List<Space>>> getBuildingFloorMap() {
        return index.getBuildingFloorIndex();
    }

    /** 获取当前缓存列表（用于保存修改或添加空间） */
    public List<Space> getCachedSpaces() {
        return cachedSpaces;
    }

    public void addSpace(Space s) {
        List<Space> existing = new ArrayList<>();

        // ⚠️ 第一步：读取旧数据
        try (BufferedReader br = Files.newBufferedReader(Paths.get(FILE), StandardCharsets.UTF_8)) {
            br.readLine(); // 跳过表头
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

        // ⚠️ 第二步：加入新空间
        existing.add(s);

        // ⚠️ 第三步：重写文件（不是APPEND）
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

        // ⚠️ 第四步：更新缓存和索引
        cachedSpaces.clear();
        cachedSpaces.addAll(existing);
        index.buildIndex(existing);
    }

    /** 每栋楼的预订数量（Admin 统计用） */
    public Map<String, Long> getBookingCountByBuilding() {
        Map<String, Long> result = new HashMap<>();
        for (Space s : index.getAllSpaces()) {
            long count = BookingDataUtil.getBookingsBySpaceId(s.getSpaceId()).size();
            result.merge(s.getBuilding(), count, Long::sum);
        }
        return result;
    }

    /** 每栋楼的总收入（Admin 统计用） */
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

    /** 保存空间到 CSV（Admin 新增/修改后使用） */
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

    /** 按条件过滤空间（User 端搜索 / 筛选功能） */
    public List<Space> filterSpaces(String building, String floor, String type, int minCapacity, boolean onlyAvailable) {
        return index.filterSpaces(building, floor, type, minCapacity, onlyAvailable);
    }
}
