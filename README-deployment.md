# Guide de Déploiement Terragis sur Railway

## 📋 Prérequis

1. Compte Railway (gratuit) : https://railway.app
2. Git installé localement
3. Compte GitHub (optionnel mais recommandé)

## 🚀 Étapes de Déploiement

### 1. Préparer le Repository Git

```bash
# Initialiser Git dans le dossier du projet
cd terragis-app
git init
git add .
git commit -m "Initial commit - Terragis application"

# Créer un repository sur GitHub et le pousser
git remote add origin https://github.com/VOTRE-USERNAME/terragis-app.git
git branch -M main
git push -u origin main
```

### 2. Déploiement sur Railway

#### Option A: Via GitHub (Recommandé)

1. Connectez-vous à [Railway](https://railway.app)
2. Cliquez sur "New Project"
3. Sélectionnez "Deploy from GitHub repo"
4. Choisissez votre repository `terragis-app`
5. Railway détectera automatiquement le Dockerfile

#### Option B: Via Railway CLI

```bash
# Installer Railway CLI
npm install -g @railway/cli

# Se connecter à Railway
railway login

# Initialiser le projet
railway init

# Déployer
railway up
```

### 3. Configuration de la Base de Données

1. Dans votre projet Railway, cliquez sur "Add Service"
2. Sélectionnez "Database" > "PostgreSQL"
3. Railway créera automatiquement une base PostgreSQL

### 4. Variables d'Environnement

Dans l'onglet "Variables" de votre service Railway, ajoutez :

```
# Base de données (automatiquement configurées par Railway)
DATABASE_URL=postgresql://user:pass@host:port/db
DB_USERNAME=your-db-user
DB_PASSWORD=your-db-password

# JWT Configuration
JWT_SECRET=VotreCleSuperSecurePourLaProduction256Bits!
JWT_EXPIRATION=86400000

# Port (Railway le configure automatiquement)
PORT=8080

# Profil Spring
SPRING_PROFILES_ACTIVE=prod

# CORS (remplacez par votre domaine)
CORS_ORIGINS=https://votre-app.railway.app,https://votre-frontend.com
```

### 5. Domaine Personnalisé (Optionnel)

1. Dans l'onglet "Settings" de votre service
2. Cliquez sur "Domains"
3. Ajoutez votre domaine personnalisé

## 🔧 Structure du Projet

```
terragis-app/
├── src/                          # Code source Java
├── Dockerfile                    # Configuration Docker
├── docker-compose.yml           # Pour développement local
├── railway.json                 # Configuration Railway
├── application-prod.properties  # Config production
├── pom.xml                     # Dépendances Maven
└── README-deployment.md        # Ce guide
```

## 🧪 Tests Locaux avec Docker

```bash
# Build et test local
docker-compose up --build

# L'application sera accessible sur http://localhost:8080
```

## 📊 Monitoring et Logs

- **Logs Railway** : Consultables directement dans l'interface Railway
- **Health Check** : `https://votre-app.railway.app/actuator/health`
- **Métriques** : Disponibles dans l'onglet "Metrics" de Railway

## 🚨 Dépannage

### Problème de Connexion Base de Données
- Vérifiez que les variables `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` sont correctes
- Assurez-vous que le service PostgreSQL est démarré

### Problème de Mémoire
- Railway offre 512MB en gratuit, optimisé avec `-XX:MaxRAMPercentage=70.0`
- Surveillez l'usage dans l'onglet "Metrics"

### Problème de Démarrage
- Consultez les logs dans l'onglet "Deploy Logs"
- Vérifiez que le port `PORT` est bien configuré

## 💰 Coûts Railway

- **Plan Gratuit** : 500 heures/mois, suffisant pour le développement
- **Pro Plan** : $5/mois, pour la production avec usage élevé

## 🔐 Sécurité

- Les variables d'environnement sont chiffrées par Railway
- JWT secret généré automatiquement sécurisé
- HTTPS activé par défaut sur Railway
- Cookies sécurisés configurés pour la production

## 📞 Support

Pour toute question sur ce déploiement, consultez :
- [Documentation Railway](https://docs.railway.app)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)