package org.example.my.ai.dynamic;

import org.example.my.ai.CarAI;
import org.example.my.ai.DynamicCarAI;
import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

public class DynamicCarAIImpl implements DynamicCarAI {

    private String aiCode;
    private CarAI loadedAI;
    private String compilationError;
    private final Path tempDir;
    private String customName; // Храним имя отдельно

    public DynamicCarAIImpl() {
        try {
            tempDir = Files.createTempDirectory("car_ai");
            tempDir.toFile().deleteOnExit();
            this.customName = "Dynamic AI (Not Loaded)";
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp directory", e);
        }
    }

    @Override
    public CarAction decideAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
        if (loadedAI != null) {
            try {
                return loadedAI.decideAction(myCar, opponentCar, bullets);
            } catch (Exception e) {
                // Fallback to default behavior if custom AI fails
                return new CarAction(CarAction.ActionType.IDLE);
            }
        }
        return new CarAction(CarAction.ActionType.IDLE);
    }

    @Override
    public String getAIName() {
        if (loadedAI != null) {
            try {
                return loadedAI.getAIName();
            } catch (Exception e) {
                return "Dynamic AI (Error)";
            }
        }
        return customName;
    }

    @Override
    public String getCustomAIName() {
        return customName;
    }

    @Override
    public boolean isLoaded() {
        return loadedAI != null;
    }

    @Override
    public String getCode() {
        return aiCode;
    }

    @Override
    public void setCode(String code) {
        this.aiCode = code;
        this.compilationError = null;
        // Пытаемся извлечь имя из кода
        this.customName = extractAINameFromCode(code);
    }

    @Override
    public boolean compileAndLoad() {
        if (aiCode == null || aiCode.trim().isEmpty()) {
            compilationError = "AI code is empty";
            return false;
        }

        try {
            // Create Java file
            Path javaFile = tempDir.resolve("UserCarAI.java");
            Files.write(javaFile, aiCode.getBytes());

            // Compile
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                compilationError = "Java compiler not available. Make sure you're running with JDK, not JRE.";
                return false;
            }

            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(tempDir.toFile()));

            StringWriter errorWriter = new StringWriter();
            boolean success = compiler.getTask(
                    errorWriter,
                    fileManager,
                    null,
                    null,
                    null,
                    fileManager.getJavaFileObjects(javaFile.toFile())
            ).call();

            fileManager.close();

            if (!success) {
                compilationError = errorWriter.toString();
                loadedAI = null;
                return false;
            }

            // Load class
            URLClassLoader classLoader = new URLClassLoader(new URL[]{tempDir.toUri().toURL()});
            Class<?> aiClass = classLoader.loadClass("UserCarAI");
            loadedAI = (CarAI) aiClass.getDeclaredConstructor().newInstance();

            // Обновляем имя после успешной загрузки
            this.customName = loadedAI.getAIName();
            compilationError = null;
            return true;

        } catch (Exception e) {
            compilationError = "Compilation/Loading failed: " + e.getMessage();
            loadedAI = null;
            return false;
        }
    }

    @Override
    public String getCompilationError() {
        return compilationError;
    }

    /**
     * Пытается извлечь имя AI из кода
     */
    private String extractAINameFromCode(String code) {
        if (code == null) return "Unnamed AI";

        // Ищем возвращаемое значение в getAIName()
        try {
            int nameIndex = code.indexOf("getAIName()");
            if (nameIndex != -1) {
                int returnIndex = code.indexOf("return", nameIndex);
                if (returnIndex != -1) {
                    int quoteStart = code.indexOf("\"", returnIndex);
                    if (quoteStart != -1) {
                        int quoteEnd = code.indexOf("\"", quoteStart + 1);
                        if (quoteEnd != -1) {
                            return code.substring(quoteStart + 1, quoteEnd);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки парсинга
        }

        return "Custom AI";
    }
}