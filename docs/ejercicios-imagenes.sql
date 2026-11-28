-- Ejecutar con una cuenta propietaria antes de desplegar el backend con imágenes.
-- Solo añade columnas opcionales; no sustituye ni elimina registros existentes.
BEGIN;
ALTER TABLE public."Ejercicios"
  ADD COLUMN IF NOT EXISTS "ImagenUrl" varchar(2048),
  ADD COLUMN IF NOT EXISTS "ImagenAutor" varchar(255),
  ADD COLUMN IF NOT EXISTS "ImagenLicencia" varchar(255),
  ADD COLUMN IF NOT EXISTS "ImagenLicenciaUrl" varchar(2048);
COMMIT;
