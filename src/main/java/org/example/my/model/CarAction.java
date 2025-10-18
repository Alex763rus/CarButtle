package org.example.my.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarAction {
    /**
     * Типы действий, которые может выполнять танк
     * Используется в классе CarAction для определения поведения AI
     */
    public enum ActionType {
        /**
         * Движение вперед
         * Влияет на скорость танка в направлении текущего угла поворота
         * Параметр power: от 0.0 (минимальная скорость) до 1.0 (максимальная скорость)
         */
        MOVE_FORWARD,

        /**
         * Движение назад
         * Танк двигается в противоположном направлении от текущего угла
         * Параметр power: от 0.0 (минимальная скорость) до 1.0 (максимальная скорость назад)
         * Обычно ограничено 50% от максимальной скорости вперед
         */
        MOVE_BACKWARD,

        /**
         * Поворот против часовой стрелки (влево)
         * Изменяет угол поворота танка
         * Параметр power: от 0.0 (медленный поворот) до 1.0 (быстрый поворот)
         */
        TURN_LEFT,

        /**
         * Поворот по часовой стрелке (вправо)
         * Изменяет угол поворота танка
         * Параметр power: от 0.0 (медленный поворот) до 1.0 (быстрый поворот)
         */
        TURN_RIGHT,

        /**
         * Произвести выстрел
         * Создает пулю в направлении текущего угла поворота танка
         * Не требует параметра power (игнорируется если передан)
         * Учитывает время перезарядки (cooldown) танка
         */
        SHOOT,

        /**
         * Бездействие
         * Танк продолжает движение по инерции, но не выполняет активных действий
         * Не требует параметра power (игнорируется если передан)
         * Используется когда AI решает ничего не делать
         */
        IDLE
    }

    private ActionType type;

    @Builder.Default
    private double power = 1.0; // от 0.0 до 1.0

    public CarAction(ActionType type) {
        this.type = type;
        this.power = 1.0;
    }

    public boolean requiresPower() {
        return type == ActionType.MOVE_FORWARD ||
                type == ActionType.MOVE_BACKWARD ||
                type == ActionType.TURN_LEFT ||
                type == ActionType.TURN_RIGHT;
    }
}