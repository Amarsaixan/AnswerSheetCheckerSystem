package com.amarsaikhan.ascs.answersheetcheckersystem;

public class Kontours {
    public double area;
    int areaIdx = 0;
    Kontours(){
        
    }
    Kontours(double area, int areaid){
        this.area = area;
        this.areaIdx = areaid;
    }
    public double getarea() {
        return area;
    }

    public void setarea(double area) {
        this.area = area;
    }


    public int getareaIdx() {
        return areaIdx;
    }

    public void setareaIdx(int areaIdx) {
        this.areaIdx = areaIdx;
    }

}
