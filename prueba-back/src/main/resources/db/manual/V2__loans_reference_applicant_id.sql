BEGIN;

ALTER TABLE public.loans
	ADD COLUMN IF NOT EXISTS applicant_id BIGINT;

DO $$
BEGIN
	IF EXISTS (
		SELECT 1
		FROM information_schema.columns
		WHERE table_schema = 'public'
			AND table_name = 'loans'
			AND column_name = 'applicant_name'
	) THEN
		UPDATE public.loans AS loan
		SET applicant_id = app_user.id
		FROM public.app_users AS app_user
		WHERE lower(app_user.username) = lower(loan.applicant_name)
			AND loan.applicant_id IS NULL;
	END IF;

	IF EXISTS (
		SELECT 1
		FROM public.loans
		WHERE applicant_id IS NULL
	) THEN
		RAISE EXCEPTION 'Hay préstamos sin usuario relacionado. Revisa los nombres en loans.applicant_name y app_users.username antes de continuar.';
	END IF;
END $$;

ALTER TABLE public.loans
	ALTER COLUMN applicant_id SET NOT NULL;

DO $$
BEGIN
	IF NOT EXISTS (
		SELECT 1
		FROM pg_constraint
		WHERE conname = 'loans_applicant_id_fkey'
			AND conrelid = 'public.loans'::regclass
	) THEN
		ALTER TABLE public.loans
			ADD CONSTRAINT loans_applicant_id_fkey
			FOREIGN KEY (applicant_id)
			REFERENCES public.app_users(id)
			ON DELETE RESTRICT;
	END IF;
END $$;

ALTER TABLE public.loans
	DROP COLUMN IF EXISTS applicant_name;

COMMIT;
