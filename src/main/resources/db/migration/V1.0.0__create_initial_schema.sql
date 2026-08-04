CREATE TABLE families (
  id BINARY(16) NOT NULL,
  name VARCHAR(100) NOT NULL,

  CONSTRAINT pk_families PRIMARY KEY (id)
);

CREATE TABLE users (
   id BINARY(16) NOT NULL,
   name VARCHAR(100) NOT NULL,
   email VARCHAR(150) NOT NULL,
   password_hash VARCHAR(255) NOT NULL,
   role VARCHAR(30) NOT NULL,
   family_id BINARY(16),

   CONSTRAINT pk_users PRIMARY KEY (id),
   CONSTRAINT uk_users_email UNIQUE (email),
   CONSTRAINT fk_users_family
       FOREIGN KEY (family_id)
           REFERENCES families (id)
);

CREATE INDEX idx_users_family
    ON users (family_id);

CREATE TABLE dependents (
    id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    birth_date DATE NOT NULL,
    family_id BINARY(16) NOT NULL,

    CONSTRAINT pk_dependents PRIMARY KEY (id),
    CONSTRAINT fk_dependents_family
        FOREIGN KEY (family_id)
            REFERENCES families (id)
);

CREATE INDEX idx_dependents_family
    ON dependents (family_id);

CREATE TABLE specialties (
     id BINARY(16) NOT NULL,
     name VARCHAR(100) NOT NULL,
     family_id BINARY(16) NOT NULL,

     CONSTRAINT pk_specialties PRIMARY KEY (id),
     CONSTRAINT uk_specialties_family_name
         UNIQUE (family_id, name),
     CONSTRAINT fk_specialties_family
         FOREIGN KEY (family_id)
             REFERENCES families (id)
);

CREATE INDEX idx_specialties_family
    ON specialties (family_id);

CREATE TABLE professionals (
   id BINARY(16) NOT NULL,
   name VARCHAR(100) NOT NULL,
   pix_key VARCHAR(150) NOT NULL,
   specialty_id BINARY(16) NOT NULL,
   family_id BINARY(16) NOT NULL,

   CONSTRAINT pk_professionals PRIMARY KEY (id),
   CONSTRAINT fk_professionals_specialty
       FOREIGN KEY (specialty_id)
           REFERENCES specialties (id),
   CONSTRAINT fk_professionals_family
       FOREIGN KEY (family_id)
           REFERENCES families (id)
);

CREATE INDEX idx_professionals_specialty
    ON professionals (specialty_id);

CREATE INDEX idx_professionals_family
    ON professionals (family_id);

CREATE TABLE therapies (
   id BINARY(16) NOT NULL,
   dependent_id BINARY(16) NOT NULL,
   professional_id BINARY(16) NOT NULL,
   default_session_value DECIMAL(10, 2) NOT NULL,
   start_date DATE NOT NULL,
   end_date DATE,
   active BOOLEAN NOT NULL DEFAULT TRUE,
   description VARCHAR(500),

   CONSTRAINT pk_therapies PRIMARY KEY (id),
   CONSTRAINT fk_therapies_dependent
       FOREIGN KEY (dependent_id)
           REFERENCES dependents (id),
   CONSTRAINT fk_therapies_professional
       FOREIGN KEY (professional_id)
           REFERENCES professionals (id)
);

CREATE INDEX idx_therapies_dependent
    ON therapies (dependent_id);

CREATE INDEX idx_therapies_professional
    ON therapies (professional_id);

CREATE TABLE reimbursements (
    id BINARY(16) NOT NULL,
    therapy_id BINARY(16) NOT NULL,
    reference_month DATE NOT NULL,
    sessions_quantity INT NOT NULL,
    session_value DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(500),

    CONSTRAINT pk_reimbursements PRIMARY KEY (id),
    CONSTRAINT uk_reimbursements_therapy_month
        UNIQUE (therapy_id, reference_month),
    CONSTRAINT fk_reimbursements_therapy
        FOREIGN KEY (therapy_id)
            REFERENCES therapies (id)
);

CREATE INDEX idx_reimbursements_reference_month
    ON reimbursements (reference_month);

CREATE TABLE solicitations (
   id BINARY(16) NOT NULL,
   reimbursement_id BINARY(16) NOT NULL,
   attempt_number INT NOT NULL,
   status VARCHAR(30) NOT NULL,
   protocol_number VARCHAR(100),
   request_date DATE,
   reimbursement_date DATE,
   amount_received DECIMAL(10, 2),
   note VARCHAR(500),

   CONSTRAINT pk_solicitations PRIMARY KEY (id),
   CONSTRAINT uk_solicitations_reimbursement_attempt
       UNIQUE (reimbursement_id, attempt_number),
   CONSTRAINT uk_solicitations_reimbursement_protocol
       UNIQUE (reimbursement_id, protocol_number),
   CONSTRAINT fk_solicitations_reimbursement
       FOREIGN KEY (reimbursement_id)
           REFERENCES reimbursements (id)
);

CREATE INDEX idx_solicitations_status
    ON solicitations (status);
