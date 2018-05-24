package com.datamining.hciris;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Model {
    @SerializedName("sepalLength")
    @Expose
    private Double sepalLength;
    @SerializedName("sepalWidth")
    @Expose
    private Double sepalWidth;
    @SerializedName("petalLength")
    @Expose
    private Double petalLength;
    @SerializedName("petalWidth")
    @Expose
    private Double petalWidth;
    @SerializedName("species")
    @Expose
    private String species;
    private List<Integer> node ;

    public Model(Double sepalLength, Double sepalWidth, Double petalLength, Double petalWidth) {
        this.sepalLength = sepalLength;
        this.sepalWidth = sepalWidth;
        this.petalLength = petalLength;
        this.petalWidth = petalWidth;
    }

    public Model(Double sepalLength, Double sepalWidth, Double petalLength, Double petalWidth, List<Integer> node) {
        this.sepalLength = sepalLength;
        this.sepalWidth = sepalWidth;
        this.petalLength = petalLength;
        this.petalWidth = petalWidth;
        this.node = node;
    }

    public Model(Double sepalLength, Double sepalWidth, Double petalLength, Double petalWidth, String species) {
        this.sepalLength = sepalLength;
        this.sepalWidth = sepalWidth;
        this.petalLength = petalLength;
        this.petalWidth = petalWidth;
        this.species = species;
    }

    public List<Integer> getNode() {
        return node;
    }

    public void setNode(List<Integer> node) {
        this.node = node;
    }

    public Double getSepalLength() {
        return sepalLength;
    }

    public void setSepalLength(Double sepalLength) {
        this.sepalLength = sepalLength;
    }

    public Double getSepalWidth() {
        return sepalWidth;
    }

    public void setSepalWidth(Double sepalWidth) {
        this.sepalWidth = sepalWidth;
    }

    public Double getPetalLength() {
        return petalLength;
    }

    public void setPetalLength(Double petalLength) {
        this.petalLength = petalLength;
    }

    public Double getPetalWidth() {
        return petalWidth;
    }

    public void setPetalWidth(Double petalWidth) {
        this.petalWidth = petalWidth;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }
}
