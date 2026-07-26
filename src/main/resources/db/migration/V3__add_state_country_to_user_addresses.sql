ALTER TABLE user_addresses
ADD COLUMN IF NOT EXISTS state VARCHAR(100),
ADD COLUMN IF NOT EXISTS country VARCHAR(100);

DO $$
BEGIN
  IF EXISTS(SELECT *
            FROM information_schema.columns
            WHERE table_name='user_addresses' and column_name='zip_code')
  THEN
      ALTER TABLE user_addresses RENAME COLUMN zip_code TO postal_code;
  END IF;
END $$;
