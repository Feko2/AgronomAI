-- ========================================
-- AGROAPP - DATABASE SETUP SCRIPT
-- Para ejecutar en Oracle Cloud Infrastructure
-- ========================================

-- 1. ELIMINAR TABLAS EXISTENTES (si existen)
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE sensor_readings CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE parcelas CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'DROP SEQUENCE sensor_readings_seq';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

-- 2. CREAR TABLA DE PARCELAS
CREATE TABLE parcelas (
    parcela_id VARCHAR2(50) NOT NULL,
    nombre VARCHAR2(100) NOT NULL,
    tipo_cultivo VARCHAR2(50) NOT NULL,
    area_hectareas NUMBER(10,2),
    ubicacion VARCHAR2(200),
    fecha_siembra TIMESTAMP,
    estado VARCHAR2(20) DEFAULT 'ACTIVA',
    humedad_optima_min NUMBER(8,2),
    humedad_optima_max NUMBER(8,2),
    ph_optimo_min NUMBER(6,2),
    ph_optimo_max NUMBER(6,2),
    nitrogeno_optimo_min NUMBER(10,2),
    nitrogeno_optimo_max NUMBER(10,2),
    CONSTRAINT pk_parcelas PRIMARY KEY (parcela_id)
);

-- 3. CREAR TABLA DE LECTURAS DE SENSORES
CREATE TABLE sensor_readings (
    id NUMBER NOT NULL,
    parcela_id VARCHAR2(50) NOT NULL,
    humedad NUMBER(8,2),
    nitrogeno NUMBER(10,2),
    ph NUMBER(6,2),
    temperatura NUMBER(6,2),
    luminosidad NUMBER(12,2),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR2(20) DEFAULT 'NORMAL',
    CONSTRAINT pk_sensor_readings PRIMARY KEY (id),
    CONSTRAINT fk_sensor_parcela FOREIGN KEY (parcela_id) REFERENCES parcelas(parcela_id)
);

-- 4. CREAR SECUENCIA PARA AUTO-INCREMENT
CREATE SEQUENCE sensor_readings_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 5. CREAR TRIGGER PARA AUTO-INCREMENT
CREATE OR REPLACE TRIGGER trg_sensor_readings_id
    BEFORE INSERT ON sensor_readings
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := sensor_readings_seq.NEXTVAL;
    END IF;
END;
/

-- 6. CREAR ÍNDICES PARA PERFORMANCE
CREATE INDEX idx_sensor_parcela ON sensor_readings(parcela_id);
CREATE INDEX idx_sensor_fecha ON sensor_readings(fecha);
CREATE INDEX idx_parcela_tipo ON parcelas(tipo_cultivo);
CREATE INDEX idx_parcela_estado ON parcelas(estado);

-- 7. VERIFICAR CREACIÓN
SELECT 'Tabla parcelas creada correctamente' as status FROM dual 
WHERE EXISTS (SELECT 1 FROM user_tables WHERE table_name = 'PARCELAS');

SELECT 'Tabla sensor_readings creada correctamente' as status FROM dual 
WHERE EXISTS (SELECT 1 FROM user_tables WHERE table_name = 'SENSOR_READINGS');

-- 8. MOSTRAR ESTRUCTURA DE TABLAS
DESCRIBE parcelas;
DESCRIBE sensor_readings;

-- 9. CONFIRMAR CAMBIOS
COMMIT;

-- ========================================
-- SCRIPT COMPLETADO
-- Base de datos lista para AgroApp
-- ======================================== 