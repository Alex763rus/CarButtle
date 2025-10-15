package org.example.my.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    @Builder.Default
    private double x = 0;

    @Builder.Default
    private double y = 0;

    @Builder.Default
    private double angle = 0; // в градусах

    public double distanceTo(Position other) {
        if (other == null) return Double.MAX_VALUE;
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Position copy() {
        return new Position(x, y, angle);
    }
}