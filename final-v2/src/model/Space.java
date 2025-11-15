package model;

public class Space {
    private String id;              // id
    private String name;            // event_001
    private String floor;           // floor
    private String spaceId;         // A-01-01
    private String type;            // event / office / desk / conference / etc.
    private String building;        // A / B / C / D / E
    private int capacity;              
    private String status;          // available / maintenance / etc.
    private double creditsPerHour;  
    private double x, y, w, h;      // coordinates

    // ===== 无参构造 =====
    public Space() {}

    // ===== 全参构造（适用于CSV完整字段） =====
    public Space(String id, String name, String floor, String spaceId, String type, String building,
                 int capacity, String status, double creditsPerHour,
                 double x, double y, double w, double h) {
        this.id = id;
        this.name = name;
        this.floor = floor;
        this.spaceId = spaceId;
        this.type = type;
        this.building = building;
        this.capacity = capacity;
        this.status = status;
        this.creditsPerHour = creditsPerHour;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // ===== 重载构造（不包含坐标，仅用于控制逻辑，不参与可视化） =====
    public Space(String id, String name, String floor, String spaceId, String type, String building,
                 int capacity, String status, double creditsPerHour) {
        this(id, name, floor, spaceId, type, building, capacity, status, creditsPerHour, 0, 0, 0, 0);
    }

    // ===== Getter & Setter  =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCreditsPerHour() {
        return creditsPerHour;
    }

    public void setCreditsPerHour(double creditsPerHour) {
        this.creditsPerHour = creditsPerHour;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }
    
	public boolean isAvailable() {
		return "available".equalsIgnoreCase(status);
	}
}
