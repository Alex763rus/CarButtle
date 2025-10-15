package org.example.my.ai;

public interface DynamicCarAI extends CarAI {
    String getCode();

    void setCode(String code);

    boolean compileAndLoad();

    String getCompilationError();

    String getCustomAIName(); // Новый метод для получения имени

    boolean isLoaded(); // Проверка, загружен ли AI
}