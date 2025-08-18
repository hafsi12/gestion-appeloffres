#!/bin/bash

# Script de déploiement automatique pour Terragis sur Railway
# Usage: ./deploy.sh

set -e  # Arrêter le script en cas d'erreur

echo "🚀 Déploiement Terragis sur Railway"
echo "=================================="

# Vérifier les prérequis
echo "📋 Vérification des prérequis..."

# Vérifier Git
if ! command -v git &> /dev/null; then
    echo "❌ Git n'est pas installé. Veuillez l'installer d'abord."
    exit 1
fi

# Vérifier Railway CLI (optionnel)
if command -v railway &> /dev/null; then
    echo "✅ Railway CLI détecté"
    RAILWAY_CLI=true
else
    echo "⚠️  Railway CLI non détecté. Utilisez le déploiement manuel via GitHub."
    RAILWAY_CLI=false
fi

# Initialiser Git si nécessaire
if [ ! -d ".git" ]; then
    echo "🔧 Initialisation du repository Git..."
    git init
    git add .
    git commit -m "Initial commit - Terragis Spring Boot application ready for Railway deployment"
    
    echo "📝 Ajoutez votre repository GitHub distant :"
    echo "   git remote add origin https://github.com/VOTRE-USERNAME/terragis-app.git"
    echo "   git branch -M main"
    echo "   git push -u origin main"
else
    echo "📦 Mise à jour du repository Git..."
    git add .
    git commit -m "Update Terragis application - $(date)" || echo "Aucun changement à committer"
fi

# Déploiement avec Railway CLI si disponible
if [ "$RAILWAY_CLI" = true ]; then
    echo "🚂 Déploiement avec Railway CLI..."
    
    # Se connecter à Railway
    echo "🔐 Connexion à Railway..."
    railway login
    
    # Vérifier si le projet existe
    if railway status &> /dev/null; then
        echo "📊 Mise à jour du projet existant..."
        railway up
    else
        echo "🆕 Création d'un nouveau projet Railway..."
        railway init
        
        # Ajouter PostgreSQL
        echo "🗄️ Ajout de PostgreSQL..."
        railway add --database postgresql
        
        # Déployer
        echo "🚀 Déploiement initial..."
        railway up
    fi
    
    # Afficher l'URL
    echo "🌐 Récupération de l'URL de déploiement..."
    railway domain
else
    echo "📖 Déploiement manuel nécessaire :"
    echo ""
    echo "1. Poussez votre code sur GitHub"
    echo "2. Allez sur https://railway.app"
    echo "3. Cliquez sur 'New Project'"
    echo "4. Sélectionnez 'Deploy from GitHub repo'"
    echo "5. Choisissez votre repository terragis-app"
    echo "6. Ajoutez une base PostgreSQL dans Railway"
    echo "7. Configurez les variables d'environnement"
fi

echo ""
echo "✅ Configuration terminée !"
echo ""
echo "📋 Variables d'environnement à configurer dans Railway :"
echo "   DATABASE_URL=postgresql://user:pass@host:port/db"
echo "   JWT_SECRET=VotreCleSuperSecurePourLaProduction256Bits!"
echo "   JWT_EXPIRATION=86400000"
echo "   SPRING_PROFILES_ACTIVE=prod"
echo ""
echo "🔗 Endpoints de votre API :"
echo "   Health Check: https://votre-app.railway.app/actuator/health"
echo "   Login: https://votre-app.railway.app/api/auth/login"
echo "   API Docs: https://votre-app.railway.app/swagger-ui.html (si activé)"
echo ""
echo "📚 Consultez README-deployment.md pour plus de détails"