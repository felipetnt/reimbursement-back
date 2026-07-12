CREATE TABLE family (
    id BINARY(16) PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE users (
   id BINARY(16) PRIMARY KEY NOT NULL,
   name VARCHAR(100) NOT NULL,
   email VARCHAR(150) UNIQUE NOT NULL,
   password VARCHAR(255) NOT NULL,
   role VARCHAR(20) NOT NULL,

   family_id BINARY(16) NOT NULL,
   CONSTRAINT uk_user_email
       UNIQUE (email),
   CONSTRAINT fk_user_family
       FOREIGN KEY (family_id)
           REFERENCES family(id)

);

CREATE TABLE dependents (
    id BINARY(16) PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL,
    birth_date DATE,

    family_id BINARY(16) NOT NULL,
    CONSTRAINT fk_dependent_family
        FOREIGN KEY (family_id)
            REFERENCES family(id)

);

CREATE TABLE therapy_types (

   id BINARY(16) PRIMARY KEY NOT NULL,
   name VARCHAR(100) NOT NULL,
   dependent_id BINARY(16) NOT NULL,

   CONSTRAINT fk_therapy_type_dependent
       FOREIGN KEY (dependent_id)
           REFERENCES dependents(id),

   CONSTRAINT uk_therapy_type_name_dependent
       UNIQUE (name, dependent_id)

);

CREATE TABLE reimbursements (
    id BINARY(16) NOT NULL,
    dependent_id BINARY(16) NOT NULL,
    therapy_type_id BINARY(16) NOT NULL,
    reference_month DATE NOT NULL,
    sessions_quantity INT NOT NULL,
    session_value DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    therapist_name VARCHAR(100) NOT NULL,
    therapist_pix VARCHAR(150) NOT NULL,
    description VARCHAR(500),

    PRIMARY KEY (id),

    CONSTRAINT fk_reimbursement_dependent
        FOREIGN KEY (dependent_id)
            REFERENCES dependents(id),

    CONSTRAINT fk_reimbursement_therapy_type
        FOREIGN KEY (therapy_type_id)
            REFERENCES therapy_types(id)

);

CREATE TABLE solicitations (
   id BINARY(16) PRIMARY KEY NOT NULL,
   reimbursement_id BINARY(16) NOT NULL,
   attempt_number INT NOT NULL,
   status VARCHAR(30) NOT NULL,
   protocol_number VARCHAR(100),
   request_date DATE,
   reimbursement_date DATE,
   amount_received DECIMAL(10, 2),
   note VARCHAR(500),

   CONSTRAINT fk_solicitation_reimbursement
       FOREIGN KEY (reimbursement_id)
           REFERENCES reimbursements(id),

   CONSTRAINT uk_solicitation_protocol_number
       UNIQUE (protocol_number),

   CONSTRAINT uk_solicitation_reimbursement_attempt
       UNIQUE (reimbursement_id, attempt_number)

);