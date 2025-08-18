# Multi-stage build pour optimiser la taille de l'image
FROM maven:3.8.6-openjdk-17-slim AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .

# Télécharger les dépendances (mise en cache des layers Docker)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Construire l'application
RUN mvn clean package -DskipTests

# Image de production
FROM openjdk:17-jre-slim

# Installer les packages nécessaires pour PostgreSQL et les fonts (pour PDF)
RUN apt-get update && apt-get install -y \
    postgresql-client \
    fontconfig \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# Créer un utilisateur non-root pour la sécurité
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Définir le répertoire de travail
WORKDIR /app

# Copier l'artifact depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Créer les dossiers nécessaires
RUN mkdir -p /app/uploads /app/files && \
    chown -R spring:spring /app

# Changer vers l'utilisateur non-root
USER spring

# Exposer le port
EXPOSE 8080

# Définir les variables d'environnement par défaut
ENV SPRING_PROFILES_ACTIVE=prod

# Commande de démarrage avec optimisations JVM
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=70.0", \
    "-XX:+UseG1GC", \
    "-XX:+UnlockExperimentalVMOptions", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]