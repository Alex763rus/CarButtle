# Переходим в target и выполняем docker команды
cd target/tmp

# Собираем Docker образ
echo "Building Docker image..."
docker build -t alex163rus/carbuttle:latest .

# Пушим образ в Docker Hub
echo "Pushing Docker image..."
docker push alex163rus/carbuttle:latest

echo "Deployment completed successfully!"
echo ""
read -p "Press Enter to close this window..."