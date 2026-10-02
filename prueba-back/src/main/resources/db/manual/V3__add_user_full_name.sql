BEGIN;

ALTER TABLE public.app_users
	ADD COLUMN IF NOT EXISTS full_name VARCHAR(120);

UPDATE public.app_users
SET full_name = username
WHERE full_name IS NULL OR btrim(full_name) = '';

ALTER TABLE public.app_users
	ALTER COLUMN full_name SET NOT NULL;

COMMIT;
