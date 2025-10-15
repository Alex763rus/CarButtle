package org.example.my.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Position {
    private double x;
    private double y;
    private double angle; // угол поворота в градусах
}