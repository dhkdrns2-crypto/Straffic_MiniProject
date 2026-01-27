package com.example.straffic.mobility.dto;

public class RouteRequest {
    private String startX;
    private String startY;
    private String endX;
    private String endY;
    private String startName;
    private String endName;
    private String searchType;

    public RouteRequest() {}

    public String getStartX() { return startX; }
    public void setStartX(String startX) { this.startX = startX; }

    public String getStartY() { return startY; }
    public void setStartY(String startY) { this.startY = startY; }

    public String getEndX() { return endX; }
    public void setEndX(String endX) { this.endX = endX; }

    public String getEndY() { return endY; }
    public void setEndY(String endY) { this.endY = endY; }

    public String getStartName() { return startName; }
    public void setStartName(String startName) { this.startName = startName; }

    public String getEndName() { return endName; }
    public void setEndName(String endName) { this.endName = endName; }

    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }

    @Override
    public String toString() {
        return "RouteRequest{" +
                "startX='" + startX + '\'' +
                ", startY='" + startY + '\'' +
                ", endX='" + endX + '\'' +
                ", endY='" + endY + '\'' +
                ", startName='" + startName + '\'' +
                ", endName='" + endName + '\'' +
                ", searchType='" + searchType + '\'' +
                '}';
    }
}
