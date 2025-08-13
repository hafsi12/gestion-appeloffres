# TerraGIS Project - Business Management System

## Overview
TerraGIS Project is a comprehensive business management system designed for handling tender processes, client management, opportunities, offers, contracts, and deliverables. The system is built using Spring Boot with JPA/Hibernate for data persistence.

## Project Structure

### Package Structure
```
com.terragis.appeloffre.terragis_project.entity
├── User.java                    # User management and authentication
├── Role.java                    # User roles enumeration
├── MaitreOeuvrage.java         # Client/Master Builder entities
├── Contact.java                 # Client contact information
├── Opportunite.java            # Business opportunities
├── EtatOpportunite.java        # Opportunity states
├── EtatOpportuniteEnum.java    # Opportunity status enumeration
├── DocumentOpportunite.java    # Opportunity documents
├── Offre.java                  # Business offers/bids
├── DocumentOffre.java          # Offer documents
├── Tache.java                  # Tasks management
├── Event.java                  # Event management
├── Contrat.java                # Contracts
├── Livrable.java               # Deliverables
├── Facture.java                # Invoices
├── File.java                   # File attachments
├── Status.java                 # Generic status entity
├── StatutValidation.java       # Validation status enumeration
├── StatutPaiement.java         # Payment status enumeration
└── Adjuge.java                 # Bid result enumeration
```

## Entity Relationships

### Core Entities Overview

#### 1. User Management
- **User**: Main user entity implementing Spring Security UserDetails
- **Role**: Enum defining user roles (ADMIN, GESTION_CLIENTS_OPPORTUNITES, GESTION_OFFRES, GESTION_CONTRATS)

#### 2. Client Management
- **MaitreOeuvrage** (Master Builder/Client): Main client entity
- **Contact**: Client contact persons (Many-to-One with MaitreOeuvrage)

#### 3. Business Process Flow
```
MaitreOeuvrage → Opportunite → Offre → Contrat → Livrable
     ↓              ↓           ↓        ↓         ↓
  Contact    EtatOpportunite  Tache   Facture   Files
             DocumentOpp.   DocumentOffre
```

### Detailed Entity Relationships

#### MaitreOeuvrage (Client)
- **Fields**: clientCode, name, webSite, address, country, city, landline, secteur
- **Relationships**:
  - One-to-Many with Contact
  - One-to-Many with Opportunite

#### Opportunite (Opportunity)
- **Fields**: projectName, budget, deadline, description, archived
- **Relationships**:
  - Many-to-One with MaitreOeuvrage (client)
  - One-to-One with EtatOpportunite (state)
  - One-to-Many with DocumentOpportunite
  - One-to-One with Offre (bidirectional)

#### Offre (Offer/Bid)
- **Fields**: budget, detail, sent, adjuge (GAGNEE/PERDUE/EN_ATTENTE)
- **Relationships**:
  - One-to-One with Opportunite
  - One-to-Many with DocumentOffre
  - One-to-Many with Tache (tasks)
  - One-to-One with Contrat
  - Many-to-Many with Event

#### Contrat (Contract)
- **Fields**: startDate, endDate, details, nameClient, statut
- **Digital Signature**: signature (Base64), signerName, dateSignature, signed
- **Relationships**:
  - One-to-One with Offre
  - One-to-Many with Livrable (deliverables)

#### Livrable (Deliverable)
- **Fields**: titre, description, dateLivraison, montant, fichierJoint
- **Status**: StatutValidation, StatutPaiement
- **Relationships**:
  - Many-to-One with Contrat

## Key Features

### 1. User Management & Security
- Spring Security integration with UserDetails
- Role-based access control
- User authentication and authorization

### 2. Client Management (CRM)
- Client information management
- Contact person management
- Client archiving functionality
- Unique client code generation

### 3. Opportunity Management
- Business opportunity tracking
- Opportunity state management (EN_COURS, GO, NO_GO)
- Document attachment support
- Opportunity archiving

### 4. Bid/Offer Management
- Bid creation and management
- Task assignment and tracking
- Document management for offers
- Bid result tracking (Won/Lost/Pending)

### 5. Contract Management
- Contract lifecycle management
- Digital signature support
- Contract status tracking
- Date tracking (creation, sending, signature)

### 6. Deliverable & Invoice Management
- Deliverable tracking with validation status
- Payment status management
- Invoice generation and tracking
- File attachment support

### 7. Event Management
- Event scheduling and tracking
- Multi-offer event associations
- Event history management

## Enumerations

### Role
- `ADMIN`: Full system access
- `GESTION_CLIENTS_OPPORTUNITES`: Client and opportunity management
- `GESTION_OFFRES`: Offer management
- `GESTION_CONTRATS`: Contract management

### StatutValidation
- `EN_ATTENTE`: Pending validation
- `VALIDE`: Validated
- `REFUSE`: Refused

### StatutPaiement
- `NON_PAYE`: Not paid
- `SOLDE`: Balanced/Settled
- `PAYE`: Paid

### Adjuge (Bid Result)
- `GAGNEE`: Won
- `PERDUE`: Lost
- `EN_ATTENTE`: Pending

### EtatOpportuniteEnum
- `EN_COURS`: In progress
- `GO`: Approved to proceed
- `NO_GO`: Rejected

## Technology Stack

- **Framework**: Spring Boot
- **ORM**: JPA/Hibernate
- **Security**: Spring Security
- **Data Validation**: Jakarta Validation
- **JSON Processing**: Jackson (with circular reference handling)
- **Code Generation**: Lombok
- **Database**: JPA-compatible database

## Database Design Patterns

### Circular Reference Prevention
The project uses Jackson annotations to prevent circular references:
- `@JsonIgnore`: Prevents serialization of back-references
- `@JsonBackReference`/`@JsonManagedReference`: Manages bidirectional relationships

### Audit Trail
- `createdAt` and `updatedAt` timestamps in User entity
- `@PreUpdate` hooks for automatic timestamp updates

### Cascade Operations
- Proper cascade configurations for entity relationships
- Orphan removal for dependent entities

## Development Notes

### Entity Relationships Best Practices
1. **Bidirectional OneToOne**: Opportunite ↔ Offre
2. **Cascade Management**: Proper cascade types for data integrity
3. **Fetch Strategies**: Lazy loading for performance optimization
4. **JSON Handling**: Circular reference prevention

### Security Considerations
- Password field marked with `@JsonIgnore`
- User account status tracking (enabled, locked, expired)
- Role-based authorization structure

## Getting Started

### Prerequisites
- Java 17+
- Spring Boot 3.x
- JPA-compatible database (PostgreSQL, MySQL, etc.)
- Maven or Gradle build tool

### Installation
1. Clone the repository
2. Configure database connection in `application.properties`
3. Run database migrations (if applicable)
4. Start the Spring Boot application

### Database Setup
Ensure your database schema supports the entity relationships defined in this project. The entities use JPA annotations for automatic table creation in development environments.

## Contributing

When contributing to this project:
1. Follow the existing entity naming conventions
2. Maintain proper JPA relationship annotations
3. Use appropriate Jackson annotations for JSON serialization
4. Update this README when adding new entities or relationships

## License

[Add your license information here]

---

*This project represents a comprehensive business management system for tender processes, client relationship management, and contract lifecycle management.*