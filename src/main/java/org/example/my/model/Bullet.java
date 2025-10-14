package org.example.my.model;

import lombok.Data;

@Data
public class Bullet {
    private String id;
    private Position position;
    private double speed;
    private double angle;
    private String ownerId; // id машинки, которая выстрелила
}